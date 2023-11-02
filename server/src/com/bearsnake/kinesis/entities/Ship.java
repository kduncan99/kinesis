/*
 * kinesis
 * Copyright (c) 2020,2023 by Kurt Duncan - All Rights Reserved
 */

package com.bearsnake.kinesis.entities;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Ship {

    private static final Logger LOGGER = LogManager.getLogger("Ship");
    private static final Map<ShipId, Ship> _inventory = new HashMap<>();
    private static int _nextShipIdentifier = 1;

    private static final String CREATE_TABLE_SQL = "CREATE TABLE ships ("
        + "  shipId integer PRIMARY KEY,"
        + "  shipName text NOT NULL,"
        + "  ownerId integer NOT NULL,"
        + "  clusterId integer,"
        + "  sectorNumber integer,"
        + "  fuel real NOT NULL,"
        + "  shields real NOT NULL,"
        + "  cargoHolds integer NOT NULL,"
        + "  FOREIGN KEY (ownerId) REFERENCES players(playerId),"
        + "  FOREIGN KEY (clusterId, sectorNumber) REFERENCES sectors(clusterId, sectorNumber)"
        + ") WITHOUT ROWID;";

    private static final String INSERT_SQL =
        "INSERT INTO ships (shipId, shipName, ownerId, clusterId, sectorNumber, fuel, shields, cargoHolds)"
            + " VALUES (%s, \"%s\", %s, %s, %s, %f, %f, %d);";

    private static final String QUERY_SQL =
        "SELECT shipId, shipName, ownerId, fuel, shields, cargoHolds FROM ships ORDER BY shipId;";

    private final ShipId _identifier;
    private String _name;
    private final PlayerId _ownerId;
    private SectorId _sectorId;
    private float _fuel;
    private float _shields;
    private int _cargoHolds;

    private Ship(
        final ShipId identifier,
        final String name,
        final PlayerId ownerId,
        final SectorId sectorId,
        final float fuel,
        final float shields,
        final int cargoHolds
    ) {
        _identifier = identifier;
        _name = name;
        _ownerId = ownerId;
        _sectorId = sectorId;
        _fuel = fuel;
        _shields = shields;
        _cargoHolds = cargoHolds;
    }

    public int getCargoHoldCount() { return _cargoHolds; }
    public float getFuelAmount() { return _fuel; }
    public PlayerId getOwnerId() { return _ownerId; }
    public SectorId getSectorId() { return _sectorId; }
    public float getShieldsLevel() { return _shields; }
    public ShipId getShipId() { return _identifier; }
    public String getShipName() { return _name; }
    public void setCargoHoldCount(final int value) { _cargoHolds = value; }
    public void setFuelAmount(final float value) { _fuel = value; }
    public void setSectorId(final SectorId value) { _sectorId = value; }
    public void setShieldsLevel(final float value) { _shields = value; }
    public void setShipName(final String value) { _name = value; }

    public static Ship createShip(
        final String name,
        final PlayerId ownerId,
        final float fuelAmount,
        final float shieldsLevel,
        final int cargoHoldCount
    ) {
        var sid = new ShipId(_nextShipIdentifier++);
        while (_inventory.containsKey(sid)) {
            sid = sid.next();
        }

        var s = new Ship(sid, name, ownerId, null, fuelAmount, shieldsLevel, cargoHoldCount);
        _inventory.put(sid, s);
        return s;
    }

    public static Ship getShip(
        final ShipId sid
    ) {
        return _inventory.get(sid);
    }

    public static void dbCreateTable(
        final Connection conn
    ) throws SQLException {
        LOGGER.trace(CREATE_TABLE_SQL);
        var statement = conn.createStatement();
        statement.execute(CREATE_TABLE_SQL);
    }

    public static void dbLoad(
        final Connection conn
    ) throws SQLException {
        LOGGER.trace("dbLoad()");
        _inventory.clear();
        var statement = conn.createStatement();
        var rs = statement.executeQuery(QUERY_SQL);
        while (rs.next()) {
            var ident = rs.getInt("shipId");
            var shipId = new ShipId(ident);
            var shipName = rs.getString("shipName");

            var clid = rs.getInt("clusterId");
            var clidNull = rs.wasNull();
            var snum = rs.getInt("sectorNumber");
            var snumNull = rs.wasNull();
            SectorId sectorId = null;
            if (!clidNull && !snumNull) {
                sectorId = new SectorId(new ClusterId(clid), snum);
            }

            var ownerId = new PlayerId(rs.getInt("ownerId"));
            var fuel = rs.getFloat("fuel");
            var shields = rs.getFloat("shields");
            var cargoHolds = rs.getInt("cargoHolds");

            var ship = new Ship(shipId, shipName, ownerId, sectorId, fuel, shields, cargoHolds);
            _inventory.put(shipId, ship);
            _nextShipIdentifier = ident + 1;
        }
    }

    public void dbPersist(
        final Connection conn
    ) throws SQLException {
        var sql = String.format(INSERT_SQL,
                                _identifier,
                                _name,
                                _ownerId,
                                _sectorId == null ? "null" : _sectorId.getClusterId(),
                                _sectorId == null ? "null" : _sectorId.getSectorNumber().toString(),
                                _fuel,
                                _shields,
                                _cargoHolds);
        var statement = conn.createStatement();
        statement.execute(sql);
    }
}

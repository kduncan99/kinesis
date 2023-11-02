/*
 * kinesis
 * Copyright (c) 2020,2023 by Kurt Duncan - All Rights Reserved
 */

package com.bearsnake.kinesis.entities;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Ship {

    public enum ShipType {
        CRUISER("C"),
        DRONE("D"),
        FIGHTER("F"),
        PROBE("P");

        public final String _code;

        ShipType(final String code) { _code = code; }

        public static ShipType getShipType(
            final String code
        ) {
            return Arrays.stream(values()).filter(st -> st._code.equals(code)).findFirst().orElse(null);
        }
    }

    private static final Logger LOGGER = LogManager.getLogger("Ship");
    private static final Map<ShipId, Ship> _inventory = new HashMap<>();
    private static int _nextShipIdentifier = 1;

    private static final String CREATE_TABLE_SQL = "CREATE TABLE ships ("
        + "  shipId integer PRIMARY KEY,"
        + "  shipType text NOT NULL,"
        + "  shipName text NOT NULL,"
        + "  ownerId integer NOT NULL,"
        + "  locationId integer NOT NULL,"
        + "  fuel real NOT NULL,"
        + "  shields real NOT NULL,"
        + "  cargoHolds integer NOT NULL,"
        + "  FOREIGN KEY (ownerId) REFERENCES players(playerId),"
        + "  FOREIGN KEY (locationId) REFERENCES sectors(sectorId)"
        + ") WITHOUT ROWID;";

    private static final String INSERT_SQL =
        "INSERT INTO ships (shipId, shipType, shipName, ownerId, locationId, fuel, shields, cargoHolds)"
            + " VALUES (%s, \"%s\", \"%s\", %s, %s, %f, %f, %d);";

    private final ShipId _shipId;
    private String _shipName;
    private final ShipType _shipType;
    private Player _owner;
    private Sector _location;
    private float _fuel;
    private float _shields;
    private int _cargoHolds;

    private Ship(
        final ShipId shipId,
        final ShipType shipType,
        final String shipName,
        final Player owner,
        final Sector location,
        final float fuel,
        final float shields,
        final int cargoHolds
    ) {
        _shipId = shipId;
        _shipType = shipType;
        _shipName = shipName;
        _owner = owner;
        _location = location;
        _fuel = fuel;
        _shields = shields;
        _cargoHolds = cargoHolds;
    }

    public int getCargoHoldCount() { return _cargoHolds; }
    public float getFuelAmount() { return _fuel; }
    public Sector getLocation() { return _location; }
    public Player getOwner() { return _owner; }
    public float getShieldsLevel() { return _shields; }
    public static Ship getShip(final ShipId sid) { return _inventory.get(sid); }
    public ShipId getShipId() { return _shipId; }
    public String getShipName() { return _shipName; }
    public ShipType getShipType() { return _shipType; }
    public void setCargoHoldCount(final int value) { _cargoHolds = value; }
    public void setFuelAmount(final float value) { _fuel = value; }
    public void setLocation(final Sector value) { _location = value; }
    public void setOwner(final Player value) { _owner = value; }
    public void setShieldsLevel(final float value) { _shields = value; }
    public void setShipName(final String value) { _shipName = value; }

    public static Ship createShip(
        final ShipType shipType,
        final String shipName,
        final Player owner,
        final Sector location,
        final float fuelAmount,
        final float shieldsLevel,
        final int cargoHoldCount
    ) {
        var sid = new ShipId(_nextShipIdentifier++);
        var s = new Ship(sid, shipType, shipName, owner, location, fuelAmount, shieldsLevel, cargoHoldCount);
        _inventory.put(sid, s);
        return s;
    }

    public static void dbCreateTable(
        final Connection conn
    ) throws SQLException {
        LOGGER.trace(CREATE_TABLE_SQL);
        var statement = conn.createStatement();
        statement.execute(CREATE_TABLE_SQL);
    }

    /**
     * Loads all the Ship entities from the database. MUST be invoked AFTER loading Players and Sectors.
     */
    public static void dbLoad(
        final Connection conn
    ) throws SQLException {
        LOGGER.trace("dbLoad()");

        _inventory.clear();
        var statement = conn.createStatement();
        var rs = statement.executeQuery("SELECT * FROM ships ORDER BY shipId;");

        while (rs.next()) {
            var ident = rs.getInt("shipId");
            var shipId = new ShipId(ident);
            var shipType = ShipType.getShipType(rs.getString("shipType"));
            var shipName = rs.getString("shipName");
            var owner = Player.getPlayer(new Player.PlayerId(rs.getLong("ownerId")));
            var location = Sector.getSector(new Sector.SectorId(rs.getLong("locationId")));
            Sector.SectorId sectorId = null;
            var fuel = rs.getFloat("fuel");
            var shields = rs.getFloat("shields");
            var cargoHolds = rs.getInt("cargoHolds");

            var ship = new Ship(shipId, shipType, shipName, owner, location, fuel, shields, cargoHolds);
            _inventory.put(shipId, ship);
            _nextShipIdentifier = ident + 1;
        }

        var msg = String.format("Loaded %d ships...\n", _inventory.size());
        System.out.println(msg);
        LOGGER.info(msg);
    }

    public void dbPersist(
        final Connection conn
    ) throws SQLException {
        var sql = String.format(INSERT_SQL,
                                _shipId,
                                _shipType._code,
                                _shipName,
                                _owner.getPlayerId(),
                                _location.getSectorId(),
                                _fuel,
                                _shields,
                                _cargoHolds);
        var statement = conn.createStatement();
        statement.execute(sql);
    }

    public static class ShipId {

        private final long _value;

        public ShipId(
            final long id
        ) {
            _value = id;
        }

        @Override
        public boolean equals(
            final Object obj
        ) {
            if (obj instanceof ShipId id) {
                return id._value == _value;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return (int)_value;
        }

        @Override
        public String toString() {
            return String.valueOf(_value);
        }
    }
}

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

/**
 * Represents a planet within our universe.
 * A planet is located at a specific sector within a specific cluster
 */
public class Planet {

    private static final Logger LOGGER = LogManager.getLogger("Planet");
    private static final Map<PlanetId, Planet> _inventory = new HashMap<>();
    private static int _nextPlanetIdentifier = 1;

    private static final String CREATE_TABLE_SQL = "CREATE TABLE planets ("
        + "  planetId integer PRIMARY KEY,"
        + "  planetName text NOT NULL,"
        + "  clusterId integer NOT NULL,"
        + "  sectorNumber integer NOT NULL,"
        + "  ownerId integer NOT NULL,"
        + "  FOREIGN KEY (clusterId, sectorNumber) REFERENCES sectors(clusterId, sectorNumber),"
        + "  FOREIGN KEY (ownerId) REFERENCES players(playerId)"
        + ") WITHOUT ROWID;";

    private static final String INSERT_SQL =
        "INSERT INTO ports (planetId, planetName, clusterId, sectorNumber, ownerId)"
            + " VALUES (%s, \"%s\", %s, %d, %s);";

    private static final String QUERY_SQL =
        "SELECT planetId, planetName, clusterId, sectorNumber, ownerId FROM planets ORDER BY planetId;";

    private final PlanetId _identifier;
    private final String _name;
    private PlayerId _ownerId;
    private SectorId _sectorId;

    private Planet(
        final PlanetId identifier,
        final String name,
        final SectorId sectorId,
        final PlayerId ownerId
    ) {
        _identifier = identifier;
        _name = name;
        _ownerId = ownerId;
        _sectorId = sectorId;
    }

    public PlanetId getPlanetId() { return _identifier; }
    public String getName() { return _name; }
    public PlayerId getOwnerId() { return _ownerId; }
    public SectorId getSectorId() { return _sectorId; }
    public void setOwnerId(final PlayerId value) { _ownerId = value; }
    public void setSectorId(final SectorId value) { _sectorId = value; }

    public static Planet createPlanet(
        final String name,
        final SectorId sectorId,
        final PlayerId ownerId
    ) {
        var plid = new PlanetId(_nextPlanetIdentifier++);
        while (_inventory.containsKey(plid)) {
            plid = plid.next();
        }

        var p = new Planet(plid, name, sectorId, ownerId);
        _inventory.put(plid, p);
        return p;
    }

    public static Planet getPlanet(
        final PlanetId plid
    ) {
        return _inventory.get(plid);
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
            var ident = rs.getInt("planetId");
            var planetId = new PlanetId(ident);
            var planetName = rs.getString("planetName");
            var sectorId = new SectorId(new ClusterId(rs.getInt("clusterId")),
                                        rs.getInt("sectorNumber"));
            var ownerId = new PlayerId(rs.getInt("ownerId"));

            var planet = new Planet(planetId, planetName, sectorId, ownerId);
            _inventory.put(planetId, planet);
            _nextPlanetIdentifier = ident + 1;
        }
    }

    public void dbPersist(
        final Connection conn
    ) throws SQLException {
        var sql = String.format(INSERT_SQL,
                                _identifier,
                                _name,
                                _sectorId.getClusterId(),
                                _sectorId.getSectorNumber(),
                                _ownerId);
        var statement = conn.createStatement();
        statement.execute(sql);
    }
}

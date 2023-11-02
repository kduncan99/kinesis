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
    private static long _nextPlanetId = 1;

    private static final String CREATE_TABLE_SQL = "CREATE TABLE planets ("
        + "  planetId integer PRIMARY KEY,"
        + "  planetName text NOT NULL,"
        + "  locationId integer NOT NULL,"
        + "  ownerId integer NOT NULL,"
        + "  FOREIGN KEY (locationId) REFERENCES sectors(sectorId),"
        + "  FOREIGN KEY (ownerId) REFERENCES players(playerId)"
        + ") WITHOUT ROWID;";

    private static final String INSERT_SQL =
        "INSERT INTO ports (planetId, planetName, locationId, ownerId)"
            + " VALUES (%s, \"%s\", %s, %s);";

    private final PlanetId _planetId;
    private final String _planetName;
    private Player _owner;
    private Sector _location;

    private Planet(
        final PlanetId identifier,
        final String name,
        final Sector location,
        final Player owner
    ) {
        _planetId = identifier;
        _planetName = name;
        _owner = owner;
        _location = location;
    }

    public static Planet getPlanet(final PlanetId plid) { return _inventory.get(plid); }
    public PlanetId getPlanetId() { return _planetId; }
    public String getPlanetName() { return _planetName; }
    public Sector getLocation() { return _location; }
    public Player getOwner() { return _owner; }
    public void setLocation(final Sector value) { _location = value; }
    public void setOwner(final Player value) { _owner = value; }

    public static Planet createPlanet(
        final String name,
        final Sector location,
        final Player owner
    ) {
        var pid = new PlanetId(_nextPlanetId++);
        var p = new Planet(pid, name, location, owner);
        _inventory.put(pid, p);
        return p;
    }

    public static void dbCreateTable(
        final Connection conn
    ) throws SQLException {
        LOGGER.trace(CREATE_TABLE_SQL);
        var statement = conn.createStatement();
        statement.execute(CREATE_TABLE_SQL);
    }

    /**
     * Loads Planet objects from the database. Sectors and Players MUST be loaded before calling here.
     */
    public static void dbLoad(
        final Connection conn
    ) throws SQLException {
        LOGGER.trace("dbLoad()");

        _inventory.clear();
        var sql = "SELECT * FROM planets ORDER BY planetId;";
        var statement = conn.createStatement();
        var rs = statement.executeQuery(sql);

        while (rs.next()) {
            var pid = rs.getLong("planetId");
            var planetId = new PlanetId(pid);
            var planetName = rs.getString("planetName");
            var sid = new Sector.SectorId(rs.getLong("locationId"));
            var location = Sector.getSector(sid);
            var plid = new Player.PlayerId(rs.getLong("ownerId"));
            var owner = Player.getPlayer(plid);
            var planet = new Planet(planetId, planetName, location, owner);

            _inventory.put(planetId, planet);
            _nextPlanetId = pid + 1;

            location.setPlanet(planet);
        }

        var msg = String.format("Loaded %d planet(s)...", _inventory.size());
        System.out.println(msg);
        LOGGER.info(msg);
    }

    public void dbPersist(
        final Connection conn
    ) throws SQLException {
        var sql = String.format(INSERT_SQL, _planetId, _planetName, _location.getSectorId(), _owner.getPlayerId());
        var statement = conn.createStatement();
        statement.execute(sql);
    }

    public static class PlanetId {

        private final long _value;

        public PlanetId(
            final long id
        ) {
            _value = id;
        }

        @Override
        public boolean equals(
            final Object obj
        ) {
            if (obj instanceof PlanetId id) {
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

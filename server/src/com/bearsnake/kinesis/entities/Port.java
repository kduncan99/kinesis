/*
 * kinesis
 * Copyright (c) 2020,2023 by Kurt Duncan - All Rights Reserved
 */

package com.bearsnake.kinesis.entities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a port within our universe.
 * Ports are assigned to a particular sector (never more than one per) and never move.
 * Ports *may* be owned by a player.
 */
public class Port {

    private static final Logger LOGGER = LogManager.getLogger("Port");
    private static final Map<PortId, Port> _inventory = new HashMap<>();
    private static long _nextPortIdentifier = 1;

    private static final String CREATE_TABLE_SQL = "CREATE TABLE ports ("
        + "  portId integer PRIMARY KEY,"
        + "  portName text NOT NULL,"
        + "  locationId integer NOT NULL,"
        + "  ownerId integer,"
        + "  FOREIGN KEY (locationId) REFERENCES sectors(sectorId),"
        + "  FOREIGN KEY (ownerId) REFERENCES players(playerId)"
        + ") WITHOUT ROWID;";

    private static final String INSERT_SQL =
        "INSERT INTO ports (portId, portName, locationId, ownerId)"
        + " VALUES (%s, \"%s\", %s, %s);";

    private final PortId _portId;
    private final String _portName;
    private final Sector _location;
    private Player _owner;
    // TODO resource amounts, production/consumption

    private Port(
        final PortId identifier,
        final String name,
        final Sector location,
        final Player owner
    ) {
        _portId = identifier;
        _portName = name;
        _location = location;
        _owner = owner;
    }

    public Sector getLocation() { return _location; }
    public Player getOwner() { return _owner; }
    public static Port getPort(final PortId portId) { return _inventory.get(portId); }
    public PortId getPortId() { return _portId; }
    public String getPortName() { return _portName; }
    public boolean hasOwner() { return _owner != null; }
    public void setOwnerId(final Player value) { _owner = value; }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append(_portName).append(" (").append(_portId).append(")");
        //TODO show production/consumption levels and resource amounts when we have them.
        return sb.toString();
    }

    public static Port createPort(
        final Sector location,
        final Player owner
    ) {
        var name = PortNames.selectName();
        var pid = new PortId(_nextPortIdentifier++);
        var p = new Port(pid, name, location, owner);
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
     * Loads Port entities from the database. MUST load Players and Sectors first.
     */
    public static void dbLoad(
        final Connection conn
    ) throws SQLException {
        LOGGER.trace("dbLoad()");

        _inventory.clear();
        var sql = String.format("SELECT * FROM ports ORDER BY portId;");
        var statement = conn.createStatement();
        var rs = statement.executeQuery(sql);

        while (rs.next()) {
            var portId = new PortId(rs.getLong("portId"));
            var portName = rs.getString("portName");
            var location = Sector.getSector(new Sector.SectorId(rs.getLong("locationId")));
            Player owner = null;
            var pid = rs.getLong("ownerId");
            if (!rs.wasNull()) {
                owner = Player.getPlayer(new Player.PlayerId(pid));
            }

            var p = new Port(portId, portName, location, owner);
            _inventory.put(portId, p);
            _nextPortIdentifier = portId._value + 1;

            location.setPort(p);
        }

        var msg = String.format("Loaded %d ports...\n", _inventory.size());
        System.out.println(msg);
        LOGGER.info(msg);
    }

    public void dbPersist(
        final Connection conn
    ) throws SQLException {
        var sql = String.format(INSERT_SQL,
                                _portId,
                                _portName,
                                _location.getSectorId(),
                                hasOwner() ? _owner.getPlayerId() : "null");
        var statement = conn.createStatement();
        statement.execute(sql);
    }

    public static class PortId {

        private final long _value;

        public PortId(
            final long id
        ) {
            _value = id;
        }

        @Override
        public boolean equals(
            final Object obj
        ) {
            if (obj instanceof PortId id) {
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

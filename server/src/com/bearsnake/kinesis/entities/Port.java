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
 * A port is located at a specific sector within a specific cluster - somewhere a little bit away from a planet
 */
public class Port {

    private static final Logger LOGGER = LogManager.getLogger("Port");
    private static final Map<PortId, Port> _inventory = new HashMap<>();
    private static int _nextPortIdentifier = 1;

    private static final String CREATE_TABLE_SQL = "CREATE TABLE ports ("
        + "  portId integer PRIMARY KEY,"
        + "  portName text NOT NULL,"
        + "  clusterId integer NOT NULL,"
        + "  sectorNumber integer NOT NULL,"
        + "  ownerId integer,"
        + "  FOREIGN KEY (clusterId, sectorNumber) REFERENCES sectors(clusterId, sectorNumber),"
        + "  FOREIGN KEY (ownerId) REFERENCES players(playerId)"
        + ") WITHOUT ROWID;";

    private static final String INSERT_SQL =
        "INSERT INTO ports (portId, portName, clusterId, sectorNumber, ownerId)"
        + " VALUES (%s, \"%s\", %s, %d, null);";

    private final PortId _identifier;
    private final String _name;
    private final SectorId _sectorId;
    private PlayerId _ownerId = null;
    // TODO resource amounts, production/consumption

    private Port(
        final PortId identifier,
        final String name,
        final SectorId sectorId
    ) {
        _identifier = identifier;
        _name = name;
        _sectorId = sectorId;
    }

    public PlayerId getOwnerId() { return _ownerId; }
    public PortId getPortId() { return _identifier; }
    public String getName() { return _name; }
    public SectorId getSectorId() { return _sectorId; }
    public void setOwnerId(final PlayerId value) { _ownerId = value; }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append(_name).append(" (").append(_identifier).append(")");
        //TODO show production/consumption levels and resource amounts when we have them.
        return sb.toString();
    }

    public static Port createPort(
        final SectorId sectorId
    ) {
        var name = PortNames.selectName();
        var pid = new PortId(_nextPortIdentifier++);
        while (_inventory.containsKey(pid)) {
            pid = pid.next();
        }

        var p = new Port(pid, name, sectorId);
        _inventory.put(pid, p);
        return p;
    }

    public static Port getPort(
        final PortId pid
    ) {
        return _inventory.get(pid);
    }

    public static void dbCreateTable(
        final Connection conn
    ) throws SQLException {
        LOGGER.trace(CREATE_TABLE_SQL);
        var statement = conn.createStatement();
        statement.execute(CREATE_TABLE_SQL);
    }

    public void dbPersist(
        final Connection conn
    ) throws SQLException {
        var sql = String.format(INSERT_SQL, _identifier, _name, _sectorId.getClusterId(), _sectorId.getSectorNumber());
        var statement = conn.createStatement();
        statement.execute(sql);
    }
}

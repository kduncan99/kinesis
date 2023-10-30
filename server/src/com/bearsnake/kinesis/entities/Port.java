/*
 * kinesis
 * Copyright (c) 2020,2023 by Kurt Duncan - All Rights Reserved
 */

package com.bearsnake.kinesis.entities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a port within our universe.
 * A port is located at a specific sector within a specific cluster - somewhere a little bit away from a planet
 */
public class Port {

    private static final Logger LOGGER = LogManager.getLogger("Port");
    private static final Map<PortId, Port> _inventory = new HashMap<>();
    private static final int _nextPortIdentifier = 1;

    private final PortId _identifier;
    private final String _name;
    private final SectorId _sectorId;
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

    public PortId getPortId() { return _identifier; }
    public String getName() { return _name; }
    public SectorId getSectorId() { return _sectorId; }

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
        var pid = new PortId(_nextPortIdentifier);
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
}

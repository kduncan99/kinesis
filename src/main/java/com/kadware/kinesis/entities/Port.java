/*
 * kinesis
 * Copyright (c) 2020 by Kurt Duncan - All Rights Reserved
 */

package com.kadware.kinesis.entities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents a port within our universe.
 * A port is located at a specific sector within a specific cluster - somewhere a little bit away from a planet
 */
public class Port {

    private static final Logger LOGGER = LogManager.getLogger("Port");
    private static int _nextIdentifier = 1;

    public final int _identifier;
    public final int _portIdentifier;
    public final String _name;
    //  TODO resource information
    //  TODO dependence on cluster government / planet government

    public Port(
        final int identifier,
        final int portIdentifier,
        final String name
    ) {
        _identifier = identifier;
        _portIdentifier = portIdentifier;
        _name = name;
    }

    public static synchronized int getNextIdentifier() {
        return _nextIdentifier++;
    }
}

/*
 * kinesis
 * Copyright (c) 2020,2023 by Kurt Duncan - All Rights Reserved
 */

package com.bearsnake.kinesis.lib;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents a port within our universe.
 * A port is located at a specific sector within a specific cluster - somewhere a little bit away from a planet
 */
public class Port {

    private static final Logger LOGGER = LogManager.getLogger("Port");

    public final int _identifier;
    public final String _name;

    public Port(
        final int identifier,
        final String name
    ) {
        _identifier = identifier;
        _name = name;
    }
}

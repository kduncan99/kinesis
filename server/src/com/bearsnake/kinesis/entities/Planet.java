/*
 * kinesis
 * Copyright (c) 2020,2023 by Kurt Duncan - All Rights Reserved
 */

package com.bearsnake.kinesis.entities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents a planet within our universe.
 * A planet is located at a specific sector within a specific cluster
 */
public class Planet {

    private static final Logger LOGGER = LogManager.getLogger("Planet");

    public final int _identifier;
    public final String _name;

    public Planet(
        final int identifier,
        final String name
    ) {
        _identifier = identifier;
        _name = name;
    }
}

/*
 * kinesis
 * Copyright (c) 2020 by Kurt Duncan - All Rights Reserved
 */

package com.kadware.kinesis.entities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents a planet within our universe.
 * A planet is located at a specific sector within a specific cluster
 */
public class Planet {

    private static final Logger LOGGER = LogManager.getLogger("Planet");
    private static int _nextIdentifier = 1;

    public final int _identifier;
    public final int _planetIdentifier;
    public final String _name;
    //  TODO geography/meteorology
    //  TODO resource information
    //  TODO infrastructure information (part of this in the City object?)
    //  TODO economy / financials (part of this in the City object?)
    //  TODO sociology information (or is this contained among all the population?)
    //  TODO government / military

    public Planet(
        final int identifier,
        final int planetIdentifier,
        final String name
    ) {
        _identifier = identifier;
        _planetIdentifier = planetIdentifier;
        _name = name;
    }

    public static synchronized int getNextIdentifier() {
        return _nextIdentifier++;
    }
}

/*
 * kinesis
 * Copyright (c) 2020 by Kurt Duncan - All Rights Reserved
 */

package com.kadware.kinesis.entities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents a city within our universe
 * A city is located on a planet.
 */
public class City {

    private static final Logger LOGGER = LogManager.getLogger("City");
    private static int _nextIdentifier = 1;

    public final int _identifier;
    public final int _planetIdentifier;
    public final String _name;
    //  TODO geography/meteorology
    //  TODO resource information
    //  TODO city information (or do we need a City object)

    public City(
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

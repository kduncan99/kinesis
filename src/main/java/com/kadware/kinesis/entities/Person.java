/*
 * kinesis
 * Copyright (c) 2020 by Kurt Duncan - All Rights Reserved
 */

package com.kadware.kinesis.entities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents a person within the universe
 */
public abstract class Person {

    public enum LocationType {
        City,
        Port,
        Ship,
    }

    private static final Logger LOGGER = LogManager.getLogger("Person");
    private static int _nextIdentifier = 1;

    public final int _identifier;
    public final String _name;
    public LocationType _locationType;
    public int _locationIdentifier;       //  city, port, or ship identifier

    Person(
        final int identifier,
        final String name,
        final LocationType initialLocationType,
        final int initialLocationIdentifier
    ) {
        _identifier = identifier;
        _name = name;
        _locationType = initialLocationType;
        _locationIdentifier = initialLocationIdentifier;
    }

    public static synchronized int getNextIdentifier() {
        return _nextIdentifier++;
    }
}

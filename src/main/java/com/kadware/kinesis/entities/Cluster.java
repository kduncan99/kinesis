/*
 * kinesis
 * Copyright (c) 2020 by Kurt Duncan - All Rights Reserved
 */

package com.kadware.kinesis.entities;

/**
 * Represents a cluster of sectors
 */
public class Cluster {

    public static final String SOURCE_TABLE = "Clusters";
    public final int _identifier;
    public final String _name;

    private Cluster(
        final int identifier,
        final String name
    ) {
        _identifier = identifier;
        _name = name;
    }
}

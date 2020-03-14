/*
 * kinesis
 * Copyright (c) 2020 by Kurt Duncan - All Rights Reserved
 */

package com.kadware.kinesis.entities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;

public class Sector {

    public static final Logger _logger = LogManager.getLogger("Sector");
    private static int _nextIdentifier = 1;

    public final int _identifier;
    public final int _clusterIdentifier;    //  identifier
    public final int _sectorNumber;         //  unique within the containing cluster
    public final List<Integer> _links = new LinkedList<>();
    public List<Integer> _pathToCenter = null;

    public Sector (
        final int identifier,
        final int clusterIdentifier,
        final int sectorNumber
    ) {
        _identifier = identifier;
        _clusterIdentifier = clusterIdentifier;
        _sectorNumber = sectorNumber;
    }

    public static synchronized int getNextIdentifier() {
        return _nextIdentifier++;
    }

}

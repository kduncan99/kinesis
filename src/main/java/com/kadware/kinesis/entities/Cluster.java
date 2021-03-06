/*
 * kinesis
 * Copyright (c) 2020 by Kurt Duncan - All Rights Reserved
 */

package com.kadware.kinesis.entities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a cluster of sectors
 */
public abstract class Cluster {

    public enum Geometry {
        Traditional,
        Spoke,
        Grouped,
    }

    private static final Logger LOGGER = LogManager.getLogger("Cluster");
    private static int _nextIdentifier = 1;

    public final int _identifier;
    public final int _maxLinksPerSector;
    public final String _name;
    final Map<Integer, Sector> _sectorMap = new HashMap<>();
    //  TODO government / military

    Cluster(
        final int identifier,
        final String name,
        final int maxLinksPerSector
    ) {
        _identifier = identifier;
        _name = name;
        _maxLinksPerSector = maxLinksPerSector;
    }

    public abstract Geometry getGeometry();

    public static synchronized int getNextIdentifier() {
        return _nextIdentifier++;
    }

    boolean createSectorLink(
        final int sectorId1,
        final int sectorId2
    ) {
        if ((sectorId1 < 1) || (sectorId2 < 1) || (sectorId1 == sectorId2)) {
            return false;
        }

        Sector sector1 = _sectorMap.get(sectorId1);
        Sector sector2 = _sectorMap.get(sectorId2);
        if ((sector1 == null) || (sector2 == null)) {
            return false;
        }

        return createSectorLink(sector1, sector2);
    }

    boolean createSectorLink(
        final Sector sector1,
        final Sector sector2
    ) {
        if ((sector1._links.size() == _maxLinksPerSector)
            || (sector2._links.size() == _maxLinksPerSector)
            || sector1._links.contains(sector2._sectorNumber)
            || sector2._links.contains(sector1._sectorNumber)
            || (sector1._sectorNumber == sector2._sectorNumber)) {
            return false;
        } else {
            sector1._links.add(sector2._sectorNumber);
            sector2._links.add(sector1._sectorNumber);
            return true;
        }
    }

    void traceSectorLinks() {
        for (Sector sector : _sectorMap.values()) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%d:", sector._sectorNumber));
            for (int link : sector._links) {
                sb.append(String.format(" %d", link));
            }
            System.out.println(sb.toString());//TODO remove
            LOGGER.trace(sb.toString());
        }
    }
}

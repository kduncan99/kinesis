/*
 * kinesis
 * Copyright (c) 2020 by Kurt Duncan - All Rights Reserved
 */

package com.bearsnake.kinesis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class Sector {

    public static final Logger _logger = LogManager.getLogger("Sector");

    private final int _sectorNumber; //  unique within the containing cluster
    private final Set<Sector> _links = new HashSet<>();

    private Player _owner = null;
    private Planet _planet = null;
    private Port _port = null;

    public Sector (
        final int sectorNumber
    ) {
        _sectorNumber = sectorNumber;
    }

    public void createLinkTo(
        final Sector sector
    ) {
        _links.add(sector);
    }

    public Collection<Sector> getLinks() { return new LinkedList<>(_links); }
    public int getLinkCount() { return _links.size(); }
    public Player getOwner() { return _owner; }
    public Planet getPlanet() { return _planet; }
    public Port getPort() { return _port; }
    public int getSectorNumber() { return _sectorNumber; }

    public boolean hasLinkTo(
        final Sector target
    ) {
        return _links.contains(target);
    }

    public boolean hasLinkTo(
        final int sectorNumber
    ) {
        return _links.stream().anyMatch(target -> target._sectorNumber == sectorNumber);
    }
}

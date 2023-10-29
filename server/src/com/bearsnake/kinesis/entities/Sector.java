/*
 * kinesis
 * Copyright (c) 2020,2023 by Kurt Duncan - All Rights Reserved
 */

package com.bearsnake.kinesis.entities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Sector {

    public static final Logger _logger = LogManager.getLogger("Sector");
    private static final Map<SectorId, Sector> _inventory = new HashMap<>();

    private final SectorId _sectorId;
    private final Set<SectorId> _links;
    private PlayerId _ownerId;
    private PlanetId _planetId;
    private PortId _portId;

    private Sector (
        final SectorId sid
    ) {
        _sectorId = sid;
        _links = new HashSet<>();
        _ownerId = null;
        _planetId = null;
        _portId = null;
    }

    public static Sector createNewSector(
        final ClusterId clusterId,
        final Integer sectorNumber
    ) {
        SectorId sid = new SectorId(clusterId, sectorNumber);
        if (_inventory.containsKey(sid)) {
            throw new RuntimeException("Attempted to create sector with existing SID");
        }

        var s = new Sector(sid);
        _inventory.put(sid, s);
        return s;
    }

    public static void createBidirectionalLink(
        final SectorId sid1,
        final SectorId sid2
    ) {
        _inventory.get(sid1)._links.add(sid2);
        _inventory.get(sid2)._links.add(sid1);
    }

    public void createLinkTo(
        final SectorId sid
    ) {
        _links.add(sid);
    }

    public void createLinkTo(
        final Sector sector
    ) {
        _links.add(sector.getSectorId());
    }

    public Collection<Sector> getLinkedSectors() {
        return _links.stream().map(_inventory::get).collect(Collectors.toCollection(LinkedList::new));
    }

    public Collection<SectorId> getLinks() {
        return new LinkedList<>(_links);
    }

    public static Sector getSector(
        final SectorId sid
    ) {
        return _inventory.get(sid);
    }

    public int getLinkCount() { return _links.size(); }
    public PlayerId getOwnerId() { return _ownerId; }
    public PlanetId getPlanetId() { return _planetId; }
    public PortId getPortId() { return _portId; }
    public SectorId getSectorId() { return _sectorId; }
    public int getSectorNumber() { return _sectorId.getSectorNumber(); }

    public void setOwnerId(final PlayerId value) { _ownerId = value; }
    public void setPlanetId(final PlanetId value) { _planetId = value; }
    public void setPortId(final PortId value) { _portId = value; }

    public boolean hasLinkTo(
        final Sector target
    ) {
        return _links.contains(target._sectorId);
    }
}

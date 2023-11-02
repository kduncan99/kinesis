/*
 * kinesis
 * Copyright (c) 2020,2023 by Kurt Duncan - All Rights Reserved
 */

package com.bearsnake.kinesis.entities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Sector {

    private static final String CREATE_SECTORS_TABLE_SQL =
        "CREATE TABLE sectors ("
            + "  clusterId integer NOT NULL,"
            + "  sectorNumber integer NOT NULL,"
            + "  FOREIGN KEY (clusterId) REFERENCES clusters(clusterId),"
            + "  PRIMARY KEY (clusterId, sectorNumber)"
            + ") WITHOUT ROWID;";

    private static final String CREATE_LINKS_TABLE_SQL =
        "CREATE TABLE sectorLinks ("
            + "  fromClusterId integer NOT NULL,"
            + "  fromSectorNumber integer NOT NULL,"
            + "  toClusterId integer NOT NULL,"
            + "  toSectorNumber integer NOT NULL,"
            + "  FOREIGN KEY (fromClusterId, fromSectorNumber) REFERENCES sectors(clusterId, sectorNumber),"
            + "  FOREIGN KEY (toClusterId, toSectorNumber) REFERENCES sectors(clusterId, sectorNumber),"
            + "  PRIMARY KEY (fromClusterId, fromSectorNumber, toClusterId, toSectorNumber)"
            + ") WITHOUT ROWID;";

    private static final String INSERT_SECTOR_SQL =
        "INSERT INTO sectors (clusterId, sectorNumber) VALUES (%s, %d);";

    private static final String INSERT_SECTOR_LINK_SQL =
        "INSERT INTO sectorLinks (fromClusterId, fromSectorNumber, toClusterId, toSectorNumber)"
            + " VALUES (%s, %d, %s, %d);";

    private static final Logger LOGGER = LogManager.getLogger("Sector");
    private static final Map<SectorId, Sector> _inventory = new HashMap<>();

    private final SectorId _sectorId;
    private final Set<SectorId> _links;
    private PlanetId _planetId; // TODO do we need this here?
    private PortId _portId; // TODO do we need this here?

    private Sector (
        final SectorId sid
    ) {
        _sectorId = sid;
        _links = new HashSet<>();
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
    public PlanetId getPlanetId() { return _planetId; }
    public PortId getPortId() { return _portId; }
    public SectorId getSectorId() { return _sectorId; }
    public int getSectorNumber() { return _sectorId.getSectorNumber(); }

    public void setPlanetId(final PlanetId value) { _planetId = value; }
    public void setPortId(final PortId value) { _portId = value; }

    public boolean hasLinkTo(
        final Sector target
    ) {
        return _links.contains(target._sectorId);
    }

    public static void dbCreateTables(
        final Connection conn
    ) throws SQLException {
        LOGGER.trace(CREATE_SECTORS_TABLE_SQL);
        var statement = conn.createStatement();
        statement.execute(CREATE_SECTORS_TABLE_SQL);

        LOGGER.trace(CREATE_LINKS_TABLE_SQL);
        statement = conn.createStatement();
        statement.execute(CREATE_LINKS_TABLE_SQL);
    }

    public void dbPersist(
        final Connection conn
    ) throws SQLException {
        var sql = String.format(INSERT_SECTOR_SQL, _sectorId.getClusterId(), _sectorId.getSectorNumber());
        var statement = conn.createStatement();
        statement.execute(sql);

        for (var linkSectorId : _links) {
            sql = String.format(INSERT_SECTOR_LINK_SQL,
                                _sectorId.getClusterId(),
                                _sectorId.getSectorNumber(),
                                linkSectorId.getClusterId(),
                                linkSectorId.getSectorNumber());
            statement = conn.createStatement();
            statement.execute(sql);
        }
    }
}

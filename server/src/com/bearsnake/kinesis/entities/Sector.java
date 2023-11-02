/*
 * kinesis
 * Copyright (c) 2020,2023 by Kurt Duncan - All Rights Reserved
 */

package com.bearsnake.kinesis.entities;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Sector {

    private static final String CREATE_SECTORS_TABLE_SQL =
        "CREATE TABLE sectors ("
            + "  sectorId integer PRIMARY KEY,"
            + "  clusterId integer NOT NULL,"
            + "  sectorNumber integer NOT NULL,"
            + "  FOREIGN KEY (clusterId) REFERENCES clusters(clusterId)"
            + ") WITHOUT ROWID;";

    private static final String CREATE_LINKS_TABLE_SQL =
        "CREATE TABLE sectorLinks ("
            + "  fromSectorId integer NOT NULL,"
            + "  toSectorId integer NOT NULL,"
            + "  FOREIGN KEY (fromSectorId) REFERENCES sectors(sectorId),"
            + "  FOREIGN KEY (toSectorId) REFERENCES sectors(sectorId),"
            + "  PRIMARY KEY (fromSectorId, toSectorId)"
            + ") WITHOUT ROWID;";

    private static final String INSERT_SECTOR_SQL =
        "INSERT INTO sectors (sectorId, clusterId, sectorNumber) VALUES (%s, %s, %d);";

    private static final String INSERT_SECTOR_LINK_SQL =
        "INSERT INTO sectorLinks (fromSectorId, toSectorId) VALUES (%s, %s);";

    private static final String UPDATE_SECTOR_SQL =
        "UPDATE sectors"
            + "  SET planetId = %s,"
            + "  SET portId = %s,"
            + "  WHERE sectorId = %s;";

    private static final Logger LOGGER = LogManager.getLogger("Sector");
    private static final Map<SectorId, Sector> _inventory = new HashMap<>();
    private static long _nextSectorId = 1;

    private final Cluster _cluster;
    private final Set<Sector> _links = new HashSet<>();
    private Planet _planet; // only if there is a planet in this sector
    private Port _port;     // only if there is a port in this sector
    private final SectorId _sectorId;
    private final int _sectorNumber;

    private Sector (
        final SectorId sid,
        final int sectorNumber,
        final Cluster cluster,
        final Set<Sector> links,
        final Planet planet,
        final Port port
    ) {
        _sectorId = sid;
        _sectorNumber = sectorNumber;
        _cluster = cluster;
        _links.addAll(links);
        _planet = planet;
        _port = port;
    }

    public static Sector createNewSector(
        final Cluster cluster,
        final Integer sectorNumber
    ) {
        SectorId sid = new SectorId(_nextSectorId++);
        var s = new Sector(sid, sectorNumber, cluster, Collections.emptySet(), null, null);
        _inventory.put(sid, s);
        return s;
    }

    public static Sector getSector(final SectorId sectorId) { return _inventory.get(sectorId); }

    public Cluster getCluster() { return _cluster; }
    public Collection<Sector> getLinkedSectors() { return new LinkedList<>(_links); }
    public int getLinkCount() { return _links.size(); }
    public Planet getPlanet() { return _planet; }
    public Port getPort() { return _port; }
    public SectorId getSectorId() { return _sectorId; }
    public int getSectorNumber() { return _sectorNumber; }
    public boolean hasLinkTo(final Sector target) { return _links.contains(target); }

    public void setPlanet(final Planet value) { _planet = value; }
    public void setPort(final Port value) { _port = value; }

    public static void createBidirectionalLink(
        final Sector sector1,
        final Sector sector2
    ) {
        sector1._links.add(sector2);
        sector2._links.add(sector1);
    }

    public void createLinkTo(
        final Sector target
    ) {
        _links.add(target);
    }

    public static void dbCreateBidirectionalLink(
        final Connection conn,
        final Sector sector1,
        final Sector sector2
    ) throws SQLException {
        sector1._links.add(sector2);
        sector2._links.add(sector1);

        var sql = String.format(INSERT_SECTOR_LINK_SQL, sector1._sectorId, sector2._sectorId);
        var statement = conn.createStatement();
        statement.execute(sql);

        sql = String.format(INSERT_SECTOR_LINK_SQL, sector2._sectorId, sector1._sectorId);
        statement.execute(sql);
    }

    public void dbCreateLinkTo(
        final Connection conn,
        final Sector target
    ) throws SQLException {
        _links.add(target);

        var sql = String.format(INSERT_SECTOR_LINK_SQL, _sectorId, target._sectorId);
        var statement = conn.createStatement();
        statement.execute(sql);
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

    /**
     * Loads all sectors, including links. Clusters MUST be loaded before invoking this.
     */
    public static void dbLoad(
        final Connection conn
    ) throws SQLException {
        LOGGER.trace("dbLoad()");

        _inventory.clear();
        var sql = "SELECT * FROM sectors ORDER BY sectorId;";
        var statement = conn.createStatement();
        var rs = statement.executeQuery(sql);

        while (rs.next()) {
            var sid = rs.getLong("sectorId");
            var sectorId = new Sector.SectorId(sid);
            var clusterId = new Cluster.ClusterId(rs.getLong("clusterId"));
            var cluster = Cluster.getCluster(clusterId);
            var sectorNumber = rs.getInt("sectorNumber");
            var sector = new Sector(sectorId, sectorNumber, cluster, Collections.emptySet(), null, null);
            _inventory.put(sectorId, sector);
            _nextSectorId = sid + 1;

            cluster.addSector(sector);
        }

        sql = "SELECT * FROM sectorLinks;";
        rs = statement.executeQuery(sql);
        var linkCount = 0;
        while (rs.next()) {
            var fromId = new Sector.SectorId(rs.getLong("fromSectorId"));
            var toId = new Sector.SectorId(rs.getLong("toSectorId"));
            Sector.getSector(fromId).createLinkTo(Sector.getSector(toId));
            linkCount++;
        }

        var msg = String.format("Loaded %d sectors with %d links...\n", _inventory.size(), linkCount);
        System.out.println(msg);
        LOGGER.info(msg);
    }

    public void dbPersist(
        final Connection conn
    ) throws SQLException {
        var sql = String.format(INSERT_SECTOR_SQL, _sectorId, _cluster.getClusterId(), _sectorNumber);
        var statement = conn.createStatement();
        statement.execute(sql);
    }

    public void dbUpdate(
        final Connection conn
    ) throws SQLException {
        var sql = String.format(UPDATE_SECTOR_SQL,
                                _planet == null ? "null" : _planet.getPlanetId(),
                                _port == null ? "null" : _port.getPortId(),
                                _sectorId);
        var statement = conn.createStatement();
        statement.execute(sql);
    }

    public static class SectorId {

        private final long _value;

        public SectorId(
            final long id
        ) {
            _value = id;
        }

        @Override
        public boolean equals(
            final Object obj
        ) {
            if (obj instanceof SectorId id) {
                return id._value == _value;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return (int)_value;
        }

        @Override
        public String toString() {
            return String.valueOf(_value);
        }
    }
}

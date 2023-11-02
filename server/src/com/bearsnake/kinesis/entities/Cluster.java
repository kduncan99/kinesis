/*
 * kinesis
 * Copyright (c) 2020,2023 by Kurt Duncan - All Rights Reserved
 */

package com.bearsnake.kinesis.entities;

import com.bearsnake.kinesis.DatabaseWrapper;
import com.bearsnake.kinesis.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Represents a cluster of sectors
 */
public class Cluster {

    private static final String CREATE_TABLE_SQL =
        "CREATE TABLE clusters ("
            + "  clusterId integer PRIMARY KEY,"
            + "  clusterName text NOT NULL"
            + ") WITHOUT ROWID;";

    private static final String INSERT_SQL =
        "INSERT INTO clusters (clusterId, clusterName) VALUES (%s, '%s');";

    private static final Logger LOGGER = LogManager.getLogger("Cluster");
    private static final int LONGEST_PATH_TO_HOME = 20;
    private static long _nextClusterId = 1;
    private static final Map<ClusterId, Cluster> _inventory = new HashMap<>();

    private final ClusterId _clusterId;
    private final String _clusterName;
    final Set<Sector> _sectors = new HashSet<>();

    private Cluster(
        final ClusterId clusterId,
        final String name,
        final Collection<Sector> sectors
    ) {
        _clusterId = clusterId;
        _clusterName = name;
        _sectors.addAll(sectors);
    }

    void addSector(final Sector sector) { _sectors.add(sector); }
    public static Cluster getCluster(final ClusterId clusterId) { return _inventory.get(clusterId); }
    public ClusterId getClusterId() { return _clusterId; }
    public String getClusterName() { return _clusterName; }

    public Sector getSector(
        final int sectorNumber
    ) {
        for (var sector : _sectors) {
            if (sector.getSectorNumber() == sectorNumber) {
                return sector;
            }
        }

        return null;
    }

    public static Cluster createStandardCluster(
        final DatabaseWrapper databaseWrapper,
        final String name,
        final int sectorCount,
        final int portCount
    ) throws DatabaseException {
        LOGGER.trace("Creating standard cluster name={} sectors={}", name, sectorCount);

        if ((sectorCount < 100) || (sectorCount > 10000)) {
            throw new RuntimeException("Invalid sector count");
        }

        if (portCount > sectorCount / 10) {
            throw new RuntimeException("Too many ports specified");
        }

        var clusterId = new ClusterId(_nextClusterId++);
        var cluster = new Cluster(clusterId, name, Collections.emptySet());
        for (int sectorNum = 1; sectorNum <= sectorCount; sectorNum++) {
            cluster._sectors.add(Sector.createNewSector(cluster, sectorNum));
        }

        // establish initial random links between sectors
        var random = new Random(System.currentTimeMillis());
        var lowLimit = 1;
        var highLimit = cluster._sectors.size();
        for (var sourceSector : cluster._sectors) {
            var sourceSectorNumber = sourceSector.getSectorNumber();
            var targetSectorNumber = sourceSectorNumber + random.nextInt(21) - 10;
            while ((targetSectorNumber == sourceSectorNumber)
                || (targetSectorNumber < lowLimit)
                || (targetSectorNumber > highLimit)) {
                targetSectorNumber = sourceSectorNumber + random.nextInt(21) - 10;
            }

            Sector.createBidirectionalLink(sourceSector, cluster.getSector(targetSectorNumber));
        }

        // Make sure sector 1 has at least 5 links. That is an arbitrary, but probably good number
        var firstSector = cluster.getSector(1);
        while (firstSector.getLinkCount() < 5) {
            var targetSectorNum = random.nextInt(10) + 2; // sector number ranges from 2 to 11
            Sector.createBidirectionalLink(firstSector, cluster.getSector(targetSectorNum));
        }

        // Create an initial group of interconnected sectors, which includes sector 1.
        var group = new HashSet<Sector>();
        cluster.collectRelatedSectors(group, firstSector);

        // Now find any orphaned sectors and link them into the main group.
        // We do this by checking the existing sectors, and if we find one which is not in the main group,
        // we create an orphan group for the orphan (which may include many other orphaned sectors as well),
        // we link the original orphan to a sector in the main group, then add all the orphan sectors to
        // the main group... then continue iterating over the sectors until done.
        for (var thisSector : cluster._sectors) {
            if (!group.contains(thisSector)) {
                var orphans = new HashSet<Sector>();
                cluster.collectRelatedSectors(orphans, thisSector);

                // choose a sector from the main group at random
                var targetSectorNumber = random.nextInt(sectorCount) + 1;
                var targetSector = cluster.getSector(targetSectorNumber);
                while ((targetSectorNumber < 2) || !group.contains(targetSector)) {
                    targetSectorNumber = random.nextInt(sectorCount) + 1;
                    targetSector = cluster.getSector(targetSectorNumber);
                }

                Sector.createBidirectionalLink(thisSector, targetSector);
                group.addAll(orphans);
            }
        }

        // Now there are no orphans. But there might be sectors which are too far from sector 1.
        // Any such sector gets a one-way link back to sector 1.
        for (var thisSector : cluster._sectors) {
            if (!thisSector.equals(firstSector)) {
                var path = getShortestPath(thisSector, firstSector);
                if (path.size() > LONGEST_PATH_TO_HOME) {
                    thisSector.createLinkTo(firstSector);
                }
            }
        }

        // Now do ports.
        var counter = 0;
        while (counter < portCount) {
            // choose a sector at least 3 away from the first sector.
            var sectorNumber = random.nextInt(sectorCount) + 1;
            var sector = cluster.getSector(sectorNumber);
            var path = getShortestPath(sector, firstSector);
            while ((sector.getPort() != null) || (path.size() < 3)) {
                sectorNumber = random.nextInt(sectorCount) + 1;
                sector = cluster.getSector(sectorNumber);
                path = getShortestPath(sector, firstSector);
            }

            var port = Port.createPort(sector, null);
            sector.setPort(port);
            counter++;
        }

        try {
            var conn = databaseWrapper.createConnection();
            conn.setAutoCommit(false);
            conn.beginRequest();
            cluster.dbPersist(conn);
            conn.commit();
            conn.close();
        } catch (SQLException ex) {
            LOGGER.catching(ex);
            throw new DatabaseException(ex.getMessage());
        }

        _inventory.put(cluster._clusterId, cluster);
        return cluster;
    }

    /**
     * Creates a collection of sectorIds for sectors which are connected to each other,
     * starting with one particular sector.
     */
    private void collectRelatedSectors(
        final HashSet<Sector> set,
        final Sector baseSector
    ) {
        if (!set.contains(baseSector)) {
            set.add(baseSector);
            for (var link : baseSector.getLinkedSectors()) {
                collectRelatedSectors(set, link);
            }
        }
    }

    /**
     * Wrapper which has no initial avoidance list
     */
    public static List<Sector> getShortestPath(
        final Sector start,
        final Sector goal
    ) {
        return getShortestPath(start, goal, Collections.emptyList());
    }

    /**
     * Finds the shortest path from start to goal.
     * Path does not contain the actual starting point, but it does contain the goal.
     * @param start starting sector
     * @param goal target sector
     * @return ordered path, ending with the goal sector.
     * If the starting goal is equal to the ending goal, the return path is empty.
     * If there is no path from the starting sector to the ending sector (which can happen with a non-empty avoid list)
     * the result is null.
     */
    public static LinkedList<Sector> getShortestPath(
        final Sector start,
        final Sector goal,
        final Collection<Sector> avoid
    ) {
        if (avoid.contains(goal)) {
            return null;
        }

        if (start.equals(goal)) {
            return new LinkedList<>();
        }

        var links = start.getLinkedSectors();
        if (links.contains(goal)) {
            var result = new LinkedList<Sector>();
            result.add(goal);
            return result;
        }

        var subAvoid = new LinkedList<>(avoid);
        LinkedList<Sector> subPath = null;
        subAvoid.add(start);
        for (var link : links) {
            if (!avoid.contains(link)) {
                var subResult = getShortestPath(link, goal, subAvoid);
                if (subResult != null) {
                    subResult.add(0, link);
                    if ((subPath == null) || (subPath.size() > subResult.size())) {
                        subPath = subResult;
                    }
                }
            }
        }

        return subPath;
    }

    public void showGeometry() {
        for (var sector : _sectors) {
            var sb = new StringBuilder();
            sb.append(sector.getSectorNumber()).append(":").append(" ");
            for (var link : sector.getLinkedSectors()) {
                sb.append(" ").append(link.getSectorNumber());
            }

            var planet = sector.getPlanet();
            if (planet != null) {
                sb.append(" Planet:").append(planet);
            }

            var port = sector.getPort();
            if (port != null) {
                sb.append(" Port:").append(port);
            }

            System.out.println(sb);
        }
    }

    public static void showGroup(
        final String caption,
        final Collection<Sector> sectors
    ) {
        var sb = new StringBuilder();
        sb.append(caption);
        for (var sector : sectors) {
            sb.append(" ").append(sector.getSectorNumber());
        }
        System.out.println(sb);
    }

    public static void dbCreateTable(
        final Connection conn
    ) throws SQLException {
        LOGGER.trace(CREATE_TABLE_SQL);
        var statement = conn.createStatement();
        statement.execute(CREATE_TABLE_SQL);
    }

    /**
     * Loads all clusters
     */
    public static void dbLoad(
        final Connection conn
    ) throws SQLException {
        LOGGER.trace("dbLoad()");

        _inventory.clear();
        var sql = "SELECT * FROM clusters ORDER BY clusterId;";
        var statement = conn.createStatement();
        var rs = statement.executeQuery(sql);

        while (rs.next()) {
            var cid = rs.getLong("clusterId");
            var clusterId = new ClusterId(cid);
            var clusterName = rs.getString("clusterName");
            var cluster = new Cluster(clusterId, clusterName, Collections.emptySet());
            _inventory.put(clusterId, cluster);
            _nextClusterId = cid + 1;
        }

        var msg = String.format("Loaded %d cluster(s)...", _inventory.size());
        System.out.println(msg);
        LOGGER.info(msg);
    }

    public void dbPersist(
        final Connection conn
    ) throws SQLException {
        var sql = String.format(INSERT_SQL, _clusterId, _clusterName);
        var statement = conn.createStatement();
        statement.execute(sql);

        for (var sector : _sectors) {
            sector.dbPersist(conn);

            var planet = sector.getPlanet();
            if (planet != null) {
                planet.dbPersist(conn);
            }

            var port = sector.getPort();
            if (port != null) {
                port.dbPersist(conn);
            }
        }

        conn.commit();
    }

    public static class ClusterId {

        private final long _value;

        public ClusterId(
            final long id
        ) {
            _value = id;
        }

        @Override
        public boolean equals(
            final Object obj
        ) {
            if (obj instanceof ClusterId id) {
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

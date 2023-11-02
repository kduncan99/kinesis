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

    private static final int LONGEST_PATH_TO_HOME = 20;
    private static final Logger LOGGER = LogManager.getLogger("Cluster");
    private static final Map<ClusterId, Cluster> _inventory = new HashMap<>();

    private final ClusterId _clusterId;
    private final String _name;
    final Set<SectorId> _sectors = new HashSet<>();

    private Cluster(
        final String name,
        final int sectorCount
    ) {
        ClusterId cid = new ClusterId(1);
        while (_inventory.containsKey(cid)) {
            cid = cid.next();
        }

        _clusterId = cid;
        _name = name;
        for (int sectorNum = 1; sectorNum <= sectorCount; sectorNum++) {
            var s = Sector.createNewSector(cid, sectorNum);
            _sectors.add(s.getSectorId());
        }
    }

    public ClusterId getClusterId() { return _clusterId; }
    public String getName() { return _name; }

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

        var random = new Random(System.currentTimeMillis());
        var cluster = new Cluster(name, sectorCount);

        // establish initial random links between sectors
        var lowLimit = 1;
        var highLimit = cluster._sectors.size();
        for (var thisSid : cluster._sectors) {
            var thisSectorNumber = thisSid.getSectorNumber();
            var targetSectorNumber = thisSectorNumber + random.nextInt(21) - 10;
            while ((targetSectorNumber == thisSectorNumber)
                || (targetSectorNumber < lowLimit)
                || (targetSectorNumber > highLimit)) {
                targetSectorNumber = thisSectorNumber + random.nextInt(21) - 10;
            }

            var targetSid = new SectorId(cluster._clusterId, targetSectorNumber);
            Sector.createBidirectionalLink(thisSid, targetSid);
        }

        // Make sure sector 1 has at least 5 links. That is an arbitrary, but probably good number
        var firstSectorId = new SectorId(cluster._clusterId, 1);
        var firstSector = Sector.getSector(firstSectorId);
        while (firstSector.getLinkCount() < 5) {
            var targetSectorNum = random.nextInt(10) + 2; // sector number ranges from 2 to 11
            var targetSid = new SectorId(cluster._clusterId, targetSectorNum);
            Sector.createBidirectionalLink(firstSectorId, targetSid);
        }

        // Create an initial group of interconnected sectors, which includes sector 1.
        var group = new HashSet<SectorId>();
        cluster.collectRelatedSectors(group, firstSectorId);

        // Now find any orphaned sectors and link them into the main group.
        // We do this by checking the existing sectors, and if we find one which is not in the main group,
        // we create an orphan group for the orphan (which may include many other orphaned sectors as well),
        // we link the original orphan to a sector in the main group, then add all the orphan sectors to
        // the main group... then continue iterating over the sectors until done.
        for (var thisSectorId : cluster._sectors) {
            if (!group.contains(thisSectorId)) {
                var orphans = new HashSet<SectorId>();
                cluster.collectRelatedSectors(orphans, thisSectorId);

                // choose a sector from the main group at random
                var targetSectorNumber = random.nextInt(sectorCount) + 1;
                var targetSectorId = new SectorId(cluster._clusterId, targetSectorNumber);
                while ((targetSectorNumber < 2) || !group.contains(targetSectorId)) {
                    targetSectorNumber = random.nextInt(sectorCount) + 1;
                    targetSectorId = new SectorId(cluster._clusterId, targetSectorNumber);
                }

                Sector.createBidirectionalLink(thisSectorId, targetSectorId);
                group.addAll(orphans);
            }
        }

        // Now there are no orphans. But there might be sectors which are too far from sector 1.
        // Any such sector gets a one-way link back to sector 1.
        for (var thisSectorId : cluster._sectors) {
            if (!thisSectorId.equals(firstSectorId)) {
                var path = getShortestPath(thisSectorId, firstSectorId);
                if (path.size() > LONGEST_PATH_TO_HOME) {
                    Sector.getSector(thisSectorId).createLinkTo(firstSectorId);
                }
            }
        }

        // Now do ports.
        var counter = 0;
        while (counter < portCount) {
            // choose a sector at least 3 away from the first sector.
            var sectorNumber = random.nextInt(sectorCount) + 1;
            var sectorId = new SectorId(cluster._clusterId, sectorNumber);
            var sector = Sector.getSector(sectorId);
            var path = getShortestPath(sectorId, firstSectorId);
            while ((Sector.getSector(sectorId).getPortId() != null) || (path.size() < 3)) {
                sectorNumber = random.nextInt(sectorCount) + 1;
                sectorId = new SectorId(cluster._clusterId, sectorNumber);
                sector = Sector.getSector(sectorId);
                path = getShortestPath(sectorId, firstSectorId);
            }

            var port = Port.createPort(sectorId);
            sector.setPortId(port.getPortId());
            counter++;
        }

        _inventory.put(cluster._clusterId, cluster);

        cluster.persist(databaseWrapper);
        return cluster;
    }

    /**
     * Creates a collection of sectorIds for sectors which are connected to each other,
     * starting with one particular sector.
     */
    private void collectRelatedSectors(
        final HashSet<SectorId> set,
        final SectorId initialSid
    ) {
        if (!set.contains(initialSid)) {
            set.add(initialSid);
            for (var linkedSectorId : Sector.getSector(initialSid).getLinks()) {
                collectRelatedSectors(set, linkedSectorId);
            }
        }
    }

    public static List<SectorId> getShortestPath(
        final SectorId start,
        final SectorId goal
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
    public static LinkedList<SectorId> getShortestPath(
        final SectorId start,
        final SectorId goal,
        final Collection<SectorId> avoid
    ) {
        if (avoid.contains(goal)) {
            return null;
        }

        if (start.equals(goal)) {
            return new LinkedList<>();
        }

        var links = Sector.getSector(start).getLinks();
        if (links.contains(goal)) {
            var result = new LinkedList<SectorId>();
            result.add(goal);
            return result;
        }

        var subAvoid = new LinkedList<>(avoid);
        LinkedList<SectorId> subPath = null;
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

    /**
     * Stores everything related to this cluster (including the cluster) in the database.
     * Used as a last step in creating and populating a cluster -- NOT for piece-meal things.
     */
    public void persist(
        final DatabaseWrapper db
    ) throws DatabaseException {
        try {
            var conn = db.createConnection();
            conn.setAutoCommit(false);
            conn.beginRequest();

            dbPersist(conn);

            for (var sectorId : _sectors) {
                var sector = Sector.getSector(sectorId);
                sector.dbPersist(conn);

                // TODO planet for sector

                var portId = sector.getPortId();
                if (portId != null) {
                    Port.getPort(portId).dbPersist(conn);
                }
            }

            conn.commit();
            db.deleteConnection(conn);
        } catch (SQLException ex) {
            LOGGER.catching(ex);
            throw new DatabaseException(ex.getMessage());
        }
    }

    public void showGeometry() {
        for (var sectorId : _sectors) {
            var sb = new StringBuilder();
            sb.append(sectorId.getSectorNumber()).append(":").append(" ");
            var sector = Sector.getSector(sectorId);
            for (var link : sector.getLinks()) {
                sb.append(" ").append(link.getSectorNumber());
            }

            var pid = sector.getPortId();
            if (pid != null) {
                sb.append(" Port:").append(Port.getPort(pid));
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

    public void dbPersist(
        final Connection conn
    ) throws SQLException {
        var sql = String.format(INSERT_SQL, _clusterId, _name);
        var statement = conn.createStatement();
        statement.execute(sql);
    }
}

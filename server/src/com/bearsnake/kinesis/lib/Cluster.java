/*
 * kinesis
 * Copyright (c) 2020,2023 by Kurt Duncan - All Rights Reserved
 */

package com.bearsnake.kinesis.lib;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.IntStream;

/**
 * Represents a cluster of sectors
 */
public class Cluster {

    private static final int LONGEST_PATH_TO_HOME = 20;
    private static final Logger LOGGER = LogManager.getLogger("Cluster");

    public final int _identifier;
    public final String _name;
    final Map<Integer, Sector> _sectorMap = new HashMap<>(); // key is sector number, unique to this map

    private Cluster(
        final int identifier,
        final String name,
        final int sectorCount
    ) {
        _identifier = identifier;
        _name = name;
        IntStream.rangeClosed(1, sectorCount).forEach(sn -> _sectorMap.put(sn, new Sector(sn)));
    }

    public static Cluster createCluster(
        final int identifier,
        final String name,
        final int sectorCount
    ) {
        LOGGER.trace("Creating cluster id={} name={} sectors={}", identifier, name, sectorCount);

        var random = new Random(System.currentTimeMillis());
        var cluster = new Cluster(identifier, name, sectorCount);

        // establish initial random links between sectors
        var lowLimit = 1;
        var highLimit = cluster._sectorMap.size();
        for (var thisSector : cluster._sectorMap.values()) {
            var thisSectorNumber = thisSector.getSectorNumber();
            var targetSectorNumber = thisSectorNumber + random.nextInt(21) - 10;
            while ((targetSectorNumber == thisSectorNumber)
                || (targetSectorNumber < lowLimit)
                || (targetSectorNumber > highLimit)) {
                targetSectorNumber = thisSectorNumber + random.nextInt(21) - 10;
            }

            var targetSector = cluster._sectorMap.get(targetSectorNumber);
            cluster.link(thisSector, targetSector);
        }

        // Make sure sector 1 has at least 5 links. That is an arbitrary, but probably good number
        var firstSector = cluster._sectorMap.get(1);
        while (firstSector.getLinkCount() < 5) {
            var sn = random.nextInt(10) + 2; // sector number ranges from 2 to 11
            var target = cluster._sectorMap.get(sn);
            cluster.link(firstSector, target);
        }

        // Create an initial group of interconnected sectors, which includes sector 1.
        var group = new HashSet<Sector>();
        cluster.collectRelatedSectors(group, firstSector);

        // Now find any orphaned sectors and link them into the main group.
        // We do this by checking the existing sectors, and if we find one which is not in the main group,
        // we create an orphan group for the orphan (which may include many other orphaned sectors as well),
        // we link the original orphan to a sector in the main group, then add all the orphan sectors to
        // the main group... then continue iterating over the sectors until done.
        for (var thisSector : cluster._sectorMap.values()) {
            if (!group.contains(thisSector)) {
                var orphans = new HashSet<Sector>();
                cluster.collectRelatedSectors(orphans, thisSector);

                // choose a sector from the main group at random
                var sn = random.nextInt(sectorCount) + 1;
                while ((sn < 2) || (!group.contains(cluster._sectorMap.get(sn)))) {
                    sn = random.nextInt(sectorCount) + 1;
                }

                var target = cluster._sectorMap.get(sn);
                cluster.link(thisSector, target);
                group.addAll(orphans);
            }
        }

        // Now there are no orphans. But there might be sectors which are too far from sector 1.
        // Any such sector gets a one-way link back to sector 1.
        for (var thisSector : cluster._sectorMap.values()) {
            if (!thisSector.equals(firstSector)) {
                var path = cluster.getShortestPath(thisSector, firstSector);
                if (path.size() > LONGEST_PATH_TO_HOME) {
                    thisSector.createLinkTo(firstSector);
                }
            }
        }

        return cluster;
    }

    /**
     * Creates a collection of sectors which are connected to each other, starting with one sector.
     */
    private void collectRelatedSectors(
        final HashSet<Sector> set,
        final Sector start
    ) {
        if (!set.contains(start)) {
            set.add(start);
            for (var linkedSector : start.getLinks()) {
                collectRelatedSectors(set, linkedSector);
            }
        }
    }

    private void link(
        final Sector sector1,
        final Sector sector2
    ) {
        sector1.createLinkTo(sector2);
        sector2.createLinkTo(sector1);
    }

    public List<Sector> getShortestPath(
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
    public LinkedList<Sector> getShortestPath(
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

        var links = start.getLinks();
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
        for (var sector : _sectorMap.values()) {
            var sb = new StringBuilder();
            sb.append(sector.getSectorNumber()).append(":").append(" ");
            for (var link : sector.getLinks()) {
                sb.append(" ").append(link.getSectorNumber());
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
}

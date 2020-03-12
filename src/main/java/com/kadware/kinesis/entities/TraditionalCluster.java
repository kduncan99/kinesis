/*
 * kinesis
 * Copyright (c) 2020 by Kurt Duncan - All Rights Reserved
 */

package com.kadware.kinesis.entities;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents a traditional cluster...
 * Cluster center will be at sector 1
 * Sectors will have a maximum of 6 links
 * Minimum size is 1000, maximum is 10000
 * Every sector will be {n} hops, at most, away from cluster center - this will be aided if necessary by warps
 *     which are uni-directional links directly to cluster center.
 *     For a sector count of 1000, {n} is 20, increasing to 200 for sector count of 10000.
 * Wormholes exist - these are uni-directional hidden links. They take an unsuspecting traveller who lands at the source
 *     point, and try to subsequently move, and place them in a destination sector, damaging the ship in the process.
 *     One or two of them will be created each day at a given source/destination pair, and will disappear at the end of
 *     that day.
 * Normal sector links are bi-directional, and are normally no more than 20 sectors away, ordinally, from all linked sectors
 * Sector count never changes, and links are never altered (except for the wormhole, which moves from day to day)
 */
public class TraditionalCluster extends Cluster {

    public static final int MAX_SECTORS = 10000;
    public static final int MIN_SECTORS = 1000;
    private static final int MAX_MAX_PATH = 200;
    private static final int MIN_MAX_PATH = 20;
    private static final float MAX_PATH_SLOPE = (float)(MAX_MAX_PATH - MIN_MAX_PATH) / (float)(MAX_SECTORS - MIN_SECTORS);
    private static final Logger _logger = LogManager.getLogger("TraditionalCluster");

    private TraditionalCluster(
        final int identifier,
        final String name,
        final Map<Integer, Sector> map
    ) {
        super(identifier, name, map);
    }

    private static void createPathsRecursive(
        final Map<Integer, Sector> sectorMap,
        final Map<Integer, List<Integer>> resultMap,
        final int sourceSectorId
    ) {
        Sector source = sectorMap.get(sourceSectorId);
        List<Integer> newPath = resultMap.get(sourceSectorId);
        newPath.add(sourceSectorId);

        for (int destSectorId : source._links) {
            if (resultMap.get(destSectorId) == null) {
                resultMap.put(destSectorId, newPath);
                createPathsRecursive(sectorMap, resultMap, destSectorId);
            }
        }
    }

    /**
     * For each sector in the sector map, we create the shortest path from that sector to sector 1.
     * E.g., if there is a path: 25 - 19 - 11 - 1, the path for sector 25 is [19, 11, 1].
     * This is effected by starting at 1, and working our way recursively through all the sectors which we can reach.
     * As a result, we will know the distance from every unorphaned sector to sector 1, and we'll know which
     * sectors are orphaned (they won't have a path).
     * The resulting map is keyed by the source sector ID, with the value set to that sector's path to sector 1.
     */
    private static Map<Integer, List<Integer>> createPathsToCenter(
        final Map<Integer, Sector> sectorMap
    ) {
        Map<Integer, List<Integer>> resultMap = new HashMap<>();
        resultMap.put(1, new LinkedList<>()); //  empty path from 1 to 1
        createPathsRecursive(sectorMap, resultMap, 1);
        return resultMap;
    }

    /**
     * Creates a Cluster object and populates it with an appropriate number of Sector objects,
     * then creates links between all of the objects in accordance with the general structure requirements
     * of this cluster type.
     * @param name Name for the cluster
     * @param sectorCount Number of sectors to be created
     */
    public static TraditionalCluster createCluster(
        final String name,
        final int sectorCount
    ) {
        _logger.traceEntry();

        final int maxLinks = 6;
        final int maxPath = (int)(sectorCount * MAX_PATH_SLOPE);

        long t0 = System.currentTimeMillis();//TODO remove
        _logger.trace(String.format("Creating %d sectors...", sectorCount));
        Map<Integer, Sector> resultMap = new HashMap<>();
        for (int sectorId = 1; sectorId <= sectorCount; ++sectorId) {
            resultMap.put(sectorId, new Sector(sectorId));
        }
        long t1 = System.currentTimeMillis();//TODO remove
        System.out.println(String.format("Done creating sectors - %d milliseconds", t1 - t0));//TODO remove

        _logger.trace("Creating sector links...");
        Random random = new Random(System.currentTimeMillis());
        for (int sectorId = 1; sectorId <= sectorCount; ++sectorId) {
            int destSectorId = sectorId + random.nextInt(21) - 10;
            while ((destSectorId == sectorId)
                   || (destSectorId < 1)
                   || (destSectorId > sectorCount)
                   || (resultMap.get(destSectorId)._links.size() == maxLinks)) {
                destSectorId = sectorId + random.nextInt(21) - 10;
            }

            resultMap.get(sectorId)._links.add(destSectorId);
            resultMap.get(destSectorId)._links.add(sectorId);
        }
        long t2 = System.currentTimeMillis();//TODO remove
        System.out.println(String.format("Done creating links - %d milliseconds", t2 - t1));//TODO remove

        //  Create a snapshot of all the shortest paths to center (sector 1),
        //  then connect orphans, then create warps such that no path to sector 1 is greater than maxPath.
        Map<Integer, List<Integer>> pathsToCenter = createPathsToCenter(resultMap);
        long t3 = System.currentTimeMillis();//TODO remove
        System.out.println(String.format("Done creating paths - %d milliseconds", t3 - t2));//TODO remove

        for (int sectorId = 1; sectorId <= sectorCount; ++sectorId) {
            if (pathsToCenter.get(sectorId) == null) {
                int destSectorId = random.nextInt(sectorCount - 1) + 1;
                while ((destSectorId == sectorId)
                       || (destSectorId < 1)
                       || (destSectorId > sectorCount)
                       || (resultMap.get(destSectorId)._links.size() == maxLinks)
                       || (pathsToCenter.get(destSectorId) == null)) {
                    destSectorId = random.nextInt(sectorCount - 1) + 1;
                }

                resultMap.get(sectorId)._links.add(destSectorId);
                resultMap.get(destSectorId)._links.add(sectorId);
                List<Integer> newPath = pathsToCenter.get(destSectorId);
                newPath.add(destSectorId);
                pathsToCenter.put(sectorId, newPath);
                createPathsRecursive(resultMap, pathsToCenter, sectorId);
            }
        }
        long t4 = System.currentTimeMillis();//TODO remove
        System.out.println(String.format("Done checking for orphans - %d milliseconds", t4 - t3));//TODO remove

        int identifier = 1; // TODO make this unique
        return new TraditionalCluster(identifier, name, resultMap);
    }

    @Override
    public Geometry getGeometry() {
        return Geometry.Traditional;
    }
}

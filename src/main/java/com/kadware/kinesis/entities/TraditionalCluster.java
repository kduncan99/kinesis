/*
 * kinesis
 * Copyright (c) 2020 by Kurt Duncan - All Rights Reserved
 */

package com.kadware.kinesis.entities;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.kadware.kinesis.exceptions.BadParameterException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents a traditional cluster...
 * Cluster center will be at sector 1
 * Sectors will have a maximum of 6 links
 * Minimum size is 1000, maximum is 10000
 * Every sector will be {n} hops, at most, away from cluster center - this will be aided if necessary by warps
 *     which are uni-directional links directly to cluster center.
 *     For a sector count of 1000, {n} is 20, increasing to 50 for sector count of 10000.
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
    private static final int MAX_MAX_PATH = 50;
    private static final int MIN_MAX_PATH = 20;
    private static final int MAX_LINKS = 6;
    private static final float MAX_PATH_SLOPE = (float)(MAX_MAX_PATH - MIN_MAX_PATH) / (float)(MAX_SECTORS - MIN_SECTORS);
    private static final Logger LOGGER = LogManager.getLogger("TraditionalCluster");
    private static final Random RANDOM = new Random(System.currentTimeMillis());

    private final int _maxPath;
    private final int _sectorCount;

    private TraditionalCluster(
        final int identifier,
        final String name,
        final int sectorCount,
        final int maxPath
    ) {
        super(identifier, name, MAX_LINKS);
        _sectorCount = sectorCount;
        _maxPath = maxPath;
    }

    /**
     * Create links among the sectors
     */
    private void createSectorLinks() {
        LOGGER.trace("Creating sector links...");
        System.out.println("Creating sector links...");//TODO remove
        for (int sectorId = 1; sectorId <= _sectorCount; ++sectorId) {
            int destSectorId = sectorId + RANDOM.nextInt(21) - 10;
            if (destSectorId < 1) {
                destSectorId = -destSectorId;
            }
            while (!createSectorLink(sectorId, destSectorId)) {
                destSectorId = sectorId + RANDOM.nextInt(21) - 10;
                if (destSectorId < 1) {
                    destSectorId = -destSectorId;
                }
            }
        }
    }

    /**
     * For each sector in the sector map, we create the shortest path from that sector to sector 1.
     * E.g., if there is a path: 25 - 19 - 11 - 1, the path for sector 25 is [19, 11, 1].
     */
    private void createSectorPathsToCenter() {
        //  Clear out existing paths, in case this is a re-create
        for (Sector sector : _sectorMap.values()) {
            sector._pathToCenter = null;
        }

        boolean didSomething = true;
        while (didSomething) {
            didSomething = false;
            _sectorMap.get(1)._pathToCenter = new LinkedList<>();   //  empty path to itself
            for (Sector sector : _sectorMap.values()) {
                if (sector._pathToCenter == null) {
                    List<Integer> shortestPath = null;
                    int shortestPathDestSectorId = 0;
                    for (int destSectorId : sector._links) {
                        Sector destSector = _sectorMap.get(destSectorId);
                        if (destSector._pathToCenter != null) {
                            if (!destSector._pathToCenter.contains(sector._sectorNumber)) {
                                if (shortestPath == null) {
                                    shortestPath = destSector._pathToCenter;
                                    shortestPathDestSectorId = destSectorId;
                                } else if (destSector._pathToCenter.size() < shortestPath.size()) {
                                    shortestPath = destSector._pathToCenter;
                                    shortestPathDestSectorId = destSectorId;
                                }
                            }
                        }
                    }

                    if (shortestPath != null) {
                        sector._pathToCenter = new LinkedList<>();
                        sector._pathToCenter.add(shortestPathDestSectorId);
                        sector._pathToCenter.addAll(shortestPath);
                        didSomething = true;
                    }
                }
            }
        }
    }

    private void fixLongPaths() {
        LOGGER.trace("Fixing long paths...");
        System.out.println("Fixing long paths...");//TODO remove
        for (Sector sector : _sectorMap.values()) {
            if ((sector._pathToCenter.size() > _maxPath) && (sector._links.size() < _maxLinksPerSector)) {
                LOGGER.trace(String.format("Creating warp from sector %d to sector 1", sector._sectorNumber));
                System.out.println(String.format("Creating warp from sector %d to sector 1", sector._sectorNumber));//TODO remove
                sector._links.add(1);
                createSectorPathsToCenter();
            }
        }
    }

    /**
     * Find orphaned sectors (i.e., which have no path back to sector 1), and link them to some random
     * other sector which is not maxed out on links, and which *does* have a path back to sector 1.
     */
    private void fixOrphans() {
        LOGGER.trace("Fixing orphaned sectors...");
        System.out.println("Fixing orphaned sectors...");//TODO remove
        for (Sector sector : _sectorMap.values()) {
            if ((sector._sectorNumber > 2)
                    && (sector._pathToCenter == null)
                    && (sector._links.size() < MAX_LINKS)) {
                int destSectorId = RANDOM.nextInt(_sectorCount) + 1;
                while (true) {
                    Sector destSector = _sectorMap.get(destSectorId);
                    if ((destSector._pathToCenter != null) && (createSectorLink(sector, destSector))) {
                        break;
                    }
                    destSectorId = RANDOM.nextInt(_sectorCount) + 1;
                }

                LOGGER.trace(String.format("Added links between orphan sector %d and sector %d",
                                           sector._sectorNumber,
                                           destSectorId));
                System.out.println(String.format("Added links between orphan sector %d and sector %d",
                                                 sector._sectorNumber,
                                                 destSectorId)); // TODO remove

                createSectorPathsToCenter();
            }
        }
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
    ) throws BadParameterException {
        LOGGER.traceEntry();

        if ((sectorCount < MIN_SECTORS) || (sectorCount > MAX_SECTORS)) {
            throw new BadParameterException(String.format("Illegal number of sectors:%d", sectorCount));
        }

        int clusterIdentifier = getNextIdentifier();
        int maxPath = (int)(sectorCount * MAX_PATH_SLOPE);
        TraditionalCluster cluster = new TraditionalCluster(clusterIdentifier, name, sectorCount, maxPath);

        LOGGER.trace(String.format("Creating %d sectors...", sectorCount));
        cluster._sectorMap.clear();
        for (int sectorId = 1; sectorId <= sectorCount; ++sectorId) {
            cluster._sectorMap.put(sectorId, new Sector(Sector.getNextIdentifier(), clusterIdentifier, sectorId));
        }

        cluster.createSectorLinks();
        cluster.createSectorPathsToCenter();
        cluster.fixOrphans();
        cluster.fixLongPaths();

        return cluster;
    }

    @Override
    public Geometry getGeometry() {
        return Geometry.Traditional;
    }
}

/*
 * kinesis
 * Copyright (c) 2020 by Kurt Duncan - All Rights Reserved
 */

package com.kadware.kinesis.entities;

import com.kadware.kinesis.exceptions.BadParameterException;
import com.kadware.kinesis.exceptions.NoSuchPathException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Sector {

    public static final Logger _logger = LogManager.getLogger();

    public final int _identifier;
    public final int _clusterIdentifier;
    public final List<Integer> _links;

    public Sector (
        int identifier,
        int clusterIdentifier
    ) {
        _identifier = identifier;
        _clusterIdentifier = clusterIdentifier;
        _links = new LinkedList<>();
    }

    /**
     * Creates a sector map representing a cluster
     * @param clusterId identifier of a (presumably existing) cluster
     * @param startingSectorId first sector id to create - must not conflict with any existing sectors
     * @param sectorCount number of sectors to recte
     * @param coreSectorId identifies the sector which serves as the core for this cluster
     *                     must be within the range of sectors to be created
     * @param pathLimit longest path allowed from any none-core sector to the core sector
     * @return sector map
     */
    public static Map<Integer, Sector> createCluster(
        final int clusterId,
        final int startingSectorId,
        final int sectorCount,
        final int coreSectorId,
        final int pathLimit
    ) throws BadParameterException {
        // TODO change errors to infos or whatever
        // TODO this is very inefficient -
        //      start at core, and work forward and back?
        //      every sector preserves it's path to core, to speed things up?
        //      keep list of orphans and non-orphans to expedite de-orphaning?
        _logger.error(String.format("Creating sector map for cluster %d", clusterId));
        _logger.error(String.format("  Starting id=%d  count=%d  core=%d  limit=%d",
                                   startingSectorId,
                                   sectorCount,
                                   coreSectorId,
                                   pathLimit));

        if (sectorCount < 100) {
            String msg = "Sector count must be >= 100";
            _logger.error(msg);
            throw new BadParameterException(msg);
        }

        if ((coreSectorId < startingSectorId)
            || (coreSectorId >= startingSectorId + sectorCount)) {
            String msg = "CoreSectorId out of limits";
            _logger.error(msg);
            throw new BadParameterException(msg);
        }

        if (pathLimit < 8) {
            String msg = "Path limit cannot be less than 8";
            _logger.error(msg);
            throw new BadParameterException(msg);
        }

        Random random = new Random(System.nanoTime());
        Map<Integer, Sector> sectorMap = new HashMap<>();

        for (int sectorId = startingSectorId; sectorId < startingSectorId + sectorCount; ++sectorId) {
            sectorMap.put(sectorId, new Sector(sectorId, clusterId));
        }

        for (int sectorId = startingSectorId; sectorId < startingSectorId + sectorCount; ++sectorId) {
            int low = Math.max(startingSectorId, sectorId - 10);
            int high = Math.min(sectorId + 10, startingSectorId + sectorCount - 1);
            int range = high - low + 1;

            boolean good = false;
            while (!good) {
                int destId = random.nextInt(range) + low;
                if ((sectorId != destId) && !sectorMap.get(sectorId)._links.contains(destId)) {
                    sectorMap.get(destId)._links.add(sectorId);
                    sectorMap.get(sectorId)._links.add(destId);
                    good = true;
                }
            }
        }

        for (int sectorId = startingSectorId; sectorId < startingSectorId + sectorCount; ++sectorId) {
            Sector sector = sectorMap.get(sectorId);
            if (sectorId != coreSectorId) {
                try {
                    List<Integer> path = sector.getPath(sectorMap, coreSectorId, sectorCount, null);
                    if (path.size() > pathLimit) {
                        String msg = String.format("Creating wormhole from %d to %d", sectorId, coreSectorId);
                        _logger.error(msg);
                        sectorMap.get(sectorId)._links.add(coreSectorId);
                    }
                } catch (NoSuchPathException e) {
                    int destId = (sectorId == startingSectorId) ? coreSectorId : sectorId - 1;
                    String msg = String.format("Linking orphaned sector %d to %d", sectorId, destId);
                    _logger.error(msg);
                    sector._links.add(destId);
                    sectorMap.get(destId)._links.add(sectorId);
                }
            }
        }

        return sectorMap;
    }

    /**
     * Retrieves a path of sector ids which describe the shortest path to reach a destination sector from this sector.
     * The search may be modified by limiting the maximum number of hops, and by specifying a list of sectors which are to be
     * avoided.  If the destination identifier is that of this sector, then an empty list is returned.
     * @param sectorMap container of Sector objects describing the entire playing field
     * @param destinationId sector id of the destination sector
     * @param pathLimit maximum number of hops to be considered - if null, there is no limit.
     * @param blackList list of sectors to be avoided when generating the path
     * @return A list of sector ids to be traversed to get from the source to the destination.
     * @throws NoSuchPathException if the search cannot be satisfied, given the inputs
     */
    public List<Integer> getPath(
        final Map<Integer, Sector> sectorMap,
        final int destinationId,
        final Integer pathLimit,
        final List<Integer> blackList
    ) throws NoSuchPathException {
//        System.out.println(String.format("getPath(from=%d, dest=%d, lim=%s, black=%s)",
//                                         _identifier,
//                                         destinationId,
//                                         String.valueOf(pathLimit),
//                                         getPathString(blackList)));//TODO remove
        if ((pathLimit != null) && (pathLimit == 0)) {
            throw new NoSuchPathException();
        }

        if ((blackList != null) && blackList.contains(destinationId)) {
            throw new NoSuchPathException();
        }

        if (destinationId == _identifier) {
            return new LinkedList<>();
        }

        for (Integer linkId : _links) {
            if (linkId == destinationId) {
                List<Integer> result = new LinkedList<>();
                result.add(linkId);
                return result;
            }
        }

        List<Integer> newBlackList = new LinkedList<>();
        if (blackList != null) {
            newBlackList.addAll(blackList);
        }
        newBlackList.add(_identifier);
        Integer newPathLimit = pathLimit == null ? null : pathLimit - 1;

        List<Integer> shortest = null;
        int shortestLink = 0;
        for (Integer linkId : _links) {
            if (!newBlackList.contains(linkId)) {
                Sector dest = sectorMap.get(linkId);
                try {
                    List<Integer> path = dest.getPath(sectorMap, destinationId, newPathLimit, newBlackList);
                    if ((shortest == null) || (path.size() < shortest.size())) {
                        shortest = path;
                        shortestLink = linkId;
                    }
                } catch(NoSuchPathException e) {
                    //  ignore
                }
            }
        }
        if (shortest != null) {
            List<Integer> result = new LinkedList<>();
            result.add(shortestLink);
            result.addAll(shortest);
            return result;
        }

        throw new NoSuchPathException();
    }

    public String getPathString(
        final List<Integer> path
    ) {
        if (path == null) {
            return "<null>";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("<");
            for (Integer i : path) {
                if (sb.length() != 1) {
                    sb.append(",");
                }
                sb.append(i);
            }
            sb.append(">");
            return sb.toString();
        }
    }
}

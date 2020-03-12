/*
 * kinesis
 * Copyright (c) 2020 by Kurt Duncan - All Rights Reserved
 */

package com.kadware.kinesis.entities;

import com.kadware.kinesis.exceptions.NoSuchPathException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Represents a cluster of sectors
 */
public abstract class Cluster {

    public static final Logger _logger = LogManager.getLogger("Cluster");

    public static enum Geometry {
        Traditional,
        Spoke,
        Grouped,
    }

    public static final String SOURCE_TABLE = "Clusters";
    public final int _identifier;
    public final String _name;
    public final Map<Integer, Sector> _map;

    Cluster(
        final int identifier,
        final String name,
        final Map<Integer, Sector> map
    ) {
        _identifier = identifier;
        _name = name;
        _map = map;
    }

    public abstract Geometry getGeometry();

    /**
     * Retrieves a path of sector ids which describe the shortest path from the source sector to the destination sector.
     * The search may be modified by limiting the maximum number of hops, and by specifying a list of sectors which are to be
     * avoided.  If the source and destination sectors are the same, the result is an empty list.
     * It is expected that this algorithm will work for any cluster geometry.
     * @param sourceSectorId sector id of the source sector
     * @param destinationSectorId sector id of the destination sector
     * @param pathLimit maximum number of hops to be considered - if null, there is no limit.
     * @param blackList list of sectors to be avoided when generating the path
     * @return A list of sector ids to be traversed to get from the source to the destination.
     * @throws NoSuchPathException if the search cannot be satisfied, given the inputs
     */
    public List<Integer> getPath(
        final int sourceSectorId,
        final int destinationSectorId,
        final Integer pathLimit,
        final List<Integer> blackList
    ) throws NoSuchPathException {
        System.out.println(String.format("getPath(from=%d, dest=%d, lim=%s, black=%s)",
                                         sourceSectorId,
                                         destinationSectorId,
                                         String.valueOf(pathLimit),
                                         getPathString(blackList)));//TODO remove

        if ((pathLimit != null) && (pathLimit == 0)) {
            throw new NoSuchPathException();
        }

        if ((blackList != null) && blackList.contains(destinationSectorId)) {
            throw new NoSuchPathException();
        }

        if (sourceSectorId == destinationSectorId) {
            return new LinkedList<>();
        }

//        for (Integer linkId : _links) {
//            if (linkId == destinationId) {
//                List<Integer> result = new LinkedList<>();
//                result.add(linkId);
//                return result;
//            }
//        }
//
//        List<Integer> newBlackList = new LinkedList<>();
//        if (blackList != null) {
//            newBlackList.addAll(blackList);
//        }
//        newBlackList.add(_identifier);
//        Integer newPathLimit = pathLimit == null ? null : pathLimit - 1;
//
//        List<Integer> shortest = null;
//        int shortestLink = 0;
//        for (Integer linkId : _links) {
//            if (!newBlackList.contains(linkId)) {
//                Sector dest = sectorMap.get(linkId);
//                try {
//                    List<Integer> path = dest.getPath(sectorMap, destinationId, newPathLimit, newBlackList);
//                    if ((shortest == null) || (path.size() < shortest.size())) {
//                        shortest = path;
//                        shortestLink = linkId;
//                    }
//                } catch(NoSuchPathException e) {
//                    //  ignore
//                }
//            }
//        }
//        if (shortest != null) {
//            List<Integer> result = new LinkedList<>();
//            result.add(shortestLink);
//            result.addAll(shortest);
//            return result;
//        }

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

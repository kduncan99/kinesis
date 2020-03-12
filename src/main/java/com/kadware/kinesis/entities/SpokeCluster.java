/*
 * kinesis
 * Copyright (c) 2020 by Kurt Duncan - All Rights Reserved
 */

package com.kadware.kinesis.entities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * Represents a hub-and-spoke cluster...
 * The geometry is a center hub of one sector (sector 1), surrounded by interconnected sectors 2 through 9
 * Each of sectors 2 through 9 are the root of a slim tree, branching out every several sectors.
 * The final leaves on the ends of all the tress are connected in a circumference.
 * Sectors have no artificial limits on number of links, but the geometry generally limits most of them to 3 (at most)
 * Sectors just inside the circumference are one-way, from interior to exterior, forcing the player to navigate through
 * a different spoke to return to cluster center.
 * The structure never changes during the game.
 */
public class SpokeCluster extends Cluster {

    public static final Logger _logger = LogManager.getLogger("SpokeCluster");

    private SpokeCluster(
        final int identifier,
        final String name,
        final Map<Integer, Sector> map
    ) {
        super(identifier, name, map);
    }

    public static SpokeCluster createCluster(
        final String name,
        final int sectorCount
    ) {
        return null;    //  TODO
    }

    @Override
    public Geometry getGeometry() {
        return Geometry.Spoke;
    }
}

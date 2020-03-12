/*
 * kinesis
 * Copyright (c) 2020 by Kurt Duncan - All Rights Reserved
 */

package com.kadware.kinesis.entities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * Represents a curdled cluster... that is, a cluster consisting of tightly-linked groups, which are loosely-linked to each
 * other.  The group size should vary from 20 to 40, and the sectors in the group are well-linked to each other.
 * However, there will be only 2 or 3 links to sectors in other groups, so it is tricky to move between groups.
 * Several of the groups will be linked to center.
 * Many of the links will be unidirectional.
 * The structure *can* change over time, in that new groups can be added, and existing groups might drop out of existence.
 * Additionally, the inter-group links may change during the course of the game, perhaps every day or so.
 */
public class GroupCluster extends Cluster {

    public static final Logger _logger = LogManager.getLogger("GroupCluster");

    private GroupCluster(
        final int identifier,
        final String name,
        final Map<Integer, Sector> map
    ) {
        super(identifier, name, map);
    }

    public static GroupCluster createCluster(
        final String name,
        final int sectorCount
    ) {
        return null;    //  TODO
    }

    @Override
    public Geometry getGeometry() {
        return Geometry.Grouped;
    }
}

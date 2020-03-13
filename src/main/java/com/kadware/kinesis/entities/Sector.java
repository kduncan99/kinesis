/*
 * kinesis
 * Copyright (c) 2020 by Kurt Duncan - All Rights Reserved
 */

package com.kadware.kinesis.entities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;

public class Sector {

    public static final Logger _logger = LogManager.getLogger("Sector");

    public final int _identifier;
    public final List<Integer> _links = new LinkedList<>();
    public List<Integer> _pathToCenter = null;

    public Sector (
        int identifier
    ) {
        _identifier = identifier;
    }
}

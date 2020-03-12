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

    public static final Logger _logger = LogManager.getLogger("Sector");

    public final int _identifier;
    public final List<Integer> _links;

    public Sector (
        int identifier
    ) {
        _identifier = identifier;
        _links = new LinkedList<>();
    }
}

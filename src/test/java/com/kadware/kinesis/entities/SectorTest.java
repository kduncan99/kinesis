/*
 * kinesis
 * Copyright (c) 2020 by Kurt Duncan - All Rights Reserved
 */

package com.kadware.kinesis.entities;

import com.kadware.kinesis.exceptions.BadParameterException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SectorTest {

    @Test
    public void testMap(
    ) throws BadParameterException {
        Map<Integer, Sector> sectorMap =
            Sector.createCluster(1, 1000, 1000, 1500, 20);

        for (Sector sector : sectorMap.values()) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("[%03d]:", sector._identifier));
            for (int destId : sector._links) {
                sb.append(String.format(" %03d", destId));
            }
            System.out.println(sb.toString());
        }
    }
}

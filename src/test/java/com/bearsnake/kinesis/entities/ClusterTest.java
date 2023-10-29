/*
 * kinesis
 * Copyright (c) 2020 by Kurt Duncan - All Rights Reserved
 */

package com.bearsnake.kinesis.entities;

import com.bearsnake.kinesis.Cluster;
import com.bearsnake.kinesis.exceptions.BadParameterException;
import org.junit.jupiter.api.Test;

public class ClusterTest {

    @Test
    public void testMap(
    ) throws BadParameterException {
        Cluster cluster = TraditionalGalaxy.createCluster("Foo", 10000);
    }
}

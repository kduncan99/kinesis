/*
 * kinesis
 * Copyright (c) 2020 by Kurt Duncan - All Rights Reserved
 */

package com.kadware.kinesis.entities;

import com.kadware.kinesis.exceptions.BadParameterException;
import org.junit.jupiter.api.Test;

public class ClusterTest {

    @Test
    public void testMap(
    ) throws BadParameterException {
        Cluster cluster = TraditionalCluster.createCluster("Foo", 10000);
    }
}

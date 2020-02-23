/*
 * kinesis
 * Copyright (c) 2020 by Kurt Duncan - All Rights Reserved
 */

package com.kadware.kinesis;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;

public class KinesisDatabaseTest {

    private static KinesisDatabase getDatabase() {
        return new KinesisDatabase("localhost",
                                   5432,
                                   "postgres",
                                   "postgres",
                                   "postgres");
    }

    @Test
    public void testClear(
    ) throws SQLException {
        KinesisDatabase kd = getDatabase();
        kd.clear();
    }

    @Test
    public void testInit(
    ) throws SQLException {
        KinesisDatabase kd = getDatabase();
        kd.initialize();
    }
}

/*
 * kinesis
 * Copyright (c) 2020 by Kurt Duncan - All Rights Reserved
 */

package com.bearsnake.kinesis;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;

public class DatabaseTest {

    private static Database getDatabase() {
        return new Database("localhost",
                            5432,
                            "postgres",
                            "postgres",
                            "postgres");
    }

    @Test
    public void testClear(
    ) throws SQLException {
        Database kd = getDatabase();
        kd.clear();
    }

    @Test
    public void testInit(
    ) throws SQLException {
        Database kd = getDatabase();
        kd.initialize();
    }
}

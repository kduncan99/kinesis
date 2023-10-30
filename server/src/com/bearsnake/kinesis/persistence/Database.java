/*
 * kinesis
 * Copyright (c) 2020,2023 by Kurt Duncan - All Rights Reserved
 */

package com.bearsnake.kinesis.persistence;

import com.bearsnake.kinesis.exceptions.DatabaseException;

public interface Database {

    void close() throws DatabaseException;
    void create() throws DatabaseException;
    void open() throws DatabaseException;
}

/*
 * kinesis
 * Copyright (c) 2020,2023 by Kurt Duncan - All Rights Reserved
 */

package com.bearsnake.kinesis.exceptions;

public class DatabaseException extends KinesisException {

    public DatabaseException(
        final String msg
    ) {
        super(msg);
    }
}

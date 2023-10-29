/*
 * kinesis
 * Copyright (c) 2020 by Kurt Duncan - All Rights Reserved
 */

package com.bearsnake.kinesis.exceptions;

public class BadParameterException extends KinesisException {

    public BadParameterException(
        final String msg
    ) {
        super(msg);
    }
}

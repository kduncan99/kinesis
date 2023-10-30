/*
 * kinesis
 * Copyright (c) 2020,2023 by Kurt Duncan - All Rights Reserved
 */

package com.bearsnake.kinesis.exceptions;

public abstract class KinesisException extends Exception {

    public KinesisException() {}
    public KinesisException(String msg) {super(msg);}
}

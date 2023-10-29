/*
 * kinesis
 * Copyright (c) 2020,2023 by Kurt Duncan - All Rights Reserved
 */

package com.bearsnake.kinesis.entities;

public class ClusterId {

    private final Integer _value;

    public ClusterId(
        final int id
    ) {
        _value = id;
    }

    ClusterId next() {
        return new ClusterId(_value + 1);
    }

    @Override
    public boolean equals(
        final Object obj
    ) {
        if (obj instanceof ClusterId id) {
            return id._value.equals(_value);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return _value;
    }

    @Override
    public String toString() {
        return String.valueOf(_value);
    }
}

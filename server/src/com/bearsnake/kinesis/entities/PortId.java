/*
 * kinesis
 * Copyright (c) 2020,2023 by Kurt Duncan - All Rights Reserved
 */

package com.bearsnake.kinesis.entities;

public class PortId {

    private final Integer _value;

    public PortId(
        final int id
    ) {
        _value = id;
    }

    @Override
    public boolean equals(
        final Object obj
    ) {
        if (obj instanceof PortId id) {
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

/*
 * kinesis
 * Copyright (c) 2020,2023 by Kurt Duncan - All Rights Reserved
 */

package com.bearsnake.kinesis.entities;

import java.util.Objects;

public class SectorId {
    private final ClusterId _clusterId;
    private final Integer _sectorNumber;

    public SectorId(
        final ClusterId clusterId,
        final Integer sectorNumber
    ) {
        _clusterId = clusterId;
        _sectorNumber = sectorNumber;
    }

    public ClusterId getClusterId() {
        return _clusterId;
    }

    public Integer getSectorNumber() {
        return _sectorNumber;
    }

    @Override
    public boolean equals(
        final Object obj
    ) {
        if (obj instanceof SectorId sid) {
            return (Objects.equals(_clusterId, sid._clusterId)) && (Objects.equals(_sectorNumber, sid._sectorNumber));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (_clusterId.hashCode() ^ _sectorNumber);
    }

    @Override
    public String toString() {
        return String.format("%s.%d", _clusterId, _sectorNumber);
    }
}

/*
 * kinesis
 * Copyright (c) 2020,2023 by Kurt Duncan - All Rights Reserved
 */

package com.bearsnake.kinesis.entities;

public class ProbeShip extends Ship {

    protected ProbeShip(
        final ShipId shipId,
        final Player owner,
        final Sector location,
        final float fuel
    ) {
        super(shipId, ShipType.PROBE, "", owner, location, fuel, 0, 0);
    }

    @Override
    public String getShipName() {
        return String.format("probe%s", getShipId());
    }

    public static ProbeShip createShip(
        final Player owner,
        final Sector location,
        final float fuelAmount
    ) {
        synchronized (Ship.class) {
            var shipId = getNextShipId();
            return new ProbeShip(shipId, owner, location, fuelAmount);
        }
    }
}

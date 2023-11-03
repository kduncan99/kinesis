/*
 * kinesis
 * Copyright (c) 2020,2023 by Kurt Duncan - All Rights Reserved
 */

package com.bearsnake.kinesis.entities;

public class DroneShip extends Ship {

    private static final int CARGO_HOLD_COUNT = 100;

    protected DroneShip(
        final ShipId shipId,
        final Player owner,
        final Sector location,
        final float fuel,
        final float shields
    ) {
        super(shipId, ShipType.CRUISER, "", owner, location, fuel, shields, CARGO_HOLD_COUNT);
    }

    @Override
    public String getShipName() {
        return String.format("drone%s", getShipId());
    }

    public static DroneShip createShip(
        final Player owner,
        final Sector location,
        final float fuelAmount,
        final float shieldsLevel
    ) {
        synchronized (Ship.class) {
            var shipId = getNextShipId();
            return new DroneShip(shipId, owner, location, fuelAmount, shieldsLevel);
        }
    }
}

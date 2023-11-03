/*
 * kinesis
 * Copyright (c) 2020,2023 by Kurt Duncan - All Rights Reserved
 */

package com.bearsnake.kinesis.entities;

public class FighterShip extends Ship {

    protected FighterShip(
        final ShipId shipId,
        final Player owner,
        final Sector location,
        final float fuel,
        final float shields
    ) {
        super(shipId, ShipType.FIGHTER, "", owner, location, fuel, shields, 0);
    }

    @Override
    public String getShipName() {
        return String.format("fighter%s", getShipId());
    }

    public static FighterShip createShip(
        final Player owner,
        final Sector location,
        final float fuelAmount,
        final float shieldsLevel
    ) {
        synchronized (Ship.class) {
            var shipId = getNextShipId();
            return new FighterShip(shipId, owner, location, fuelAmount, shieldsLevel);
        }
    }
}

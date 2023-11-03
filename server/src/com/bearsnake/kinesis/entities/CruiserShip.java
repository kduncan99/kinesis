/*
 * kinesis
 * Copyright (c) 2020,2023 by Kurt Duncan - All Rights Reserved
 */

package com.bearsnake.kinesis.entities;

public class CruiserShip extends Ship {

    protected CruiserShip(
        final ShipId shipId,
        final String shipName,
        final Player owner,
        final Sector location,
        final float fuel,
        final float shields,
        final int cargoHolds
    ) {
        super(shipId, ShipType.CRUISER, shipName, owner, location, fuel, shields, cargoHolds);
    }

    public static CruiserShip createShip(
        final String shipName,
        final Player owner,
        final Sector location,
        final float fuelAmount,
        final float shieldsLevel,
        final int cargoHoldCount
    ) {
        synchronized (Ship.class) {
            return new CruiserShip(getNextShipId(), shipName, owner, location, fuelAmount, shieldsLevel, cargoHoldCount);
        }
    }
}

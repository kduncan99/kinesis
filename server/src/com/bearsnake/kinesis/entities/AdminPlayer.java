/*
 * kinesis
 * Copyright (c) 2020,2023 by Kurt Duncan - All Rights Reserved
 */

package com.bearsnake.kinesis.entities;

public class AdminPlayer extends Player {

    protected AdminPlayer(
        final PlayerId identifier,
        final String username,
        final String password,
        final String gameName
    ) {
        super(identifier, PlayerType.ADMINISTRATOR, username, password, gameName);
    }

    public static AdminPlayer createPlayer(
        final String username,
        final String password,
        final String gameName
    ) {
        synchronized (Player.class) {
            return new AdminPlayer(getNextPlayerId(), username, password, gameName);
        }
    }
}

/*
 * kinesis
 * Copyright (c) 2020,2023 by Kurt Duncan - All Rights Reserved
 */

package com.bearsnake.kinesis.entities;

public class HumanPlayer extends Player {

    protected HumanPlayer(
        final PlayerId identifier,
        final String username,
        final String password,
        final String gameName
    ) {
        super(identifier, PlayerType.HUMAN, username, password, gameName);
    }

    public static HumanPlayer createPlayer(
        final String username,
        final String password,
        final String gameName
    ) {
        synchronized (Player.class) {
            return new HumanPlayer(getNextPlayerId(), username, password, gameName);
        }
    }
}

/*
 * kinesis
 * Copyright (c) 2020,2023 by Kurt Duncan - All Rights Reserved
 */

package com.bearsnake.kinesis.entities;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Player {

    private static final Logger LOGGER = LogManager.getLogger("Player");
    private static final Map<PlayerId, Player> _inventory = new HashMap<>();
    private static int _nextPlayerIdentifier = 1;

    private static final String CREATE_TABLE_SQL = "CREATE TABLE players ("
        + "  playerId integer PRIMARY KEY,"
        + "  username text NOT NULL,"
        + "  password text"
        + ") WITHOUT ROWID;";

    private static final String INSERT_SQL =
        "INSERT INTO players (playerId, username, password)"
            + " VALUES (%s, '%s', '%s');";

    private final PlayerId _identifier;
    private final String _username;
    private final String _password;

    private Player(
        final PlayerId identifier,
        final String username,
        final String password
    ) {
        _identifier = identifier;
        _username = username;
        _password = password;
    }

    public PlayerId getPlayerId() { return _identifier; }
    public String getUsername() { return _username; }
    public String getPassword() { return _password; }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append(_username).append(" (").append(_identifier).append(")");
        return sb.toString();
    }

    public static Player createPlayer(
        final String username,
        final String password
    ) {
        var plid = new PlayerId(_nextPlayerIdentifier++);
        while (_inventory.containsKey(plid)) {
            plid = plid.next();
        }

        var p = new Player(plid, username, password);
        _inventory.put(plid, p);
        return p;
    }

    public static Player getPlayer(
        final PlayerId plid
    ) {
        return _inventory.get(plid);
    }

    public static Player getPlayer(
        final String username
    ) {
        for (var p : _inventory.values()) {
            if (p.getUsername().equals(username)) {
                return p;
            }
        }
        return null;
    }

    public static void dbCreateTable(
        final Connection conn
    ) throws SQLException {
        LOGGER.trace(CREATE_TABLE_SQL);
        var statement = conn.createStatement();
        statement.execute(CREATE_TABLE_SQL);
    }

    public void dbPersist(
        final Connection conn
    ) throws SQLException {
        var sql = String.format(INSERT_SQL, _identifier, _username, _password);
        var statement = conn.createStatement();
        statement.execute(sql);
    }
}

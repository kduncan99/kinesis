/*
 * kinesis
 * Copyright (c) 2020,2023 by Kurt Duncan - All Rights Reserved
 */

package com.bearsnake.kinesis.entities;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Player {

    public enum PlayerType {
        ADMINISTRATOR("A"),
        HUMAN("H");

        public final String _code;

        PlayerType(
            final String code
        ) {
            _code = code;
        }

        static PlayerType getPlayerType(
            final String code
        ) {
            return Arrays.stream(PlayerType.values()).filter(pt -> pt._code.equals(code)).findFirst().orElse(null);
        }
    }

    private static final Logger LOGGER = LogManager.getLogger("Player");
    private static final Map<PlayerId, Player> _inventory = new HashMap<>();
    private static int _nextPlayerIdentifier = 1;

    private static final String CREATE_TABLE_SQL = "CREATE TABLE players ("
        + "  playerId integer PRIMARY KEY,"
        + "  playerType string NOT NULL,"
        + "  username text NOT NULL,"
        + "  password text NOT NULL,"
        + "  gamename text NOT NULL"
        + ") WITHOUT ROWID;";

    private static final String INSERT_SQL =
        "INSERT INTO players (playerId, playerType, username, password, gamename)"
            + " VALUES (%s, \"%s\", \"%s\", \"%s\", \"%s\");";

    private final PlayerId _playerId;
    private final PlayerType _playerType;
    private final String _username;
    private final String _password;
    private String _gameName;

    protected Player(
        final PlayerId playerId,
        final PlayerType playerType,
        final String username,
        final String password,
        final String gameName
    ) {
        _playerId = playerId;
        _playerType = playerType;
        _username = username;
        _password = password;
        _gameName = gameName;

        _inventory.put(playerId, this);
    }

    public static PlayerId getNextPlayerId() {
        synchronized (Player.class) {
            var result = new PlayerId(_nextPlayerIdentifier);
            _nextPlayerIdentifier++;
            return result;
        }
    }

    public String getGameName() { return _gameName; }
    public PlayerId getPlayerId() { return _playerId; }
    public PlayerType getPlayerType() { return _playerType; }
    public String getUsername() { return _username; }
    public String getPassword() { return _password; }
    public boolean isAdministrator() { return _playerType == PlayerType.ADMINISTRATOR; }
    public void setGameName(final String value) { _gameName = _gameName; }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append(_username).append(" (").append(_playerId).append(")");
        return sb.toString();
    }

    public static Player getPlayer(
        final PlayerId plid
    ) {
        return _inventory.get(plid);
    }

    public static Player getPlayerByGameName(
        final String gameName
    ) {
        synchronized (Player.class) {
            for (var p : _inventory.values()) {
                if (p.getGameName().equals(gameName)) {
                    return p;
                }
            }
            return null;
        }
    }

    public static Player getPlayerByUserName(
        final String username
    ) {
        synchronized (Player.class) {
            for (var p : _inventory.values()) {
                if (p.getUsername().equals(username)) {
                    return p;
                }
            }
            return null;
        }
    }

    public static void dbCreateTable(
        final Connection conn
    ) throws SQLException {
        LOGGER.trace(CREATE_TABLE_SQL);
        var statement = conn.createStatement();
        statement.execute(CREATE_TABLE_SQL);
    }

    public static void dbLoad(
        final Connection conn
    ) throws SQLException {
        var sql = "SELECT * FROM players ORDER BY playerId;";
        var statement = conn.createStatement();
        var rs = statement.executeQuery(sql);

        while (rs.next()) {
            var playerId = new PlayerId(rs.getLong("playerId"));
            var playerType = rs.getString("playerType");
            var username = rs.getString("username");
            var password = rs.getString("password");
            var gameName = rs.getString("gameName");

            switch (PlayerType.getPlayerType(playerType)) {
                case ADMINISTRATOR -> new AdminPlayer(playerId, username, password, gameName);
                case HUMAN -> new HumanPlayer(playerId, username, password, gameName);
            }
        }

        var msg = String.format("Loaded %d player(s)...", _inventory.size());
        System.out.println(msg);
        LOGGER.info(msg);
    }

    public void dbPersist(
        final Connection conn
    ) throws SQLException {
        var sql = String.format(INSERT_SQL, _playerId, _playerType._code, _username, _password, _gameName);
        var statement = conn.createStatement();
        statement.execute(sql);
    }

    public static class PlayerId {

        private final long _value;

        public PlayerId(
            final long id
        ) {
            _value = id;
        }

        @Override
        public boolean equals(
            final Object obj
        ) {
            if (obj instanceof PlayerId id) {
                return id._value == _value;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return (int)_value;
        }

        @Override
        public String toString() {
            return String.valueOf(_value);
        }
    }
}

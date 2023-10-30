/*
 * kinesis
 * Copyright (c) 2020,2023 by Kurt Duncan - All Rights Reserved
 */

package com.bearsnake.kinesis.persistence;

import com.bearsnake.kinesis.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteDatabase implements Database {

    private Connection _connection = null;
    private final String _path;

    private SQLiteDatabase(
        final String path
    ) {
        _path = path;
    }

    public static SQLiteDatabase createDatabase(
        final String path
    ) {
        return new SQLiteDatabase(path);
    }

    @Override
    public void close() throws DatabaseException {
        if (_connection == null) {
            throw new DatabaseException("Database is not open");
        }

        try {
            _connection.close();
        } catch (SQLException ex) {
            throw new DatabaseException("Failed to close database:" + ex);
        } finally {
            _connection = null;
        }
    }

    @Override
    public void create() throws DatabaseException {
        if (_connection != null) {
            throw new DatabaseException("Database is already open");
        }

        var url = "jdbc:sqlite:" + _path;
        try {
            _connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void open() throws DatabaseException {
        if (_connection != null) {
            throw new DatabaseException("Database is already open");
        }

        var url = "jdbc:sqlite:" + _path;
        try {
            _connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

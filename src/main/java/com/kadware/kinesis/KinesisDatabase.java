/*
 * kinesis
 * Copyright (c) 2020 by Kurt Duncan - All Rights Reserved
 */

package com.kadware.kinesis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class KinesisDatabase {

    public static final String SCHEMA_NAME = "Kinesis";
    public static final Logger _logger = LogManager.getLogger("KinesisDatabase");

    private static final String TABLE_NAME_CLUSTERS = SCHEMA_NAME + ".Clusters";
    private static final String TABLE_NAME_SECTORS = SCHEMA_NAME + ".Sectors";
    private static final String TABLE_NAME_SECTOR_LINKS = SCHEMA_NAME + ".SectorLinks";

    private static final String CREATE_CLUSTERS =
        "CREATE TABLE " + TABLE_NAME_CLUSTERS + " (" +
            "id SERIAL," +
            "clusterName TEXT UNIQUE NOT NULL," +
            "PRIMARY KEY (id))";

    private static final String CREATE_SECTORS =
        "CREATE TABLE " + TABLE_NAME_SECTORS + " (" +
            "id SERIAL," +
            "clusterId INTEGER NOT NULL," +
            "PRIMARY KEY (id)," +
            "FOREIGN KEY (clusterId) REFERENCES " + TABLE_NAME_CLUSTERS + " (id))";

    private static final String CREATE_SECTOR_LINKS =
        "CREATE TABLE " + TABLE_NAME_SECTOR_LINKS + " (" +
            "sourceSectorId INTEGER NOT NULL," +
            "destinationSectorId INTEGER NOT NULL," +
            "PRIMARY KEY (sourceSectorId, destinationSectorId)," +
            "FOREIGN KEY (sourceSectorId) REFERENCES " + TABLE_NAME_SECTORS + " (id)," +
            "FOREIGN KEY (destinationSectorId) REFERENCES " + TABLE_NAME_SECTORS + " (id))";

    private static final String[] _createStrings = {
        CREATE_CLUSTERS,
        CREATE_SECTORS,
        CREATE_SECTOR_LINKS
    };

    private final String _hostName;
    private final int _port;
    private final String _databaseName;
    private final String _username;
    private final String _password;
    private final String _connectionString;

    public KinesisDatabase(
        final String hostName,
        final int port,
        final String databaseName,
        final String username,
        final String password
    ) {
        _hostName = hostName;
        _port = port;
        _databaseName = databaseName;
        _username = username;
        _password = password;

        _connectionString = String.format("jdbc:postgresql://%s:%d/%s", _hostName, _port, _databaseName);
    }

    public void clear(
    ) throws SQLException {
        try (Connection cxn = DriverManager.getConnection(_connectionString, _username, _password);
             Statement statement = cxn.createStatement()) {
            statement.executeUpdate("DROP SCHEMA IF EXISTS " + SCHEMA_NAME + " CASCADE");
        } catch (SQLException e) {
            _logger.catching(e);
            throw e;
        }
    }

    public void initialize(
    ) throws SQLException {
        try (Connection cxn = DriverManager.getConnection(_connectionString, _username, _password);
             Statement statement = cxn.createStatement()) {
            statement.executeUpdate(String.format("CREATE SCHEMA %s", SCHEMA_NAME));
            for (String sql : _createStrings) {
                _logger.error(sql);//TODO make it a trace
                statement.executeUpdate(sql);
            }
        } catch (SQLException e) {
            _logger.catching(e);
            throw e;
        }
    }
}

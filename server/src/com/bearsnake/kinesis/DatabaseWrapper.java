/*
 * kinesis
 * Copyright (c) 2020,2023 by Kurt Duncan - All Rights Reserved
 */

package com.bearsnake.kinesis;

import com.bearsnake.kinesis.entities.Cluster;
import com.bearsnake.kinesis.entities.Planet;
import com.bearsnake.kinesis.entities.Player;
import com.bearsnake.kinesis.entities.Port;
import com.bearsnake.kinesis.entities.Sector;
import com.bearsnake.kinesis.entities.Ship;
import com.bearsnake.kinesis.exceptions.DatabaseException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DatabaseWrapper {

    private static final Logger LOGGER = LogManager.getLogger("DatabaseWrapper");

    private final String _path;
    private final String _url;

    public DatabaseWrapper(
        final String path
    ) {
        _path = path;
        _url = "jdbc:sqlite:" + _path;
    }

    public synchronized Connection createConnection() throws DatabaseException {
        LOGGER.trace("createConnection");
        try {
            return DriverManager.getConnection(_url);
        } catch (SQLException ex) {
            LOGGER.catching(ex);
            throw new DatabaseException(ex.getMessage());
        }
    }

    public void createDatabase() throws DatabaseException {
        LOGGER.trace("createDatabase()");
        if (Files.exists(Path.of(_path))) {
            var ex = new DatabaseException("Database already exists");
            LOGGER.throwing(ex);
            throw ex;
        }

        try {
            var conn = DriverManager.getConnection(_url);
            conn.close();
        } catch (SQLException ex) {
            LOGGER.catching(ex);
            throw new DatabaseException(ex.getMessage());
        }
    }

    public void createTables() throws DatabaseException {
        LOGGER.trace("createTables()");
        try {
            var conn = createConnection();
            conn.setAutoCommit(false);
            conn.beginRequest();

            Player.dbCreateTable(conn);
            Ship.dbCreateTable(conn);
            Cluster.dbCreateTable(conn);
            Sector.dbCreateTables(conn);
            Port.dbCreateTable(conn);
            Planet.dbCreateTable(conn);

            conn.commit();
            conn.close();
        } catch (SQLException ex) {
            LOGGER.catching(ex);
            throw new DatabaseException(ex.getMessage());
        }
    }

    public void deleteDatabase() throws DatabaseException {
        LOGGER.trace("deleteDatabase()");
        try {
            Files.deleteIfExists(Path.of(_path));
        } catch (IOException ex) {
            LOGGER.catching(ex);
            throw new DatabaseException(ex.getMessage());
        }
    }

    public void loadFromDatabase() throws DatabaseException {
        LOGGER.trace("loadFromDatabase()");
        try {
            var conn = createConnection();
            Player.dbLoad(conn);
            Cluster.dbLoad(conn);
            Sector.dbLoad(conn);
            Ship.dbLoad(conn);
            Planet.dbLoad(conn);
            Port.dbLoad(conn);
            conn.close();
        } catch (SQLException ex) {
            LOGGER.catching(ex);
            throw new DatabaseException(ex.getMessage());
        }
    }
}

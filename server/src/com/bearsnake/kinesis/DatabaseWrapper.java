/*
 * kinesis
 * Copyright (c) 2020,2023 by Kurt Duncan - All Rights Reserved
 */

package com.bearsnake.kinesis;

import com.bearsnake.kinesis.entities.Cluster;
import com.bearsnake.kinesis.entities.Port;
import com.bearsnake.kinesis.entities.Sector;
import com.bearsnake.kinesis.exceptions.DatabaseException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DatabaseWrapper {

    private static final Logger LOGGER = LogManager.getLogger("DatabaseWrapper");

    private final String _path;
    private final String _url;
    protected final Collection<Connection> _connections = new LinkedList<>();

    public DatabaseWrapper(
        final String path
    ) {
        _path = path;
        _url = "jdbc:sqlite:" + _path;
    }

    public synchronized void closeConnections() throws DatabaseException {
        LOGGER.trace("closeConnections");
        var iter = _connections.iterator();
        while (iter.hasNext()) {
            var conn = iter.next();
            try {
                conn.close();
            } catch (SQLException ex) {
                LOGGER.catching(ex);
            }
            iter.remove();
        }
    }

    public synchronized Connection createConnection() throws DatabaseException {
        LOGGER.trace("createConnection");
        try {
            var conn = DriverManager.getConnection(_url);
            conn.setAutoCommit(false);
            _connections.add(conn);
            return conn;
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

            Cluster.dbCreateTable(conn);
            Sector.dbCreateTables(conn);
            Port.dbCreateTable(conn);

            conn.commit();
            deleteConnection(conn);
        } catch (SQLException ex) {
            LOGGER.catching(ex);
            throw new DatabaseException(ex.getMessage());
        }
    }

    public synchronized void deleteConnection(
        final Connection conn
    ) throws DatabaseException {
        LOGGER.trace("deleteConnection()");
        if (!_connections.contains(conn)) {
            var ex = new DatabaseException("Connection is not registered with database wrapper");
            LOGGER.throwing(ex);
            throw ex;
        }

        _connections.remove(conn);
        try {
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
}

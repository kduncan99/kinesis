/*
 * kinesis
 * Copyright (c) 2020,2023 by Kurt Duncan - All Rights Reserved
 */

package com.bearsnake.kinesis;

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

    private final static String CREATE_CLUSTER_TABLE =
        "CREATE TABLE clusters ("
            + "  clusterId integer PRIMARY KEY,"
            + "  clusterName text NOT NULL"
            + ") WITHOUT ROWID;";

    private final static String CREATE_SECTOR_TABLE =
        "CREATE TABLE sectors ("
            + "  clusterId integer NOT NULL,"
            + "  sectorNumber integer NOT NULL,"
            + "  FOREIGN KEY (clusterId) REFERENCES clusters(clusterId),"
            + "  PRIMARY KEY (clusterId, sectorNumber)"
            + ") WITHOUT ROWID;";

    private final static String CREATE_SECTOR_LINK_TABLE =
        "CREATE TABLE sectorLinks ("
            + "  fromClusterId integer NOT NULL,"
            + "  fromSectorNumber integer NOT NULL,"
            + "  toClusterId integer NOT NULL,"
            + "  toSectorNumber integer NOT NULL,"
            + "  FOREIGN KEY (fromClusterId, fromSectorNumber) REFERENCES sectors(clusterId, sectorNumber),"
            + "  FOREIGN KEY (toClusterId, toSectorNumber) REFERENCES sectors(clusterId, sectorNumber),"
            + "  PRIMARY KEY (fromClusterId, fromSectorNumber, toClusterId, toSectorNumber)"
            + ") WITHOUT ROWID;";

    private final static String CREATE_PORT_TABLE =
        "CREATE TABLE ports ("
            + "  portId integer PRIMARY KEY,"
            + "  portName text NOT NULL,"
            + "  clusterId integer NOT NULL,"
            + "  sectorNumber integer NOT NULL,"
            + "  FOREIGN KEY (clusterId, sectorNumber) REFERENCES sectors(clusterId, sectorNumber)"
            + ") WITHOUT ROWID;";

    private final static String[] CREATE_STATEMENTS = {
        CREATE_CLUSTER_TABLE,
        CREATE_SECTOR_TABLE,
        CREATE_SECTOR_LINK_TABLE,
        CREATE_PORT_TABLE,
    };

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
            for (var sql : CREATE_STATEMENTS) {
                LOGGER.trace(sql);
                var statement = conn.createStatement();
                statement.execute(sql);
            }
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

/*
 * kinesis
 * Copyright (c) 2020,2023 by Kurt Duncan - All Rights Reserved
 */

package com.bearsnake.kinesis;

import com.bearsnake.kinesis.entities.Cluster;
import com.bearsnake.kinesis.entities.Player;
import com.bearsnake.kinesis.exceptions.DatabaseException;
import com.bearsnake.kinesis.exceptions.KinesisException;
import com.bearsnake.komando.ArgumentSwitch;
import com.bearsnake.komando.CommandLineHandler;
import com.bearsnake.komando.Switch;
import com.bearsnake.komando.exceptions.KomandoException;
import com.bearsnake.komando.values.StringValue;
import com.bearsnake.komando.values.ValueType;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.bearsnake.kinesis.Kinesis.KINESIS_VERSION;

public class Initializer {

    private static final Logger LOGGER = LogManager.getLogger("Initializer");
    private static final CommandLineHandler _commandLineHandler;
    private static final Switch _databaseFileSwitch;

    static {
        try {
            _databaseFileSwitch = new ArgumentSwitch.Builder().setShortName("db")
                                                              .setLongName("database")
                                                              .setValueType(ValueType.STRING)
                                                              .setValueName("fileName")
                                                              .setIsRequired(true)
                                                              .setIsMultiple(false)
                                                              .addDescription("Path and filename of the kinesis database")
                                                              .build();

            _commandLineHandler = new CommandLineHandler();
            _commandLineHandler.addCanonicalHelpSwitch()
                               .addCanonicalVersionSwitch()
                               .addSwitch(_databaseFileSwitch);
        } catch (KomandoException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(
        final String[] args
    ) {
        System.out.println("Kinesis Initializer");
        var result = _commandLineHandler.processCommandLine(args);

        for (var msg : result._messages) {
            System.err.println(msg);
        }

        if (result.hasWarnings() || result.hasErrors()) {
            System.exit(-1);
        }

        if (result.isHelpRequested()) {
            _commandLineHandler.displayUsage("initializer");
        } else if (result.isVersionRequested()) {
            System.out.printf("Version %s\n", KINESIS_VERSION);
        } else {
            var specs = result._switchSpecifications.get(_databaseFileSwitch);
            var value = (StringValue) specs.get(0);
            var init = new Initializer(value.getValue());
            try {
                init.process();
            } catch (KinesisException ex) {
                System.err.println("ERROR:" + ex);
            }
        }
    }

    private final DatabaseWrapper _databaseWrapper;
    private final String _dbPath;

    private Initializer(
        final String dbPath
    ) {
        _dbPath = dbPath;
        _databaseWrapper = new DatabaseWrapper(dbPath);
    }

    private void process() throws KinesisException {
        _databaseWrapper.deleteDatabase();
        _databaseWrapper.createDatabase();
        _databaseWrapper.createTables();

        // Initialize admin player
        try {
            var conn = _databaseWrapper.createConnection();
            conn.setAutoCommit(true);
            var admin = Player.createPlayer("admin", "admin");
            admin.dbPersist(conn);
            conn.close();
            System.out.println("Created admin");
        } catch (SQLException ex) {
            throw new DatabaseException(ex.getMessage());
        }

        // Initialize initial cluster
        var cluster = Cluster.createStandardCluster(_databaseWrapper, "Sanctuary", 100, 10);
        System.out.printf("Created %s cluster\n", cluster.getClusterName());
    }
}

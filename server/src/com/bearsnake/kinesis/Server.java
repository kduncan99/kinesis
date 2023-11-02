/*
 * kinesis
 * Copyright (c) 2020,2023 by Kurt Duncan - All Rights Reserved
 */

package com.bearsnake.kinesis;

import com.bearsnake.kinesis.exceptions.KinesisException;
import com.bearsnake.komando.ArgumentSwitch;
import com.bearsnake.komando.CommandLineHandler;
import com.bearsnake.komando.SimpleSwitch;
import com.bearsnake.komando.Switch;
import com.bearsnake.komando.exceptions.KomandoException;
import com.bearsnake.komando.restrictions.RangeRestriction;
import com.bearsnake.komando.values.FixedPointValue;
import com.bearsnake.komando.values.StringValue;
import com.bearsnake.komando.values.ValueType;

import static com.bearsnake.kinesis.Kinesis.KINESIS_VERSION;

public class Server {

    private static final CommandLineHandler _commandLineHandler;
    private static final Switch _databaseFileSwitch;
    private static final Switch _portNumberSwitch;

    static {
        try {
            var portRestriction = new RangeRestriction(new FixedPointValue(1L), new FixedPointValue(32767L));

            _databaseFileSwitch = new ArgumentSwitch.Builder().setShortName("db")
                                                              .setLongName("database")
                                                              .setValueType(ValueType.STRING)
                                                              .setValueName("fileName")
                                                              .addDescription("Path and filename of the kinesis database")
                                                              .build();
            _portNumberSwitch = new ArgumentSwitch.Builder().setShortName("p")
                                                            .setLongName("portNumber")
                                                            .setValueType(ValueType.FIXED_POINT)
                                                            .setValueName("portNumber")
                                                            .setRestriction(portRestriction)
                                                            .addDescription("Port number for kinesis server")
                                                            .build();

            _commandLineHandler = new CommandLineHandler();
            _commandLineHandler.addCanonicalHelpSwitch()
                               .addCanonicalVersionSwitch()
                               .addSwitch(_databaseFileSwitch)
                               .addSwitch(_portNumberSwitch);
        } catch (KomandoException e) {
            throw new RuntimeException(e);
        }
    }

    private final DatabaseWrapper _databaseWrapper;
    private final int _port;

    public static void main(
        final String[] args
    ) {
        System.out.println("Kinesis Server");
        var result = _commandLineHandler.processCommandLine(args);

        for (var msg : result._messages) {
            System.err.println(msg);
        }

        if (result.hasWarnings() || result.hasErrors()) {
            System.exit(-1);
        }

        if (result.isHelpRequested()) {
            _commandLineHandler.displayUsage("server");
        } else if (result.isVersionRequested()) {
            System.out.printf("Version %s\n", KINESIS_VERSION);
        } else {
            var dbPath = ((StringValue)(result._switchSpecifications.get(_databaseFileSwitch).get(0))).getValue();
            var port = ((FixedPointValue)(result._switchSpecifications.get(_portNumberSwitch).get(0))).getValue();

            var server = new Server(dbPath, (int)(long)port);
            try {
                server.process();
            } catch (KinesisException ex) {
                System.err.println("ERROR:" + ex);
            }
        }
    }

    private Server(
        final String dbPath,
        final int port
    ) {
        _databaseWrapper = new DatabaseWrapper(dbPath);
        _port = port;
    }

    private void process() throws KinesisException {
        _databaseWrapper.loadFromDatabase();
    }
}

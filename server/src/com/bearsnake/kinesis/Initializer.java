/*
 * kinesis
 * Copyright (c) 2020,2023 by Kurt Duncan - All Rights Reserved
 */

package com.bearsnake.kinesis;

import com.bearsnake.komando.ArgumentSwitch;
import com.bearsnake.komando.CommandLineHandler;
import com.bearsnake.komando.Switch;
import com.bearsnake.komando.exceptions.KomandoException;
import com.bearsnake.komando.values.ValueType;

import static com.bearsnake.kinesis.Kinesis.KINESIS_VERSION;

public class Initializer {

    private static CommandLineHandler _commandLineHandler;
    private static Switch _databaseFileSwitch;

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
            //TODO
        }
    }
}

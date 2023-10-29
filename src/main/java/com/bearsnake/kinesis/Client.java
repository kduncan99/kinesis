package com.bearsnake.kinesis;

import com.bearsnake.komando.ArgumentSwitch;
import com.bearsnake.komando.CommandLineHandler;
import com.bearsnake.komando.SimpleSwitch;
import com.bearsnake.komando.Switch;
import com.bearsnake.komando.exceptions.KomandoException;
import com.bearsnake.komando.restrictions.RangeRestriction;
import com.bearsnake.komando.values.FixedPointValue;
import com.bearsnake.komando.values.ValueType;

public class Client {

    private static CommandLineHandler _commandLineHandler;
    private static Switch _databaseFileSwitch;
    private static Switch _initSwitch;
    private static Switch _portNumberSwitch;
    private static Switch _runSwitch;

    static {
        try {
            var portRestriction = new RangeRestriction(new FixedPointValue(1L), new FixedPointValue(32767L));

            _initSwitch = new SimpleSwitch.Builder().setShortName("i")
                                                    .setLongName("initialize")
                                                    .addDescription("Initializes the database according to defaults and/or other switches")
                                                    .build();
            _runSwitch = new SimpleSwitch.Builder().setShortName("r")
                                                   .setLongName("run")
                                                   .addDescription("Runs the program against the current database")
                                                   .build();

            _databaseFileSwitch = new ArgumentSwitch.Builder().setShortName("db")
                                                              .setLongName("database")
                                                              .setValueType(ValueType.STRING)
                                                              .setValueName("fileName")
                                                              .addDescription("Path and filename of the kinesis database")
                                                              .build();
            _portNumberSwitch = new ArgumentSwitch.Builder().setShortName("p")
                                                            .setLongName("portNumber")
                                                            .setValueType(ValueType.FIXED_POINT)
                                                            .setRestriction(portRestriction)
                                                            .build();

            _commandLineHandler = new CommandLineHandler();
            _commandLineHandler.addCanonicalHelpSwitch()
                               .addCanonicalVersionSwitch()
                               .addSwitch(_databaseFileSwitch)
                               .addSwitch(_initSwitch)
                               .addSwitch(_portNumberSwitch)
                               .addDependency(_initSwitch, _databaseFileSwitch)
                               .addDependency(_runSwitch, _databaseFileSwitch)
                               .addDependency(_runSwitch, _portNumberSwitch)
                               .addMutualExclusion(_initSwitch, _runSwitch);
        } catch (KomandoException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(
        final String[] args
    ) {
    }
}

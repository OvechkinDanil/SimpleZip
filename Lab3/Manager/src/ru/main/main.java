package ru.main;

import java.io.*;
import ru.spbstu.pipeline.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class main {

    public static void main(String[] argv) {
        RC error_code;
        Logger logger = Logger.getLogger("Pipeline_log");
        if (argv.length != 1 || argv[0] == null) {
            logger.log(Level.SEVERE, Log.LoggerItems.CODE_INVALID_ARGUMENT.getTitle());
            return;
        }

        Manager manager = new Manager(logger);
        if ((error_code = manager.setConfig(argv[0])) != RC.CODE_SUCCESS) {
            return;
        }

        if ((error_code = manager.makePipeLine()) != RC.CODE_SUCCESS) {
            return;
        }

        if ((error_code = manager.execute()) != RC.CODE_SUCCESS) {
            return;
        }
    }
}

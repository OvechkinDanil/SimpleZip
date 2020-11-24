import sun.rmi.runtime.Log;

import java.io.File;

public class Manager {
    private LogCreator log;
    private File file;


    Manager(String cfgFile, String logFile) {
        file = new File(cfgFile);
        log = new LogCreator(logFile);
    }

    private boolean IntToBool(ConfigParcer cfg)
    {
        try
        {
            int isCompressed = Integer.parseInt(cfg.GetConfigItemInf(ConfigParcer.ConfigItems.ISCOMPRESSED));
            if (isCompressed == 1)
                return true;
            else if (isCompressed == 0)
                return false;
            else
            {
                log.writeToLog(LogCreator.LogItems.ERROR_CONFIG_ITEM);
                return false;
             }

        }
        catch (NumberFormatException e)
        {
            log.writeToLog(LogCreator.LogItems.ERROR_INTEGER);
            return false;
        }
    }

    public void run()
    {

        ConfigParcer cfg = new ConfigParcer(file, log);
        boolean isCompressed = IntToBool(cfg);

        if (LogCreator.numErrors != 0)
            return;

        if (isCompressed)
        {
            CompressionBlock compressor = new CompressionBlock(cfg, log);
            compressor.compress();
        }
        else if (!isCompressed)
        {
            Decompression decompressor = new Decompression(cfg, log);
            decompressor.deCompress();
        }
    }
}

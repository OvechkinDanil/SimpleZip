import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.File;

public class Manager {
    private File file;
    private Logger logger;


    Manager(String cfgFile, Logger logger) {
        file = new File(cfgFile);
        this.logger = logger;
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
                logger.log(Level.SEVERE, Log.LoggerItems.CODE_CONFIG_SEMANTIC_ERROR.getTitle());
                return false;
             }

        }
        catch (NumberFormatException e)
        {
            logger.log(Level.SEVERE, Log.LoggerItems.CODE_CONFIG_SEMANTIC_ERROR.getTitle());
            return false;
        }
    }

    public void run()
    {

        ConfigParcer cfg = new ConfigParcer(file, logger);
        boolean isCompressed = IntToBool(cfg);


        if (isCompressed)
        {
            CompressionBlock compressor = new CompressionBlock(cfg, logger);
            compressor.compress();
        }
        else if (!isCompressed)
        {
            Decompression decompressor = new Decompression(cfg, logger);
            decompressor.deCompress();
        }
    }
}

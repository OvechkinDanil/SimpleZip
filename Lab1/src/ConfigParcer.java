import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigParcer {
    final String Separator = ":";
    private String line;
    Logger logger;

    public enum ConfigItems
    {
        INPUT("input"),
        OUTPUT("output"),
        LOGGER("logger"),
        ISCOMPRESSED("compress"),
        BUFSIZE("bufsize");

        private String title;

        ConfigItems(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }

    Map<ConfigItems, String> dic = new HashMap<>();

    public String GetConfigItemInf(ConfigItems item)
    {
        return dic.get(item);
    }

    private boolean ParseLine()
    {
        String[] tokens = line.split(Separator);
        boolean isFind = false;
        for (ConfigItems item : ConfigItems.values())
        {
            if (item.getTitle().equals(tokens[0]))
            {
                dic.put(item, tokens[1]);
                return true;
            }
        }
        return false;
    }

    ConfigParcer(File file, Logger logger)
    {
        this.logger = logger;
        try {
            Scanner sc = new Scanner(file);
            while (sc.hasNextLine()) {
                line = sc.nextLine();
                if (!ParseLine())
                    logger.log(Level.SEVERE, Log.LoggerItems.CODE_CONFIG_GRAMMAR_ERROR.getTitle());
            }
        }
        catch(FileNotFoundException e)
        {
            logger.log(Level.SEVERE, Log.LoggerItems.CODE_FAILED_TO_READ.getTitle());
        }
    }
}

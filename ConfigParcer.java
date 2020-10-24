import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Map;

public class ConfigParcer {
    final String Separator = ":";
    String line;
    public enum ConfigItems
    {
        INPUT("input"),
        OUTPUT("output"),
        DECOMPRESSED("decompressed"),
        LOGGER("logger"),
        TYPE("type"),
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

    private void ParseLine(LogCreator log)
    {
        String[] tokens = line.split(Separator);
        boolean isFind = false;
        for (ConfigItems item : ConfigItems.values())
        {
            if (item.getTitle().equals(tokens[0]))
            {
                dic.put(item, tokens[1]);
                isFind = true;
                break;
            }
        }
        if (!isFind)
        {
            log.writeToLog(LogCreator.LogItems.ERROR_WITH_TOKEN_IN_CONFIG);
        }
    }

    ConfigParcer(File file, LogCreator log)
    {
        try {
            Scanner sc = new Scanner(file);
            while (sc.hasNextLine()) {
                line = sc.nextLine();
                ParseLine(log);
            }
        }
        catch(FileNotFoundException e)
        {
            log.writeToLog(LogCreator.LogItems.ERROR_OPEN_FILE);
        }
    }
}

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogCreator {
    public Logger logger;
    public LogItems logItems;
    public static int numErrors;

    public enum LogItems
    {
        ERROR_OPEN_FILE("File cannot be opened"),
        ERROR_WITH_TOKEN_IN_CONFIG("Left token in config cannot be found in ConfigItems"),
        ERROR_DICTIONARY_INDEX("Cannot find element in dictionary"),
        ERROR_CONFIG_ITEM("Problem with config item"),
        ERROR_INTEGER("Integer can't be parsed");

        private String title;

        LogItems(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }

    LogCreator(String log)
    {
        logger = Logger.getLogger("MyLog");
        FileHandler fh;
        try
        {
            fh = new FileHandler(log);
            logger.addHandler(fh);
            logger.setUseParentHandlers(false);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void writeToLog(LogItems item)
    {
        logger.info(item.getTitle());
        numErrors++;
    }
}

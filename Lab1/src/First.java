import java.util.logging.Level;
import java.util.logging.Logger;


public class First
{
    public static void main(String[] argv)
    {
        Logger logger = Logger.getLogger("Lab1_log");
        if (argv.length != 1 || argv[0] == null)
        {
            logger.log(Level.SEVERE, Log.LoggerItems.CODE_INVALID_ARGUMENT.getTitle());
            return;
        }

        Manager manager = new Manager(argv[0], logger);
        manager.run();
    }
}

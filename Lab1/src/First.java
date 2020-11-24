import java.io.*;
import java.util.logging.*;


public class First
{

    public static void main(String[] argv)
    {
        if (argv.length == 0 || argv[0] == null)
        {
            System.err.println("No argument or it is null");
            return;
        }
        Manager manager = new Manager(argv[0], argv[1]);
        manager.run();

    }
}

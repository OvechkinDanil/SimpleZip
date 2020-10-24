import java.io.*;
import java.util.logging.*;


public class First
{

    public static void main(String[] argv) throws FileNotFoundException
    {
        if (argv.length == 0 || argv[0] == null)
        {
            System.err.println("No argument or it is null");
            return;
        }
        File file = new File(argv[0]);
        LogCreator log = new LogCreator(argv[1]);

        ConfigParcer cfg = new ConfigParcer(file, log);


        CompressionBlock compressor = new CompressionBlock(cfg, log);
        compressor.compress();

        Decompression decompressor = new Decompression(cfg, log);
        decompressor.deCompress();
    }
}

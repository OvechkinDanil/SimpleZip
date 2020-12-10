import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.logging.Level;

public class Decompression {
    private final String compressedFile;
    private final String output;
    private boolean isNotFirstBlock = false;
    private final ArrayList<Short> dictionary;
    private final ArrayList<Byte> compressedBuffer;
    private Logger logger;

    Decompression(ConfigParcer cfg, Logger logger)
    {
        compressedFile = cfg.GetConfigItemInf(ConfigParcer.ConfigItems.INPUT);
        output = cfg.GetConfigItemInf(ConfigParcer.ConfigItems.OUTPUT);
        dictionary = new ArrayList<>();
        compressedBuffer = new ArrayList<>();
        this.logger = logger;
    }


    public void deCompress()
    {
        int dictSize = 0, bufSize = 0, i = 0;
        byte first, second;
        try(FileOutputStream fileOutputStream = new FileOutputStream(output)) {
            try (FileInputStream fileInputStream = new FileInputStream(compressedFile)) {

                while ((dictSize = fileInputStream.read()) != -1) {
                    for (i = 0; i < dictSize; i++) {
                        first = (byte) fileInputStream.read();
                        second = (byte) fileInputStream.read();
                        dictionary.add(bytesToShort(first, second));
                    }
                    bufSize = fileInputStream.read();
                    for (i = 0; i < bufSize; i++) {
                        byte a = (byte)fileInputStream.read();
                        compressedBuffer.add(a);
                    }
                    OutputData(fileOutputStream);
                    dictionary.clear();
                    compressedBuffer.clear();
                }
            }
        }
        catch (Exception e)
        {
            logger.log(Level.SEVERE, Log.LoggerItems.CODE_FAILED_TO_READ.getTitle());
        }
    }

    private void OutputData(FileOutputStream fileOutputStream)
    {
        byte[] byteBuf;
        try
        {
            for (byte elem : compressedBuffer)
            {
                byteBuf = shortToBytes(dictionary.get(elem));
                fileOutputStream.write(byteBuf[0]);
                fileOutputStream.write(byteBuf[1]);
            }
        }
        catch (IOException e)
        {
            logger.log(Level.SEVERE, Log.LoggerItems.CODE_FAILED_TO_WRITE.getTitle());
        }
    }

    private short bytesToShort(byte first, byte second)
    {
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(first);
        bb.put(second);
        return bb.getShort(0);
    }

    private byte[] shortToBytes(short value)
    {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.putShort(value);
        return buffer.array();
    }
}

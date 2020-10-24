import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class Decompression {
    private final String compressedFile;
    private final String output;
    private boolean isNotFirstBlock = false;
    private final ArrayList<Short> dictionary;
    private final ArrayList<Byte> compressedBuffer;
    private LogCreator log;
    Decompression(ConfigParcer cfg, LogCreator l)
    {
        compressedFile = cfg.GetConfigItemInf(ConfigParcer.ConfigItems.OUTPUT);
        output = cfg.GetConfigItemInf(ConfigParcer.ConfigItems.DECOMPRESSED);
        dictionary = new ArrayList<>();
        compressedBuffer = new ArrayList<>();
        log = l;
    }

    public void deCompress()
    {
        int dictSize = 0, bufSize = 0, i = 0;
        byte first, second;
        try(FileInputStream fileInputStream = new FileInputStream(compressedFile))
        {
            while ((dictSize = fileInputStream.read()) != -1)
            {
                for (i = 0; i < dictSize; i++)
                {
                    first = (byte)fileInputStream.read();
                    second = (byte)fileInputStream.read();
                    dictionary.add(bytesToShort(first, second));
                }
                bufSize = fileInputStream.read();
                for (i = 0; i < bufSize; i++)
                {
                    compressedBuffer.add((byte)fileInputStream.read());
                }

                if (!isNotFirstBlock) {
                    OutputData(isNotFirstBlock);
                    isNotFirstBlock = true;
                }
                else
                    OutputData(isNotFirstBlock);
                dictionary.clear();
                compressedBuffer.clear();
            }
        }
        catch (Exception e)
        {
            log.writeToLog(log.logItems.ERROR_OPEN_FILE);
        }
    }

    private void OutputData(boolean append)
    {
        byte[] byteBuf;
        try(FileOutputStream fileOutputStream = new FileOutputStream(output, append))
        {
            for (byte elem : compressedBuffer)
            {
                byteBuf = shortToBytes(dictionary.get(elem));
                fileOutputStream.write(byteBuf[0]);
                fileOutputStream.write(byteBuf[1]);
            }
        }
        catch (Exception e)
        {
            log.writeToLog(LogCreator.LogItems.ERROR_OPEN_FILE);
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

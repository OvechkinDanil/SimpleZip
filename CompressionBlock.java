import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.*;

public class CompressionBlock {
    private final String input;
    private final String output;
    private boolean isNotFirstBuf = false;
    private int bufSize;
    private byte[] buffer;
    private ArrayList<Short> dictionary;
    private ArrayList<Byte> compressedBuffer;
    LogCreator log;

    CompressionBlock(ConfigParcer cfg, LogCreator l)
    {
        log = l;
        input = cfg.GetConfigItemInf(ConfigParcer.ConfigItems.INPUT);
        output = cfg.GetConfigItemInf(ConfigParcer.ConfigItems.OUTPUT);
        try
        {
            bufSize = Integer.parseInt(cfg.GetConfigItemInf(ConfigParcer.ConfigItems.BUFSIZE));
        }
        catch (NumberFormatException e)
        {
            log.writeToLog(LogCreator.LogItems.ERROR_INTEGER);
        }
        buffer = new byte[bufSize];

        dictionary = new ArrayList<>();
        compressedBuffer = new ArrayList<>();
    }

    public void compress()
    {
        int byteCount = 0;
        try(FileInputStream fileInputStream = new FileInputStream(input))
        {
            while((byteCount = fileInputStream.read(buffer, 0, bufSize)) != -1)
            {
                CompressBuffer(byteCount);
                if (!isNotFirstBuf) {
                    OutputData(isNotFirstBuf);
                    isNotFirstBuf = true;
                }
                else
                    OutputData(isNotFirstBuf);
                dictionary.clear();
                compressedBuffer.clear();
            }
        }
        catch (Exception e)
        {
            log.writeToLog(LogCreator.LogItems.ERROR_OPEN_FILE);
        }
    }

    private void OutputData(boolean append)
    {
        byte[] buf;
        try(FileOutputStream fileOutputStream = new FileOutputStream(output, append))
        {
            fileOutputStream.write((byte)dictionary.size());
            for (short elem: dictionary) {
                fileOutputStream.write(shortToBytes(elem));
            }
            fileOutputStream.write((byte)compressedBuffer.size());
            for (byte elem: compressedBuffer)
            {
                fileOutputStream.write(elem);
            }
        }
        catch (Exception e)
        {
            log.writeToLog(LogCreator.LogItems.ERROR_OPEN_FILE);
        }
    }

    private void CompressBuffer(int byteCount)
    {
        CreateListOfPairs(byteCount);
        CreateCompressedBuffer(byteCount);
    }

    private void CreateListOfPairs(int byteCount)
    {
        int i;
        short pairBytes;
        for (i = 1; i < byteCount; i+=2)
        {
            pairBytes = bytesToShort(buffer[i-1], buffer[i]);
            if (!dictionary.contains(pairBytes) || dictionary.size() == 0)
            {
                dictionary.add(pairBytes);
            }
        }
    }

    private void CreateCompressedBuffer(int byteCount)
    {
        int i;
        byte ind;
        short pairBytes;
        for (i = 1; i < byteCount; i+= 2)
        {
            pairBytes = bytesToShort(buffer[i-1], buffer[i]);
            ind = (byte)dictionary.indexOf(pairBytes);
            if (ind == -1)
            {
                log.writeToLog(LogCreator.LogItems.ERROR_DICTIONARY_INDEX);
            }
            compressedBuffer.add(ind);
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

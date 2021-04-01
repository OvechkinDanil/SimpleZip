package ru.main;

import ru.spbstu.pipeline.*;


import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Compression extends ArrayTypeChanger implements IExecutor {
    private byte[] buffer;
    private byte[] mergedArray;

    private ArrayList<Short> dictionary;
    private ArrayList<Byte> compressedBuffer;
    private IConsumer consumer;
    private IProducer producer;
    private IMediator mediator;
    private final TYPE[] availableTypes = {TYPE.BYTE, TYPE.SHORT, TYPE.CHAR};
    private String configName;
    private final Logger logger;
    private TYPE uType;

    public Compression(Logger logger)
    {
        this.logger = logger;
        dictionary = new ArrayList<>();
        compressedBuffer = new ArrayList<>();
    }

    @Override
    public RC setConfig(String s)
    {
        configName = s;
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC setConsumer(IConsumer iExecutable)
    {
        if (iExecutable == null)
        {
            logger.log(Level.SEVERE, Log.LoggerItems.CODE_FAILED_PIPELINE_CONSTRUCTION.getTitle());
            return RC.CODE_FAILED_PIPELINE_CONSTRUCTION;
        }
        consumer = iExecutable;
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC setProducer(IProducer iExecutable)
    {
        if (iExecutable == null)
        {
            logger.log(Level.SEVERE, Log.LoggerItems.CODE_FAILED_PIPELINE_CONSTRUCTION.getTitle());
            return RC.CODE_FAILED_PIPELINE_CONSTRUCTION;
        }
        producer = iExecutable;
        uType = getUnionType();

        if (uType == null)
            return RC.CODE_FAILED_PIPELINE_CONSTRUCTION;

        mediator = producer.getMediator(uType);

        return RC.CODE_SUCCESS;
    }

    @Override
    public RC execute()
    {
        Object inputArray = mediator.getData();

        if (inputArray == null)
        {
            mergedArray = null;
            return consumer.execute();
        }

        buffer = byteArray(inputArray, uType);

        if (buffer == null)
        {
            logger.log(Level.SEVERE, Log.LoggerItems.CODE_INVALID_INPUT_STREAM.getTitle());
            return RC.CODE_INVALID_INPUT_STREAM;
        }

        if (buffer.length % 2 != 0)
        {
            logger.log(Level.SEVERE, Log.LoggerItems.CODE_INVALID_INPUT_STREAM.getTitle());
            return RC.CODE_INVALID_INPUT_STREAM;
        }

        CompressBuffer();
        mergedArray = createMergedArray();
        dictionary.clear();
        compressedBuffer.clear();
        return consumer.execute();
    }


    private byte[] createMergedArray()
    {
        int index = 4;
        int dictSize = dictionary.size(), compBufSize = compressedBuffer.size();
        int mergedArraySize = dictSize * 2 + compBufSize + 8;

        if (mergedArraySize % 2 != 0)
            mergedArraySize += 1;


        byte[] mergedArray = new byte[mergedArraySize];
        mergedArray[mergedArraySize - 1] = 0;

        byte[] dictSizeInBytes = intToByteArray(dictSize);

        //Здесь я записываю размер словаря. Так как он в int, то записываю 4 байта
        mergedArray[0] = dictSizeInBytes[0];
        mergedArray[1] = dictSizeInBytes[1];
        mergedArray[2] = dictSizeInBytes[2];
        mergedArray[3] = dictSizeInBytes[3];

        for (short elem: dictionary) {
            System.arraycopy(shortToBytes(elem), 0, mergedArray, index, 2);
            index+=2;
        }


        byte[] compBufSizeInBytes = intToByteArray(compBufSize);

        //Здесь я записываю размер сжатого блока. Так как он в int, то записываю 4 байта
        mergedArray[index] = compBufSizeInBytes[0];
        mergedArray[++index] = compBufSizeInBytes[1];
        mergedArray[++index] = compBufSizeInBytes[2];
        mergedArray[++index] = compBufSizeInBytes[3];
        index++;

        for (byte elem: compressedBuffer)
        {
            mergedArray[index] = elem;
            index++;
        }
        return mergedArray;
    }


    private byte[] shortToBytes(short value)
    {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.putShort(value);
        return buffer.array();
    }

    private void CompressBuffer()
    {
        CreateListOfPairs();
        CreateCompressedBuffer();
    }

    private void CreateListOfPairs()
    {
        int i;
        int byteCount = buffer.length;
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

    private void CreateCompressedBuffer()
    {
        int i;
        int byteCount = buffer.length;
        byte ind;
        short pairBytes;
        for (i = 1; i < byteCount; i+= 2)
        {
            pairBytes = bytesToShort(buffer[i-1], buffer[i]);
            ind = (byte)dictionary.indexOf(pairBytes);
            compressedBuffer.add(ind);
        }
    }

    public byte[] intToByteArray(int data)
    {
        byte[] result = new byte[4];
        result[0] = (byte) ((data & 0xFF000000) >> 24);
        result[1] = (byte) ((data & 0x00FF0000) >> 16);
        result[2] = (byte) ((data & 0x0000FF00) >> 8);
        result[3] = (byte) ((data & 0x000000FF) >> 0);
        return result;
    }


    private short bytesToShort(byte first, byte second)
    {
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(first);
        bb.put(second);
        return bb.getShort(0);
    }



    private TYPE getUnionType()
    {
      TYPE[] producerTypes = producer.getOutputTypes();

      for (TYPE pType : producerTypes)
      {
          for (TYPE cType : availableTypes) {
              if (cType == pType)
                  return cType;
          }
      }

      logger.log(Level.SEVERE, Log.LoggerItems.CODE_FAILED_PIPELINE_CONSTRUCTION.getTitle());
      return null;
    }

    private class shortMediator implements IMediator
    {
        @Override
        public Object getData()
        {
            if (mergedArray == null)
                return null;
            return byteArrayToShortArray(mergedArray);
        }
    }

    private class byteMediator implements IMediator
    {
        @Override
        public Object getData()
        {
            if (mergedArray == null)
                return null;

            int newBufSize = mergedArray.length;

            byte[] copyBuffer = new byte[newBufSize];
            System.arraycopy(mergedArray, 0, copyBuffer, 0, newBufSize);

            return copyBuffer;
        }
    }

    private class charMediator implements IMediator
    {
        @Override
        public Object getData()
        {
            if (mergedArray == null)
                return null;
            else
                return new String(mergedArray, StandardCharsets.UTF_8).toCharArray();
        }
    }

    @Override
    public TYPE[] getOutputTypes()
    {
        return new TYPE[] {TYPE.BYTE, TYPE.SHORT, TYPE.CHAR};
    }

    @Override
    public IMediator getMediator(TYPE type)
    {
        switch(type)
        {
            case BYTE:
                return new Compression.byteMediator();
            case SHORT:
                return new Compression.shortMediator();
            case CHAR:
                return new Compression.charMediator();
            default:
                return null;
        }
    }
}

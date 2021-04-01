package ru.main;


import ru.spbstu.pipeline.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Decompression extends ArrayTypeChanger implements IExecutor {
    private ArrayList<Short> dictionary;
    private ArrayList<Byte> compressedBuffer;
    private IConsumer consumer;
    private IProducer producer;
    private IMediator mediator;
    private String configName;
    private Logger logger;

    private final TYPE[] availableTypes = {TYPE.BYTE, TYPE.SHORT, TYPE.CHAR};
    private byte[] outputArray;
    private int dictSize = 0;
    private TYPE uType;
    private int compBufferSize = 0;
    private ArrayList<Byte> untreatedBytes;
    private int necessarySize = 0;


    public Decompression(Logger logger)
    {
        this.logger = logger;
        untreatedBytes = new ArrayList<Byte>();
        dictionary = new ArrayList<>();
        compressedBuffer = new ArrayList<>();
    }

    @Override
    public RC setConfig(String s) {
        configName = s;
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC setConsumer(IConsumer iExecutable) {
        if (iExecutable == null)
        {
            logger.log(Level.SEVERE, Log.LoggerItems.CODE_FAILED_PIPELINE_CONSTRUCTION.getTitle());
            return RC.CODE_FAILED_PIPELINE_CONSTRUCTION;
        }

        consumer = iExecutable;
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC setProducer(IProducer iExecutable) {
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
        RC err;
        Object inArr =mediator.getData();

        if (inArr == null)
        {
            outputArray = null;
            return consumer.execute();
        }

        byte[] inputArray = byteArray(inArr, uType);

        if (inputArray.length % 2 != 0)
        {
            logger.log(Level.SEVERE, Log.LoggerItems.CODE_INVALID_INPUT_STREAM.getTitle());
            return RC.CODE_INVALID_INPUT_STREAM;
        }

        byte[] ArrayBytesDictSize = new byte[4];


        for (byte elem : inputArray)
            untreatedBytes.add(elem);

        if (dictSize == 0)
        {
            ArrayBytesDictSize[0] = untreatedBytes.get(0);
            ArrayBytesDictSize[1] = untreatedBytes.get(1);
            ArrayBytesDictSize[2] = untreatedBytes.get(2);
            ArrayBytesDictSize[3] = untreatedBytes.get(3);

            dictSize = ByteBuffer.wrap(ArrayBytesDictSize).getInt();
            necessarySize = dictSize * 2 + 1;
        }



        if (untreatedBytes.size() > necessarySize && compBufferSize == 0)
        {
            ArrayBytesDictSize[0] = untreatedBytes.get(dictSize * 2 + 4);
            ArrayBytesDictSize[1] = untreatedBytes.get(dictSize * 2 + 5);
            ArrayBytesDictSize[2] = untreatedBytes.get(dictSize * 2 + 6);
            ArrayBytesDictSize[3] = untreatedBytes.get(dictSize * 2 + 7);

            compBufferSize = ByteBuffer.wrap(ArrayBytesDictSize).getInt();
            necessarySize += compBufferSize + 1;
        }

        if (compBufferSize != 0 && dictSize != 0 && untreatedBytes.size() >= necessarySize)
        {
            outputArray = deCompress();
            err = consumer.execute();
            dictSize = 0;
            compBufferSize = 0;
            necessarySize = 0;
            dictionary.clear();
            compressedBuffer.clear();
            return err;
        }

        return RC.CODE_SUCCESS;
    }

    private byte[] deCompress()
    {
        int i;
        byte first, second;
        byte a;


        untreatedBytes.remove(3);
        untreatedBytes.remove(2);
        untreatedBytes.remove(1);
        untreatedBytes.remove(0);

        untreatedBytes.remove(dictSize * 2 + 3);
        untreatedBytes.remove(dictSize * 2 + 2);
        untreatedBytes.remove(dictSize * 2 + 1);
        untreatedBytes.remove(dictSize * 2);



        for (i = 0; i < dictSize; i++) {
            second = untreatedBytes.remove(1);
            first = untreatedBytes.remove(0);
            dictionary.add(bytesToShort(first, second));
        }

        for (i = 0; !untreatedBytes.isEmpty() && i < compBufferSize; i++) {
            a = untreatedBytes.remove(0);
            compressedBuffer.add(a);
        }

        return CreateOutputArray();
    }

    private byte[] shortToBytes(short value)
    {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.putShort(value);
        return buffer.array();
    }

    private byte[] CreateOutputArray()
    {
        int arrSize = compBufferSize * 2;
        byte[] outputArray = new byte[arrSize];
        int index = 0;

        byte[] byteBuf;

        for (byte elem : compressedBuffer)
        {
            byteBuf = shortToBytes(dictionary.get(elem));
            outputArray[index] = byteBuf[0];
            outputArray[index + 1] = byteBuf[1];

            index += 2;
        }

        return outputArray;
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
            for (TYPE aType : availableTypes) {
                if (aType == pType)
                    return aType;
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
            if (outputArray == null)
                return null;
            else
                return byteArrayToShortArray(outputArray);
        }
    }

    private class byteMediator implements IMediator
    {
        @Override
        public Object getData()
        {
            if (outputArray == null)
                return null;

            int newBufSize = outputArray.length;

            byte[] copyBuffer = new byte[newBufSize];
            System.arraycopy(outputArray, 0, copyBuffer, 0, newBufSize);

            return copyBuffer;
        }
    }

    private class charMediator implements IMediator
    {
        @Override
        public Object getData()
        {
            if (outputArray == null)
                return null;
            else
                return new String(outputArray, StandardCharsets.UTF_8).toCharArray();
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
                return new Decompression.byteMediator();
            case SHORT:
                return new Decompression.shortMediator();
            case CHAR:
                return new Decompression.charMediator();
            default:
                return null;
        }
    }

}

package ru.main;

import ru.spbstu.pipeline.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Reader extends ArrayTypeChanger implements IReader {
    private final ReaderWriterGrammar grammar;
    private int bufSize;
    private BufferedInputStream fis;
    private String configName;
    private IConsumer consumer;
    private IProducer producer;
    private Logger logger;
    private INotifier notifier;
    private final HashMap<Integer, byte[]> chunkContainer;

    private final TYPE[] availableTypes = {TYPE.BYTE, TYPE.SHORT, TYPE.CHAR};
    private final TYPE[] outputTypes = {TYPE.BYTE, TYPE.SHORT, TYPE.CHAR};
    private byte[] buffer;

    public Reader(Logger logger)
    {
        chunkContainer = new HashMap<>();
        this.logger = logger;
        grammar = new ReaderWriterGrammar();
    }
    @Override
    public RC setInputStream(FileInputStream fileInputStream) {
        fis = new BufferedInputStream(fileInputStream);
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC setConfig(String s) {

        if (s.length() == 0)
        {
            logger.log(Level.SEVERE, Log.LoggerItems.CODE_CONFIG_SEMANTIC_ERROR.getTitle());
            return RC.CODE_CONFIG_SEMANTIC_ERROR;
        }
        configName = s;
        File file = new File(configName);
        Map<String, String > configItems = ConfigParser.parse(file, new HashMap<>(), grammar);
        
        if (configItems == null)
        {
            logger.log(Level.SEVERE, Log.LoggerItems.CODE_CONFIG_SEMANTIC_ERROR.getTitle());
            return RC.CODE_CONFIG_GRAMMAR_ERROR;
        }

        return SemanticParser(configItems);
    }

    public RC execute() {
        int byteCount;
        int chunkID = 0;
        try
        {
            if (bufSize % 2 != 0)
            {
                logger.log(Level.SEVERE, Log.LoggerItems.CODE_INVALID_INPUT_STREAM.getTitle());
                return RC.CODE_INVALID_INPUT_STREAM;
            }

            while (true)
            {
                buffer = new byte[bufSize];
                byteCount = fis.read(buffer, 0, bufSize);

                if (byteCount == -1)
                    return RC.CODE_SUCCESS;

                if (byteCount != bufSize)
                {
                    buffer = Arrays.copyOfRange(buffer, 0, byteCount);
                    bufSize = byteCount;
                }
                synchronized (chunkContainer)
                {
                    chunkContainer.put(chunkID, buffer);
                }
                notifier.notify(chunkID);
                chunkID++;
            }
        }
        catch (IOException e)
        {
            logger.log(Level.SEVERE, Log.LoggerItems.CODE_FAILED_TO_READ.getTitle());
            return RC.CODE_FAILED_TO_READ;
        }

    }

    private RC SemanticParser(Map <String, String > configItems)
    {

        for (Map.Entry<String, String> item : configItems.entrySet())
        {
            if (isNumberItem(item.getKey()))
            {
                try
                {
                    bufSize = Integer.parseInt(item.getValue().trim());
                }
                catch (NumberFormatException nfe)
                {
                    logger.log(Level.SEVERE, Log.LoggerItems.CODE_CONFIG_SEMANTIC_ERROR.getTitle());
                    return RC.CODE_CONFIG_SEMANTIC_ERROR;
                }
            }

        }

        return RC.CODE_SUCCESS;
    }

    private boolean isNumberItem(String key)
    {
        return ReaderWriterGrammar.GrammarItems.BUFSIZE.getTitle().equals(key);
    }

    @Override
    public RC addNotifier(INotifier iNotifier) {
        notifier = iNotifier;
        return RC.CODE_SUCCESS;
    }

    @Override
    public void run()
    {
        execute();
    }

    private class shortMediator implements IMediator
    {
        @Override
        public Object getData(int chunkID)
        {
            byte[] outputArr;
            synchronized (chunkContainer)
            {
                outputArr = chunkContainer.get(chunkID);
                if (outputArr == null)
                    return null;
                else
                {
                    chunkContainer.remove(chunkID);
                    return byteArrayToShortArray(outputArr);
                }
            }
        }
    }


    private class byteMediator implements IMediator
    {
        @Override
        public Object getData(int chunkID)
        {
            byte[] outputArr;
            synchronized (chunkContainer)
            {
                outputArr = chunkContainer.get(chunkID);
                if (outputArr == null)
                    return null;

                byte[] copyBuffer = new byte[outputArr.length];
                System.arraycopy(outputArr, 0, copyBuffer, 0, outputArr.length);
                chunkContainer.remove(chunkID);
                return copyBuffer;
            }
        }
    }

    private class charMediator implements IMediator
    {
        @Override
        public Object getData(int chunkID)
        {
            byte[] outputArr;
            synchronized (chunkContainer)
            {
                outputArr = chunkContainer.get(chunkID);
                if (outputArr == null)
                    return null;

                chunkContainer.remove(chunkID);
                return new String(outputArr, StandardCharsets.UTF_8).toCharArray();
            }
        }
    }

    @Override
    public TYPE[] getOutputTypes()
    {
        return outputTypes;
    }

    @Override
    public IMediator getMediator(TYPE type)
    {
        switch(type)
        {
            case BYTE:
                return new Reader.byteMediator();
            case SHORT:
                return new Reader.shortMediator();
            case CHAR:
                return new Reader.charMediator();
            default:
                return null;
        }
    }


}

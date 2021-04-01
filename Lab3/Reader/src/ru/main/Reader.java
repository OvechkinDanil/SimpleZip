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

    private final TYPE[] availableTypes = {TYPE.BYTE, TYPE.SHORT, TYPE.CHAR};
    private byte[] buffer;

    public Reader(Logger logger)
    {
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
        producer = iExecutable;
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC execute() {
        int byteCount;
        RC err;
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
                {
                    buffer = null;
                    bufSize = 0;
                    return consumer.execute();
                }


                if (byteCount != bufSize)
                {
                    buffer = Arrays.copyOfRange(buffer, 0, byteCount);
                    bufSize = byteCount;
                }


                if ((err = consumer.execute()) != RC.CODE_SUCCESS)
                {
                    return err;
                }

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

    private class shortMediator implements IMediator
    {
        @Override
        public Object getData()
        {
            if (buffer == null)
                return null;
            return byteArrayToShortArray(buffer);
        }
    }


    private class byteMediator implements IMediator
    {
        @Override
        public Object getData()
        {
            if (buffer == null)
                return null;

            byte[] copyBuffer = new byte[bufSize];
            System.arraycopy(buffer, 0, copyBuffer, 0, bufSize);

            return copyBuffer;
        }
    }

    private class charMediator implements IMediator
    {
        @Override
        public Object getData()
        {
            if (buffer == null)
                return null;

            return new String(buffer, StandardCharsets.UTF_8).toCharArray();
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

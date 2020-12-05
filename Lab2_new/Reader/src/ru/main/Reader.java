package ru.main;

import ru.spbstu.pipeline.IExecutable;
import ru.spbstu.pipeline.IReader;
import ru.spbstu.pipeline.RC;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Reader implements IReader {
    private final ReaderWriterGrammar grammar;
    private int bufSize;
    private BufferedInputStream fis;
    private String configName;
    private IExecutable consumer;
    private IExecutable producer;
    private Logger logger;

    Reader(Logger logger)
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
    public RC setConsumer(IExecutable iExecutable)
    {
        consumer = iExecutable;
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC setProducer(IExecutable iExecutable)
    {
        producer = iExecutable;
        return RC.CODE_SUCCESS;
    }

    @Override
    public RC execute(byte[] bytes) {
        int byteCount;
        RC err;
        try
        {
            while (true)
            {
                byte[] buffer = new byte[bufSize];
                byteCount = fis.read(buffer, 0, bufSize);

                if (byteCount == -1)
                    return RC.CODE_SUCCESS;

                if (byteCount != bufSize)
                    buffer = Arrays.copyOfRange(buffer, 0, byteCount);


                if ((err = consumer.execute(buffer)) != RC.CODE_SUCCESS)
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
}

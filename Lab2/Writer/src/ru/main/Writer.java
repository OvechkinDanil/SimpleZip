package ru.main;

import ru.spbstu.pipeline.IExecutable;
import ru.spbstu.pipeline.IWriter;
import ru.spbstu.pipeline.RC;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Writer implements IWriter {
    private BufferedOutputStream fos;
    private int bufSize;
    private String configName;
    private final ReaderWriterGrammar grammar;
    private IExecutable consumer;
    private IExecutable producer;
    private Logger logger;


    public Writer(Logger logger)
    {
        this.logger = logger;
        grammar = new ReaderWriterGrammar();
    }

    @Override
    public RC setOutputStream(FileOutputStream fileOutputStream) {
        fos = new BufferedOutputStream(fileOutputStream);
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
            logger.log(Level.SEVERE, Log.LoggerItems.CODE_CONFIG_GRAMMAR_ERROR.getTitle());
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
    public RC execute(byte[] bytes)
    {
        int byteSize = bytes.length;
        int index = 0, length = 0;
        try
        {
            while(byteSize > 0)
            {
                if (byteSize - bufSize < 0)
                    length = byteSize;
                else
                    length = bufSize;
                fos.write(bytes, index, length);

                byteSize -= bufSize;
                index += length;
            }
            fos.flush();
            return RC.CODE_SUCCESS;
        }
        catch (IOException e)
        {
            logger.log(Level.SEVERE, Log.LoggerItems.CODE_FAILED_TO_WRITE.getTitle());
            return RC.CODE_FAILED_TO_WRITE;
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

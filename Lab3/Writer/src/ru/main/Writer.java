package ru.main;

import ru.spbstu.pipeline.*;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Writer extends ArrayTypeChanger implements IWriter {
    private BufferedOutputStream fos;
    private int bufSize;
    private String configName;
    private final ReaderWriterGrammar grammar;

    private final TYPE[] availableTypes = {TYPE.BYTE, TYPE.SHORT, TYPE.CHAR};
    private TYPE uType;
    private IConsumer consumer;
    private IProducer producer;
    private IMediator mediator;
    private final Logger logger;


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
    public RC setConsumer(IConsumer iExecutable)
    {
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
            return RC.CODE_SUCCESS;

        byte[] outputArr = byteArray(inputArray, uType);

        int arrSize = outputArr.length;
        int index = 0, length;
        try
        {
            while(arrSize > 0)
            {
                if (arrSize - bufSize < 0)
                    length = arrSize;
                else
                    length = bufSize;
                fos.write(outputArr, index, length);
                arrSize -= bufSize;
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

    private boolean isNumberItem(String key)
    {
        return ReaderWriterGrammar.GrammarItems.BUFSIZE.getTitle().equals(key);
    }
}

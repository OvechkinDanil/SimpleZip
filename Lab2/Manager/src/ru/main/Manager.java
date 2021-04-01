package ru.main;

import java.io.*;
import ru.spbstu.pipeline.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Manager {
    private final Map<String, Object> configParams;
    private final ManagerGrammar grammar;
    private static final String list_delimiter = "-";
    private RC error_code = RC.CODE_SUCCESS;
    private IConfigurable[] pipeLine;
    private Logger logger;



    Manager(Logger logger)
    {
        this.logger = logger;

        grammar = new ManagerGrammar();

        configParams = new HashMap<>();

    }

    public RC setConfig(String cfgFile)
    {
        File file = new File(cfgFile);
        Map <String, String > configItems = ConfigParser.parse(file, new HashMap<>(), grammar);
        if (configItems == null)
        {
            logger.log(Level.SEVERE, Log.LoggerItems.CODE_CONFIG_GRAMMAR_ERROR.getTitle());
            return RC.CODE_CONFIG_GRAMMAR_ERROR;
        }

        SemanticParser(configItems);

        return RC.CODE_SUCCESS;
    }

    public RC execute()
    {
        String inputFileName = (String)configParams.get(ManagerGrammar.GrammarItems.INPUT.getTitle());
        if (inputFileName.length() == 0)
        {
            logger.log(Level.SEVERE, Log.LoggerItems.CODE_CONFIG_SEMANTIC_ERROR.getTitle());
            return RC.CODE_CONFIG_SEMANTIC_ERROR;
        }


        String outputFileName = (String)configParams.get(ManagerGrammar.GrammarItems.OUTPUT.getTitle());
        if (outputFileName.length() == 0)
        {
            logger.log(Level.SEVERE, Log.LoggerItems.CODE_CONFIG_SEMANTIC_ERROR.getTitle());
            return RC.CODE_CONFIG_SEMANTIC_ERROR;
        }

        try(FileInputStream fis = new FileInputStream(inputFileName))
        {
           try(FileOutputStream fos = new FileOutputStream(outputFileName)) {

               ((IWriter) pipeLine[pipeLine.length - 1]).setOutputStream(fos);
               ((IReader) pipeLine[0]).setInputStream(fis);

               RC err = ((IExecutable) pipeLine[0]).execute(null);

               if (err == RC.CODE_SUCCESS)
               {
                   logger.log(Level.SEVERE, Log.LoggerItems.CODE_SUCCESS.getTitle());
               }
               else
               {
                   logger.log(Level.SEVERE, Log.LoggerItems.CODE_PROBLEMS.getTitle());
               }

               fos.close();
               fis.close();

           }
           catch (Exception e)
           {
               logger.log(Level.SEVERE, Log.LoggerItems.CODE_INVALID_OUTPUT_STREAM.getTitle());
               return RC.CODE_INVALID_OUTPUT_STREAM;
           }
        }
        catch (Exception e)
        {
            logger.log(Level.SEVERE, Log.LoggerItems.CODE_INVALID_INPUT_STREAM.getTitle());
            return RC.CODE_INVALID_INPUT_STREAM;
        }
        return RC.CODE_SUCCESS;
    }

    private RC checkLine()
    {
        int i;
        boolean isFind = false;
        String[] lineStep = (String[])configParams.get(ManagerGrammar.GrammarItems.ORDER.getTitle());
        int lineWidth = lineStep.length;

        if (!configParams.get(ManagerGrammar.GrammarItems.READER_NAME.getTitle()).equals(lineStep[0]) ||
                !configParams.get(ManagerGrammar.GrammarItems.WRITER_NAME.getTitle()).equals(lineStep[lineWidth - 1]))
        {
            logger.log(Level.SEVERE, Log.LoggerItems.CODE_CONFIG_SEMANTIC_ERROR.getTitle());
            return RC.CODE_CONFIG_SEMANTIC_ERROR;
        }


        String[] exNames = (String[])configParams.get(ManagerGrammar.GrammarItems.EXECUTORS_NAME.getTitle());

        if (exNames.length == 0)
        {
            logger.log(Level.SEVERE, Log.LoggerItems.CODE_CONFIG_SEMANTIC_ERROR.getTitle());
            return RC.CODE_CONFIG_SEMANTIC_ERROR;
        }

        for (i = 1; i < lineWidth - 1; i++)
        {
            for (String ex : exNames)
            {
                if (lineStep[i].equals(ex))
                {
                    isFind = true;
                    break;
                }
            }
            if (!isFind)
            {
                logger.log(Level.SEVERE, Log.LoggerItems.CODE_FAILED_PIPELINE_CONSTRUCTION.getTitle());
                return RC.CODE_FAILED_PIPELINE_CONSTRUCTION;
            }
            isFind = false;
        }
        return RC.CODE_SUCCESS;
    }

    public RC makePipeLine()
    {
        if ((error_code = checkLine()) != RC.CODE_SUCCESS)
        {
            logger.log(Level.SEVERE, Log.LoggerItems.CODE_FAILED_CHECK_ORDER.getTitle());
            return error_code;
        }
        String[] line = (String[]) configParams.get(ManagerGrammar.GrammarItems.ORDER.getTitle());
        pipeLine = new IConfigurable[line.length];
        IConfigurable worker;

        for (int i = 0; i < pipeLine.length; i++)
        {
            if ((worker = createWorker(line[i])) != null)
                pipeLine[i] = worker;
            else
            {
                logger.log(Level.SEVERE, Log.LoggerItems.CODE_FAILED_PIPELINE_CONSTRUCTION.getTitle());
                return RC.CODE_FAILED_PIPELINE_CONSTRUCTION;
            }
        }

        if ((error_code = initPipeLine()) != RC.CODE_SUCCESS)
        {
            logger.log(Level.SEVERE, Log.LoggerItems.CODE_FAILED_INIT_PIPELINE.getTitle());
            return error_code;
        }

        return RC.CODE_SUCCESS;
    }


    private IConfigurable createWorker(String className)
    {
        try
        {
            Class<?> clazz = Class.forName(className);
            Constructor<?> constructor = clazz.getDeclaredConstructor(Logger.class);
            return (IConfigurable)constructor.newInstance(logger);
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e)
        {
            return null;
        }
    }

    private RC initPipeLine()
    {
        if ((error_code = setWorkerConfigs()) != RC.CODE_SUCCESS)
            return error_code;

        if ((error_code = setReaderParams()) != RC.CODE_SUCCESS)
            return error_code;

        if ((error_code = setWriterParams()) != RC.CODE_SUCCESS)
            return error_code;

        if ((error_code = setExecutorsParams()) != RC.CODE_SUCCESS)
            return error_code;

        return RC.CODE_SUCCESS;
    }

    private RC setWorkerConfigs()
    {
        String readerConfigName = (String)configParams.get(ManagerGrammar.GrammarItems.READER_CONFIG.getTitle());
        if (readerConfigName.length() == 0)
        {
            logger.log(Level.SEVERE, Log.LoggerItems.CODE_CONFIG_SEMANTIC_ERROR.getTitle());
            return RC.CODE_CONFIG_SEMANTIC_ERROR;
        }

        pipeLine[0].setConfig(readerConfigName);

        String writerConfigName = (String)configParams.get(ManagerGrammar.GrammarItems.WRITER_CONFIG.getTitle());
        if (writerConfigName.length() == 0)
        {
            logger.log(Level.SEVERE, Log.LoggerItems.CODE_CONFIG_SEMANTIC_ERROR.getTitle());
            return RC.CODE_CONFIG_SEMANTIC_ERROR;
        }

        pipeLine[pipeLine.length - 1].setConfig(writerConfigName);

        String[] executorsConfigNames = (String[])configParams.get(ManagerGrammar.GrammarItems.EXECUTORS_CONFIGS.getTitle());
        if (executorsConfigNames.length != pipeLine.length - 2)
        {
            logger.log(Level.SEVERE, Log.LoggerItems.CODE_CONFIG_SEMANTIC_ERROR.getTitle());
            return RC.CODE_CONFIG_SEMANTIC_ERROR;
        }

        for (int i = 1; i < pipeLine.length - 1; i++)
        {
            pipeLine[i].setConfig(executorsConfigNames[i-1]);
        }

        return RC.CODE_SUCCESS;
    }

    private RC setReaderParams()
    {
        IReader reader= (IReader)pipeLine[0];
        reader.setProducer(null);
        reader.setConsumer((IExecutable)pipeLine[1]);

        return RC.CODE_SUCCESS;

    }

    private RC setWriterParams()
    {
        IWriter writer = (IWriter)pipeLine[pipeLine.length - 1];
        writer.setProducer((IExecutable)pipeLine[pipeLine.length - 2]);
        writer.setConsumer(null);

        return RC.CODE_SUCCESS;
    }

    private RC setExecutorsParams()
    {
        for (int i = 1; i < pipeLine.length - 1; i++)
        {
            ((IExecutor)pipeLine[i]).setConsumer((IExecutable)pipeLine[i+1]);
            ((IExecutor)pipeLine[i]).setProducer((IExecutable)pipeLine[i-1]);
        }

        return RC.CODE_SUCCESS;
    }

    private boolean isListStringItem(String key)
    {
        return ManagerGrammar.GrammarItems.EXECUTORS_CONFIGS.getTitle().equals(key) ||
                ManagerGrammar.GrammarItems.EXECUTORS_NAME.getTitle().equals(key) ||
                ManagerGrammar.GrammarItems.ORDER.getTitle().equals(key);
    }

    private void SemanticParser(Map <String, String > configItems)
    {

        for (Map.Entry<String, String> item : configItems.entrySet())
        {
            if (isListStringItem(item.getKey()))
            {
                configParams.put(item.getKey(), item.getValue().split(list_delimiter));
            }
            else
            {
                configParams.put(item.getKey(), item.getValue());
            }
        }
    }

}

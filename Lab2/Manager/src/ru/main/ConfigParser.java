package ru.main;
import ru.spbstu.pipeline.BaseGrammar;
import ru.spbstu.pipeline.RC;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigParser {
    private static Logger logger;
    ConfigParser()
    {
        logger = Logger.getLogger("ConfigParser_log");
    }

    private static RC ParseLine(Map<String, String> dic, String line, BaseGrammar grammar)
    {
        String gToken;
        boolean isFind = false;
        int i;

        String[] tokens = line.split(grammar.delimiter());
        if (tokens.length != 2)
        {
            return RC.CODE_CONFIG_GRAMMAR_ERROR;
        }
        tokens[0] = tokens[0].trim();
        tokens[1] = tokens[1].trim();

        for (i = 0; i < grammar.numberTokens(); i++)
        {
            gToken = grammar.token(i);
            if (gToken != null && gToken.equals(tokens[0]))
            {
                dic.put(gToken, tokens[1]);
                return RC.CODE_SUCCESS;
            }
        }
        return RC.CODE_CONFIG_GRAMMAR_ERROR;
    }

    static Map<String, String> parse(File file, Map<String, String> dic, BaseGrammar grammar)
    {
        String line;

        try {
            Scanner sc = new Scanner(file);
            while (sc.hasNextLine()) {
                line = sc.nextLine();

                if (line.length() == 0)
                    continue;

                if (ParseLine(dic, line, grammar) != RC.CODE_SUCCESS)
                    return null;
            }
            return dic;
        }
        catch(FileNotFoundException e)
        {
            return null;
        }
    }
}

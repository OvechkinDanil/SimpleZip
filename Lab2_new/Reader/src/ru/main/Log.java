package ru.main;

public class Log {
    public enum LoggerItems
    {
        CODE_SUCCESS("Done without errors"),
        CODE_PROBLEMS("Done with some errors"),
        CODE_INVALID_ARGUMENT("Invalid arguments"),
        CODE_FAILED_INIT_PIPELINE("Failed init pipeline"),
        CODE_FAILED_CHECK_ORDER("Problem in order in config"),
        CODE_FAILED_TO_READ("Failed to read"),
        CODE_FAILED_TO_WRITE("Failed to write"),
        CODE_INVALID_INPUT_STREAM("Invalid input stream"),
        CODE_INVALID_OUTPUT_STREAM("Invalid output stream"),
        CODE_CONFIG_GRAMMAR_ERROR("problem with config grammar"),
        CODE_CONFIG_SEMANTIC_ERROR("problem with semantic of config"),
        CODE_FAILED_PIPELINE_CONSTRUCTION("Failed pipeline construction");

        private String title;

        LoggerItems(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }
}

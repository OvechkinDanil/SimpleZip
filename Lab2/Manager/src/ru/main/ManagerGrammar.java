package ru.main;
import ru.spbstu.pipeline.BaseGrammar;

public class ManagerGrammar extends BaseGrammar {
    public enum GrammarItems
    {
        INPUT("INPUT"),
        OUTPUT("OUTPUT"),
        WRITER_NAME("WRITER_NAME"),
        READER_NAME("READER_NAME"),
        EXECUTORS_NAME("EXECUTORS_NAME"),
        WRITER_CONFIG("WRITER_CONFIG"),
        READER_CONFIG("READER_CONFIG"),
        EXECUTORS_CONFIGS("EXECUTORS_CONFIGS"),
        ORDER("ORDER");

        private String title;

        GrammarItems(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }

    private static final String[] sa = new String[]
            {
                    GrammarItems.INPUT.getTitle(),
                    GrammarItems.OUTPUT.getTitle(),
                    GrammarItems.WRITER_NAME.getTitle(),
                    GrammarItems.READER_NAME.getTitle(),
                    GrammarItems.EXECUTORS_NAME.getTitle(),
                    GrammarItems.WRITER_CONFIG.getTitle(),
                    GrammarItems.READER_CONFIG.getTitle(),
                    GrammarItems.EXECUTORS_CONFIGS.getTitle(),
                    GrammarItems.ORDER.getTitle()
            };


    public ManagerGrammar()
    {
        super(sa);
    }
}

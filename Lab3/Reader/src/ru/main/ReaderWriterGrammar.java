package ru.main;

import ru.spbstu.pipeline.BaseGrammar;

public class ReaderWriterGrammar extends BaseGrammar {


    public enum GrammarItems
    {
        BUFSIZE("BUFSIZE");

        private String title;

        GrammarItems(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }
    private static final String[] sa = new String[] { GrammarItems.BUFSIZE.getTitle() };


    public ReaderWriterGrammar()
    {
        super(sa);
    }
}

public class Grammar implements IGrammar {
    private int NumberTokens = 0;
    private final String delimeter = ":";

    @Override
    public int NumberGrammarTokens() {
        return NumberTokens;
    }

    @Override
    public String delimeter() {
        return delimeter;
    }

    @Override
    public String grammarToken(int index) {
        return null;
    }
}

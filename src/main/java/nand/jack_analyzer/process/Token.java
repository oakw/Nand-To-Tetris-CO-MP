package nand.jack_analyzer.process;

public class Token {
    public String named;
    public Boolean isNull;
    public int lineNumber;
    public String originFilePath;

    public Token() {
        isNull = false;
    }

    public boolean isOfType(Class<?> cl) {
        return this.getClass() == cl;
    }
}

class Keyword extends Token {
    public KeywordEnum type;

    public Keyword(String key) {
        if (KeywordEnum.valueOfLabel(key) != null) {
            named = key;
            type = KeywordEnum.valueOfLabel(key);
        } else {
            isNull = true;
        }
    }
}

class Symbol extends Token {
    public SymbolEnum symbolType;
    public Symbol(char key) {
        if (SymbolEnum.valueOfLabel(key) != null) {
            symbolType = SymbolEnum.valueOfLabel(key);
            named = String.valueOf(key);
        } else {
            isNull = true;
        }
    }
}

abstract class Constant extends Token {}

class IntegerConstant extends Constant {
    public int constant;

    public IntegerConstant(String value) {
        try {
            constant = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            isNull = true;
        }
    }
}

class StringConstant extends Constant {
    public String constant;

    public StringConstant(String value) {
        constant = value;
    }
}

class Identifier extends Token {
    public String name;

    public Identifier(String value) {
        name = value;
    }
}
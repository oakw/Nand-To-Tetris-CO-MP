package nand.jack_analyzer.process;

/**
 * Type of symbol
 */
public enum SymbolEnum {

    S_LEFTBRACKET('{'),
    S_RIGHTBRACKET('}'),
    S_LEFTBRACET('('),
    S_RIGHTBRACET(')'),
    S_LEFTSQUAREBRACKET('['),
    S_RIGHTSQUAREBRACKET(']'),
    S_DOT('.'),
    S_COMMA(','),
    S_SEMICOLON(';'),
    S_PLUS('+'),
    S_MINUS('-'),
    S_ASTERIX('*'),
    S_SLASH('/'),
    S_AND('&'),
    S_COL('|'),
    S_LESSTHAN('<'),
    S_LARGETHAN('>'),
    S_EQUALS('='),
    S_TILDE('~');


    public final char label;

    SymbolEnum(char label, Object... content) {
        this.label = label;
    }


    public static SymbolEnum valueOfLabel(char label) {
        for (SymbolEnum e : values()) {
            if (e.label == label) {
                return e;
            }
        }
        return null;
    }
}

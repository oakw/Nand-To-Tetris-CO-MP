package nand.jack_analyzer.process;

/**
 * Type of symbol available in .jack
 */
public enum SymbolEnum {

    S_LEFTBRACKET('{', false),
    S_RIGHTBRACKET('}', false),
    S_LEFTBRACET('(', false),
    S_RIGHTBRACET(')', false),
    S_LEFTSQUAREBRACKET('[', false),
    S_RIGHTSQUAREBRACKET(']', false),
    S_DOT('.', false),
    S_COMMA(',', false),
    S_SEMICOLON(';', false),
    S_PLUS('+', true),
    S_MINUS('-', true),
    S_ASTERIX('*', true),
    S_SLASH('/', true),
    S_AND('&', true),
    S_COL('|', true),
    S_LESSTHAN('<', true),
    S_LARGETHAN('>', true),
    S_EQUALS('=', true),
    S_TILDE('~', false);

    public final char label;
    public final boolean isOperand;

    SymbolEnum(char label, boolean isOperand) {
        this.label = label;
        this.isOperand = isOperand;
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

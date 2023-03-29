package nand.jack_analyzer.process;

/**
 * Type of keywords in .jack language
 */
public enum KeywordEnum {

    K_CLASS("class"),
    K_CONSTRUCTOR("constructor"),
    K_FUNCTION("function"),
    K_METHOD("method"),
    K_FIELD("field"),
    K_STATIC("static"),
    K_VAR("var"),
    K_INT("int"),
    K_CHAR("char"),
    K_BOOLEAN("boolean"),
    K_VOID("void"),
    K_TRUE("true"),
    K_FALSE("false"),
    K_NULL("null"),
    K_THIS("this"),
    K_DO("do"),
    K_IF("if"),
    K_ELSE("else"),
    K_WHILE("while"),
    K_RETURN("return"),
    K_LET("let");

    public final String label;

    KeywordEnum(String label) {
        this.label = label;
    }

    public static KeywordEnum valueOfLabel(String label) {
        for (KeywordEnum e : values()) {
            if (e.label.equals(label)) {
                return e;
            }
        }
        return null;
    }
}

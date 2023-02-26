package nand.vm_translator.process.command;

/**
 * Type of arithmetic operation, like "add"/"sub"/"gt" etc.
 */
public enum ArithmeticType {

    A_ADD("add", "+"),
    A_SUB("sub", "-"),
    A_AND("and", "&"),
    A_OR("or", "|"),
    A_GT("gt", "GT"),
    A_EQ("eq", "EQ"),
    A_LT("lt", "LT"),
    A_NOT("not", ""),
    A_NEG("neg", "");

    public final String label;
    public final String operand;

    ArithmeticType(String label, String operand) {
        this.label = label;
        this.operand = operand;
    }

    public static ArithmeticType valueOfLabel(String label) {
        for (ArithmeticType e : values()) {
            if (e.label.equals(label)) {
                return e;
            }
        }
        return null;
    }
}

package nand.vm_translator.process.command;

/**
 * Type of command, like "push"/"pop" etc.
 */
public enum CommandType {

    C_ARITHMETIC(""),
    C_PUSH("push"),
    C_POP("pop"),
    C_LABEL("label"),
    C_GOTO("goto"),
    C_IF("if-goto"),
    C_FUNCTION("function"),
    C_RETURN("return"),
    C_CALL("call");

    public final String label;

    CommandType(String label) {
        this.label = label;
    }

    public static CommandType valueOfLabel(String label) {
        for (CommandType e : values()) {
            if (e.label.equals(label)) {
                return e;
            }
        }
        return null;
    }
}

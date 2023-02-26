package nand.vm_translator.process.command;

/**
 * Type of command, like "push"/"pop" etc.
 */
public enum CommandType {

    C_ARITHMETIC(""),
    C_PUSH("push"),
    C_POP("pop"),
    C_LABEL(""),
    C_GOTO(""),
    C_IF(""),
    C_FUNCTION(""),
    C_RETURN(""),
    C_CALL("");

    public final String label;

    CommandType(String label) {
        this.label = label;
    }
}

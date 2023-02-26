package nand.vm_translator.process.command;

/**
 * Type of memory segment used in operation
 */
public enum SegmentType {

    S_STATIC("static", 16),
    S_TEMP("temp", 5),
    S_CONSTANT("constant", 0),
    S_POINTER("pointer", 3),
    S_LOCAL("local", 1),
    S_ARGUMENT("argument", 2),
    S_THIS("this", 3),
    S_THAT("that", 4);


    public final String label;
    public final int startIndex;

    SegmentType(String label, int startIndex) {
        this.label = label;
        this.startIndex = startIndex;
    }

    public static SegmentType valueOfLabel(String label) {
        for (SegmentType e : values()) {
            if (e.label.equals(label)) {
                return e;
            }
        }
        return null;
    }
}

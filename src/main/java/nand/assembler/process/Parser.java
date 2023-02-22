package nand.assembler.process;

import static nand.assembler.process.InstructionType.*;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.io.FileNotFoundException;
import java.io.File;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class Parser {

    private RandomAccessFile asmFile;

    private String currentLine = ""; // Current line content
    public int lineIndex = 0; // Current line number

    private List<String> lineParts = new ArrayList<>();

    /**
     * REGEX pattern that is used to retrieve parts of each line
     * Possible result groups:
     * 1 - Symbol of A_INSTRUCTION
     * 2 - Symbol of L_INSTRUCTION
     * 3 - Instruction 'dest' part
     * 4 - Instruction 'comp' part
     * 5 - N/A
     * 6 - Instruction 'jump' part
     * 7 - Comment in line
     */
    private final Pattern patternCompiled = Pattern.compile("@(\\S+)|\\((.*)\\)|(\\b(?!\\w*\\w+\\1)[ADM]+)?=?((?!\\w*(\\w)\\w*\\1)[!&|ADM\\-+10]+\\b);?(\\b(?!\\w*\\w+\\1)[JNMPELTG01]+)?");


    public Parser(String textFileLocation) {
        try {
            File file = new File(textFileLocation);
            asmFile = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reset the position of file to zero. Further reading will go from beginning
     *
     * @throws IOException Failed seek to line results in an exception
     */
    public void reset() throws IOException {
        asmFile.seek(0);
        lineIndex = 0;
    }

    /**
     * Checks whether there are lines in the file left
     *
     * @throws IOException Failed reading from file results in an exception
     */
    public boolean hasMoreLines() throws IOException {
        return asmFile.getFilePointer() < asmFile.length();
    }

    /**
     * Move to the next line in parser
     *
     * @param ignoreLInstruction Skips to next instruction if is true
     * @throws IOException Failed next line reading results in an exception
     */
    public void advance(boolean ignoreLInstruction) throws IOException {
        currentLine = asmFile.readLine().trim();

        lineParts = patternCompiled
            .matcher(currentLine)
            .results()
            .flatMap(mr -> IntStream.rangeClosed(1, mr.groupCount())
                    .mapToObj(mr::group))
            .collect(Collectors.toList());

        // Go to next line if current is insignificant
        if (Objects.equals(currentLine, "") || lineParts.isEmpty() || currentLine.startsWith("//") || (ignoreLInstruction && instructionType() == L_INSTRUCTION)) {
            advance(ignoreLInstruction);
            return;
        }

        lineIndex += 1;
    }

    /**
     * Indicates current instruction type
     */
    public InstructionType instructionType() {
        if (currentLine.matches("^@\\S+")) return A_INSTRUCTION;
        if (currentLine.matches("\\(.*\\)")) return L_INSTRUCTION;
        return C_INSTRUCTION;
    }

    /**
     * Returns symbol of current instruction
     *
     * @throws Exception Trying to get symbol for other than A or L instruction results in Exception
     */
    public String symbol() throws Exception {
        return switch (instructionType()) {
            case A_INSTRUCTION -> lineParts.get(0);
            case L_INSTRUCTION -> lineParts.get(1);
            default -> throw new Exception("Tried to get symbol for illegal instruction type");
        };
    }

    /**
     * Gets dest part of the instruction
     */
    public String dest() {
        return lineParts.get(2);
    }

    /**
     * Gets comp part of the instruction
     */
    public String comp() {
        return lineParts.get(3);
    }

    /**
     * Gets jump part of the instruction
     */
    public String jump() {
        return lineParts.get(5);
    }
}

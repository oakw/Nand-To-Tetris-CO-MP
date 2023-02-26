package nand.vm_translator.process;

import nand.vm_translator.process.command.CommandType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class Parser {

    private RandomAccessFile vmFile;

    private String currentLine = ""; // Current line content
    public int lineIndex = 0; // Current line number

    private List<String> lineParts = new ArrayList<>();

    /**
     * REGEX pattern that is used to retrieve parts of each line
     * Possible result groups:
     * 1 - Push/pop command
     * 2 - push/pop
     * 3 - Memory segment
     * 4 - Command value
     * 5 - Arithmetical/logical command
     */
    private final Pattern patternCompiled = Pattern.compile("((push|pop) (local|argument|static|constant|this|that|that|that) (\\S+))|(add|sub|neg|eq|gt|lt|and|or|not)");


    public Parser(String textFileLocation) {
        try {
            File file = new File(textFileLocation);
            vmFile = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks whether there are lines in the file left
     *
     * @throws IOException Failed reading from file results in an exception
     */
    public boolean hasMoreLines() throws IOException {
        return vmFile.getFilePointer() < vmFile.length();
    }

    /**
     * Move to the next line in parser
     *
     * @throws IOException Failed next line reading results in an exception
     */
    public void advance() throws IOException {
        currentLine = vmFile.readLine().trim();

        lineParts = patternCompiled
            .matcher(currentLine)
            .results()
            .flatMap(mr -> IntStream.rangeClosed(1, mr.groupCount())
                    .mapToObj(mr::group))
            .collect(Collectors.toList());

        // Go to next line if current is insignificant
        if (Objects.equals(currentLine, "") || lineParts.isEmpty() || currentLine.startsWith("//")) {
            advance();
            return;
        }

        lineIndex += 1;
    }


    /**
     * Indicates current command type
     */
    public CommandType commandType() {
        if (lineParts.get(1) != null && lineParts.get(1).equals("push")) return CommandType.C_PUSH;
        if (lineParts.get(1) != null && lineParts.get(1).equals("pop")) return CommandType.C_POP;
        if (lineParts.get(4) != null) return CommandType.C_ARITHMETIC;
        throw new UnsupportedOperationException(String.format("This command is not implemented %s", currentLine));
    }


    /**
     * Returns the first argument of the current command
     */
    public String arg1() {
        if (commandType() == CommandType.C_PUSH || commandType() == CommandType.C_POP) return lineParts.get(2);
        if (commandType() == CommandType.C_ARITHMETIC) return lineParts.get(4);
        throw new IllegalCallerException(String.format("Cannot get first argument of command %s", currentLine));
    }

    /**
     * Returns the second argument of the current command
     */
    public int arg2() {
        if (commandType() == CommandType.C_PUSH || commandType() == CommandType.C_POP) return Integer.parseInt(lineParts.get(3));
        throw new IllegalCallerException(String.format("Cannot get second argument of command %s", currentLine));
    }
}

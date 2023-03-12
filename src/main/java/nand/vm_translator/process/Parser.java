package nand.vm_translator.process;

import nand.vm_translator.process.command.CommandType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class Parser {

    private RandomAccessFile vmFile;

    private String currentLine = ""; // Current line content
    public int lineIndex = 0; // Current line number

    private List<String> lineParts = new ArrayList<>();
    public List<File> filesInPath = new ArrayList<>();
    public String currentFileName = "";

    /**
     * REGEX pattern that is used to retrieve parts of each line
     * Possible result groups:
     * 1 - Push/pop command
     * 2 - push/pop
     * 3 - Memory segment
     * 4 - Command value
     * 5 - Arithmetical/logical command
     * 6 - function definition/call
     * 7 - call/function
     * 8 - functionName
     * 9 - nVars
     * 10 - label/goto/if-goto command
     * 11 - label/goto/if-goto
     * 12 - labelName
     */
    private final Pattern patternCompiled = Pattern.compile("((push|pop) (local|argument|static|constant|this|that|temp|pointer) (\\S+))|(add|sub|neg|eq|gt|lt|and|or|not|return)|((call|function) (\\S+)\\s+(\\d+)?)|((if-goto|goto|label) (\\S+))");


    public Parser(String textFileLocation) {
        File file = new File(textFileLocation);
        if (file.isDirectory() && file.listFiles() != null) {
            // If directory passed, parse all '.vm' files from it. Sys.vm should be first
            Optional<File> sysFile = Arrays.stream(Objects.requireNonNull(file.listFiles())).filter(file1 -> file1.getName().equals("Sys.vm")).findFirst();
            sysFile.ifPresent(value -> filesInPath.add(value));

            for (File child: Objects.requireNonNull(file.listFiles())) {
                if (child.getName().endsWith(".vm") && !child.getName().equals("Sys.vm")) {
                    filesInPath.add(child);
                }
            }
        } else {
            filesInPath.add(file);
        }

        getNextFile();
    }

    /**
     * Checks whether there are lines in the file left. Otherwise, go to the next file in folder if any.
     *
     * @throws IOException Failed reading from file results in an exception
     */
    public boolean hasMoreLines() throws IOException {
        if (vmFile.getFilePointer() < vmFile.length()) {
            return true;
        } else {
            if (! filesInPath.isEmpty()) {
                getNextFile();
                return true;
            } else {
                return false;
            }
        }
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
        if (lineParts.get(1) != null) return CommandType.valueOfLabel(lineParts.get(1));
        if (lineParts.get(4) != null && lineParts.get(4).equals("return")) return CommandType.C_RETURN;
        if (lineParts.get(4) != null) return CommandType.C_ARITHMETIC;
        if (lineParts.get(5) != null) return CommandType.valueOfLabel(lineParts.get(6));
        if (lineParts.get(9) != null) return CommandType.valueOfLabel(lineParts.get(10));
        throw new UnsupportedOperationException(String.format("This command is not implemented %s", currentLine));
    }


    /**
     * Returns the first argument of the current command
     */
    public String arg1() {
        if (commandType() == CommandType.C_PUSH || commandType() == CommandType.C_POP) return lineParts.get(2);
        if (commandType() == CommandType.C_ARITHMETIC) return lineParts.get(4);
        if (commandType() == CommandType.C_FUNCTION || commandType() == CommandType.C_CALL) return lineParts.get(7);
        if (commandType() == CommandType.C_IF || commandType() == CommandType.C_LABEL || commandType() == CommandType.C_GOTO) return lineParts.get(11);
        throw new IllegalCallerException(String.format("Cannot get first argument of command %s", currentLine));
    }

    /**
     * Returns the second argument of the current command
     */
    public int arg2() {
        if (commandType() == CommandType.C_FUNCTION || commandType() == CommandType.C_CALL) return lineParts.get(8) != null ? Integer.parseInt(lineParts.get(8)) : 0;
        if (commandType() == CommandType.C_PUSH || commandType() == CommandType.C_POP) return Integer.parseInt(lineParts.get(3));
        throw new IllegalCallerException(String.format("Cannot get second argument of command %s", currentLine));
    }

    /**
     * Gets next file from filesInPath. Will continue reading from it
     */
    private void getNextFile() {
        try {
            vmFile = new RandomAccessFile(filesInPath.get(0), "r");
            currentFileName = filesInPath.get(0).getName();
            filesInPath.remove(0);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}

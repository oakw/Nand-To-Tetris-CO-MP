package nand.jack_analyzer.process;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class JackTokenizer {

    private RandomAccessFile vmFile;
    public int lineIndex = 0; // Current line number

    public ArrayList<Token> tokens = new ArrayList<>();


    /**
     * REGEX pattern that is used to retrieve first match of asked line
     * Possible result groups:
     * 1 - any first match
     * 2 - keyword
     * 3 - identifier
     * 4 - string constant
     * 5 - integer constant
     * 6 - char symbol
     */
    private final Pattern patternCompiled = Pattern.compile("((class|constructor|function|method|field|static|var|int|char|boolean|void|true|false|null|this|do|if|else|while|return|let)|([a-zA-Z]+)|\"(.*?)\"|(\\d+)|(\\S)).*");

    public List<File> filesInPath = new ArrayList<>();
    public String currentFileName = "";


    public JackTokenizer(String textFileLocation) {
        File file = new File(textFileLocation);
        if (file.isDirectory() && file.listFiles() != null) {
            // If directory passed, parse all '.jack' files from it
            for (File child: Objects.requireNonNull(file.listFiles())) {
                if (child.getName().endsWith(".jack")) {
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
     * Move to the next line in parser and tokenize it, adding all to the list of Tokens
     *
     * @throws IOException Failed next line reading results in an exception
     */
    public void tokenizeLine() throws IOException {
        lineIndex += 1;
        // Current line content
        String currentLine = vmFile.readLine();
        if (currentLine == null) {
            return;
        }
        currentLine = currentLine.trim();

        List<String> lineParts;

        // Go to next line if current is insignificant
        if (Objects.equals(currentLine, "") || currentLine.startsWith("/") || currentLine.startsWith("*")) {
            tokenizeLine();
            return;
        }


        while(currentLine.length() > 0) {
            currentLine = currentLine.stripLeading();

            if (Objects.equals(currentLine, "") || currentLine.startsWith("//")) {
                break;
            }

            lineParts = getMatches(currentLine);
            Token token;

            if (lineParts.get(1) != null) {
                // Keyword
                token = new Keyword(lineParts.get(1));
                currentLine = getAllExceptFirst(currentLine, lineParts.get(1));

            } else if (lineParts.get(2) != null) {
                // Identifier
                token = new Identifier(lineParts.get(2));
                currentLine = getAllExceptFirst(currentLine, lineParts.get(2));

            } else if (lineParts.get(3) != null) {
                // String constant
                token = new StringConstant(lineParts.get(3));
                currentLine = getAllExceptFirst(currentLine, lineParts.get(0));

            } else if (lineParts.get(4) != null) {
                // Integer constant
                token = new IntegerConstant(lineParts.get(4));
                currentLine = getAllExceptFirst(currentLine, lineParts.get(4));

            } else if (lineParts.get(5) != null){
                // Symbol, maybe
                token = new Symbol(currentLine.charAt(0));
                currentLine = getAllExceptFirstChar(currentLine);

                if (token.isNull) {
                    continue;
                }
            } else {
                continue;
            }

            token.originFilePath = currentFileName;
            token.lineNumber = lineIndex;
            tokens.add(token);
        }

    }

    private List<String> getMatches(String line) {
        return patternCompiled
                .matcher(line)
                .results()
                .flatMap(mr -> IntStream.rangeClosed(1, mr.groupCount())
                        .mapToObj(mr::group))
                .collect(Collectors.toList());
    }

    /**
     * Gets next file from filesInPath. Will continue reading from it
     */
    private void getNextFile() {
        try {
            vmFile = new RandomAccessFile(filesInPath.get(0), "r");
            lineIndex = 0;
            currentFileName = filesInPath.get(0).getAbsolutePath();
            filesInPath.remove(0);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the provided string without the first at the beginning
     * @param severalWords Where to remove from
     * @param first What to remove
     * @return Shortened string
     */
    private String getAllExceptFirst(String severalWords, String first) {
        if (severalWords.length() > first.length()) {
            return severalWords.substring(severalWords.indexOf(first) + first.length());
        } else {
            return "";
        }
    }

    /**
     * Returns the provided string without the first char at the beginning
     */
    private String getAllExceptFirstChar(String severalWords) {
        return severalWords.substring(1);
    }

}

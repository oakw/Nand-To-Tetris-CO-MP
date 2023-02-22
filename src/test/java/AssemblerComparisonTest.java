import nand.assembler.HackAssembler;
import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;


public class AssemblerComparisonTest {
    public static final String inputDir = "./src/test/resources/";
    public static final String outputDir = "./src/test/tempOutput/";

    @BeforeAll
    // Create temp folder for outputted files
    public static void setUpTestFolder() {
        new File(outputDir).mkdirs();
    }

    @Test
    @DisplayName("Comparing Rect file to nand2tetris version")
    public void testRect() throws Exception {
        final String rectAsmFile = inputDir + "Rect.asm";
        final String rectHackFile = inputDir + "Rect.hack";
        final String rectHackOutputFile = outputDir + "Rect.hack";

        HackAssembler.translate(rectAsmFile, rectHackOutputFile);
        long failedLine = filesCompareByLine(Paths.get(rectHackFile), Paths.get(rectHackOutputFile));

        Assertions.assertEquals(-1, failedLine, "comparison failed in line " + failedLine);
    }

    @Test
    @DisplayName("Comparing Add file to nand2tetris version")
    public void testAdd() throws Exception {
        final String addAsmFile = inputDir + "Add.asm";
        final String addHackFile = inputDir + "Add.hack";
        final String addHackOutputFile = outputDir + "Add.hack";

        HackAssembler.translate(addAsmFile, addHackOutputFile);
        long failedLine = filesCompareByLine(Paths.get(addHackFile), Paths.get(addHackOutputFile));

        Assertions.assertEquals(-1, failedLine, "comparison failed in line " + failedLine);
    }

    @Test
    @DisplayName("Comparing Max file to nand2tetris version")
    public void testMax() throws Exception {
        final String maxAsmFile = inputDir + "Max.asm";
        final String maxHackFile = inputDir + "Max.hack";
        final String maxHackOutputFile = outputDir + "Max.hack";

        HackAssembler.translate(maxAsmFile, maxHackOutputFile);
        long failedLine = filesCompareByLine(Paths.get(maxHackFile), Paths.get(maxHackOutputFile));

        Assertions.assertEquals(-1, failedLine, "comparison failed in line " + failedLine);
    }

    @Test
    @DisplayName("Comparing Pong file to nand2tetris version")
    public void testPong() throws Exception {
        final String pongAsmFile = inputDir + "Pong.asm";
        final String pongHackFile = inputDir + "Pong.hack";
        final String pongHackOutputFile = outputDir + "Pong.hack";

        HackAssembler.translate(pongAsmFile, pongHackOutputFile);
        long failedLine = filesCompareByLine(Paths.get(pongHackFile), Paths.get(pongHackOutputFile));

        Assertions.assertEquals(-1, failedLine, "comparison failed in line " + failedLine);
    }

    @AfterAll
    // Delete temp folder of output files
    public static void flushTestFolder() throws IOException {
        Files.walk(Paths.get(outputDir))
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    // Copied from https://www.baeldung.com/java-compare-files
    public static long filesCompareByLine(Path path1, Path path2) throws IOException {
        try (BufferedReader bf1 = Files.newBufferedReader(path1);
             BufferedReader bf2 = Files.newBufferedReader(path2)) {

            long lineNumber = 1;
            String line1, line2;
            while ((line1 = bf1.readLine()) != null) {
                line2 = bf2.readLine();
                if (line2 == null || !line1.equals(line2)) {
                    return lineNumber;
                }
                lineNumber++;
            }
            if (bf2.readLine() == null) {
                return -1;
            }
            else {
                return lineNumber;
            }
        }
    }

}

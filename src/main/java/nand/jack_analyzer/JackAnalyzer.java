package nand.jack_analyzer;
import nand.jack_analyzer.process.CompilationEngine;
import nand.jack_analyzer.process.JackTokenizer;
import nand.jack_analyzer.process.Token;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

// This part of project sometimes repeats the assembler,
// but the separation of both is done intentionally to make the submission more examinable
public class JackAnalyzer {

    public static void translate(String inputFile, String outputFile) throws Exception {
        JackTokenizer jackTokenizer = new JackTokenizer(inputFile);

        BufferedWriter outputWriter = new BufferedWriter(new FileWriter(outputFile));

        // Set stack pointer to 256 initially. Bootstrap code.
        outputWriter.write("""

                """);

        while (jackTokenizer.hasMoreLines()) {
            jackTokenizer.tokenizeLine();


        }
        ArrayList<Token> tokens = jackTokenizer.tokens;
        CompilationEngine compilationEngine = new CompilationEngine(tokens);

        compilationEngine.compileAll();

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(compilationEngine.doc);
        StreamResult result = new StreamResult(new File("test.xml"));
        transformer.transform(source, result);

        // Output to console for testing
        StreamResult consoleResult = new StreamResult(System.out);
        transformer.transform(source, consoleResult);

        outputWriter.close();
    }
}

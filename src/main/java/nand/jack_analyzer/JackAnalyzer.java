package nand.jack_analyzer;
import nand.jack_analyzer.process.CompilationEngine;
import nand.jack_analyzer.process.JackTokenizer;

// TODO: Add comments for improved readability
public class JackAnalyzer {

    public static void translate(String inputFile) throws Exception {
        JackTokenizer jackTokenizer = new JackTokenizer(inputFile);

        while (jackTokenizer.hasMoreLines()) {
            jackTokenizer.tokenizeLine();
        }

        CompilationEngine compilationEngine = new CompilationEngine(jackTokenizer);
        compilationEngine.compileAll();
    }
}

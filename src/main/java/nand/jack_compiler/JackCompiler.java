package nand.jack_compiler;

import nand.jack_compiler.process.CompilationEngine;
import nand.jack_analyzer.process.JackTokenizer;

import java.io.File;

// TODO: Add comments for improved readability
public class JackCompiler {

    public static void translate(String inputFile) throws Exception {
        JackTokenizer jackTokenizer = new JackTokenizer(inputFile);

        while (jackTokenizer.hasMoreLines()) {
            jackTokenizer.tokenizeLine();
        }

        nand.jack_analyzer.process.CompilationEngine analyzerEngine = new nand.jack_analyzer.process.CompilationEngine(jackTokenizer);
        analyzerEngine.compileAll();

        CompilationEngine compilationEngine = new CompilationEngine(analyzerEngine.fileNames);
        compilationEngine.compileAll();

        // Delete temporarily generated xml files
        analyzerEngine.fileNames.forEach(jackFilePath -> {
            new File(jackFilePath.replace(".jack", ".xml")).delete();
        });
    }
}

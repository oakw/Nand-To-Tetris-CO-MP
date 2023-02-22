package nand.assembler;

import nand.assembler.process.*;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class HackAssembler {
    final static public int MAX_MEMORY_SIZE = 32767;

    public static void translate(String inputFile, String outputFile) throws Exception {
        Parser parser = new Parser(inputFile);
        Code code = new Code();
        SymbolTable symbolTable = new SymbolTable();

        BufferedWriter outputWriter = new BufferedWriter(new FileWriter(outputFile));

        // Loop through all lines to save locations of L_INSTRUCTIONs
        while (parser.hasMoreLines()) {
            parser.advance(false);

            if (parser.instructionType() == InstructionType.L_INSTRUCTION) {
                if (!symbolTable.containsVariable(parser.symbol())) {
                    symbolTable.addLineEntry(parser.symbol(), parser.lineIndex);
                }
            }
        }

        // Reset parser to the beginning of a file
        parser.reset();

        // Loop through all lines to translate to binary
        while (parser.hasMoreLines()) {
            parser.advance(true);
            String word;

            if (parser.instructionType() == InstructionType.C_INSTRUCTION) {
                 word = code.getCWord(parser.comp(), parser.dest(), parser.jump());

            } else { // A_INSTRUCTION
                try {
                    // Assumes the A instruction points to number (e.g. @314)
                    word = code.getAWord(Integer.parseInt(parser.symbol()));

                } catch (NumberFormatException e) {
                    // Actually the A instruction points to symbol (e.g. @LOOP)

                    if (!symbolTable.containsVariable(parser.symbol())) {
                        // Creates a new entry in symbol table
                        symbolTable.addEntry(parser.symbol());
                    }

                    word = code.getAWord(symbolTable.getAddress(parser.symbol()));
                }
            }

            outputWriter.write(String.format("%s\n", word));
        }

        outputWriter.close();
    }
}

package nand.vm_translator;
import nand.vm_translator.process.*;
import nand.vm_translator.process.command.CommandType;

import java.io.BufferedWriter;
import java.io.FileWriter;

// This part of project sometimes repeats the assembler,
// but the separation of both is done intentionally to make the submission more examinable
public class VMTranslator {

    public static void translate(String inputFile, String outputFile) throws Exception {
        Parser parser = new Parser(inputFile);
        Code code = new Code();

        BufferedWriter outputWriter = new BufferedWriter(new FileWriter(outputFile));

        // Set stack pointer to 256 initially
        outputWriter.write("""
                // Set stack pointer to 256
                @256
                D=A
                @SP
                M=D
                """);

        while (parser.hasMoreLines()) {
            String line = "";
            parser.advance();

            if (parser.commandType() == CommandType.C_ARITHMETIC) {
                line = code.getArithmetic(parser.arg1());
            } else if (parser.commandType() == CommandType.C_PUSH || parser.commandType() == CommandType.C_POP){
                line = code.getPushPop(parser.commandType(), parser.arg1(), parser.arg2());
            }

            outputWriter.write(String.format("%s\n", line));
        }

        outputWriter.write("""
                // Script ended loop
                (SCRIPT_ENDED)
                @SCRIPT_ENDED
                0;JMP
                """);

        outputWriter.close();
    }
}

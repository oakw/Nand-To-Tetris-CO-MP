package nand.vm_translator;
import nand.vm_translator.process.*;
import nand.vm_translator.process.command.SegmentType;

import java.io.BufferedWriter;
import java.io.FileWriter;

// This part of project sometimes repeats the assembler,
// but the separation of both is done intentionally to make the submission more examinable
public class VMTranslator {

    public static void translate(String inputFile, String outputFile) throws Exception {
        Parser parser = new Parser(inputFile);
        Code code = new Code();

        BufferedWriter outputWriter = new BufferedWriter(new FileWriter(outputFile));

        // Set stack pointer to 256 initially. Bootstrap code.
        outputWriter.write("""
                // Set stack pointer to 256
                @256
                D=A
                @SP
                M=D
                """);

        // Sys.init is executed first
        if (parser.currentFileName.equals("Sys.vm")) {
            outputWriter.write(code.getFunctionCall("Sys.init", 0));
        }

        while (parser.hasMoreLines()) {
            String lines = "";
            parser.advance();

            switch (parser.commandType()) {
                case C_ARITHMETIC -> lines = code.getArithmetic(parser.arg1());
                case C_PUSH, C_POP -> lines = code.getPushPop(parser.commandType(), SegmentType.valueOfLabel(parser.arg1()), parser.arg2(), false, parser.currentFileName);
                case C_LABEL -> lines = code.getLabel(parser.arg1());
                case C_GOTO -> lines = code.getGoTo(parser.arg1());
                case C_IF -> lines = code.getIfGoTo(parser.arg1());
                case C_FUNCTION -> lines = code.getFunctionDef(parser.arg1(), parser.arg2());
                case C_CALL -> lines = code.getFunctionCall(parser.arg1(), parser.arg2());
                case C_RETURN -> lines = code.getReturn();
            }

            outputWriter.write(String.format("%s\n", lines));
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

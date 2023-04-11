package nand.jack_compiler.process;

import nand.vm_translator.process.command.ArithmeticType;
import nand.vm_translator.process.command.CommandType;
import nand.vm_translator.process.command.SegmentType;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class VMWriter {
    private BufferedWriter vmFile;

    public VMWriter(String outputFilePath) throws IOException {
        vmFile = new BufferedWriter(new FileWriter(outputFilePath));
    }

    // Writes a VM push command
    public void writePush(SegmentType segment, int index) throws IOException {
        vmFile.write(String.format("push %s %s\n", segment.label, index));
    }


    // Writes a VM pop command
    public void writePop(SegmentType segment, int index) throws IOException {
        vmFile.write(String.format("pop %s %s\n", segment.label, index));
    }

    // Writes a VM arithmetic-logical command
    public void writeArithmetic(ArithmeticType command) throws IOException {
        vmFile.write(command.label + "\n");
    }

    // Writes a VM label command
    public void writeLabel(String label) throws IOException {
        vmFile.write(String.format("%s %s\n", CommandType.C_LABEL.label, label));
    }

    // Writes a VM goto command
    public void writeGoto(String label) throws IOException {
        vmFile.write(String.format("%s %s\n", CommandType.C_GOTO.label, label));
    }

    // Writes a VM if-goto command
    public void writeIf(String label) throws IOException {
        vmFile.write(String.format("%s %s\n", CommandType.C_IF.label, label));
    }

    // Writes a VM call command
    public void writeCall(String label, int nArgs) throws IOException {
        vmFile.write(String.format("%s %s %s\n", CommandType.C_CALL.label, label, nArgs));
    }

    // Writes a VM function command
    public void writeFunction(String functionName, int nVars) throws IOException {
        vmFile.write(String.format("%s %s %s\n", CommandType.C_FUNCTION.label, functionName, nVars));
    }

    // Writes a VM return command
    public void writeReturn() throws IOException {
        vmFile.write(String.format("%s\n", CommandType.C_RETURN.label));
    }

    // Closes the output file / stream
    public void close() throws IOException {
        vmFile.close();
    }
}
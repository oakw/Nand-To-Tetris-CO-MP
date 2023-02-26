package nand.vm_translator.process;

import nand.vm_translator.process.command.ArithmeticType;
import nand.vm_translator.process.command.CommandType;
import nand.vm_translator.process.command.SegmentType;

public class Code {

    int arithmeticOperationCount = 0;

    /**
     * Responsible for command translation to ASM instructions
     */
    public Code() {}

    /**
     * Returns the whole code of supplied stack arithmetic operation
     */
    public String getArithmetic(String command) {
        ArithmeticType operation = ArithmeticType.valueOfLabel(command);
        arithmeticOperationCount += 1;

        if (operation == null) {
            return "";
        }

        // Arithmetic operations start by decrementing stack pointer and end by incrementing it
        return String.format("""
                // %s
                @SP
                AM=M-1
                %s
                @SP
                M=M+1""", operation.label, getDynamicArithmetic(operation));
    }

    /**
     * Returns the whole code of supplied pop/push operation
     */
    public String getPushPop(CommandType commandType, String commandSegment, int index) {
        String valueInstruction = "D=M"; // In default, the operation value is located in some memory location
        SegmentType segment = SegmentType.valueOfLabel(commandSegment);

        if (segment == null) {
            return "";
        } else if (segment == SegmentType.S_CONSTANT) {
            valueInstruction = "D=A"; // If constant passed, the operation value is located in the A register
        } else {
            index = getMemoryLocation(segment, index);
        }

        switch (commandType) {
            case C_PUSH:
                return String.format("""
                    // %1$s %2$s %3$s
                    @%3$s
                    %4$s
                    @SP
                    A=M
                    M=D
                    @SP
                    M=M+1""", commandType.label, segment.label, index, valueInstruction);
            case C_POP:
                return String.format("""
                    // %1$s %2$s %3$s
                    @SP
                    A=M-1
                    D=M
                    M=0
                    @%3$s
                    M=D
                    @SP
                    M=M-1""", commandType.label, segment, index);
            default: return "";
        }
    }


    private int getMemoryLocation(SegmentType segment, int index) {
        // TODO: finish

        switch (segment) {
            default: return segment.startIndex + index;
        }
    }

    /**
     * Returns the changing part of arithmetic operation
     */
    private String getDynamicArithmetic(ArithmeticType operation) {
        switch (operation) {
            case A_NOT: return "M=!M";
            case A_NEG: return "M=-M";
            case A_ADD: case A_SUB: case A_AND: case A_OR: return arithmeticCode(operation);
            default: return comparisonCode(operation);
        }
    }


    /**
     * Code that simulates less than/grater than/equals operations
     */
    private String comparisonCode(ArithmeticType operation) {
        return String.format(
                """
                D=M
                M=0
                @SP
                AM=M-1
                D=M-D
                @%2$s_%1$s
                D;J%2$s
                @SP
                A=M
                M=0
                @END_%2$s_%1$s
                0;JMP
                (%2$s_%1$s)
                @SP
                A=M
                M=-1
                (END_%2$s_%1$s)""", arithmeticOperationCount, operation.operand);
    }

    /**
     * Code that simulates add/subtract/and/or arithmetic operations
     */
    private String arithmeticCode(ArithmeticType operation) {
        return String.format("""
                D=M
                M=0
                @SP
                AM=M-1
                M=M%sD""", operation.operand);
    }
}

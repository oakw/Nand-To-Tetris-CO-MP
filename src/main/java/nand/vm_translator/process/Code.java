package nand.vm_translator.process;

import nand.vm_translator.process.command.ArithmeticType;
import nand.vm_translator.process.command.CommandType;
import nand.vm_translator.process.command.SegmentType;
import java.util.Arrays;


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
        String valueInstruction = "D=M"; // By default, memory location is based on value in another field
        SegmentType segment = SegmentType.valueOfLabel(commandSegment);

        if (segment == null) {
            return "";
        } else if (segment == SegmentType.S_CONSTANT) {
            return pushConstantCode(commandType, segment, index);
        } else if (Arrays.asList(SegmentType.S_TEMP, SegmentType.S_STATIC, SegmentType.S_POINTER).contains(segment)) {
            valueInstruction = "D=A"; // Memory location is based on index provided
        }

        switch (commandType) {
            case C_PUSH:
                return String.format("""
                        // %s %s %3$s
                        @%4$s
                        %5$s
                        @%3$s
                        A=D+A
                        D=M
                        @SP
                        A=M
                        M=D
                        @SP
                        M=M+1""", commandType.label, segment.label, index, segment.startIndex, valueInstruction);
            case C_POP:
                return String.format("""
                        // %s %s %3$s
                        @%4$s
                        %5$s
                        @%3$s
                        D=D+A
                        // Save the location temporarily
                        @R15
                        M=D
                        @SP
                        A=M-1
                        D=M
                        M=0
                        @R15
                        A=M
                        M=D
                        @SP
                        M=M-1""", commandType.label, segment.label, index, segment.startIndex, valueInstruction);
            default:
                return "";
        }
    }

    /**
     * Returns code of pushed constant
     */
    private String pushConstantCode(CommandType commandType, SegmentType segment, int constant) {
        return String.format("""
                    // %s %s %3$s
                    @%3$s
                    D=A
                    @SP
                    A=M
                    M=D
                    @SP
                    M=M+1""", commandType.label, segment.label, constant);
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

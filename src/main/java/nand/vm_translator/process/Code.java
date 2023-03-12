package nand.vm_translator.process;

import nand.vm_translator.process.command.ArithmeticType;
import nand.vm_translator.process.command.CommandType;
import nand.vm_translator.process.command.SegmentType;
import java.util.Arrays;
import java.util.List;


public class Code {

    int arithmeticOperationCount = 0;
    int functionCallCount = 0;

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
    public String getPushPop(CommandType commandType, SegmentType segment, int index, boolean movingPointer, Object... additionalParams) {
        String valueInstruction = "D=M"; // By default, memory location is based on value in another field

        if (segment == null) {
            return "";
        } else if (segment == SegmentType.S_CONSTANT) {
            return pushConstantCode(commandType, segment, Integer.toString(index));
        } else if (Arrays.asList(SegmentType.S_TEMP, SegmentType.S_STATIC, SegmentType.S_POINTER).contains(segment)) {
            valueInstruction = "D=A"; // Memory location is based on index provided
        }

        switch (commandType) {
            case C_PUSH:
                StringBuilder push = new StringBuilder();

                // Comment and pointer selection
                push.append(String.format("""
                        // %s %s %3$s
                        @%4$s
                        """, commandType.label, segment.label, index, segment.startIndex));

                if (segment.equals(SegmentType.S_STATIC)) {
                    // Static variable is referenced using filename on push
                    String vmFile = (String) additionalParams[0];
                    push.append(String.format("@%s.%s\n", vmFile.replace(".vm", ""), index));
                } else if (! movingPointer) {
                    // Push is used differently by caller
                    push.append(String.format("""
                        %1$s
                        @%2$s
                        A=D+A
                        """, valueInstruction, index));
                }

                // Change value and increment SP
                push.append("""                        
                        D=M
                        @SP
                        A=M
                        M=D
                        @SP
                        M=M+1
                        """);

                return push.toString();
            case C_POP:
                if (segment.equals(SegmentType.S_STATIC)) {
                    // Static variable ir referenced using filename on pop
                    String vmFile = (String) additionalParams[0];
                    return String.format("""
                        // %s %s %3$s
                        @SP
                        A=M-1
                        D=M
                        @%4$s.%3$s
                        M=D
                        @SP
                        M=M-1""", commandType.label, segment.label, index, vmFile.replace(".vm", ""));
                } else {
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
                        @R15
                        A=M
                        M=D
                        @SP
                        M=M-1""", commandType.label, segment.label, index, segment.startIndex, valueInstruction);
                }
            default:
                return "";
        }
    }


    /**
     * Create label
     */
    public String getLabel(String label) {
        return String.format("""
                // label %1$s
                (%1$s)""", label);
    }


    /**
     * Get goto code for label
     */
    public String getGoTo(String label) {
        return String.format("""
                // goto %1$s
                @%1$s
                0;JMP""", label);
    }


    /**
     * Get if-goto code for label
     */
    public String getIfGoTo(String label) {
        return String.format("""
                // if goto %1$s
                @SP
                AM=M-1
                D=M
                @%1$s
                D;JNE""", label);
    }


    /**
     * Create return. Get back caller frame content to parent memory
     */
    public String getReturn() {
        StringBuilder lines = new StringBuilder("""
                // return
                // save function stack location at R13
                @LCL
                D=M
                @R13
                M=D
                // save return address at R14
                @5
                A=D-A
                D=M
                @R14
                M=D
                // pop returned value to ARG place
                @SP
                A=M-1
                D=M
                @ARG
                A=M
                M=D
                @ARG
                D=M+1
                @SP
                M=D
                """);

        // Move previously saved pointers back in place
        List<String> pointersToMove = Arrays.asList("THAT", "THIS", "ARG", "LCL");
        int backIndex = 0;
        for (String pointer: pointersToMove) {
            backIndex++;
            lines.append(String.format("""
                    // moving %1$s back
                    @R13
                    D=M
                    @%2$s
                    A=D-A
                    D=M
                    @%1$s
                    M=D
                    """, pointer, backIndex));
        }

        // Go to return address
        lines.append("""
                @R14
                A=M
                0;JMP
                """);

        return lines.toString();
    }


    /**
     * Create caller frame for a function
     */
    public String getFunctionCall(String functionName, int nArgs) {
        functionCallCount += 1;
        String returnAddr = String.format("return-addr-%s-%s", functionName, functionCallCount);

        return String.format("""
                // call function %s %s
                %3$s
                %4$s
                %5$s
                %6$s
                %7$s
                // argument calculation and placement
                @5
                D=A
                @%2$s
                D=D+A
                @SP
                D=M-D
                @ARG
                M=D
                // local calculation and placement
                @SP
                D=M
                @LCL
                M=D
                // Redirect to function
                @%1$s
                0;JMP
                (%8$s)""", functionName, nArgs,
                pushConstantCode(CommandType.C_PUSH, SegmentType.S_CONSTANT, returnAddr),
                getPushPop(CommandType.C_PUSH, SegmentType.S_LOCAL, 0, true),
                getPushPop(CommandType.C_PUSH, SegmentType.S_ARGUMENT, 0, true),
                getPushPop(CommandType.C_PUSH, SegmentType.S_THIS, 0, true),
                getPushPop(CommandType.C_PUSH, SegmentType.S_THAT, 0, true),
                returnAddr
        );
    }

    /**
     * Get header of function like '(functionName)'
     */
    public String getFunctionDef(String functionName, int nArgs) {
        return String.format("""
                // function %1$s %2$s
                (%1$s)""", functionName, nArgs);
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
     * Returns code of pushed constant (can also be labeled)
     */
    private String pushConstantCode(CommandType commandType, SegmentType segment, String constant) {
        return String.format("""
                    // %s %s %3$s
                    @%3$s
                    D=A
                    @SP
                    A=M
                    M=D
                    @SP
                    M=M+1""", commandType.label, constant.matches("-?\\d+") ? segment.label : "label", constant);
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

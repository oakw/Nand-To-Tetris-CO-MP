package nand.assembler.process;

import static nand.assembler.HackAssembler.MAX_MEMORY_SIZE;

public class Code {

    /**
     * Responsible for instruction translation to binary
     */
    public Code() {}

    /**
     * A instruction in binary
     * 0 v v v v v v v v v v v v v v v v
     */
    public String getAWord(Integer constant) throws Exception {
        if (constant > MAX_MEMORY_SIZE) {
            throw new Exception(String.format("Too large memory location: %s", constant));
        }
        StringBuilder word = new StringBuilder();
        String binary = Integer.toString(constant, 2);

        word.append('0');
        word.append("0".repeat(15 - binary.length())).append(binary);

        return word.toString();
    }


    /**
     * C instruction in binary
     * 1 1 1 a c c c c c c d d d j j j
     */
    public String getCWord(String compInstr, String destInst, String jumpInst) {
        String comp1 = comp(compInstr);
        String dest = dest(destInst);
        String jump = jump(jumpInst);
        char comp0 = compFirst(compInstr);

        StringBuilder word = new StringBuilder();

        word.append("111");
        word.append(comp0);
        word.append(comp1);
        word.append(dest);
        word.append(jump);

        return word.toString();
    }


    /**
     * Returns dest string of X's in   1 1 1 v v v v v v v X X X v v v
     */
    public String dest(String instruction) {
        char[] destAr = {'0', '0', '0'};

        if (instruction != null) {
            destAr[0] = instruction.contains("A") ? '1': '0';
            destAr[1] = instruction.contains("D") ? '1': '0';
            destAr[2] = instruction.contains("M") ? '1': '0';
        }

        return new String(destAr);
    }

    /**
     * Returns jump string of X's in   1 1 1 v v v v v v v v v v X X X
     */
    public String jump(String instruction) {
        if (instruction == null) {
            return "000";
        }

        return switch (instruction) {
            case "JGT" -> "001";
            case "JEQ" -> "010";
            case "JGE" -> "011";
            case "JLT" -> "100";
            case "JNE" -> "101";
            case "JLE" -> "110";
            case "JMP" -> "111";
            default ->
                    throw new UnsupportedOperationException(String.format("%s is an unknown jump part of an instruction", instruction));
        };
    }

    /**
     * Returns comp first bit char of X in   1 1 1 X v v v v v v v v v v v v
     */
    public char compFirst(String instruction) {
        return instruction.contains("M") ? '1' : '0';
    }

    /**
     * Returns comp string of X's in   1 1 1 v X X X X X X v v v v v v
     */
    public String comp(String instruction) {
        if (instruction == null) {
            return "000000";
        }

        return switch (instruction) {
            case "0"            -> "101010";
            case "1"            -> "111111";
            case "-1"           -> "111010";
            case "D"            -> "001100";
            case "M", "A"       -> "110000";
            case "!D"           -> "001101";
            case "!M", "!A"     -> "110001";
            case "-D"           -> "001111";
            case "-M", "-A"     -> "110011";
            case "D+1"          -> "011111";
            case "A+1", "M+1"   -> "110111";
            case "D-1"          -> "001110";
            case "A-1", "M-1"   -> "110010";
            case "D+M", "D+A", "M+D", "A+D" -> "000010";
            case "D-M", "D-A"   -> "010011";
            case "M-D", "A-D"   -> "000111";
            case "D&M", "D&A", "M&D", "A&D" -> "000000";
            case "D|M", "D|A", "M|D", "A|D" -> "010101";
            default ->
                    throw new UnsupportedOperationException(String.format("%s is an unknown comp part of an instruction", instruction));
        };
    }
}

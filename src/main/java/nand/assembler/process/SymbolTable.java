package nand.assembler.process;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private final Map<String, Integer> symbolTable = new HashMap<>();

    private int addedSymbols = 0;
    private int availableMemoryLocation = 16; // Additional variables are stored in location 16 and up

    public SymbolTable() {
        addDefaultSymbols();
    }

    /**
     * Checks if symbol in symbol table
     *
     * @param symbol Name in the symbol table
     */
    public boolean containsVariable(String symbol) {
        return symbolTable.containsKey(symbol);
    }

    /**
     * Gets address of symbol
     *
     * @param symbol Name in the symbol table
     */
    public int getAddress(String symbol) {
        return symbolTable.get(symbol);
    }

    /**
     * Save location (line number) of L_INSTRUCTION to recall it later when @location used
     *
     * @param symbol Name in the symbol table
     * @param lineIndex Line number where L_INSTRUCTION occurs
     */
    public void addLineEntry(String symbol, Integer lineIndex) {
        symbolTable.put(symbol, lineIndex - addedSymbols - 1);

        // Added line entries need to be counted as the actual end line number depends on occurrences of L instructions
        addedSymbols += 1;
    }

    /**
     * Add an entry in symbol table in the next available memory location after 16
     *
     * @param symbol Name in the symbol table
     */
    public void addEntry(String symbol) {
        symbolTable.put(symbol, availableMemoryLocation);
        availableMemoryLocation += 1;
    }

    /**
     * Initializes initial default symbols
     */
    private void addDefaultSymbols() {
        for (int i = 0; i <= 15; i++) {
            symbolTable.put(String.format("R%s", i), i);
        }
        symbolTable.put("SCREEN", 16384);
        symbolTable.put("KBD", 24576);
        symbolTable.put("SP", 0);
        symbolTable.put("LCL", 1);
        symbolTable.put("ARG", 2);
        symbolTable.put("THIS", 3);
        symbolTable.put("THAT", 4);
    }

}

package nand;

import nand.assembler.HackAssembler;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<String> cmdArguments = Arrays.asList(args);

        if (cmdArguments.isEmpty()) {
            usage();
            return;
        }

        String inputFile = "";
        String outputFile = "";

        for (int i = 0; i < cmdArguments.size(); i++){
            switch (cmdArguments.get(i)) {
                case "-i", "--input" -> inputFile = i + 1 != cmdArguments.size() ? cmdArguments.get(i + 1) : "";
                case "-o", "--output" -> outputFile = i + 1 != cmdArguments.size() ? cmdArguments.get(i + 1) : "";
                case "-h", "--help" -> {
                    usage();
                    return;
                }
            }
        }

        if (inputFile.isEmpty() || outputFile.isEmpty()) {
            System.out.println("No input or output file specified");
            usage();
            return;
        }

        try {
            System.out.println("Translation started\r");
            HackAssembler.translate(inputFile, outputFile);
            System.out.printf("Translation ended. Output in file %s%n", outputFile);

        } catch (FileNotFoundException e) {
            System.err.println("Specified file is not found");

        } catch (Exception e) {
            System.err.println("Failed to perform translation");
            e.printStackTrace();
        }
    }

    private static void usage() {
        System.out.println("""
                    Assembler of Hack machine language
                    Based on https://www.nand2tetris.org/project06
                    Made by Martins P, 2023
                     
                    Arguments:
                       [-i] | --input      : filename of input (.asm) file
                       [-o] | --output     : filename of output (.hack) file
                       -h   | --help       : display this help message
                    """);
    }
}
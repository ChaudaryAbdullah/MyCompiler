package example;

import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java main <filename>.sa");
            return;
        }
        
        String filename = args[0];
        if (!filename.endsWith(".sa")) {
            System.out.println("Error: File must have a .sa extension");
            return;
        }
        
        try {
            
            String sourceCode = readFile(filename);
            System.out.println("Source Code:");
            System.out.println("─".repeat(40));
            System.out.println(sourceCode);
            
            Lexer lexer = new Lexer(sourceCode);
            List<Token> tokens = lexer.tokenize();

            System.out.println("\nTokens:");
            System.out.println("─".repeat(40));
            for (Token token : tokens) {
                System.out.println(token);
            }

            AutomataVisualizer.displayAllAutomata(tokens);

            SymbolTable symbolTable = new SymbolTable();
            symbolTable.populateFromTokens(tokens);
            symbolTable.printTable();
            
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }
    
    private static String readFile(String filename) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
}
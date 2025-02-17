package example;

import java.util.ArrayList;
import java.util.List;

class SymbolTable {
    private List<SymbolTableEntry> table;

    public SymbolTable() {
        this.table = new ArrayList<>();
    }

    public void addEntry(String name, String type, String scope, String value) {
        table.add(new SymbolTableEntry(name, type, scope, value));
    }

    public void populateFromTokens(List<Token> tokens) {
        for (Token token : tokens) {
            switch (token.getType()) {
	            case Token.DATATYPE:
	                addEntry(token.getValue(), "DATATYPE", "Global", "-");
	                break;
                case Token.KEYWORD:
                    addEntry(token.getValue(), "Keyword", "Global", "-");
                    break;
                case Token.IDENTIFIER:
                    addEntry(token.getValue(), "Identifier", "Local", "-");
                    break;
                case Token.INTEGER:
                    addEntry(token.getValue(), "Integer", "Constant", token.getValue());
                    break;
                case Token.DECIMAL:
                    addEntry(token.getValue(), "Decimal", "Constant", token.getValue());
                    break;
                case Token.BOOLEAN:
                    addEntry(token.getValue(), "Boolean", "Constant", token.getValue());
                    break;
                case Token.STRING:
                    addEntry(token.getValue(), "String", "Global", token.getValue());
                    break;
                case Token.OPERATOR:
                    addEntry(token.getValue(), "Operator", "Global", "-");
                    break;
                case Token.CHARACTER:
                    addEntry(token.getValue(), "CHARACTER", "Local", "-");
                    break;
                case Token.PUNCTUATOR:
                    addEntry(token.getValue(), "Punctuator", "Global", "-");
                    break;
                case Token.OUTPUT:
                    addEntry(token.getValue(), "OUTPUT", "Global", "-");
                    break;
                case Token.INPUT:
                    addEntry(token.getValue(), "INPUT", "Global", "-");
                    break;
                case Token.EOF:
                    // No need to add EOF to the table
                    break;
                default:
                    addEntry(token.getValue(), "Unknown", "Unknown", "-");
                    break;
            }
        }
    }

    public void printTable() {
        System.out.println("\nSymbol Table:");
        System.out.println("---------------------------------------------------");
        System.out.println("| Name       | Type       | Scope    | Value      |");
        System.out.println("---------------------------------------------------");
        for (SymbolTableEntry entry : table) {
            System.out.printf("| %-10s | %-10s | %-8s | %-10s |\n",
                entry.name, entry.type, entry.scope, entry.value);
        }
        System.out.println("---------------------------------------------------");
    }
}

package example;

import java.util.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

class Lexer {
    private String input;
    private int position;
    private int length;
    private int stateCounter;
    private Stack<Set<String>> scopeStack;
    private Set<String> globalVariables;
    private int lineNumber;
    private int charPosition;

    private static final Set<String> KEYWORDS = Set.of("read", "write", "if", "else", "loop", "end");
    private static final Set<String> DATATYPE = Set.of("num", "bool", "char", "dec");
    private static final Set<String> BOOLEAN_VALUES = Set.of("true", "false");
    private static final Set<String> OPERATORS = Set.of("=", "+", "-", "*", "/", "%", "^");
    private static final Set<Character> PUNCTUATORS = Set.of('(', ')', '{', '}', ';', ',');
    
    public Lexer(String input) {
        this.input = input;
        this.position = 0;
        this.length = input.length();
        this.stateCounter = 0;
        this.scopeStack = new Stack<>();
        this.globalVariables = new HashSet<>();
        scopeStack.push(new HashSet<>());
        this.lineNumber = 1;
        charPosition=0;
    }

    public List<Token> tokenize() {
        int scopeDepth = 0;
        List<Token> tokens = new ArrayList<>();
        while (position < length) {
            char currentChar = input.charAt(position);
            List<State> statePath = new ArrayList<>();
            State initialState = new State(stateCounter++, false, currentChar);
            statePath.add(initialState);

            if (currentChar == '\n') {
                lineNumber++;    
                charPosition = 0;
            }
            
            if (currentChar == '{') {
            	tokens.add(new Token(Token.PUNCTUATOR, String.valueOf(currentChar), statePath));
                scopeDepth++;
                scopeStack.push(new HashSet<>());
                position++;
                charPosition++;
                continue;
            }
            if (currentChar == '}') {
            	tokens.add(new Token(Token.PUNCTUATOR, String.valueOf(currentChar), statePath));
                scopeDepth = Math.max(0, scopeDepth - 1);
                scopeStack.pop();
                position++;
                charPosition++;
                continue;
            }

            if (Character.isWhitespace(currentChar)) { position++;charPosition++; continue; }
            if (Character.isDigit(currentChar)) { tokens.add(scanNumber(statePath)); continue; }
            if (currentChar == '\'') { tokens.add(scanCharacter(statePath)); continue; }
            if (currentChar == '"') { tokens.add(scanString(statePath)); continue; }
            
            if (Character.isLetter(currentChar)) {
                int start = position;
                statePath.add(new State(stateCounter++, false, currentChar));
        
                boolean hasUppercase = Character.isUpperCase(currentChar);
                
                while (position < input.length() && Character.isLetterOrDigit(input.charAt(position))) {
                    char transitionChar = input.charAt(position);
                    if (Character.isUpperCase(transitionChar)) {
                        hasUppercase = true;
                    }
                    if (statePath.isEmpty() || statePath.get(statePath.size() - 1).transitionChar != transitionChar) {
                        statePath.add(new State(stateCounter++, false, transitionChar));
                    }
                    position++;
                }
        
                String word = input.substring(start, position);
        
                // If word contains an uppercase letter, throw error
                if (hasUppercase) {
                    throw new IllegalArgumentException("Error: At line number: " + lineNumber +
                        " on index: " + position + " Uppercase word '" + word + "'");
                }
        
                // If "input" or "output" is followed by '(', process it
                if ((word.equals("input") || word.equals("output")) && position < input.length() && input.charAt(position) == '(') {
                    Token ioToken = processIOFunction(word);
                    if (ioToken != null) {
                        tokens.add(ioToken);
                    }
                    continue;
                }
        
                // Determine scope
                String scope = (scopeDepth == 0) ? "Global" : "Local";
        
                // Check if it's a variable assignment
                if (position < input.length() && input.charAt(position) == '=') {
                    if (scopeDepth == 0) {
                        globalVariables.add(word);
                    } else {
                        scopeStack.peek().add(word);
                    }
                }
        
                // Store identifier with correct scope
                tokens.add(new Token(Token.IDENTIFIER, word, statePath, scope));
        
                // Debug output
                System.out.println("Variable: " + word + ", Scope Depth: " + scopeDepth);
                continue;
            }


            
            if (currentChar == '/' && peekNext() == '/') { scanComment(); continue; }
            if (currentChar == '/' && peekNext() == '*') { scanMultilineComment(); continue; }
            
            

        
            
            if (OPERATORS.contains(String.valueOf(currentChar))) {
                tokens.add(new Token(Token.OPERATOR, String.valueOf(currentChar), statePath));
                position++; continue;
            }
            if (Character.isLowerCase(currentChar)) { 
                tokens.add(scanIdentifierOrKeyword(statePath)); continue;
             }
            if (PUNCTUATORS.contains(currentChar)) {
                tokens.add(new Token(Token.PUNCTUATOR, String.valueOf(currentChar), statePath));
                position++; continue;
            }
            position++;
        }
        tokens.add(new Token(Token.EOF, "EOF", new ArrayList<>()));
        return tokens;
    }

    private char peekNext() {
        return (position + 1 < length) ? input.charAt(position + 1) : '\0';
    }

    private void scanComment() {
        while (position < length && input.charAt(position) != '\n') {
            position++;
            charPosition++;
        }
    }

	private void scanMultilineComment() {
		position += 2; // Skip "/*"
		
		while (position < length - 1 && !(input.charAt(position) == '*' && input.charAt(position + 1) == '/')) {
            if (input.charAt(position) == '\n') {
                lineNumber++;
                charPosition = 0;
            }
            charPosition++;
			position++;
		}
	
		if (position >= length - 1) {
			throw new IllegalArgumentException("Error: MultiComment didn't completed at line: "+lineNumber +" on index: " + charPosition);
		}
	
		position += 2; // Skip "*/"
	}



    private Token scanNumber(List<State> statePath) {
        int start = position;
        boolean isDecimal = false;

        while (position < length && (Character.isDigit(input.charAt(position)) || input.charAt(position) == '.')) {
        	char transitionChar = input.charAt(position);
        	if (input.charAt(position) == '.') {
                isDecimal = true;
            }
            if (statePath.isEmpty() || statePath.get(statePath.size() - 1).transitionChar != transitionChar) {
                statePath.add(new State(stateCounter++, false, transitionChar));
            }
            position++;
            charPosition++;
        }

        statePath.add(new State(stateCounter++, true, ' '));

        String numberStr = input.substring(start, position);
        if (isDecimal) {
            try {
                BigDecimal roundedValue = new BigDecimal(numberStr)
                    .setScale(5, RoundingMode.HALF_UP);
                numberStr = roundedValue.toPlainString();  // Ensure no scientific notation
            } catch (NumberFormatException e) {
                // Handle invalid decimal format if needed
            }
        }

        return new Token(isDecimal ? Token.DECIMAL : Token.INTEGER, numberStr, statePath);
    }


    private Token scanCharacter(List<State> statePath) {
        
        State startState = new State(stateCounter++, false, input.charAt(position++));
        statePath.add(startState);

        if (position < length && input.charAt(position) == '\'') {
            return null; // Handle invalid case: lone single quote
        }

        if (position < length - 1 && input.charAt(position + 1) == '\'') {
            char value = input.charAt(position);
            State charState = new State(stateCounter++, false, value);
            statePath.add(charState);

            position++; // Move to closing quote
            charPosition++;
            State finalState = new State(stateCounter++, true, '\''); // Explicit final state
            statePath.add(finalState);
            
            position++; // Move past closing quote
            charPosition++;
            startState.addTransition(value, charState); // Add transitions
            charState.addTransition('\'', finalState);

            return new Token(Token.CHARACTER, String.valueOf(value), statePath);
        }

        return null;
    }



    private Token scanString(List<State> statePath) {
        int start = position++;
        statePath.add(new State(stateCounter++, false, '"'));
        while (position < length && input.charAt(position) != '"') {
        	char transitionChar = input.charAt(position);
            if (statePath.isEmpty() || statePath.get(statePath.size() - 1).transitionChar != transitionChar) {
                statePath.add(new State(stateCounter++, false, transitionChar));
            }
            position++;
            charPosition++;
        }
        statePath.add(new State(stateCounter++, true, '"'));
        position++;
        charPosition++;
        return new Token(Token.STRING, input.substring(start + 1, position - 1), statePath);
    }

   private Token scanIdentifierOrKeyword(List<State> statePath) {
		int start = position;
		
		while (position < length && Character.isLetter(input.charAt(position))) {
			statePath.add(new State(stateCounter++, false, input.charAt(position)));
			position++;
            charPosition++;
		}
		statePath.add(new State(stateCounter++, true, ' '));
		
		String value = input.substring(start, position);
	
		// **Ensure only IDENTIFIERS get this check, not keywords or datatypes**
		if (!KEYWORDS.contains(value) && !DATATYPE.contains(value)) {
			if (!value.equals(value.toLowerCase())) {
				throw new IllegalArgumentException("Error: At line number: " +lineNumber+" on index: "+charPosition+"Variable names cannot contain uppercase letters: " + value);
			}
		}
	
		if (BOOLEAN_VALUES.contains(value)) {
			return new Token(Token.BOOLEAN, value, statePath);
		} else if (KEYWORDS.contains(value)) {
			return new Token(Token.KEYWORD, value, statePath);
		} else if (DATATYPE.contains(value)) {
			return new Token(Token.DATATYPE, value, statePath);
		}
	
		return null;
	}

    
    private Token processIOFunction(String functionType) {
        
        List<State> statePath = new ArrayList<>();
        statePath.add(new State(stateCounter++, false, '('));

        position++; // Move past '('
        int contentStart = position;

        while (position < length && input.charAt(position) != ')') {
            char transitionChar = input.charAt(position);
            if (statePath.isEmpty() || statePath.get(statePath.size() - 1).transitionChar != transitionChar) {
                statePath.add(new State(stateCounter++, false, transitionChar));
            }
            position++;
        }

        if (position >= length) {
            throw new IllegalArgumentException("Error: At line number: "+ lineNumber +" on index: "+ charPosition +" Missing closing ')' for " + functionType);
        }

        statePath.add(new State(stateCounter++, true, ')'));
        String content = input.substring(contentStart, position).trim();
        position++; // Move past ')'

        if (functionType.equals("input")) {
            if (!content.isEmpty()) {
                return new Token(Token.INPUT, "input(" + content + ")", statePath);
            } else {
                return new Token(Token.INPUT, "input()", statePath);
            }
        } else if (functionType.equals("output")) {
            if (!content.isEmpty()) {
                return new Token(Token.OUTPUT, "output(" + content + ")", statePath);
            }
        }

        return null; // Return null to prevent empty tokens
    }


}
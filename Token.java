package example;

import java.util.List;

class Token {
	public static final int DATATYPE = 10;
	public static final int OUTPUT = 11;
	public static final int INPUT = 12;
    public static final int KEYWORD = 1;
    public static final int IDENTIFIER = 2;
    public static final int INTEGER = 3;
    public static final int DECIMAL = 4;
    public static final int CHARACTER = 5;
    public static final int BOOLEAN = 6;
    public static final int STRING = 7;
    public static final int OPERATOR = 8;
    public static final int PUNCTUATOR = 9;
    public static final int EOF = 0;
    
    private int type;
    private String value;
    private List<State> statePath;

    public Token(int type, String value, List<State> statePath) {
        this.type = type;
        this.value = value;
        this.statePath = statePath;
    }

    public int getType() { return type; }
    public String getValue() { return value; }
    public List<State> getStatePath() { return statePath; }

    private String getTypeName() {
        return switch (type) {
            case KEYWORD -> "KEYWORD";
            case DATATYPE -> "DATATYPE";
            case IDENTIFIER -> "IDENTIFIER";
            case INTEGER -> "INTEGER";
            case DECIMAL -> "DECIMAL";
            case CHARACTER -> "CHARACTER";
            case BOOLEAN -> "BOOLEAN";
            case STRING -> "STRING";
            case OPERATOR -> "OPERATOR";
            case PUNCTUATOR -> "PUNCTUATOR";
            case INPUT -> "INPUT";
            case OUTPUT -> "OUTPUT";
            case EOF -> "EOF";
            default -> "UNKNOWN";
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Token{").append(getTypeName()).append(" (").append(type).append(")").append(", value='").append(value).append("', states=[ (Start)");
        for (State state : statePath) {
            sb.append("(State ").append(state.id).append(" -> '").append(state.transitionChar).append("'), ");
        }
        if (!statePath.isEmpty()) sb.setLength(sb.length() - 2);
        sb.append("](Final)}");
        return sb.toString();
    }
}

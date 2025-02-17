package example;

class SymbolTableEntry {
    String name;
    String type;
    String scope;
    String value;

    public SymbolTableEntry(String name, String type, String scope, String value) {
        this.name = name;
        this.type = type;
        this.scope = scope;
        this.value = value;
    }

    @Override
    public String toString() {
        return "Name: " + name + ", Type: " + type + ", Scope: " + scope + ", Value: " + value;
    }
}


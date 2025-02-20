package dev.sbytmacke.onlyprofit.constants;

public enum DatabaseConstants {

    CONNECTION_STRING("mongodb://localhost:27017"),
    DATABASE_NAME("only_profit");

    private final String string;

    DatabaseConstants(String string) {
        this.string = string;
    }

    public String getString() {
        return string;
    }

    @Override
    public String toString() {
        return string;
    }
}


package dev.sbytmacke.onlyprofit.constants;

public enum AppConstants {

    TITLE("OnlyProfit");

    private final String string;

    AppConstants(String string) {
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


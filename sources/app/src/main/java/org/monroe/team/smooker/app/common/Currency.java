package org.monroe.team.smooker.app.common;

public enum Currency {

    RUB (1,"Рубль","RUB"),
    U_S_DOLLAR (2,"US Dollar","$"),
    EURO(3,"Euro","€"),
    UAH(4,"Гривня","₴"),
    YUAN(5,"元","¥");

    public final static Currency[] SUPPORTED_CURRENCIES = {RUB, U_S_DOLLAR, EURO, YUAN, UAH};

    public final int id;
    public final String name;
    public final String symbol;

    Currency(int id, String name, String symbol) {
        this.id = id;
        this.name = name;
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return name+" ("+symbol+")";
    }

    public static int supportedArrayIndex(Currency currency) {
        for (int i = 0; i < SUPPORTED_CURRENCIES.length; i++) {
             if (SUPPORTED_CURRENCIES[i] ==currency) return i;
        }
        throw new IllegalStateException();
    }

    public static Currency byId(int currencyId) {
        for (int i = 0; i < SUPPORTED_CURRENCIES.length; i++) {
            if (SUPPORTED_CURRENCIES[i].id == currencyId) return SUPPORTED_CURRENCIES[i];
        }
        throw new IllegalStateException();
    }
}

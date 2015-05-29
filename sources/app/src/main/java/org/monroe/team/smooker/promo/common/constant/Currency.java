package org.monroe.team.smooker.promo.common.constant;

public enum Currency {
    RUB (1,"Рубль","Р","RUB"),
    U_S_DOLLAR (2,"US Dollar","$","USD"),
    EURO(3,"Euro","€","EUR"),
    UAH(4,"Гривня","₴","UAH"),
    YUAN(5,"元","¥","CNY");

    public final static Currency[] SUPPORTED_CURRENCIES = {RUB, U_S_DOLLAR, EURO, YUAN, UAH};

    public final int id;
    public final String name;
    public final String symbol;
    public final java.util.Currency nativeInstance;


    Currency(int id, String name, String symbol,String isoName) {
        this.id = id;
        this.name = name;
        this.symbol = symbol;
        nativeInstance = java.util.Currency.getInstance(isoName);
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

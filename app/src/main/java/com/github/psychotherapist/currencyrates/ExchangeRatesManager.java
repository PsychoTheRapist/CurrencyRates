package com.github.psychotherapist.currencyrates;

public interface ExchangeRatesManager {
    void setBaseCurrency(String newBaseCurrency);
    void requestRatesUpdates(RatesListener listener);
    void stopRatesUpdates(RatesListener listener);

    interface RatesListener {
        void onRatesUpdate(ExchangeRates rates);
    }
}

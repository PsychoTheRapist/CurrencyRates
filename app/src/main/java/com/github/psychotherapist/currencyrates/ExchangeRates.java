package com.github.psychotherapist.currencyrates;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;


class ExchangeRates {
    private static final String JSON_BASE_FIELD = "base";
    private static final String JSON_RATES_FIELD = "rates";

    private static final String TAG = ExchangeRates.class.getSimpleName();

    public static ExchangeRates DEFAULT = new ExchangeRates("EUR", new TreeMap<>());

    private String mBaseCurrency;
    private Map<String, Float> mRates;

    public ExchangeRates(String base, Map<String, Float> rates) {
        mBaseCurrency = base;
        mRates = rates;
    }

    public static ExchangeRates fromJson(String jsonString) {
        ExchangeRates result = null;
        try {
            Map<String, Float> rates = new TreeMap<>();
            JSONObject responseObject = new JSONObject(jsonString);
            String base = responseObject.getString(JSON_BASE_FIELD);
            JSONObject ratesObject = responseObject.getJSONObject(JSON_RATES_FIELD);
            for (Iterator<String> it = ratesObject.keys(); it.hasNext(); ) {
                String currency = it.next();
                String rate = ratesObject.getString(currency);
                rates.put(currency, Float.parseFloat(rate));
            }
            result = new ExchangeRates(base, rates);
        } catch (JSONException e) {
            //Log error
        }

        return result;
    }

    public String toJson() {
        JSONObject resultJson = new JSONObject();
        try {
            resultJson.put(JSON_BASE_FIELD, mBaseCurrency);
            JSONObject ratesJson = new JSONObject(mRates);
            resultJson.put(JSON_RATES_FIELD, ratesJson);
        } catch (JSONException e) {
            Log.i(TAG, String.format("Rate to json convertion error: %s", e.getMessage()));
        }
        return resultJson.toString();
    }

    public void setBaseCurrency(String newBase) {
        Map<String, Float> newRates = getRates();
        if (!getBaseCurrency().equals(newBase)) {
            float newBaseRate = newRates.get(newBase);
            newRates.put(getBaseCurrency(), 1.0f);
            newRates.remove(newBase);
            for (Map.Entry<String, Float> e : newRates.entrySet()) {
                e.setValue(e.getValue() / newBaseRate);
            }
        }
        mBaseCurrency = newBase;
        mRates = newRates;
    }

    public String getBaseCurrency() {
        return mBaseCurrency;
    }

    public Map<String, Float> getRates() {
        return new TreeMap<>(mRates);
    }

    public ExchangeRates copy() {
        return new ExchangeRates(getBaseCurrency(), getRates());
    }
}

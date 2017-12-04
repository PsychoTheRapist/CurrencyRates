package com.github.psychotherapist.currencyrates;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

public class ExchangeRatesActivity extends Activity implements ExchangeRatesManager.RatesListener {
    private static final String PREF_EXCHANGE_RATE_KEY = "preferences_exchange_rate_key";
    private static final String PREF_BASE_AMOUNT_KEY = "preferences_base_amount_key";

    private ExchangeRates mExchangeRates;
    private String mBaseAmount;
    private ExchangeRatesListAdapter mAdapter;

    private TextView mHeaderBaseView;
    private TextView mHeaderAmountView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange_rates);

        loadFromPreferences();

        mAdapter = new ExchangeRatesListAdapter(getApplicationContext());

        ListView list = findViewById(R.id.ratesList);
        list.setOnItemClickListener(new RatesListItemClick());

        View header = getLayoutInflater().inflate(R.layout.exchange_rates_list_header, null);
        mHeaderBaseView = header.findViewById(R.id.rate_list_header_cur);
        mHeaderBaseView.setText(mExchangeRates.getBaseCurrency());
        mHeaderAmountView = header.findViewById(R.id.rate_list_header_amount);
        mHeaderAmountView.setText(mBaseAmount);
        mHeaderAmountView.addTextChangedListener(new BaseAmountTextWatcher());
        mHeaderAmountView.setOnEditorActionListener(new BaseAmountEditorActionListener());

        list.addHeaderView(header);
        list.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        DefaultRatesManager.getInstance(getApplicationContext()).requestRatesUpdates(this);
    }

    @Override
    protected void onPause() {
        DefaultRatesManager.getInstance(getApplicationContext()).stopRatesUpdates(this);
        super.onPause();
    }

    @Override
    public void onRatesUpdate(ExchangeRates newRates) {
        mExchangeRates = newRates;
        updateRatesList();
        saveToPreferences();
    }

    private void loadFromPreferences() {
        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        mExchangeRates = pref.contains(PREF_EXCHANGE_RATE_KEY) ?
                ExchangeRates.fromJson(pref.getString(PREF_EXCHANGE_RATE_KEY, null)) :
                ExchangeRates.DEFAULT;
        mBaseAmount = pref.contains(PREF_BASE_AMOUNT_KEY) ?
                pref.getString(PREF_BASE_AMOUNT_KEY, null) :
                "1";
    }

    private void saveToPreferences() {
        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        pref.edit()
                .putString(PREF_EXCHANGE_RATE_KEY, mExchangeRates.toJson())
                .putString(PREF_BASE_AMOUNT_KEY, mBaseAmount)
                .commit();
    }

    private void updateRatesList() {
        String headerBase = mHeaderAmountView.getText().toString();
        float baseCurrencyAmount = Float.parseFloat(
                headerBase.isEmpty() ?
                "0" : headerBase);
        ArrayList<ExchangeRatesListAdapter.RateListItem> items = new ArrayList<>();
        for(Map.Entry<String, Float> e : mExchangeRates.getRates().entrySet()) {
            Float newAmount = baseCurrencyAmount * e.getValue();
            items.add(new ExchangeRatesListAdapter.RateListItem(e.getKey(), newAmount));
        }

        mAdapter.clear();
        mAdapter.addAll(items);
        mAdapter.notifyDataSetChanged();
    }

    class RatesListItemClick implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ExchangeRatesListAdapter.RateListItem newBaseItem =
                    (ExchangeRatesListAdapter.RateListItem) parent.getItemAtPosition(position);

            mBaseAmount = Float.toString(newBaseItem.getAmount());
            mHeaderBaseView.setText(newBaseItem.getCurrency());
            mHeaderAmountView.setText(mBaseAmount);

            DefaultRatesManager.getInstance(getApplicationContext())
                            .setBaseCurrency(newBaseItem.getCurrency());

            ListView lv = (ListView) parent;
            lv.smoothScrollToPosition(0);
        }
    }

    class BaseAmountTextWatcher implements TextWatcher {
        private CharSequence oldAmount;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            oldAmount = s.toString();
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //Not implemented
        }

        @Override
        public void afterTextChanged(Editable s) {
            try {
                if (!s.toString().isEmpty()) {
                    Float.valueOf(s.toString());
                }
                updateRatesList();
            } catch (NumberFormatException e) {
                s.clear();
                s.append(oldAmount);
            }
        }
    }

    class BaseAmountEditorActionListener implements TextView.OnEditorActionListener {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (event != null || actionId == EditorInfo.IME_ACTION_DONE) {
                InputMethodManager imm = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                v.clearFocus();
            }
            return false;
        }
    }
}

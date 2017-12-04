package com.github.psychotherapist.currencyrates;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.github.psychotherapist.currencyrates.network.NetworkUtils;

import java.io.IOException;

public class RatesRefreshIntentService extends IntentService {
    private static final String TAG = RatesRefreshIntentService.class.getSimpleName();

    public static final String ACTION_REFRESH = "com.github.psychotherapist.currencyrates.network.action.REFRESH";
    public static final String ACTION_UPDATE_RATES = "com.github.psychotherapist.currencyrates.network.action.ACTION_UPDATE_RATES";


    public static final String EXTRA_URL_STRING = "com.github.psychotherapist.currencyrates.network.extra.BASE_CURRENCY";
    public static final String EXTRA_RATES_JSON = "com.github.psychotherapist.currencyrates.network.extra.RATES_JSON";

    public static Intent getRefreshRatesIntent(Context context, String urlString) {
        Intent refreshIntent = new Intent(context, RatesRefreshIntentService.class);
        refreshIntent.setAction(ACTION_REFRESH);
        refreshIntent.putExtra(RatesRefreshIntentService.EXTRA_URL_STRING, urlString);
        return refreshIntent;
    }

    public RatesRefreshIntentService() {
        super("RatesRefreshIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_REFRESH.equals(action)) {
                final String urlString = intent.getStringExtra(EXTRA_URL_STRING);
                refreshRates(urlString);
            }
        }
    }

    private void refreshRates(String urlString) {
        try {
            String ratesJson = NetworkUtils.getJsonFromUrl(urlString);
            Intent ratesUpdatedIntent = new Intent(ACTION_UPDATE_RATES);
            ratesUpdatedIntent.putExtra(EXTRA_RATES_JSON, ratesJson);
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getApplicationContext());
            lbm.sendBroadcast(ratesUpdatedIntent);
        } catch (IOException e) {
            Log.e(TAG, String.format("Refreshing rates error: %s", e.getMessage()));
        }
    }
}

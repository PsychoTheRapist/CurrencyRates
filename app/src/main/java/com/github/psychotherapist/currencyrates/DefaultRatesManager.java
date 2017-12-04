package com.github.psychotherapist.currencyrates;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class DefaultRatesManager implements ExchangeRatesManager {

    private static final long REFRESH_INTERVAL_MILLIS = 1000L;

    private ExchangeRates mExchangeRates = ExchangeRates.DEFAULT;
    private Set<RatesListener> mListeners = new HashSet<>();
    private Object mLock = new Object();

    private LocalBroadcastManager mBroadcastManager;
    private BroadcastReceiver mReceiver;

    private Timer mTimer;

    private Context mContext;

    public static DefaultRatesManager sManager;

    private final String mUrlString;

    private DefaultRatesManager(Context context) {
        mContext = context;
        mUrlString = context.getResources().getString(R.string.rates_url_string,
                                                        mExchangeRates.getBaseCurrency());
        mBroadcastManager = LocalBroadcastManager.getInstance(context);
        mReceiver = new UpdateRatesReceiver();
    }

    public static DefaultRatesManager getInstance(Context context) {
        if (sManager == null) {
            synchronized (DefaultRatesManager.class) {
                if (sManager == null) {
                    sManager = new DefaultRatesManager(context);
                }
            }
        }
        return sManager;
    }

    @Override
    public void setBaseCurrency(String newBaseCurrency) {
        synchronized (mLock) {
            mExchangeRates.setBaseCurrency(newBaseCurrency);
            for(RatesListener listener : mListeners) {
                listener.onRatesUpdate(mExchangeRates.copy());
            }
        }
    }

    @Override
    public void requestRatesUpdates(RatesListener listener) {
        synchronized (mLock) {
            boolean wasEmpty = mListeners.isEmpty();
            if (mListeners.add(listener)) {
                if (wasEmpty) {
                    mBroadcastManager.registerReceiver(mReceiver,
                            new IntentFilter(RatesRefreshIntentService.ACTION_UPDATE_RATES));
                    scheduleRefreshTask(REFRESH_INTERVAL_MILLIS);
                }

                if (!mExchangeRates.getRates().isEmpty()) {
                    listener.onRatesUpdate(mExchangeRates.copy());
                }
            }
        }
    }

    @Override
    public void stopRatesUpdates(RatesListener listener) {
        synchronized (mLock) {
            if (mListeners.remove(listener)) {
                if (mListeners.isEmpty()) {
                    cancelRefreshTask();
                    mBroadcastManager.unregisterReceiver(mReceiver);
                }
            }
        }
    }

    private void scheduleRefreshTask(long interval) {
        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new RefreshTask(), 0, interval);
    }

    private void cancelRefreshTask() {
        mTimer.cancel();
    }

    class UpdateRatesReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(RatesRefreshIntentService.ACTION_UPDATE_RATES) &&
                    intent.hasExtra(RatesRefreshIntentService.EXTRA_RATES_JSON)) {

                String json = intent.getStringExtra(RatesRefreshIntentService.EXTRA_RATES_JSON);
                ExchangeRates newExchangeRates = ExchangeRates.fromJson(json);

                if (newExchangeRates != null) {
                    synchronized (mLock) {
                        String currentBase = mExchangeRates.getBaseCurrency();
                        //In case while we're waiting for the response current base changed
                        newExchangeRates.setBaseCurrency(currentBase);
                        mExchangeRates = newExchangeRates;
                        for (RatesListener listener : mListeners) {
                            listener.onRatesUpdate(mExchangeRates.copy());
                        }
                    }
                }
            }
        }
    }

    private class RefreshTask extends TimerTask {

        @Override
        public void run() {
            mContext.startService(
                    RatesRefreshIntentService.getRefreshRatesIntent(mContext, mUrlString));
        }
    }
}

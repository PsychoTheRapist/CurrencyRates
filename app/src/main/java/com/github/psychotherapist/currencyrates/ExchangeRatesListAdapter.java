package com.github.psychotherapist.currencyrates;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ExchangeRatesListAdapter extends ArrayAdapter<ExchangeRatesListAdapter.RateListItem> {
    public ExchangeRatesListAdapter(Context context) {
        this(context, new ArrayList<>());
    }

    public ExchangeRatesListAdapter(Context context, ArrayList<RateListItem> items) {
        super(context, 0, items);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        RateListItem item = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.exchange_rates_list_item, parent, false);
        }

        TextView currencyTv = convertView.findViewById(R.id.rate_list_item_cur);
        currencyTv.setText(item.cur);
        TextView amountTv = convertView.findViewById(R.id.rate_list_item_amount);
        amountTv.setText(String.format("%.2f",item.mAmount));

        return convertView;
    }

    static class RateListItem implements Comparable<RateListItem> {
        private String cur;
        private float mAmount;

        public RateListItem(String cur, float amount) {
            this.cur = cur;
            this.mAmount = amount;
        }

        public String getCurrency() {
            return cur;
        }

        public float getAmount() {
            return mAmount;
        }

        @Override
        public int compareTo(@NonNull RateListItem o) {
            return cur.compareTo(o.cur);
        }
    }
}

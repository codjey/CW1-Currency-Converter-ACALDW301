package com.CW1.caldwell_andrew_acaldw301;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Locale;

public class CurrencyRateAdapter extends ArrayAdapter<CurrencyRate> {

    private final LayoutInflater inflater;

    public CurrencyRateAdapter(@NonNull Context context, @NonNull List<CurrencyRate> objects) {
        super(context, 0, objects);
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            rowView = inflater.inflate(R.layout.currency_list_item, parent, false);
        }

        ImageView imageFlag = rowView.findViewById(R.id.imageFlag);
        TextView nameView = rowView.findViewById(R.id.textCurrencyName);
        TextView rateView = rowView.findViewById(R.id.textCurrencyRate);

        CurrencyRate item = getItem(position);
        if (item != null) {
            String nameText = item.getCurrencyCode() + " - " + item.getCountryName();
            nameView.setText(nameText);

            String rateText = "1 GBP = " + item.getRate() + " " + item.getCurrencyCode();
            rateView.setText(rateText);

            // --- FLAG ICON (using dynamic rule) ---
            int flagResId = getFlagResIdForCurrency(item.getCurrencyCode());
            imageFlag.setImageResource(flagResId);

            // --- COLOUR CODING (4 ranges) ---
            // Here we treat lower rate values as "stronger" foreign currency vs GBP.
            // Blue  = strongest
            // Green = strong
            // Orange = weak
            // Red   = very weak
            double rate = item.getRate();
            if (rate < 1.0) {
                // Strongest
                rowView.setBackgroundColor(Color.parseColor("#BBDEFB")); // light blue
            } else if (rate < 2.0) {
                // Strong
                rowView.setBackgroundColor(Color.parseColor("#C8E6C9")); // light green
            } else if (rate < 5.0) {
                // Weak
                rowView.setBackgroundColor(Color.parseColor("#FFE0B2")); // light orange
            } else {
                // Very weak
                rowView.setBackgroundColor(Color.parseColor("#FFCDD2")); // light red
            }

        }

        return rowView;
    }

    // Get the drawable ID for a given currency code using country flag naming rule
    private int getFlagResIdForCurrency(String currencyCode) {
        String countryCode = getCountryCodeForCurrency(currencyCode);
        if (countryCode == null) {
            return R.drawable.flag_default;
        }

        String resName = "flag_" + countryCode.toLowerCase(Locale.ROOT);
        int resId = getContext().getResources()
                .getIdentifier(resName, "drawable", getContext().getPackageName());

        if (resId == 0) {
            return R.drawable.flag_default;
        }
        return resId;
    }

    // Map currency code (3-letter) → country code (2-letter) for common currencies
    private String getCountryCodeForCurrency(String currencyCode) {
        if (currencyCode == null) return null;

        switch (currencyCode.toUpperCase(Locale.ROOT)) {
            case "GBP": return "gb";  // United Kingdom
            case "USD": return "us";  // United States
            case "EUR": return "fr";  // France as stand-in for Eurozone
            case "JPY": return "jp";  // Japan
            case "CHF": return "ch";  // Switzerland
            case "AUD": return "au";  // Australia
            case "CAD": return "ca";  // Canada
            case "CNY": return "cn";  // China
            case "NZD": return "nz";  // New Zealand
            case "SEK": return "se";  // Sweden
            case "NOK": return "no";  // Norway
            case "DKK": return "dk";  // Denmark
            case "PLN": return "pl";  // Poland
            case "HUF": return "hu";  // Hungary
            case "CZK": return "cz";  // Czechia
            case "ZAR": return "za";  // South Africa
            case "INR": return "in";  // India
            case "BRL": return "br";  // Brazil
            case "MXN": return "mx";  // Mexico
            case "SGD": return "sg";  // Singapore
            case "HKD": return "hk";  // Hong Kong
            case "KRW": return "kr";  // South Korea
            case "TRY": return "tr";  // Türkiye
            case "RUB": return "ru";  // Russia
            case "ILS": return "il";  // Israel
            case "SAR": return "sa";  // Saudi Arabia
            case "AED": return "ae";  // UAE
            // Extend this list as you encounter more currency codes in your feed
            default:
                return null;
        }
    }
}

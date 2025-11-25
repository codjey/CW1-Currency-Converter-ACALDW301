package com.CW1.caldwell_andrew_acaldw301;

// A data model representing one currency entry from the RSS feed
public class CurrencyRate {

    private String title;         // e.g. "British Pound Sterling(GBP)/US Dollar(USD)"
    private String currencyCode;  // e.g. "USD"
    private String countryName;   // e.g. "US Dollar"
    private double rate;          // numeric rate from description
    private String pubDate;       // date string from RSS
    private String description;   // raw description text

    public CurrencyRate() {
        // Empty default constructor
    }

    // ----------------- Getters & Setters -----------------

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        extractCountryAndCodeFromTitle();
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public double getRate() {
        return rate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setDescription(String description) {
        this.description = description;
        extractRateFromDescription();
    }

    public String getDescription() {
        return description;
    }

    // ----------------- Helper methods -----------------

    // Title example:
    // "British Pound Sterling(GBP)/United Arab Emirates Dirham(AED)"
    // We want: currencyCode = "AED", countryName = "United Arab Emirates Dirham"
    private void extractCountryAndCodeFromTitle() {
        if (title == null) return;

        try {
            // Take the right side of the slash
            String[] parts = title.split("/");
            String rightSide = parts[parts.length - 1].trim();
            // rightSide example: "United Arab Emirates Dirham(AED)"

            int openBracket = rightSide.lastIndexOf('(');
            int closeBracket = rightSide.lastIndexOf(')');

            if (openBracket != -1 && closeBracket != -1 && closeBracket > openBracket) {
                // Text before "(" is the country/currency name
                countryName = rightSide.substring(0, openBracket).trim();
                // Text inside "()" is the code
                currencyCode = rightSide.substring(openBracket + 1, closeBracket).trim();
            } else {
                // Fallback: no brackets found, use the full right side as name
                countryName = rightSide;
                currencyCode = "";
            }

        } catch (Exception e) {
            countryName = "";
            currencyCode = "";
        }
    }

    // Description example:
    // "1 British Pound Sterling = 4.9471 United Arab Emirates Dirham"
    // We want: rate = 4.9471
    private void extractRateFromDescription() {
        if (description == null) return;

        try {
            String trimmed = description.trim();
            // Split at '='
            String[] parts = trimmed.split("=");
            if (parts.length < 2) {
                rate = 0.0;
                return;
            }

            // Right side: "4.9471 United Arab Emirates Dirham"
            String rightSide = parts[1].trim();
            String[] bits = rightSide.split(" ");
            if (bits.length >= 1) {
                String numberOnly = bits[0]; // "4.9471"
                rate = Double.parseDouble(numberOnly);
            } else {
                rate = 0.0;
            }
        } catch (Exception e) {
            rate = 0.0;
        }
    }

    @Override
    public String toString() {
        return currencyCode + " - " + countryName + " : " + rate;
    }
}

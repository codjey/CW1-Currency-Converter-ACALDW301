/*  Starter project for Mobile Platform Development - 1st diet 25/26
    You should use this project as the starting point for your assignment.
    This project simply reads the data from the required URL and displays the
    raw data in a TextField
*/

//
// Name                 Andrew Caldwell
// Student ID           ACALDW301
// Programme of Study   BSc/Hons Software Development
//

package com.CW1.caldwell_andrew_acaldw301;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // UI widgets
    private TextView rawDataDisplay;
    private Button startButton;
    private EditText editSearch;
    private Button btnSearch;
    private ListView listViewSearchResults;
    private CurrencyRateAdapter searchAdapter;


    // List views & adapters
    private ListView listViewMain;
    private ListView listViewAll;
    private CurrencyRateAdapter mainAdapter;
    private CurrencyRateAdapter allAdapter;

    // Codes we consider "main" currencies for the summary view
    private static final String[] MAIN_CODES = {
            "USD", "EUR", "JPY", "CHF", "AUD", "CAD", "CNY", "NZD"
    };

    // Auto-refresh interval
    private static final long REFRESH_INTERVAL_MS = 1 * 60 * 1000; // 1 minute


    // Data & networking
    private final String urlSource = "https://www.fx-exchange.com/gbp/rss.xml";
    private String result = "";

    // Parsed data list (for all currencies)
    private final ArrayList<CurrencyRate> currencyList = new ArrayList<>();

    // Handler for posting back to the main (UI) thread
    private final Handler uiHandler = new Handler(Looper.getMainLooper());


    // Handler and runnable for periodic auto-refresh
    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            // Trigger a new download
            startDownload();
            // Schedule the next refresh
            refreshHandler.postDelayed(this, REFRESH_INTERVAL_MS);
        }
    };


    // ---------------------------------------------------------
    // onCreate – set up layout and start initial download
    // ---------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rawDataDisplay = findViewById(R.id.rawDataDisplay);
        startButton = findViewById(R.id.startButton);

        // NEW: find the list views
        listViewMain = findViewById(R.id.listViewMainCurrencies);
        listViewAll = findViewById(R.id.listViewAllCurrencies);

        editSearch = findViewById(R.id.editSearch);
        btnSearch = findViewById(R.id.btnSearch);
        listViewSearchResults = findViewById(R.id.listViewSearchResults);

        // NEW: create empty adapters for now
        mainAdapter = new CurrencyRateAdapter(this, new ArrayList<CurrencyRate>());
        allAdapter = new CurrencyRateAdapter(this, new ArrayList<CurrencyRate>());
        // Adapter starts empty
        searchAdapter = new CurrencyRateAdapter(this, new ArrayList<CurrencyRate>());
        listViewSearchResults.setAdapter(searchAdapter);

        // NEW: attach adapters to list views
        listViewMain.setAdapter(mainAdapter);
        listViewAll.setAdapter(allAdapter);

        // Search button listener
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runSearch();
            }
        });

        // Click search results → open converter in Step 5
        listViewSearchResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CurrencyRate selected = searchAdapter.getItem(position);
                if (selected != null) {
                    openConversionScreen(selected);
                }
            }
        });

        listViewMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CurrencyRate selected = mainAdapter.getItem(position);
                if (selected != null) {
                    openConversionScreen(selected);
                }
            }
        });

        listViewAll.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CurrencyRate selected = allAdapter.getItem(position);
                if (selected != null) {
                    openConversionScreen(selected);
                }
            }
        });

        if (startButton != null) {
            startButton.setOnClickListener(this);
        }

        // Start auto-refresh timer (periodic updates)
        refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL_MS);

        // Auto-start download once app opens (helps for auto-update marks)
        startDownload();
    }


    // ---------------------------------------------------------
    // Button click – manual refresh / re-download
    // ---------------------------------------------------------
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.startButton) {
            startDownload();
        }
    }

    // ---------------------------------------------------------
    // Start the background download on a separate thread
    // ---------------------------------------------------------
    private void startDownload() {
        Thread downloadThread = new Thread(new DownloadTask(urlSource));
        downloadThread.start();
    }

    // ---------------------------------------------------------
    // DownloadTask – runs in a background thread
    // ---------------------------------------------------------
    private class DownloadTask implements Runnable {
        private final String url;

        public DownloadTask(String url) {
            this.url = url;
        }

        @Override
        public void run() {
            BufferedReader in = null;
            StringBuilder stringBuilder = new StringBuilder();

            try {
                URL aurl = new URL(url);
                URLConnection yc = aurl.openConnection();
                yc.setRequestProperty("User-Agent", "Mozilla/5.0");
                in = new BufferedReader(new InputStreamReader(yc.getInputStream()));

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    stringBuilder.append(inputLine);
                }

                result = stringBuilder.toString();
                Log.d("DownloadTask", "Downloaded XML length: " + result.length());

                // Clean up any HTML wrapper and extract just the RSS
                String cleanXml = sanitizeXmlFromHtmlWrapper(result);
                Log.d("DownloadTask", "Clean XML length: " + cleanXml.length());

                // Parse the XML into currencyList (still on background thread)
                parseXML(cleanXml);

                // Now update the UI on the main thread using Handler
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateAfterDownload();
                    }
                });

            } catch (Exception e) {
                Log.e("DownloadTask", "Error downloading data", e);
                final String errorMessage = "Error downloading data: " + e.getMessage();
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        rawDataDisplay.setText(errorMessage);
                    }
                });
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        Log.e("DownloadTask", "Error closing stream", e);
                    }
                }
            }
        }
    }

    // ---------------------------------------------------------
    // updateAfterDownload – populate main & all lists
    // ---------------------------------------------------------
    private void updateAfterDownload() {
        if (currencyList.isEmpty()) {
            rawDataDisplay.setText("No data parsed from feed.");
            return;
        }

        // Build the main currencies subset
        ArrayList<CurrencyRate> mainList = buildMainCurrencyList();

        // Update adapters
        mainAdapter.clear();
        mainAdapter.addAll(mainList);
        mainAdapter.notifyDataSetChanged();

        allAdapter.clear();
        allAdapter.addAll(currencyList);
        allAdapter.notifyDataSetChanged();

        // Status text
        rawDataDisplay.setText(
                "Loaded " + currencyList.size() + " currencies.\n" +
                        "Main currencies: " + mainList.size()
        );
    }

    // ---------------------------------------------------------
    // buildMainCurrencyList – filter currencyList to MAIN_CODES
    // ---------------------------------------------------------
    private ArrayList<CurrencyRate> buildMainCurrencyList() {
        ArrayList<CurrencyRate> mainList = new ArrayList<>();

        for (CurrencyRate rate : currencyList) {
            String code = rate.getCurrencyCode();
            if (code == null) continue;

            for (String mainCode : MAIN_CODES) {
                if (code.equalsIgnoreCase(mainCode)) {
                    mainList.add(rate);
                    break;
                }
            }
        }
        return mainList;
    }


    // ---------------------------------------------------------
    // parseXML – FULL XmlPullParser implementation
    // Fills currencyList with CurrencyRate objects
    // ---------------------------------------------------------
    private void parseXML(String xmlData) {
        currencyList.clear();

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(xmlData));

            int eventType = xpp.getEventType();
            CurrencyRate currentRate = null;
            String text = "";
            boolean insideItem = false;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = xpp.getName();

                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if ("item".equalsIgnoreCase(tagName)) {
                            insideItem = true;
                            currentRate = new CurrencyRate();
                        }
                        break;

                    case XmlPullParser.TEXT:
                        text = xpp.getText();
                        break;

                    case XmlPullParser.END_TAG:
                        if (insideItem && currentRate != null) {
                            if ("title".equalsIgnoreCase(tagName)) {
                                currentRate.setTitle(text);
                            } else if ("description".equalsIgnoreCase(tagName)) {
                                currentRate.setDescription(text);
                            } else if ("pubDate".equalsIgnoreCase(tagName)) {
                                currentRate.setPubDate(text);
                            } else if ("item".equalsIgnoreCase(tagName)) {
                                // Finished one <item>
                                currencyList.add(currentRate);
                                insideItem = false;
                            }
                        }
                        break;
                }

                eventType = xpp.next();
            }

            Log.d("parseXML", "Parsed items: " + currencyList.size());

        } catch (XmlPullParserException e) {
            Log.e("parseXML", "XML Pull Parser exception", e);
        } catch (IOException e) {
            Log.e("parseXML", "IO exception during parsing", e);
        }
    }

    // ---------------------------------------------------------
    // runSearch – filters by code, country, or name
    // ---------------------------------------------------------
    private void runSearch() {
        String query = editSearch.getText().toString().trim().toLowerCase();
        if (query.isEmpty()) {
            rawDataDisplay.setText("Please enter a search term.");
            return;
        }

        ArrayList<CurrencyRate> results = new ArrayList<>();

        for (CurrencyRate rate : currencyList) {
            // search by code
            if (rate.getCurrencyCode() != null &&
                    rate.getCurrencyCode().toLowerCase().contains(query)) {
                results.add(rate);
                continue;
            }

            // search by country name (e.g. "Chinese Yuan")
            if (rate.getCountryName() != null &&
                    rate.getCountryName().toLowerCase().contains(query)) {
                results.add(rate);
                continue;
            }

            // search by title (e.g. full long name)
            if (rate.getTitle() != null &&
                    rate.getTitle().toLowerCase().contains(query)) {
                results.add(rate);
            }
        }

        searchAdapter.clear();
        searchAdapter.addAll(results);
        searchAdapter.notifyDataSetChanged();

        rawDataDisplay.setText("Search results: " + results.size());
    }


    // ---------------------------------------------------------
    // sanitizeXmlFromHtmlWrapper – extract just the <rss>...</rss> block
    // ---------------------------------------------------------
    private String sanitizeXmlFromHtmlWrapper(String data) {
        if (data == null) {
            return "";
        }

        // Try to find the start of the RSS XML
        int start = data.indexOf("<?xml");
        if (start < 0) {
            start = data.indexOf("<rss");
        }
        if (start < 0) {
            // No obvious XML header or <rss> tag found
            Log.e("sanitizeXml", "No XML header or <rss> tag found; returning original data");
            return data;
        }

        // Try to find the end of the RSS block
        int end = data.indexOf("</rss>");
        if (end > start) {
            end += "</rss>".length(); // include the closing tag
            String trimmed = data.substring(start, end);
            Log.d("sanitizeXml", "Extracted RSS segment length: " + trimmed.length());
            return trimmed;
        } else {
            Log.e("sanitizeXml", "No closing </rss> tag found; returning original data");
            return data;
        }
    }

    // ---------------------------------------------------------
    // openConversionScreen – launch ConversionActivity
    // ---------------------------------------------------------
    private void openConversionScreen(CurrencyRate selected) {
        Intent intent = new Intent(MainActivity.this, ConversionActivity.class);
        intent.putExtra("rate", selected.getRate());
        intent.putExtra("code", selected.getCurrencyCode());
        intent.putExtra("country", selected.getCountryName());
        startActivity(intent);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop auto-refresh when activity is destroyed
        refreshHandler.removeCallbacks(refreshRunnable);
    }


}
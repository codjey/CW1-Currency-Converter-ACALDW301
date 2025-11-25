package com.CW1.caldwell_andrew_acaldw301;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class ConversionActivity extends AppCompatActivity {

    private TextView textConversionTitle;
    private TextView textCurrentRate;
    private TextView textFromLabel;
    private TextView textToLabel;
    private TextView textResult;
    private TextView textError;
    private EditText editAmount;
    private Button btnConvert;
    private Button btnFlip;

    private double rate;          // 1 GBP = rate * CODE
    private String currencyCode;
    private String countryName;

    // true  = GBP → Other
    // false = Other → GBP
    private boolean gbpToOther = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversion);

        // Get extras from intent
        rate = getIntent().getDoubleExtra("rate", 0.0);
        currencyCode = getIntent().getStringExtra("code");
        countryName = getIntent().getStringExtra("country");

        // Find views
        textConversionTitle = findViewById(R.id.textConversionTitle);
        textCurrentRate = findViewById(R.id.textCurrentRate);
        textFromLabel = findViewById(R.id.textFromLabel);
        textToLabel = findViewById(R.id.textToLabel);
        textResult = findViewById(R.id.textResult);
        textError = findViewById(R.id.textError);
        editAmount = findViewById(R.id.editAmount);
        btnConvert = findViewById(R.id.btnConvert);
        btnFlip = findViewById(R.id.btnFlip);

        // Set up heading text
        String title = "GBP ⇄ " + currencyCode + " Converter";
        if (countryName != null && !countryName.isEmpty()) {
            title = "GBP ⇄ " + currencyCode + " (" + countryName + ") Converter";
        }
        textConversionTitle.setText(title);

        // Show current rate
        String rateText = String.format(Locale.UK,
                "1 GBP = %.4f %s", rate, currencyCode);
        textCurrentRate.setText(rateText);

        // Initial direction is GBP → Other
        gbpToOther = true;
        updateDirectionUI();

        // Flip button changes direction
        btnFlip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gbpToOther = !gbpToOther;
                updateDirectionUI();
            }
        });

        // Convert button performs conversion based on current direction
        btnConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performConversion();
            }
        });
    }

    // Update labels and input hint based on current direction
    private void updateDirectionUI() {
        textError.setText("");
        textResult.setText("");

        if (gbpToOther) {
            textFromLabel.setText("GBP");
            textToLabel.setText(currencyCode);
            editAmount.setHint("Enter amount in GBP");
        } else {
            textFromLabel.setText(currencyCode);
            textToLabel.setText("GBP");
            editAmount.setHint("Enter amount in " + currencyCode);
        }
    }

    // Perform conversion using single input and current direction
    private void performConversion() {
        textError.setText("");
        textResult.setText("");

        if (rate == 0.0) {
            textError.setText("Conversion rate is zero or unavailable.");
            return;
        }

        String input = editAmount.getText().toString().trim();
        if (input.isEmpty()) {
            if (gbpToOther) {
                textError.setText("Please enter an amount in GBP.");
            } else {
                textError.setText("Please enter an amount in " + currencyCode + ".");
            }
            return;
        }

        try {
            double amount = Double.parseDouble(input);
            if (amount < 0) {
                textError.setText("Amount cannot be negative.");
                return;
            }

            if (gbpToOther) {
                double otherAmount = amount * rate;
                String resultStr = String.format(Locale.UK,
                        "%.2f GBP = %.2f %s",
                        amount, otherAmount, currencyCode);
                textResult.setText(resultStr);
            } else {
                double gbpAmount = amount / rate;
                String resultStr = String.format(Locale.UK,
                        "%.2f %s = %.2f GBP",
                        amount, currencyCode, gbpAmount);
                textResult.setText(resultStr);
            }

        } catch (NumberFormatException ex) {
            textError.setText("Invalid number. Please enter a valid amount.");
        }
    }
}

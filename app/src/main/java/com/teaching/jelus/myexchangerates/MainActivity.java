package com.teaching.jelus.myexchangerates;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {
    private TextView infoTextView;
    private TextView dateTextView;
    private Spinner currencySpinner;
    private JSONObject dataJsonObj;
    ArrayList<String> list = new ArrayList<>();
    private String urlString = "http://api.fixer.io/latest?base=RUB";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        infoTextView = (TextView) findViewById(R.id.infoTextView);
        dateTextView = (TextView) findViewById(R.id.dateTextView);
        currencySpinner = (Spinner) findViewById(R.id.currencySpinner);
        new ParseTask().execute();
        currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                getChangedRate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    class ParseTask extends AsyncTask<Void, JSONObject, String> {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                resultJson = buffer.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultJson;
        }

        @Override
        protected void onPostExecute(String strJson) {
            super.onPostExecute(strJson);
            try {
                dataJsonObj = new JSONObject(strJson);
                JSONObject rates = dataJsonObj.getJSONObject("rates");
                fillCurrencyList(rates);
                getChangedRate();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void fillCurrencyList(JSONObject rates){
        String selectedItem;
        if (currencySpinner.getSelectedItem() != null){
            selectedItem = currencySpinner.getSelectedItem().toString();
        } else{
            selectedItem = "USD";
        }
        list.clear();
        try {
            for(int i = 0; i < rates.length(); i++){
                list.add(rates.names().getString(i));
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                list);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        currencySpinner.setAdapter(currencyAdapter);
        currencySpinner.setSelection(findSpinnerIndexByItem(selectedItem));
    }

    private int findSpinnerIndexByItem(String item){
        int j = 0;
        for (Iterator<String> i = list.iterator(); i.hasNext();){
            String name = i.next();
            if (name.equals(item)){
                return j;
            }
            j++;
        }
        return j;
    }

    private void getChangedRate(){
        try {
            String date = dataJsonObj.getString("date");
            dateTextView.setText(getResources().getString(R.string.date_text_view_string)
                    + " "
                    + date);
            JSONObject rates = dataJsonObj.getJSONObject("rates");
            double viewCheckedRate = rates.getDouble(currencySpinner.getSelectedItem().toString());
            String rubleExchangeRate = String.format("%.2f" , 1 / viewCheckedRate);
            infoTextView.setText(getResources().getString(R.string.rate_text_view_string)
                    + " "
                    + rubleExchangeRate
                    + " руб.");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

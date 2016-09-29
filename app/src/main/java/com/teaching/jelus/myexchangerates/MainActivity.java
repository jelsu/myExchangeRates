package com.teaching.jelus.myexchangerates;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private TextView infoTextView;
    private TextView dateTextView;
    private String urlString = "http://api.fixer.io/latest?base=USD";
    private final String LOG_TAG = "my_log";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        infoTextView = (TextView) findViewById(R.id.infoTextView);
        dateTextView = (TextView) findViewById(R.id.dateTextView);
        new ParseTask().execute();
    }

    class ParseTask extends AsyncTask<Void, Void, String> {
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
            Log.d(LOG_TAG, strJson);
            JSONObject dataJsonObj = null;
            String eurRates = "нет данных";
            String date = "нет данных";
            try {
                dataJsonObj = new JSONObject(strJson);
                date = dataJsonObj.getString("date");
                dateTextView.setText("Текущая дата: " + date);
                JSONObject rates = dataJsonObj.getJSONObject("rates");
                eurRates = rates.getString("RUB");
                infoTextView.setText("Курс доллара: " + eurRates + " руб.");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}

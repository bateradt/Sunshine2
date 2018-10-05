package com.br.estudo.marcelo.sunshine;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;

import com.br.estudo.marcelo.sunshine.data.SunshinePreferences;
import com.br.estudo.marcelo.sunshine.utilities.NetworkUtils;
import com.br.estudo.marcelo.sunshine.utilities.OpenWeatherJsonUtils;
import com.br.estudo.marcelo.sunshine.ForecastAdapter.ForecastAdapterOnClickHandler;

import java.net.URL;
import java.nio.file.Files;

public class MainActivity extends AppCompatActivity implements ForecastAdapterOnClickHandler {

    private TextView mWeatherTextView;
    private TextView mFalhaTextView;
    private ProgressBar pgbProgresso;
    private RecyclerView mRecyclerView;
    private ForecastAdapter mForecastAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mWeatherTextView = (TextView) findViewById(R.id.tv_weather_data);
        mFalhaTextView = (TextView)  findViewById(R.id.tv_erro);
        pgbProgresso = (ProgressBar) findViewById(R.id.pb_progresso);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_forecast);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mForecastAdapter = new ForecastAdapter(this);
        mRecyclerView.setAdapter(mForecastAdapter);

        /*String[] dummyWeatherData = {
                "Today, May 17 - Clear - 17°C / 15°C",
                "Tomorrow - Cloudy - 19°C / 15°C",
                "Thursday - Rainy- 30°C / 11°C",
                "Friday - Thunderstorms - 21°C / 9°C",
                "Saturday - Thunderstorms - 16°C / 7°C",
                "Sunday - Rainy - 16°C / 8°C",
                "Monday - Partly Cloudy - 15°C / 10°C",
                "Tue, May 24 - Meatballs - 16°C / 18°C",
                "Wed, May 25 - Cloudy - 19°C / 15°C",
                "Thu, May 26 - Stormy - 30°C / 11°C",
                "Fri, May 27 - Hurricane - 21°C / 9°C",
                "Sat, May 28 - Meteors - 16°C / 7°C",
                "Sun, May 29 - Apocalypse - 16°C / 8°C",
                "Mon, May 30 - Post Apocalypse - 15°C / 10°C",
        };

        for (String dummyWeatherDay: dummyWeatherData) {
            mWeatherTextView.append(dummyWeatherDay + "\n\n\n");
        } */


        loadWeatherData();
    }

    @Override
    public void onClick(String weatherForDay) {
        Context context = this;
        Toast.makeText(context, weatherForDay, Toast.LENGTH_SHORT)
                .show();
    }

    private void showDadosDoTempo() {
        mWeatherTextView.setVisibility(View.VISIBLE);

        mFalhaTextView.setVisibility(View.INVISIBLE);

        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showMensagemErro() {
        mRecyclerView.setVisibility(View.INVISIBLE);

        mWeatherTextView.setVisibility(View.INVISIBLE);

        mFalhaTextView.setVisibility(View.VISIBLE);
    }


    private void loadWeatherData() {
        showDadosDoTempo();
        String location = SunshinePreferences.getPreferredWeatherLocation(this);
        new FetchWeatherTask().execute(location);
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pgbProgresso.setVisibility(View.VISIBLE);
        }

        // COMPLETED (6) Override the doInBackground method to perform your network requests
        @Override
        protected String[] doInBackground(String... params) {

            /* If there's no zip code, there's nothing to look up. */
            if (params.length == 0) {
                return null;
            }

            String location = params[0];
            URL weatherRequestUrl = NetworkUtils.buildUrl(location);

            try {
                String jsonWeatherResponse = NetworkUtils
                        .getResponseFromHttpUrl(weatherRequestUrl);

                String[] simpleJsonWeatherData = OpenWeatherJsonUtils
                        .getSimpleWeatherStringsFromJson(MainActivity.this, jsonWeatherResponse);

                return simpleJsonWeatherData;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        // COMPLETED (7) Override the onPostExecute method to display the results of the network request
        @Override
        protected void onPostExecute(String[] weatherData) {
            pgbProgresso.setVisibility(View.INVISIBLE);
            if (weatherData != null) {
                /*
                 * Iterate through the array and append the Strings to the TextView. The reason why we add
                 * the "\n\n\n" after the String is to give visual separation between each String in the
                 * TextView. Later, we'll learn about a better way to display lists of data.
                 */
                showDadosDoTempo();
                mForecastAdapter.setWeatherData(weatherData);
                /*
                for (String weatherString : weatherData) {
                    mWeatherTextView.append((weatherString) + "\n\n\n");
                }*/
            } else {
                showMensagemErro();
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            Toast.makeText(this, "Atualizando", Toast.LENGTH_SHORT).show();
            mForecastAdapter.setWeatherData(null);
            mWeatherTextView.setText("");
            loadWeatherData();
        }

        return super.onOptionsItemSelected(item);
    }
}

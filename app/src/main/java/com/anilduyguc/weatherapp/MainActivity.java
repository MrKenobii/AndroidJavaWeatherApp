package com.anilduyguc.weatherapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity{
    private RelativeLayout homeRelativeLayout;
    private ProgressBar loadingProgressBar;
    private TextView cityNameTextView, temperatureTextView, conditionTextView;
    private RecyclerView weatherRecyclerView;
    private TextInputEditText cityEdit;
    private ImageView backImageView, iconImageView, searchImageView;
    private ArrayList<WeatherRVModel> weatherRVModels;
    private WeatherRVAdapter weatherRVAdapter;
    private LocationManager locationManager;
    private int PERMISSION_CODE = 1;
    private String cityName;
    private Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);
        homeRelativeLayout = findViewById(R.id.idRLHome);
        loadingProgressBar = findViewById(R.id.idPBLoading);
        cityNameTextView = findViewById(R.id.idTVCityName);
        temperatureTextView = findViewById(R.id.idTVTemperature);
        conditionTextView = findViewById(R.id.idTVCondition);
        weatherRecyclerView = findViewById(R.id.idRVWeather);
        cityEdit = findViewById(R.id.idEdtCity);
        backImageView = findViewById(R.id.idIVBlack);
        iconImageView = findViewById(R.id.idIVIcon);
        searchImageView = findViewById(R.id.idIVSearch);

        weatherRVModels = new ArrayList<>();
        weatherRVAdapter = new WeatherRVAdapter(this, weatherRVModels);

        weatherRecyclerView.setAdapter(weatherRVAdapter);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_CODE);
        }

        this.launch();

        searchImageView.setOnClickListener(v -> {
            String city = cityEdit.getText().toString();
            if (city.isEmpty())
                Toast.makeText(MainActivity.this, "Please enter a city name", Toast.LENGTH_SHORT).show();
            else {
                cityNameTextView.setText(cityName);
                getWeatherInfo(city);
            }
        });

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission granted..", Toast.LENGTH_SHORT).show();
                launch();
            }
            else{
                Toast.makeText(this, "Please provide permissions", Toast.LENGTH_SHORT).show();
                finish();
            }

        }
    }

    private void launch() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 5F, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    Log.d("Location", ("Current Location: " + location.getLatitude() + ", " + location.getLongitude()));
                    cityName = getCityName(location.getLongitude(), location.getLatitude());
                    Log.e("City name: ", "City:" + cityName);
                    String url = "http://api.weatherapi.com/v1/forecast.json?key=3b434811dcc44355a1f180651230601&q="+ cityName + "&days=1&aqi=yes&alerts=yes";
                    cityNameTextView.setText(cityName);
                    Log.d("Here launch()", "in launch()");
                    new HttpAsyncTask().execute(url);
                    //getWeatherInfo(cityName);
                }
            });
        }
        catch(SecurityException e) {
            e.printStackTrace();
        }
    }

    private String getCityName(double longitude, double latitude){
        String cityName = "Istanbul";
        Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 10);
            for(Address address: addressList){
                if(address != null){
                    String city = address.getLocality();
                    if(city != null && !city.equals("")){
                        Log.e("City:", city);
                        cityName = city;

                    }
                    else {
                        Log.e("City:", "City name is:" +city);
                        Log.d("TAG", "CITY NOT FOUND");
                        //Toast.makeText(this, "User City Not Found", Toast.LENGTH_LONG).show();
                    }
                }
            }
        } catch (IOException exception){
            exception.printStackTrace();
        }
        Log.d("City Name: ", cityName);
        return cityName;
    }
    private void getWeatherInfo(String cityName){
        loadingProgressBar.setVisibility(View.GONE);
        homeRelativeLayout.setVisibility(View.VISIBLE);
        weatherRVModels.clear();
        //cityName="Istanbul";
        String url = "http://api.weatherapi.com/v1/forecast.json?key=3b434811dcc44355a1f180651230601&q="+ cityName + "&days=1&aqi=yes&alerts=yes";
        Log.d("City Name in getWeather", cityName);
        Log.d("Url", url);

        cityNameTextView.setText(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
//            loadingProgressBar.setVisibility(View.GONE);
//            homeRelativeLayout.setVisibility(View.VISIBLE);
//            weatherRVModels.clear();

            try {
                String temperature = response.getJSONObject("current").getString("temp_c");
                temperatureTextView.setText(temperature+ " °C");
                int isDay = response.getJSONObject("current").getInt("is_day");
                String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                Picasso.get().load("http:".concat(conditionIcon)).into(iconImageView);
                conditionTextView.setText(condition);
                if(isDay==1)
                    Picasso.get().load("https://images.unsplash.com/photo-1514475984160-d259c5f2cc89?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=774&q=80").into(backImageView);
                else
                    Picasso.get().load("https://images.unsplash.com/photo-1590418606746-018840f9cd0f?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8Mnx8Ymx1ZSUyMG5pZ2h0JTIwc2t5fGVufDB8fDB8fA%3D%3D&w=1000&q=80").into(backImageView);

                JSONObject foreCastObject = response.getJSONObject("forecast");
                JSONObject foreCast0 = foreCastObject.getJSONArray("forecastday").getJSONObject(0);
                JSONArray hourArray = foreCast0.getJSONArray("hour");
                for(int i =0; i < hourArray.length(); i++){
                    JSONObject hourObj = hourArray.getJSONObject(i);
                    String time = hourObj.getString("time");
                    String temper = hourObj.getString("temp_c");
                    String img = hourObj.getJSONObject("condition").getString("icon");
                    String wind = hourObj.getString("wind_kph");
                    weatherRVModels.add(new WeatherRVModel(time, temper, img, wind));
                }
                weatherRVAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Toast.makeText(MainActivity.this, "Please enter a valid city name..", Toast.LENGTH_SHORT).show());

        requestQueue.add(jsonObjectRequest);
    }
    public static String Get(String urlString){
        String json = null;
        URL url;
        HttpURLConnection httpURLConnection = null;
        try {
            url = new URL(urlString);
            httpURLConnection = (HttpURLConnection) url.openConnection();

            InputStream inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line ;
            while((line = bufferedReader.readLine()) != null){
                stringBuilder.append(line + "\n");
                Log.d("Line:", line);
            }
            bufferedReader.close();
            json=stringBuilder.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }
    private class HttpAsyncTask extends AsyncTask<String, Void, String>{
        @Override
        protected void onPostExecute(String s) {
            Log.d("String:", s);
            loadingProgressBar.setVisibility(View.GONE);
            homeRelativeLayout.setVisibility(View.VISIBLE);
            weatherRVModels.clear();
            try {
                JSONObject response = new JSONObject(s);
                String temperature = response.getJSONObject("current").getString("temp_c");
                temperatureTextView.setText(temperature+ " °C");
                int isDay = response.getJSONObject("current").getInt("is_day");
                String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                Picasso.get().load("http:".concat(conditionIcon)).into(iconImageView);
                conditionTextView.setText(condition);
                if(isDay==1)
                    Picasso.get().load("https://images.unsplash.com/photo-1514475984160-d259c5f2cc89?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=774&q=80").into(backImageView);
                else
                    Picasso.get().load("https://images.unsplash.com/photo-1590418606746-018840f9cd0f?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxzZWFyY2h8Mnx8Ymx1ZSUyMG5pZ2h0JTIwc2t5fGVufDB8fDB8fA%3D%3D&w=1000&q=80").into(backImageView);

                JSONObject foreCastObject = response.getJSONObject("forecast");
                JSONObject foreCast0 = foreCastObject.getJSONArray("forecastday").getJSONObject(0);
                JSONArray hourArray = foreCast0.getJSONArray("hour");
                for(int i =0; i < hourArray.length(); i++){
                    JSONObject hourObj = hourArray.getJSONObject(i);
                    String time = hourObj.getString("time");
                    String temper = hourObj.getString("temp_c");
                    String img = hourObj.getJSONObject("condition").getString("icon");
                    String wind = hourObj.getString("wind_kph");
                    weatherRVModels.add(new WeatherRVModel(time, temper, img, wind));
                }
                weatherRVAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            Log.d("doInBack", strings[0]);
            return Get(strings[0]);
        }
    }
}
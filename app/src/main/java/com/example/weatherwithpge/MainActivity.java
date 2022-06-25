package com.example.weatherwithpge;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.jar.JarException;

public class MainActivity extends AppCompatActivity {
    private RelativeLayout homeRL;
    private ProgressBar loadingPB;
    private TextView cityNameTV, temperatureTV, conditionTV;
    private RecyclerView weatherRV;
    private TextInputEditText cityEDt;
    private ImageView backIV, iconIV, searchIV;
    private ArrayList<WeatherRVModal> weatherRVModalArrayList;
    private WeatherRVAdapter weatherRVAdapter;
    private LocationManager locationManager;
    private int PERMISSION_CODE = 1;
    private String cityName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        
        setContentView(R.layout.activity_main);
        homeRL = findViewById(R.id.idRLHome);
        loadingPB = findViewById(R.id.idPBLoading);
        cityNameTV = findViewById(R.id.idIVCityName);
        temperatureTV = findViewById(R.id.idTVTemperature);
        conditionTV = findViewById(R.id.idTVCondition);
        weatherRV = findViewById(R.id.idRVWeather);
        cityEDt = findViewById(R.id.idEdtCity);
        backIV = findViewById(R.id.idIVBack);
        iconIV = findViewById(R.id.idIVIcon);
        searchIV = findViewById(R.id.idIVSearch);
        weatherRVModalArrayList = new ArrayList<>();
        weatherRVAdapter = new WeatherRVAdapter(this, weatherRVModalArrayList);
        weatherRV.setAdapter(weatherRVAdapter);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_CODE);
     
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        cityName = getCityName(location.getLongitude(),location.getLatitude());
        getWeatherInfo(cityName);
        searchIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            String city = cityEDt.getText().toString();
            if (city.isEmpty()){
                Toast.makeText(MainActivity.this,"Please Enter Your City Name Again",Toast.LENGTH_SHORT).show();
            }else{
                cityNameTV.setText(cityName);
                getWeatherInfo(city);
            }
                
            }
        });
        
        

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==PERMISSION_CODE){
            
            if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "Please Provide The Permission", Toast.LENGTH_SHORT).show();
            finish();
            }
            
        }
    }

    private String getCityName(double longitude, double latitude){
        String cityName = "Not found";
            Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
            try {
                List<Address> addresses = gcd.getFromLocation(latitude,longitude,10);
                for (Address adr : addresses){
                    if (adr!=null){
                        String city = adr.getLocality();
                        if (city!=null && !city.equals("")){
                            cityName = city;
                        }else {
                            Log.d("TAG","CITY NOT FOUND");
                            Toast.makeText(this,"Your City NOT FOUND",Toast.LENGTH_SHORT).show();
                        
                        }
                    }
                }
            }catch (IOException e){
                e.printStackTrace();
            }
            return cityName;
        }
        private void getWeatherInfo(String cityName) {
            String url = "http://api.weatherapi.com/v1/current.json?key=d543b73edb8c4d4abe0102704222006&q=" + cityName + "&aqi=no\n";
            cityNameTV.setText(cityName);
            RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    loadingPB.setVisibility(View.GONE);
                    homeRL.setVisibility(View.VISIBLE);
                    weatherRVModalArrayList.clear();
                    try {

                        String temperature = response.getJSONObject("current").getString("temp_c");
                        temperatureTV.setText(temperature + "°c");
                        int isDay = response.getJSONObject("current").getInt("is_day");
                        String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                        String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                        Picasso.get().load("http".concat(conditionIcon)).into(iconIV);
                        conditionTV.setText(condition);
                        if (isDay == 1) {
                            Picasso.get().load("https://www.google.com/search?q=weather+pics&sxsrf=ALiCzsZgzVDFkPabGtHKFdUMatVPTqdxHw:1656135855975&tbm=isch&source=iu&ictx=1&vet=1&fir=bRFAD4-o1Dl3jM%252CS4hF4obo_vp3eM%252C_%253BjnNo9nK2QD7VDM%252CS4hF4obo_vp3eM%252C_%253BmeJRc1PGIUO7FM%252C3ZwohAz6XFSktM%252C_%253BEEUcwXbrOk1BIM%252CS4hF4obo_vp3eM%252C_%253B6AyNZgUJDu1vjM%252COrJOMAS9QQwwMM%252C_%253BBaK1N9x1b4QCZM%252C3ZwohAz6XFSktM%252C_%253BZO8Q1t5BzDpnQM%252CVI-qqMG50jTsqM%252C_%253B8_VxZlx6HY_CZM%252COrJOMAS9QQwwMM%252C_%253Bf8EPjQEZdIkE3M%252CqhFi7I9SPTR7mM%252C_%253Bw3S-GyZQdpkspM%252CqhFi7I9SPTR7mM%252C_%253Bb2l0FvjzBqfkKM%252C1ok82qs8U1ATPM%252C_%253B0ahrlu967bS58M%252CMo57zPvJLA2fhM%252C_&usg=AI4_-kQk--B6v3lzD5XTidlp82ORthwXTw&sa=X&ved=2ahUKEwi4oJTr8sf4AhVUwoUKHUMoCp4Q9QF6BAgIEAE&biw=1366&bih=625&dpr=1#imgrc=B2TbJtTTgheLhM").into(backIV);
                        } else {
                            Picasso.get().load("https://www.google.com/search?q=weather+pics&sxsrf=ALiCzsZgzVDFkPabGtHKFdUMatVPTqdxHw:1656135855975&tbm=isch&source=iu&ictx=1&vet=1&fir=bRFAD4-o1Dl3jM%252CS4hF4obo_vp3eM%252C_%253BjnNo9nK2QD7VDM%252CS4hF4obo_vp3eM%252C_%253BmeJRc1PGIUO7FM%252C3ZwohAz6XFSktM%252C_%253BEEUcwXbrOk1BIM%252CS4hF4obo_vp3eM%252C_%253B6AyNZgUJDu1vjM%252COrJOMAS9QQwwMM%252C_%253BBaK1N9x1b4QCZM%252C3ZwohAz6XFSktM%252C_%253BZO8Q1t5BzDpnQM%252CVI-qqMG50jTsqM%252C_%253B8_VxZlx6HY_CZM%252COrJOMAS9QQwwMM%252C_%253Bf8EPjQEZdIkE3M%252CqhFi7I9SPTR7mM%252C_%253Bw3S-GyZQdpkspM%252CqhFi7I9SPTR7mM%252C_%253Bb2l0FvjzBqfkKM%252C1ok82qs8U1ATPM%252C_%253B0ahrlu967bS58M%252CMo57zPvJLA2fhM%252C_&usg=AI4_-kQk--B6v3lzD5XTidlp82ORthwXTw&sa=X&ved=2ahUKEwi4oJTr8sf4AhVUwoUKHUMoCp4Q9QF6BAgIEAE&biw=1366&bih=625&dpr=1#imgrc=QPij5XyTjwcRiM").into(backIV);
                        }
                        JSONObject forecastObj = response.getJSONObject("forecast");
                        JSONObject forecastO = forecastObj.getJSONArray("forecastday").getJSONObject(0);
                        JSONArray hourArray = forecastO.getJSONArray("hour");
                        for (int i = 0; i < hourArray.length(); i++) {
                            JSONObject hourObj = hourArray.getJSONObject(i);
                            String time = hourObj.getString("time");
                            String temper = hourObj.getString("temp_c");
                            String img = hourObj.getJSONObject("condition").getString("icon");
                            String wind = hourObj.getString("wind_kph");
                            weatherRVModalArrayList.add(new WeatherRVModal(time, temper, img, wind));

                        }
                        weatherRVAdapter.notifyDataSetChanged();


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }


            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(MainActivity.this, "Please Enter Valid City Name", Toast.LENGTH_SHORT).show();
                }
            });
            requestQueue.add(jsonObjectRequest);
        }


        }

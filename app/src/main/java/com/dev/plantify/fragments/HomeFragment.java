package com.dev.plantify.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.dev.plantify.Product;
import com.dev.plantify.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int REQUEST_LOCATION = 1;
    double latitude, longitude;
    LocationManager locationManager;
    private final ArrayList<WeatherRvModel> weatherRvModelArrayList = new ArrayList<>();
    private WeatherAdapter weatherAdapter;
    private String code;
    private String chance_of_rain = "1";
    private TextView txt_result;

    private RecyclerView dataRecyclerView;
    private TextView txt_data;
    private ProductAdapter productAdapter;
    private DatabaseReference databaseReference;

    public HomeFragment() {
        // Required empty public constructor
        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference().child("products");
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String mParam1 = getArguments().getString(ARG_PARAM1);
            String mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        txt_result = view.findViewById(R.id.txtView);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        weatherAdapter = new WeatherAdapter(weatherRvModelArrayList);
        recyclerView.setAdapter(weatherAdapter);

        productAdapter = new ProductAdapter(new ArrayList<>());
        dataRecyclerView = view.findViewById(R.id.dataRecyclerView);
        dataRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        dataRecyclerView.setAdapter(productAdapter);

        // Initialize locationManager
        locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);

        if (checkLocationEnabled(requireContext())) {
            getWeatherData();
        } else {
            showLocationDialog(requireContext());
        }

        retrieveDataFromFirebase();

        return view;
    }

    public boolean checkLocationEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private static void showLocationDialog(final Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Location Services Required")
                .setMessage("Please enable location services to use this feature.")
                .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        context.startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void getWeatherData() {
        getLocation();

        String apiKey = "157475c3ac704f07bdd182556242804";

        String url = "https://api.weatherapi.com/v1/forecast.json?key=" + apiKey + "&q=" + latitude + "," + longitude + "&days=1";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("response", response);

                        // Parse the response and update the RecyclerView
                        parseWeatherData(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle errors
                        Log.e("error_rrr", "Weather API request failed. URL: " + url);
                        error.printStackTrace();
                        Toast.makeText(getContext(), "Weather API request failed", Toast.LENGTH_SHORT).show();
                    }
                });

        // Add the request to the Volley queue
        Volley.newRequestQueue(getContext()).add(stringRequest);
    }

    private int calculateDaysDifference(long selectedDateMillis) {
        try {
            // Get the current date
            Date currentDate = new Date();

            // Calculate the difference in milliseconds
            long differenceMillis = currentDate.getTime() - selectedDateMillis;

            // Convert milliseconds to days
            return (int) (differenceMillis / (24 * 60 * 60 * 1000));

        } catch (Exception e) {
            return 0; // Handle the exception as needed
        }
    }

    private void parseWeatherData(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);

            JSONObject jsonObjectLocation = jsonResponse.getJSONObject("location");
            String cityName = jsonObjectLocation.getString("name");
            String region = jsonObjectLocation.getString("region");
            String country = jsonObjectLocation.getString("country");

            JSONObject jsonObjectCurrent = jsonResponse.getJSONObject("current");
            double temp = jsonObjectCurrent.getDouble("temp_c");
            Double feelsLike = jsonObjectCurrent.getDouble("feelslike_c");
            JSONObject jsonObjectCurrentCondition = jsonObjectCurrent.getJSONObject("condition");
            String text = jsonObjectCurrentCondition.getString("text");
            code = jsonObjectCurrentCondition.getString("code");

            JSONObject jsonObjectForecast = jsonResponse.getJSONObject("forecast");
            JSONArray jsonArray = jsonObjectForecast.getJSONArray("forecastday");
            JSONObject jsonObjectForecastday = jsonArray.getJSONObject(0);

            JSONObject jsonObjectDay = jsonObjectForecastday.getJSONObject("day");
            Double minTemp = jsonObjectDay.getDouble("mintemp_c");
            Double maxTemp = jsonObjectDay.getDouble("maxtemp_c");
            String visibility = jsonObjectDay.getString("avgvis_km");
            String willItRain = jsonObjectDay.getString("daily_will_it_rain");
            chance_of_rain = jsonObjectDay.getString("daily_chance_of_rain");
            String willItSnow = jsonObjectDay.getString("daily_will_it_snow");
            String chanceOfSnow = jsonObjectDay.getString("daily_chance_of_snow");
            String avghumidity = jsonObjectDay.getString("avghumidity");

            JSONObject jsonObjectAstro = jsonObjectForecastday.getJSONObject("astro");
            String sunrise = jsonObjectAstro.getString("sunrise");
            String sunset = jsonObjectAstro.getString("sunset");

            String output = String.format(Locale.getDefault(),
                    "Current weather - %s, %s (%s)\n" +
                            "Temperature : %.2f °C, Feels Like : %.2f °C\n" +
                            "Description : %s, Code : %s\n" +
                            "Minimum Temperature : %.2f °C, Maximum Temperature : %.2f °C\n" +
                            "Humidity : %s%%\nVisibility : %s km\n" +
                            "Chances of Rain : %s%%\nChances of Snow : %s%%",
                    cityName, region, country, temp, feelsLike, text, code, minTemp, maxTemp, avghumidity, visibility, chance_of_rain, chanceOfSnow);

            txt_result.setText(output);

            // Parse hourly data and update RecyclerView
            updateRecyclerView(jsonResponse);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateRecyclerView(JSONObject jsonResponse) {
        weatherRvModelArrayList.clear();

        try {
            JSONObject jsonObjectForecast = jsonResponse.getJSONObject("forecast");
            JSONArray jsonArray = jsonObjectForecast.getJSONArray("forecastday");
            JSONObject jsonObjectForecastday = jsonArray.getJSONObject(0);
            JSONArray jsonObjectHour = jsonObjectForecastday.getJSONArray("hour");

            for (int i = 0; i < jsonObjectHour.length(); i++) {
                JSONObject hour = jsonObjectHour.getJSONObject(i);
                String time = hour.getString("time");
                String temp_c = hour.getString("temp_c");
                double temp_f = hour.getDouble("temp_f");
                int isday = hour.getInt("is_day");
                JSONObject condition = hour.getJSONObject("condition");
                String txt = condition.getString("text");
                String icn = condition.getString("icon");
                String cod = condition.getString("code");
                Log.d("Hour", "onResponse: " + time + "_" + temp_c + "_" + temp_f + "_" + txt + "_" + icn + "_" + cod);
                weatherRvModelArrayList.add(new WeatherRvModel(time, temp_c, "https:"+icn, chance_of_rain));
            }

            weatherAdapter.notifyDataSetChanged();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getLocation() {

        //Check Permissions again
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(),

                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            Location LocationGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location LocationNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location LocationPassive = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            if (LocationGps != null) {
                double lat = LocationGps.getLatitude();
                double longi = LocationGps.getLongitude();

                latitude = (lat);
                longitude = (longi);

            } else if (LocationNetwork != null) {
                double lat = LocationNetwork.getLatitude();
                double longi = LocationNetwork.getLongitude();

                latitude = (lat);
                longitude = (longi);

            } else if (LocationPassive != null) {
                double lat = LocationPassive.getLatitude();
                double longi = LocationPassive.getLongitude();

                latitude = (lat);
                longitude = (longi);
            }
        }
    }

    private void retrieveDataFromFirebase() {
        // Add listener for data changes
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Clear existing data
                List<Product> products = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Retrieve product data
                    Product product = snapshot.getValue(Product.class);
                    products.add(product);
                }
                Log.d("FirebaseData", "Number of products: " + products.size());
                // Update RecyclerView with retrieved data
                productAdapter.setProducts(products);
                productAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {
                // Handle cancelled event
            }

        });
    }
}

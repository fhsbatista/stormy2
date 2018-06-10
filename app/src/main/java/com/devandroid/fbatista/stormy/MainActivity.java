package com.devandroid.fbatista.stormy;

import android.Manifest;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.devandroid.fbatista.stormy.databinding.ActivityMainBinding;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 10;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private ProgressBar mProgressBar;
    private ImageView mIconImageView;


    private CurrentWeather mCurrentWeather;
    private String apiKey = "6502de5433f9f1851ce385599cbc56f4";
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildGoogleApiClient();


    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

    }

    public void refreshData(View view) {
        handleLocation();
    }

    public void handleLocation(){
        onConnected(null);
        getForecastData(mLocation);
    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds


    }

    private void getForecastData(Location location) {
        final ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        mIconImageView = findViewById(R.id.iconImageView);
        mProgressBar = findViewById(R.id.progressBar50);
        binding.setIsVisible(false);


        final String url = "https://api.darksky.net/forecast/"
                + apiKey + "/" + latitude + "," + longitude;

        Log.v(TAG, url);

        if (inNetworkAvailable()) {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder().url(url).build();
            Call call = client.newCall(request);

            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    String jsonData = response.body().string();
                    Log.v(TAG, jsonData);
                    try {

                        if (response.isSuccessful()) {

                            mCurrentWeather = getCurrentDetails(jsonData);
                            mCurrentWeather.setLocationLabel(getCityName(latitude, longitude));
                            mCurrentWeather.setVisibility(View.VISIBLE);
                            final CurrentWeather displayWeather = new CurrentWeather(
                                    mCurrentWeather.getLocationLabel(),
                                    mCurrentWeather.getIcon(),
                                    mCurrentWeather.getTemperature(),
                                    mCurrentWeather.getHumidity(),
                                    mCurrentWeather.getPrecipation(),
                                    mCurrentWeather.getSummary(),
                                    mCurrentWeather.getTimezone(),
                                    mCurrentWeather.getTime(),
                                    mCurrentWeather.getVisibility()
                            );
                            binding.setWeather(displayWeather);
                            binding.setIsVisible(true);


                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Drawable drawable = getResources().getDrawable(displayWeather.getIconId());
                                    mIconImageView.setImageDrawable(drawable);
                                    mProgressBar.setVisibility(View.GONE);

                                }
                            });


                        } else {
                            alertUserAboutError();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSONException caught: " + e.getMessage());
                    }

                }
            });

        }

    }

    private String getCityName(double latitude, double longitude) {
        String locationDescription = "empty";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {

            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (!addresses.isEmpty()) {
                String cityName;
                String countryName;
                if (addresses.get(0).getLocality() != null) {
                    cityName = addresses.get(0).getLocality() + ", ";
                } else {
                    cityName = "";
                }
                if (addresses.get(0).getCountryName() != null) {
                    countryName = addresses.get(0).getCountryName();
                } else {
                    countryName = "";
                }


                locationDescription = cityName + countryName;
            }


        } catch (IOException e) {
            Log.e(TAG, "Erro ao pegar endereço");
        }


        return locationDescription;


    }


    private CurrentWeather getCurrentDetails(String jsonData) throws JSONException {

        JSONObject forecast = new JSONObject(jsonData);
        JSONObject currently = forecast.getJSONObject("currently");

        CurrentWeather currentWeather = new CurrentWeather();
        currentWeather.setTimezone(forecast.getString("timezone"));
        currentWeather.setHumidity(currently.getDouble("humidity"));
        currentWeather.setTime(currently.getLong("time"));
        currentWeather.setIcon(currently.getString("icon"));
        currentWeather.setLocationLabel("Monte Aprazive,SP");
        currentWeather.setPrecipation(currently.getDouble("precipProbability"));
        currentWeather.setSummary(currently.getString("summary"));

        //Converting the temperture to celsius (the webservice provides it in fahrenheit only)
        Double temperature = ((currently.getDouble("temperature") - 32) * 5) / 9;
        currentWeather.setTemperature(temperature);


        return currentWeather;
    }

    private boolean inNetworkAvailable() {

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        boolean isAvailable = false;

        if (networkInfo != null && networkInfo.isConnected())
            isAvailable = true;
        else
            alertUserAboutError();
        return isAvailable;

    }

    private void alertUserAboutError() {
        AlertDialogFragment dialogFragment = new AlertDialogFragment();
        dialogFragment.show(getFragmentManager(), "error_dialog");

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {


            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (mLocation != null) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

                getForecastData(mLocation);
            } else {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);


            }


        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        handleLocation();
    }


}

package com.devandroid.fbatista.stormy;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.devandroid.fbatista.stormy.databinding.ActivityMainBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private CurrentWeather mCurrentWeather;
    private ImageView mIconImageView;
    String apiKey = "6502de5433f9f1851ce385599cbc56f4";
    double latitude = -20.766653;
    double longitude = -49.705867;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getForecastData();
    }

    public void refreshData(View view){
        getForecastData();
    }

    private void getForecastData(){
        final ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mIconImageView = findViewById(R.id.iconImageView);
        final String url = "https://api.darksky.net/forecast/"
                + apiKey + "/" + latitude + "," + longitude;

        if(inNetworkAvailable()){
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

                        if(response.isSuccessful()){

                            mCurrentWeather = getCurrentDetails(jsonData);
                            final CurrentWeather displayWeather = new CurrentWeather(
                                    mCurrentWeather.getLocationLabel(),
                                    mCurrentWeather.getIcon(),
                                    mCurrentWeather.getTemperature(),
                                    mCurrentWeather.getHumidity(),
                                    mCurrentWeather.getPrecipation(),
                                    mCurrentWeather.getSummary(),
                                    mCurrentWeather.getTimezone(),
                                    mCurrentWeather.getTime()
                            );
                            binding.setWeather(displayWeather);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Drawable drawable = getResources().getDrawable(displayWeather.getIconId());
                                    mIconImageView.setImageDrawable(drawable);
                                }
                            });


                        } else{
                            alertUserAboutError();
                        }
                    } catch (JSONException e){
                        Log.e(TAG,"JSONException caught: " + e.getMessage());
                    }

                }
            });

        }

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

}

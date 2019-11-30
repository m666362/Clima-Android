package com.londonappbrewery.climapm;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import cz.msebera.android.httpclient.Header;


public class WeatherController extends AppCompatActivity {

    //TODO: Adding constant
    String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
    String APP_ID = "370079523b58d2df129ac6a05e7313b7";
    long MIN_TIME = 5000;
    float MIN_DISTANCE = 1000;

    //TODO: Set location provider here
    String LOCATION_PROVIDER = LocationManager.GPS_PROVIDER;

    //TODO: Member variable here
    TextView ma_city_label;
    TextView ma_temperature_label;
    ImageView ma_weather_image;
    ImageButton ma_change_city_button;

    //TODO: declare a location manager and locationlistener here
    LocationManager ma_location_manager;
    LocationListener ma_location_listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.weather_controller_layout );

        ma_city_label = findViewById( R.id.locationTV );
        ma_weather_image = findViewById( R.id.weatherSymbolIV );
        ma_temperature_label = findViewById( R.id.tempTV );
        ma_change_city_button = findViewById( R.id.changeCityButton );

        //todo: add an onclicklistener to the amibchangecitybutton
        ma_change_city_button.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent( WeatherController.this, ChangeCityActivity.class );
                startActivity( myIntent );
            }
        } );

    }


    //todo: add onresume() here
    String city;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.d( "Clima", "onresume() called" );
        Intent myIntent = getIntent();
        city = myIntent.getStringExtra( "City" );

        if (city != null) {

            getWeatherforNewCity();
        } else {
            Log.d( "Clima", "Getting weather for current location" );
            getWeatherForCurrentLocation();
        }

    }

    // TODO: Add getWeatherForNewCity(String city) here:
    private void getWeatherforNewCity() {

        RequestParams params = new RequestParams();
        params.put( "q", city );
        params.put( "appid", APP_ID );
        letsDoSomeNetworking( params );
    }


    //todo: add getweatherforcurrentlocation() here
    private void getWeatherForCurrentLocation() {

        Dexter.withActivity( this )
                .withPermissions( Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION )
                .withListener( new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        checkLocationInit();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                } )
                .check();
    }

    @SuppressLint("MissingPermission")
    private void checkLocationInit() {
        ma_location_manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        ma_location_listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                Log.d( "Clima", "onLocationChanged() call back received" );
                String ma_longitude = String.valueOf( location.getLongitude() );
                String ma_lattitude = String.valueOf( location.getLatitude() );

                Log.d( "Clima", "Longitude is: " + ma_longitude );
                Log.d( "Clima", "Latitude is: " + ma_lattitude );

                RequestParams params = new RequestParams();
                params.put( "lat", ma_lattitude );
                params.put( "lon", ma_longitude );
                params.put( "appid", APP_ID );
                letsDoSomeNetworking( params );

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d( "Clima", "onStatusChanged: " );
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d( "Clima", "onProviderEnabled: " );
            }

            @Override
            public void onProviderDisabled(String provider) {

                Log.d( "Clima", "onProviderDisabled call back received" );
            }
        };

        Criteria criteria = new Criteria();
        criteria.setAccuracy( Criteria.ACCURACY_MEDIUM );
        criteria.setCostAllowed( false );
        String providerName = ma_location_manager.getBestProvider( criteria, true );
//and then you can make location update request with selected best provider
        ma_location_manager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER, 400, 1, ma_location_listener );
    }


    // TODO: Add letsDoSomeNetworking(RequestParams params) here:
    private void letsDoSomeNetworking(RequestParams params) {

        AsyncHttpClient client = new AsyncHttpClient();
        client.get( WEATHER_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                Log.d( "Clima", "Success Json" + response.toString() );
                WeatherDataModel weatherData = WeatherDataModel.fromJson( response );
                updateUI( weatherData );
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                Log.e( "Clima", "Fail" + e.toString() );
                Log.d( "Clima", "Status Code" + statusCode );
                Toast.makeText( WeatherController.this, "Request failed", Toast.LENGTH_SHORT ).show();
            }
        } );

    }


    // TODO: Add updateUI() here:
    private void updateUI(WeatherDataModel weather) {

        ma_temperature_label.setText( weather.getmTemperature() );
        ma_city_label.setText( weather.getmCity() );

        int resourceID = getResources().getIdentifier( weather.getmIconName(), "drawable", getPackageName() );
        ma_weather_image.setImageResource( resourceID );
    }

    // TODO: Add onPause() here:

    @Override
    protected void onPause() {
        super.onPause();
        if (ma_location_manager != null)
            ma_location_manager.removeUpdates( ma_location_listener );
    }
}

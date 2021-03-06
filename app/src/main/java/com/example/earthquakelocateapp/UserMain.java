package com.example.earthquakelocateapp;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.maps.android.data.Feature;
import com.google.maps.android.data.geojson.GeoJsonLayer;
import com.google.maps.android.data.geojson.GeoJsonPoint;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserMain extends AppCompatActivity implements OnMapReadyCallback {
    private static final int SPEAK_REQUEST=10;
    private ImageButton voiceButton;
    private EditText saidWords;
    private GoogleMap mMap;
    private TextView yourLocation;
    private static final int REQUEST_LOCATION = 1;
    LocationManager locationManager;
    Location locat; // location

    private EarthApi earthApi;
    private List<Marker> quakeMarkers;

    private JSONObject Cityjson,Countryjson;

    double latitude, longitude;

    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS status
    boolean canGetLocation = false;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);
        voiceButton=findViewById(R.id.voice);
        saidWords=findViewById(R.id.sW);
        yourLocation=findViewById(R.id.urLocation);
        Retrofit retrofit= new Retrofit.Builder().baseUrl("https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/").addConverterFactory(GsonConverterFactory.create()).build();
        earthApi=retrofit.create(EarthApi.class);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.g_map);
        mapFragment.getMapAsync(this);
        voiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speach();
            }
        });

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
        }

        getLocation();
    }
    private void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(UserMain.LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                showSettingsAlert();
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    //check the network permission
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
                    }

                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, new LocationListener() {
                        @Override
                        public void onLocationChanged(@NonNull Location location) {
                            if (location != null) {
                                locat=location;
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                getCompleteAddressString(latitude,longitude);
                                Toast.makeText(UserMain.this,"location Updated",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (locat == null) {
                        //check the network permission
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
                        }

                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, new LocationListener() {
                            @Override
                            public void onLocationChanged(@NonNull Location location) {
                                if (location != null) {
                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                    getCompleteAddressString(latitude,longitude);
                                    Toast.makeText(UserMain.this,"location Updated",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showSettingsAlert(){
        androidx.appcompat.app.AlertDialog.Builder alertDialog = new androidx.appcompat.app.AlertDialog.Builder(UserMain.this);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }


    private void speach(){
        PackageManager packageManager= this.getPackageManager();
        List<ResolveInfo> listofinfo=packageManager.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH),0);
        if(listofinfo.size()>0){
            Toast.makeText(this, "Your Device does support voice recognition", Toast.LENGTH_SHORT).show();
            Intent voiceIntent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            voiceIntent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Talk to me....");
            voiceIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            voiceIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,10);
            startActivityForResult(voiceIntent,SPEAK_REQUEST);
        }
        else{
            Toast.makeText(this, "Your Device does not support voice recognition", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==SPEAK_REQUEST && resultCode==RESULT_OK){
            ArrayList<String> voiceWords=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            float[] conf=data.getFloatArrayExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES);
            int index=0;
            for(String userWord:voiceWords){
                if(conf!=null && index<conf.length){
                    saidWords.setText(userWord);
                }
            }
        }
    }

    private void getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                yourLocation.setText(strAdd);
            } else {
                yourLocation.setText(""+LATITUDE+LONGITUDE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            yourLocation.setText(""+LATITUDE+LONGITUDE);
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //seattle coordinates
        addquake(mMap);
    }

    void addquake(GoogleMap myMap){
        Call<Detail>call=earthApi.getDetails();
        call.enqueue(new Callback<Detail>() {
            @Override
            public void onResponse(Call<Detail> call, Response<Detail> response) {
                if(!response.isSuccessful()){
                    Toast.makeText(UserMain.this,"Code"+response.code(),Toast.LENGTH_LONG).show();
                    return;
                }
                Detail detail=response.body();
                double mlat;
                double mlong;
                double mag;
                ArrayList<JsonObject> features=detail.getFeatures();
                for(JsonObject jsonObject : features){
                    JsonObject jsonObject1=(JsonObject) jsonObject.get("properties");
                    mag= jsonObject1.get("mag").getAsDouble();
                    JsonObject jsonObject2=(JsonObject) jsonObject.get("geometry");
                    JsonArray point;
                    point=jsonObject2.get("coordinates").getAsJsonArray();
                    mlat=point.get(0).getAsDouble();
                    mlong=point.get(1).getAsDouble();
                    LatLng quake= new LatLng(mlat,mlong);
                    myMap.addMarker(new MarkerOptions().position(quake).title(""+mag));
                }
            }

            @Override
            public void onFailure(Call<Detail> call, Throwable t) {
                Toast.makeText(UserMain.this,""+t.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }
}
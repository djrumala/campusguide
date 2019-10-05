package com.common_id.campusguide;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.webkit.PermissionRequest;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminFormClassActivity extends AppCompatActivity {

    private EditText name, longitude, latitude, floor;
    private ImageButton getCurLocButton, getLocMapsButton;
    private Button saveButton, delLocButton, getLocButton;
    private RelativeLayout infoLayout;
    private String Longitude, Latitude, Name, Floor, IdClass;

    private boolean mLocationPermissionGranted = false;
    private Integer PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 111;

    private static final String TAG = MainActivity.class.getSimpleName();

    // location last updated time
    private String mLastUpdateTime;

    // location updates interval - 10sec
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    // fastest updates interval - 5 sec
    // location updates will be received if another app is requesting the locations than your app can handle
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 5000;

    private static final int REQUEST_CHECK_SETTINGS = 100;
    // bunch of location related apis
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;

    private GoogleMap mMap;
    // boolean flag to toggle the ui
    private Boolean mRequestingLocationUpdates;

    private String IPAddress = "common-id.com";
    private String HttpURL = "http://" + IPAddress + "/campusguide";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_form_class);

        Toolbar mytoolbar = (Toolbar) findViewById(R.id.mCustomToolbar);
        setSupportActionBar(mytoolbar); //mempersiapkan toolbar
        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        name = findViewById(R.id.name);
        longitude = findViewById(R.id.lon);
        latitude = findViewById(R.id.lat);
        floor = findViewById(R.id.floor);
        saveButton = findViewById(R.id.save);
        infoLayout = findViewById(R.id.info);
        getCurLocButton = findViewById(R.id.getloc);
        getLocMapsButton = findViewById(R.id.openmap);

        init(); //Needed for GPS init to get current place
        restoreValuesFromBundle(savedInstanceState); // restore the values from saved instance state

//        Intent intent = getIntent();

        if (this.getIntent().getExtras() != null) {
            Bundle extras = getIntent().getExtras();
            Longitude = extras.getString("longitude", "0");
            Latitude = extras.getString("latitude", "0");
            Name = extras.getString("name", "");
            Floor = extras.getString("floor", "");
            IdClass = extras.getString("id", "0");

            Log.e("latitude", Latitude);
            Log.e("longitude", Longitude);

            if(!Latitude.equals("0")&&!Longitude.equals("0")) {
                longitude.setText(Longitude);
                latitude.setText(Latitude);
            }
            floor.setText(Floor);
            name.setText(Name);

            if(IdClass.equals("0"))
                getSupportActionBar().setTitle("Tambah Ruang Kelas");
            else
                getSupportActionBar().setTitle("Edit Ruang Kelas");
        }

        getLocMapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoLayout.setVisibility(View.GONE);

                Intent intent = new Intent(AdminFormClassActivity.this, MarkerPositionActivity.class);
                intent.putExtra("name", Name);
                intent.putExtra("floor", Floor);
                intent.putExtra("id", IdClass);
                startActivity(intent);
            }
        });


        getCurLocButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                delLocButton.setVisibility(View.VISIBLE);
//                getLocButton.setVisibility(View.GONE);
                startLocationButtonClick();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckEditTextIsEmptyOrNot();
            }
        });
    }

    public void CheckEditTextIsEmptyOrNot() {
        boolean cancel = false;
        View focusView = null;

        longitude.setError(null);//reset error
        latitude.setError(null);//reset error
        name.setError(null);//reset error
        floor.setError(null);//reset error

        Longitude = longitude.getText().toString();
        Latitude = latitude.getText().toString();
        Name = name.getText().toString();
        Floor = floor.getText().toString();

        if (TextUtils.isEmpty(Longitude)) {
            longitude.setError("Longitude harus diisi");
            focusView = longitude;
            cancel = true;
        }
        if (TextUtils.isEmpty(Latitude)) {
            latitude.setError("Latitude harus diisi");
            focusView = latitude;
            cancel = true;
        }
        if (TextUtils.isEmpty(Name)) {
            name.setError("Nama harus diisi");
            focusView = name;
            cancel = true;
        }
        if (TextUtils.isEmpty(Floor)) {
            floor.setError("Lantai harus diisi");
            focusView = floor;
            cancel = true;
        }
        if (cancel) {
            focusView.requestFocus();
        }
        if (!cancel) {
            DialogSubmit();
        }
    }

    public void DialogSubmit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper
                (this, R.style.myAlertDialogTheme));
        builder.setTitle("CampusGuide");
        if (IdClass.equals("0"))
            builder.setMessage("Apakah Anda ingin menambahkan kelas ini?");
        else
            builder.setMessage("Apakah Anda yakin untuk mengubah data kelas ini?");
        builder.setIcon(R.drawable.logo);
        builder.setCancelable(true);

        builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new SaveDataClass(AdminFormClassActivity.this).execute();
            }

        });

        builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private class SaveDataClass extends AsyncTask<Void, Void, Void> {
        public Context context;
        String FinalResult;
        ProgressDialog progressDialog;

        public SaveDataClass(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(AdminFormClassActivity.this, ProgressDialog.THEME_HOLO_LIGHT);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle("CampusGuide");
            progressDialog.setMessage("Loading Data...");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpServiceClass httpServiceClass = null;

            httpServiceClass = new HttpServiceClass(HttpURL + "/class.php");
            if (IdClass.equals("0")) {

                httpServiceClass.AddParam("add", "1");
                httpServiceClass.AddParam("nama", Name);
                httpServiceClass.AddParam("floor", Floor);
                httpServiceClass.AddParam("lat", Latitude);
                httpServiceClass.AddParam("long", Longitude);
            } else {
                httpServiceClass = new HttpServiceClass(HttpURL + "/class.php");

                httpServiceClass.AddParam("edit", "1");
                httpServiceClass.AddParam("id", IdClass);
                httpServiceClass.AddParam("nama", Name);
                httpServiceClass.AddParam("floor", Floor);
                httpServiceClass.AddParam("lat", Latitude);
                httpServiceClass.AddParam("long", Longitude);
            }
            try {
                httpServiceClass.ExecutePostRequest();
                if (httpServiceClass.getResponseCode() == 200) {
                    FinalResult = httpServiceClass.getResponse();

                } else {
                    Toast.makeText(context, httpServiceClass.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            if (IdClass.equals("0"))
                Toast.makeText(AdminFormClassActivity.this, "Ruang kelas ditambahkan", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(AdminFormClassActivity.this, "Ruang kelas diedit", Toast.LENGTH_LONG).show();

            finish();
            Intent intent = new Intent(AdminFormClassActivity.this, AdminClassActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }
    }

    private void init() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // location is received
                mCurrentLocation = locationResult.getLastLocation();
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

                updateLocationUI();
            }
        };

        mRequestingLocationUpdates = false;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();

    }

    /**
     * Restoring values from saved instance state
     */
    private void restoreValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("is_requesting_updates")) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean("is_requesting_updates");
            }

            if (savedInstanceState.containsKey("last_known_location")) {
                mCurrentLocation = savedInstanceState.getParcelable("last_known_location");
            }

            if (savedInstanceState.containsKey("last_updated_on")) {
                mLastUpdateTime = savedInstanceState.getString("last_updated_on");
            }
        }

        updateLocationUI();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("is_requesting_updates", mRequestingLocationUpdates);
        outState.putParcelable("last_known_location", mCurrentLocation);
        outState.putString("last_updated_on", mLastUpdateTime);

    }

    private void updateLocationUI() {
        if (mCurrentLocation != null) {
            String address = null, locality = null, subLocality = null, province = null, city = null, country, postalCode = null, knownName;
            Geocoder gcd = new Geocoder(getBaseContext(),
                    Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = gcd.getFromLocation(mCurrentLocation.getLatitude(), mCurrentLocation
                        .getLongitude(), 1);
                if (addresses.size() > 0)
                    System.out.println(addresses.get(0).getLocality());
                //cityName=addresses.get(0).getLocality();
                //address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                locality = addresses.get(0).getLocality();
                subLocality = addresses.get(0).getSubLocality();
                province = addresses.get(0).getAdminArea();
                address = addresses.get(0).getThoroughfare();
                city = addresses.get(0).getSubAdminArea();
                country = addresses.get(0).getCountryName();
                postalCode = addresses.get(0).getPostalCode();
                knownName = addresses.get(0).getFeatureName();
//                if (subLocality != null) {
//                    locality = locality + "," + subLocality;
//                } else {
//                    locality = locality;
//                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            String s = "Lat: " + mCurrentLocation.getLatitude() + ", " +
                    "Lng: " + mCurrentLocation.getLongitude() +
                    "\n\nMy Currrent City is: " + address + ", " + subLocality + ", " + locality + ", " + province + city;

//            Alamat.setText(address);
//            Alamat.setAlpha(0); //giving a blink animation on TextView
//            Alamat.animate().alpha(1).setDuration(300);
//
//            Kota.setText(locality + ", " + city + ", " + province + ", " + postalCode);
//            Kota.setAlpha(0);
//            Kota.animate().alpha(1).setDuration(300);
//
//            String alamat = address + ", " + subLocality + ", " + locality + ", " + city + ", " + province + ", " + postalCode;
//            SharedPreferences pref = getSharedPreferences(GeneralPref, Context.MODE_PRIVATE);
//
//            SharedPreferences.Editor editor = pref.edit();
//            editor.putString(mCity, city);
//            editor.putString(mStreet, address);
//            editor.putString(mFieldAddress, alamat);
//            editor.putString(mProvince, province);
//            editor.putString(mLat, String.valueOf(mCurrentLocation.getLatitude()));
//            editor.putString(mLong, String.valueOf(mCurrentLocation.getLongitude()));
//            editor.apply();

            longitude.setText(String.valueOf(mCurrentLocation.getLongitude()));
            latitude.setText(String.valueOf(mCurrentLocation.getLatitude()));

            longitude.setAlpha(0);
            longitude.animate().alpha(1).setDuration(300);
            latitude.setAlpha(0);
            latitude.animate().alpha(1).setDuration(300);

//            // location last updated time
//            txtUpdatedOn.setText("Last updated on: " + mLastUpdateTime);
            stopLocationButtonClick();
        }
//        toggleButtons();
    }

    public void startLocationButtonClick() {
        // Requesting ACCESS_FINE_LOCATION using Dexter library
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        mRequestingLocationUpdates = true;
                        startLocationUpdates();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            // open device settings when the permission is
                            // denied permanently
                            openSettings();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(com.karumi.dexter.listener.PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }

                }).check();
    }

    private void startLocationUpdates() {
        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

//                        Toast.makeText(getApplicationContext(), "Started location updates!", Toast.LENGTH_SHORT).show();

                        //noinspection MissingPermission
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        updateLocationUI();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(AdminFormClassActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);

                                Toast.makeText(AdminFormClassActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }

                        updateLocationUI();
                    }
                });
    }

    public void stopLocationButtonClick() {
        mRequestingLocationUpdates = false;
        stopLocationUpdates();
    }

    public void stopLocationUpdates() {
        // Removing location updates
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
//                        Toast.makeText(getApplicationContext(), "Location updates stopped!", Toast.LENGTH_SHORT).show();
//                        toggleButtons();
                    }
                });
    }

    public void showLastKnownLocation() {
        if (mCurrentLocation != null) {
            Toast.makeText(getApplicationContext(), "Lat: " + mCurrentLocation.getLatitude()
                    + ", Lng: " + mCurrentLocation.getLongitude(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Last known location is not available!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.e(TAG, "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.e(TAG, "User chose not to make required location settings changes.");
                        mRequestingLocationUpdates = false;
                        break;
                }
                break;
        }
    }

    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",
                BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Resuming location updates depending on button state and
        // allowed permissions
        if (mRequestingLocationUpdates && checkPermissions()) {
            startLocationUpdates();
        }

        updateLocationUI();
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mRequestingLocationUpdates) {
            // pausing location updates
            stopLocationUpdates();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //when the action button is tapped
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed(); //go back to previous activity or fragment
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        stopLocationButtonClick();
        finish();
        Intent intent = new Intent(AdminFormClassActivity.this, AdminClassActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
//        finish();
    }
}

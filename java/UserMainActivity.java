package com.common_id.campusguide;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.PermissionRequest;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.common_id.campusguide.Adapter.Classroom;
import com.common_id.campusguide.Adapter.ClassroomAdapter;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class UserMainActivity extends AppCompatActivity implements OnMapReadyCallback {

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
    private MarkerOptions options = new MarkerOptions();
    private View mapView;
    private BitmapDescriptor marks;
    private Marker marksource;
    private LatLng awal, tujuan;
    private Polyline polyline;
    // boolean flag to toggle the ui
    private Boolean mRequestingLocationUpdates;
    private ArrayList<LatLng> mMarkerPoints;

    private LinearLayout infoatasLayout, chooseButton;
    private RelativeLayout infobawahLayout;
    private TextView alamat, longitude, latitude, kanan, kiri;

    private String Latitude, Longitude, Name, Floor, IdClass, Address;
    private ImageButton settingButton, searchButton;

    public static final String MyPreferences = "Myprefs";
    //    public static final String GeneralPref = "Generalprefs";
    public static final String mLong = "longitudeKey";
    public static final String mLat = "latitudeKey";
    private String IPAddress = "common-id.com";
    private String HttpURL = "http://" + IPAddress + "/campusguide";

    private ClassroomAdapter classroomAdapter;
    private List<Classroom> classroomList = new ArrayList<>();
    private ArrayList<LatLng> latlngs = new ArrayList<>();
    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);

        infoatasLayout = findViewById(R.id.infatas);
        infobawahLayout = findViewById(R.id.info);
        chooseButton = findViewById(R.id.lin_button);
        alamat = findViewById(R.id.alamat);
        longitude = findViewById(R.id.longitude);
        latitude = findViewById(R.id.latitude);
        kiri = findViewById(R.id.tv_lat);
        kanan = findViewById(R.id.tv_lon);
        settingButton = findViewById(R.id.setting);
        searchButton = findViewById(R.id.searh);

        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserMainActivity.this, ProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserMainActivity.this, UserSearchActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();

        init(); //Needed for GPS init to get current place
        // restore the values from saved instance state
        restoreValuesFromBundle(savedInstanceState);
        startLocationButtonClick();
//        new ParseJSonDataClass(this).execute();
        getLocationPermission();
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            new ParseJSonDataClass(UserMainActivity.this).execute();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Anda menyetujui izin lokasi", Toast.LENGTH_SHORT).show();
                mLocationPermissionGranted = true;
            }
        }
        updateLocationUI();
    }

    class ParseJSonDataClass extends AsyncTask<Void, Void, Void> {
        public Context context;
        String FinalJSonResult;

        public ParseJSonDataClass(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            HttpServiceClass httpServiceClass = new HttpServiceClass(HttpURL + "/class.php");
            httpServiceClass.AddParam("list", "1");

            try {
                httpServiceClass.ExecuteGetRequest();
                if (httpServiceClass.getResponseCode() == 200) {
                    FinalJSonResult = httpServiceClass.getResponse().trim();

                    JSONArray jsonArray = null;
                    try {
                        jsonArray = new JSONArray(FinalJSonResult);
                        JSONObject jsonObject;
                        Classroom classroom;

                        classroomList = new ArrayList<Classroom>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            classroom = new Classroom();
                            jsonObject = jsonArray.getJSONObject(i);

                            classroom.setId(jsonObject.getString("id"));
                            classroom.setName(jsonObject.getString("nama"));
                            classroom.setFloor(jsonObject.getString("floor"));
                            classroom.setLongitude(jsonObject.getString("long"));
                            classroom.setLatitude(jsonObject.getString("lat"));
                            classroomList.add(classroom);
                        }

                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    //}

                } else {
                    Toast.makeText(context, httpServiceClass.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
//            Log.e("Respon ", FinalJSonResult);

            for (int i = 0; i < classroomList.size(); i++) {
                Double lat = Double.parseDouble(classroomList.get(i).getLatitude());
                Double lon = Double.parseDouble(classroomList.get(i).getLongitude());
                latlngs.add(new LatLng(lat, lon)); //some latitude and logitude value
            }
            if (classroomList.size() == 0) {
                Toast.makeText(UserMainActivity.this, "Tidak ditemukan ruang kelas", Toast.LENGTH_SHORT).show();
            }
//                Toast.makeText(InsideActivity.this, cd + City +" "+ Latitude + " "+Longitude, Toast.LENGTH_SHORT).show();

            String nama = "Lapangan ITS";
            String snippet = "Kampus ITS";
            int i = 0;
            for (LatLng point : latlngs) {
                options.position(point);
                nama = classroomList.get(i).getName();
                snippet = "Lantai " + classroomList.get(i).getFloor();
                options.title(nama);
                options.snippet(snippet);
                Bitmap bitmap = resizeMapIcons("logo", 56, 80);
                options.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
                mMap.addMarker(options);
                //Moving camera to the closest field
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latlngs.get(0)));
                // Zoom in, animating the camera.
                mMap.animateCamera(CameraUpdateFactory.zoomIn());
                // Zoom out to zoom level 10, animating with a duration of 2 seconds.
                mMap.animateCamera(CameraUpdateFactory.zoomTo(14), 2000, null);
                i++;
            }
//            progressBar.setVisibility(View.GONE);
//            recyclerView.setVisibility(View.VISIBLE);

//            classroomAdapter.notifyDataSetChanged();
//            classroomAdapter = new ClassroomAdapter(AdminClassActivity.this, classroomList, AdminClassActivity.this);
//            recyclerView.setAdapter(classroomAdapter);
        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=true&units=metric&mode=walking&key=AIzaSyBu6lTlBTHqaI8j87sDBQUxdCmE89Sn2Vc";
        //Driving mode
//        String sensor = "sensor=true&units=metric&mode=driving&key=AIzaSyBu6lTlBTHqaI8j87sDBQUxdCmE89Sn2Vc";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        Log.e("URL", url);
        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    /**
     * A class to download data from Google Directions URL
     */
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();
            DurationTask durationTask = new DurationTask();
            DistanceTask distanceTask= new DistanceTask();
            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
            durationTask.execute(result);
            distanceTask.execute(result);
        }
    }

    /**
     * A class to parse the Google Directions in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            if (polyline != null)
                polyline.remove();
            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(8);
                lineOptions.color(getResources().getColor(R.color.colorPrimary));
            }

            // Drawing polyline in the Google Map for the i-th route
            polyline = mMap.addPolyline(lineOptions);
        }
    }

    /**
     * A class to parse the Google Directions in JSON format
     */
    private class DurationTask extends AsyncTask<String, Integer, Integer> {

        // Parsing the data in non-ui thread
        @Override
        protected Integer doInBackground(String... jsonData) {

            JSONObject jObject;
            Integer duration = 0;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                duration = parser.getDuration(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return duration;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(Integer totalSecs) {
            Integer hours = totalSecs / 3600;
            Integer minutes = (totalSecs % 3600) / 60;
            Integer seconds = totalSecs % 60;

            String timeString = null;
            if (hours == 0)
                timeString = String.format("%2d menit %2d detik", minutes, seconds);
            else if(hours>0)
                timeString = String.format("%2d jam %2d menit %2d detik", hours, minutes, seconds);
//            result = result / 60;
            longitude.setText(timeString);
            kanan.setText("Durasi");
//            Toast.makeText(UserMainActivity.this, timeString, Toast.LENGTH_SHORT).show();
        }
    }
    private class DistanceTask extends AsyncTask<String, Integer, Integer> {

        // Parsing the data in non-ui thread
        @Override
        protected Integer doInBackground(String... jsonData) {

            JSONObject jObject;
            Integer duration = 0;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                duration = parser.getDistance(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return duration;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(Integer meters) {
            Integer km = meters / 1000;
            Integer m = meters % 100;

            String timeString = null;
            if (km == 0)
                timeString = String.format("%2d meter", m);
            else if(km>0)
                timeString = String.format("%2d,%02d Km", km, m);
//            result = result / 60;
            latitude.setText(timeString);
            kiri.setText("Jarak");
        }
    }

    private void drawMarker(LatLng point) {
        mMarkerPoints.add(point);

        // Creating MarkerOptions
        MarkerOptions options = new MarkerOptions();

        // Setting the position of the marker
        options.position(point);

        /**
         * For the start location, the color of marker is GREEN and
         * for the end location, the color of marker is RED.
         */
        if (mMarkerPoints.size() == 1) {
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        } else if (mMarkerPoints.size() == 2) {
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }

        // Add new marker to the Google Map Android API V2
        mMap.addMarker(options);
    }

    public Bitmap resizeMapIcons(String iconName, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
//        mMap.setOnMapClickListener(this);

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker googleMarker) {
                for (int position = 0; position < classroomList.size(); position++) {
                    Classroom classroom = classroomList.get(position);
                    String Name = classroom.getName();

                    if (googleMarker.getTitle().equals(Name)) {
                        String IdClass = classroom.getId();
                        String Floor = classroom.getFloor();
                        String Longitude = classroom.getLongitude();
                        String Latitude = classroom.getLatitude();
                        Intent intent = new Intent(UserMainActivity.this, UserDetailActivity.class);
                        intent.putExtra("latitude", Latitude);
                        intent.putExtra("longitude", Longitude);
                        intent.putExtra("name", Name);
                        intent.putExtra("floor", Floor);
                        intent.putExtra("id", IdClass);
                        intent.putExtra("address", Address);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                    }
                }

            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.getId();
                for (int position = 0; position < classroomList.size(); position++) {
                    Classroom classroom = classroomList.get(position);
                    String NameHolder = classroom.getName();

                    if (marker.getTitle().equals(NameHolder)) {
                        Name = classroom.getName();
                        IdClass = classroom.getId();
                        Floor = classroom.getFloor();
                        Longitude = classroom.getLongitude();
                        Latitude = classroom.getLatitude();

                        Double lat = Double.parseDouble(classroom.getLatitude());
                        Double lon = Double.parseDouble(classroom.getLongitude());

                        tujuan = new LatLng(lat, lon);

                        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
                        List<Address> addresses = null;
                        try {
                            addresses = gcd.getFromLocation(lat, lon, 1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        String locality = addresses.get(0).getLocality();
                        String subLocality = addresses.get(0).getSubLocality();
                        String province = addresses.get(0).getAdminArea();
                        String address = addresses.get(0).getThoroughfare();
                        String city = addresses.get(0).getSubAdminArea();
                        Address = address + ", " + subLocality + ", " + locality + ", " + province;

                        infobawahLayout.setVisibility(View.VISIBLE);
                        infoatasLayout.setVisibility(View.VISIBLE);
                        chooseButton.setVisibility(View.VISIBLE);
                        alamat.setText(Address);

                        Latitude = classroom.getLatitude();
                        Longitude = classroom.getLongitude();
//                        tujuan = Latitude + ","+Longitude;

                        longitude.setText(Longitude);
                        latitude.setText(Latitude);
                        kanan.setText("Longitude");
                        kiri.setText("Latitude");

                        // Getting URL to the Google Directions API
                    }

                }
                return false;
            }
        });
        // Do other setup activities here too, as described elsewhere in this tutorial.

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
//        getDeviceLocation();
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
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                mMap.getUiSettings().setCompassEnabled(true);

            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mMap.getUiSettings().setCompassEnabled(false);
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
        //move the position of direction
        mMap.setPadding(0, 0, 30, 175);
        //Set button position of google maps on right bottom
        if (mMap != null && mapView.findViewById(Integer.parseInt("1")) != null) {
            // Get the button view
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 30, 185);

        }

        if (mCurrentLocation != null) { //GET CURRENT LOCATION BY GPS
//            awal = String.valueOf(mCurrentLocation.getLatitude())+"," + String.valueOf(mCurrentLocation.getLongitude());
            LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            awal = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
//            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
//            mMap.animateCamera(cameraUpdate);

            if (marksource != null)
                marksource.remove(); //remove last marker of current location
            Bitmap bitmap = resizeMapIcons("mark_src", 48, 48);
            marksource = mMap.addMarker(new MarkerOptions().position(latLng).title("Current Location").
                    icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
//                    icon(BitmapDescriptorFactory.fromResource(R.drawable.mark_src)));
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));

            String address = null, locality = null, subLocality = null, province = null, city = null, country, postalCode = null, knownName;
            Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
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

            String s = "Lat: " + mCurrentLocation.getLatitude() + ", " + "Lng: " + mCurrentLocation.getLongitude() +
                    "\nMy Currrent City is: " + address + ", " + subLocality + ", " + locality + ", " + province + city;
            Log.e(TAG, s);
//            Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
        }
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
                                    rae.startResolutionForResult(UserMainActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);

                                Toast.makeText(UserMainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
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

    public void SelectLocation(View view) {
        String url = getDirectionsUrl(awal, tujuan);// Already map contain destination location
        DownloadTask downloadTask = new DownloadTask();
        // Start downloading json data from Google Directions API
        downloadTask.execute(url);
//        GMapV2Direction md = new GMapV2Direction(awal, tujuan, GMapV2Direction.MODE_DRIVING);
//        Document doc = md.getDocument(awal, tujuan, GMapV2Direction.MODE_DRIVING);
//
//        ArrayList<LatLng> directionPoint = md.getDirection(doc);
//        PolylineOptions rectLine = new PolylineOptions().width(3).color(
//                Color.RED);
//
//        for (int i = 0; i < directionPoint.size(); i++) {
//            rectLine.add(directionPoint.get(i));
//        }
//        Polyline polylin = mMap.addPolyline(rectLine);
//        Intent intent = new Intent(this, UserDetailActivity.class);
//        intent.putExtra("latitude", Latitude);
//        intent.putExtra("longitude", Longitude);
//        intent.putExtra("name", Name);
//        intent.putExtra("address", Address);
//        intent.putExtra("floor", Floor);
//        intent.putExtra("id", IdClass);
//        startActivity(intent);
//        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed() {
        stopLocationButtonClick();
        finishAffinity();
//        Intent intent = new Intent(RoomsActivity.this, MainActivity.class);
//        startActivity(intent);
//        finish();
//        finish();
    }
}

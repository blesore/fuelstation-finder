package com.ktenas.orestis.p03078.fuelstationfinder;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.ktenas.orestis.p03078.fuelstationfinder.PointProviderService.LocalBinder;
import com.ktenas.orestis.p03078.fuelstationfinder.SettingsFragment.OnFilterChangeListener;
import com.ktenas.orestis.p03078.fuelstationfinder.entities.Fuel;
import com.ktenas.orestis.p03078.fuelstationfinder.entities.FuelStation;
import com.ktenas.orestis.p03078.fuelstationfinder.enums.FuelType;

public class MainActivity extends AbstractMapAndTrackActivity implements LocationListener, OnMarkerClickListener, OnFilterChangeListener {

    private SharedPreferences prefs;
    private PointProviderService boundService;
    private boolean isServiceBound = false;
    private static ExecutorService threadPoolExecutor;
    private GoogleMap map;
    private MapFragment mapFragment;
    private static final String MAP_FRAGMENT_TAG = "map";
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Location lastKnownLocation;
    private Location lastUpdateLocation;
    private static Map<String, FuelStation> stationPoints = new HashMap<>();
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocalBinder binder = (LocalBinder) service;
            boundService = binder.getService();
            isServiceBound = true;
            Log.i("PointProviderService", "Point Provider Service Connected");
        }
    };
    // Metrics for camera update
    private int distanceThreshold = 100; // in meters
    private final float SPEED_THRESHOLD = 0.5f; // in meters/sec
    private final int UPDATE_INTERVAL = 7000;
    private final int FASTEST_UPDATE_INTERVAL = 5000;
    private int initialZoom = 10;
    private int numberOfPoints;
    private String fuelType;
    private String stationBrand;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        setDrivingMode(prefs.getString("driving_mode", "0"));
        fuelType  = prefs.getString("fuel_type", "0");
        stationBrand = prefs.getString("station_brand", "0");
        Log.i("Filters", "fuel_type: " + fuelType + " station_brand: " + stationBrand + " numPoints: " + numberOfPoints);
        

        CameraPosition initialCameraPosition = new CameraPosition(new LatLng(37.9245587, 23.8192248), initialZoom, 30, 0);
        
        if (servicesConnected()) {
            // It isn't possible to set a fragment's id programmatically so we set a tag instead and
            // search for it using that.
            mapFragment = (MapFragment) getFragmentManager().findFragmentByTag(MAP_FRAGMENT_TAG);

            if (savedInstanceState == null) {
                // We only create a fragment if it doesn't already exist.
                if (mapFragment == null) {
                    GoogleMapOptions googleMapOptions = new GoogleMapOptions();
                    googleMapOptions.mapType(GoogleMap.MAP_TYPE_NORMAL).camera(initialCameraPosition).compassEnabled(true).rotateGesturesEnabled(true)
                            .scrollGesturesEnabled(true).zoomGesturesEnabled(true).zoomControlsEnabled(false).tiltGesturesEnabled(true);
                    mapFragment = MapFragment.newInstance(googleMapOptions);
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.add(R.id.map_container, mapFragment, MAP_FRAGMENT_TAG);
                    fragmentTransaction.commit();

                    // First incarnation of this activity.
                    mapFragment.setRetainInstance(true);
                }
            } else {
                // Reincarnated activity. The obtained map is the same map instance in the previous activity life cycle.
                // There is no need to reinitialize it.
                map = mapFragment.getMap();
            }

            setUpMapIfNeeded();
            threadPoolExecutor = Executors.newFixedThreadPool(5);
            
            // Setup location services client
            googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this)
                    .build();

            locationRequest = LocationRequest.create().setFastestInterval(FASTEST_UPDATE_INTERVAL).setInterval(UPDATE_INTERVAL)
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();

        Intent serviceIntent = new Intent(this, PointProviderService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        googleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        // In case Google Play services has since become available.
        setUpMapIfNeeded();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onStop() {
        googleApiClient.disconnect();
        super.onStop();

        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!threadPoolExecutor.isTerminated()) {
            threadPoolExecutor.shutdown();
            try {
                threadPoolExecutor.awaitTermination(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (map == null) {
            map = mapFragment.getMap();
            // Check if we were successful in obtaining the map.
            if (map != null) {
                // The Map is verified. It is now safe to manipulate the map.
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                map.setOnMarkerClickListener(this);
            } else {
                Toast.makeText(this, R.string.no_maps, Toast.LENGTH_LONG).show();
            }
        } else {
            // set reincarnated activity as listener
            map.setOnMarkerClickListener(this);
        }
    }

    // Location Services callbacks
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        super.onConnectionFailed(connectionResult);
    }

    @Override
    public void onConnected(Bundle dataBundle) {
        super.onConnected(dataBundle);
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        super.onConnectionSuspended(arg0);
    }

    // Callable for worker thread to fetch points
    private Callable<List<FuelStation>> getPointsTask = new Callable<List<FuelStation>>() {

        @Override
        public List<FuelStation> call() {
            return boundService.getPoints(lastUpdateLocation, fuelType, stationBrand, numberOfPoints);
        }
    };

    @Override
    public void onLocationChanged(Location currentLocation) {
        lastKnownLocation = currentLocation;
        if (googleApiClient.isConnected() || mapFragment != null) {
            if (lastUpdateLocation == null) {
                lastUpdateLocation = currentLocation;
                // fetch first set of points
                updatePoints(threadPoolExecutor.submit(getPointsTask));
            }
            CameraPosition currentCameraPosition = map.getCameraPosition();
            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
                    .bearing((currentLocation.getSpeed() < SPEED_THRESHOLD) ? lastUpdateLocation.getBearing() : currentLocation.getBearing())
                    .zoom(currentCameraPosition.zoom).tilt(currentCameraPosition.tilt).build();
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            // get new set of points only if user has moved
            if (currentLocation.distanceTo(lastUpdateLocation) > distanceThreshold) {
                lastUpdateLocation = currentLocation;
                updatePoints(threadPoolExecutor.submit(getPointsTask));
            }
        }
    }

    // call service methods
    private void updatePoints(Future<List<FuelStation>> future) {
        if (boundService != null) {
            //clear map from previous points
            map.clear();
            stationPoints.clear();
            try {
                List<FuelStation> points = future.get();
                for (FuelStation point : points) {
                    // delegate marker building to the background to unload main thread
                    new BubbleTask().executeOnExecutor(threadPoolExecutor, point);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                Toast.makeText(this, "Update service is unavailable", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    // Marker related methods
    @Override
    public boolean onMarkerClick(Marker marker) {
        DialogFragment stationInfo = StationInfoDialogFragment.newInstance(lastKnownLocation, stationPoints.get(marker.getId()));
        stationInfo.show(getFragmentManager(), "station info");
        return true;
    }

    // AsyncTask to build marker drawables
    private class BubbleTask extends AsyncTask<FuelStation, Void, MarkerOptions> {

        protected IconGenerator iconGenerator = new IconGenerator(getApplicationContext());
        protected FuelStation processedPoint;

        @Override
        protected MarkerOptions doInBackground(FuelStation... points) {
            Bitmap b;
            synchronized (iconGenerator) {
                processedPoint = points[0];
                ViewGroup contentView = (ViewGroup) getLayoutInflater().inflate(R.layout.custom_marker_layout, null);
                ImageView logoIcon = (ImageView) contentView.findViewById(R.id.station_brand_placeholder);
                logoIcon.setImageResource(processedPoint.getBrand().getLogo());
                TextView price = (TextView) contentView.findViewById(R.id.marker_price);
                for (Fuel f : processedPoint.getAvailableFuel()) {
                    if ( f.getFuelType() == decodeFuelType(fuelType)) {
                        price.setText(Float.toString(f.getPrice()/1000));
                    }
                }
                int style = IconGenerator.STYLE_WHITE;
                if (processedPoint.isCheapestInRange()) {
                    style = IconGenerator.STYLE_GREEN;
                }
                iconGenerator.setContentView(contentView);
                iconGenerator.setStyle(style);
                b = iconGenerator.makeIcon();
            }
            return new MarkerOptions().position(points[0].getLocation()).icon(BitmapDescriptorFactory.fromBitmap(b));
        }

        @Override
        protected void onPostExecute(MarkerOptions result) {
            Marker marker = map.addMarker(result);
            stationPoints.put(marker.getId(), processedPoint);
            super.onPostExecute(result);
        }
    }

    @Override
    public void onFilterChange(Preference pref) {
        String key = pref.getKey();
        switch (key) {
            case "fuel_type":
                fuelType = prefs.getString("fuel_type", "0");
                break;
            case "station_brand":
                stationBrand = prefs.getString("station_brand", "0");
                break;
            case "driving_mode":
                setDrivingMode(prefs.getString("driving_mode", "1"));
                break;
        }
        updatePoints(threadPoolExecutor.submit(getPointsTask));
    }
    
    private void setDrivingMode(String mode) {
        switch (mode) {
            case "0":
                initialZoom = 16;
                distanceThreshold = 500;
                numberOfPoints = 25;
                break;
            case "1":
                initialZoom = 15;
                distanceThreshold = 1000;
                numberOfPoints = 17;
                break;
            case "2":
                initialZoom = 14;
                distanceThreshold = 3500;
                numberOfPoints = 10;
                break;
            default:
                initialZoom = 15;
                distanceThreshold = 1000;
                numberOfPoints = 17;
                break;
        }
        Log.i("Filters", "fuel_type: " + fuelType + " station_brand: " + stationBrand + " numPoints: " + numberOfPoints);
    }

    private FuelType decodeFuelType(String type) {
        switch (type) {
            case "0":
                return FuelType.UNLEADED_95;
            case "1":
                return FuelType.UNLEADED_100;
            case "2":
                return FuelType.DIESEL;
            case "3":
                return FuelType.AUTOGAS;
            default:
                return FuelType.UNLEADED_95;
        }
    }
}
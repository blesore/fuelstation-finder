package com.ktenas.orestis.p03078.fuelstationfinder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.WindowManager;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ktenas.orestis.p03078.fuelstationfinder.PointProviderService.LocalBinder;
import com.ktenas.orestis.p03078.fuelstationfinder.entities.FuelStation;

public class MyMapActivity extends AbstractMapAndTrackActivity implements
		LocationListener, OnMarkerClickListener {

	private SharedPreferences prefs;
	private PointProviderService boundService;
	boolean isServiceBound = false;
	private ExecutorService singleThreadExecutor;
	private GoogleMap map;
	private MapFragment mapFragment;
	private static final String MAP_FRAGMENT_TAG = "map";
	private CameraPosition DEFAULT_CAMERA_POSITION = new CameraPosition(
			new LatLng(37.9245587, 23.8192248), 10, 30, 0);
	private GoogleApiClient googleApiClient;
	private LocationRequest locationRequest;
	private Location lastKnownLocation;
	private static Map<String, FuelStation> stationPoints;
	static {
		stationPoints = new HashMap<>();
	}
	private final int UPDATE_INTERVAL = 5000;
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
			Toast.makeText(MyMapActivity.this, "Service Connected",
					Toast.LENGTH_SHORT).show();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);

		if (servicesConnected()) {
			// mapFragment = (MapFragment)
			// getFragmentManager().findFragmentById(
			// R.id.map);

			// It isn't possible to set a fragment's id programmatically so
			// we set a tag instead and
			// search for it using that.
			mapFragment = (MapFragment) getFragmentManager().findFragmentByTag(
					MAP_FRAGMENT_TAG);

			if (savedInstanceState == null) {
				// We only create a fragment if it doesn't already exist.
				if (mapFragment == null) {
					GoogleMapOptions googleMapOptions = new GoogleMapOptions();
					googleMapOptions.mapType(GoogleMap.MAP_TYPE_NORMAL)
							.camera(DEFAULT_CAMERA_POSITION)
							.compassEnabled(true).rotateGesturesEnabled(true)
							.scrollGesturesEnabled(true)
							.zoomGesturesEnabled(true)
							.zoomControlsEnabled(false)
							.tiltGesturesEnabled(true);
					mapFragment = MapFragment.newInstance(googleMapOptions);
					FragmentTransaction fragmentTransaction = getFragmentManager()
							.beginTransaction();
					fragmentTransaction.add(R.id.map_container, mapFragment,
							MAP_FRAGMENT_TAG);
					fragmentTransaction.commit();

					// First incarnation of this activity.
					mapFragment.setRetainInstance(true);
				}
			} else {
				// Reincarnated activity. The obtained map is the same map
				// instance in the previous
				// activity life cycle. There is no need to reinitialize it.
				map = mapFragment.getMap();
			}

			setUpMapIfNeeded();
			singleThreadExecutor = Executors.newSingleThreadExecutor();

			// Setup location services client
			googleApiClient = new GoogleApiClient.Builder(this)
					.addApi(LocationServices.API).addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this).build();

			locationRequest = LocationRequest.create()
					.setInterval(UPDATE_INTERVAL)
					.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

			prefs = PreferenceManager.getDefaultSharedPreferences(this);
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
		if (!singleThreadExecutor.isTerminated()) {
			singleThreadExecutor.shutdown();
			try {
				singleThreadExecutor.awaitTermination(1, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		super.onConnectionFailed(connectionResult);
	}

	@Override
	public void onConnected(Bundle dataBundle) {
		super.onConnected(dataBundle);
		LocationServices.FusedLocationApi.requestLocationUpdates(
				googleApiClient, locationRequest, this);
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		super.onConnectionSuspended(arg0);
	}

	private Callable<Set<FuelStation>> task = new Callable<Set<FuelStation>>() {

		@Override
		public Set<FuelStation> call() {
			Set<FuelStation> relativePoints = boundService
					.getPoints(lastKnownLocation);
			return relativePoints;
		}
	};

	@Override
	public void onLocationChanged(Location currentLocation) {
		lastKnownLocation = currentLocation;
		if (googleApiClient.isConnected() || mapFragment != null) {
			LatLng latLng = new LatLng(currentLocation.getLatitude(),
					currentLocation.getLongitude());
			CameraPosition cameraPosition = new CameraPosition.Builder()
					.target(latLng).bearing(currentLocation.getBearing())
					.zoom(15).build();
			map.animateCamera(CameraUpdateFactory
					.newCameraPosition(cameraPosition));
			if (boundService != null) {
				Future<Set<FuelStation>> future = singleThreadExecutor
						.submit(task);
				try {
					for (FuelStation point : future.get()) {
						addMarker(map, point);
					}
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (map == null) {
			map = mapFragment.getMap();
			// Check if we were successful in obtaining the map.
			if (map != null) {
				// The Map is verified. It is now safe to manipulate the map.
				map.setMyLocationEnabled(true);
				map.getUiSettings().setMyLocationButtonEnabled(false);
				map.setOnMarkerClickListener(this);
			} else {
				Toast.makeText(this, R.string.no_maps, Toast.LENGTH_LONG)
						.show();
			}
		} else {
			// set reincarnated activity as listener
			map.setOnMarkerClickListener(this);
		}
	}

	private void addMarker(GoogleMap map, FuelStation point) {
		Marker marker = map.addMarker(new MarkerOptions().position(point
				.getPosition()));
		String markerId = marker.getId();
		if (!stationPoints.containsKey(markerId)) {
			stationPoints.put(markerId, point);
		}
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		DialogFragment stationInfo = StationInfoDialogFragment.newInstance(
				lastKnownLocation, stationPoints.get(marker.getId()));
		stationInfo.show(getFragmentManager(), "station info");
		return true;
	}
}
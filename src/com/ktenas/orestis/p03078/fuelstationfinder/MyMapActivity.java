package com.ktenas.orestis.p03078.fuelstationfinder;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ktenas.orestis.p03078.fuelstationfinder.PointProviderService.LocalBinder;

public class MyMapActivity extends AbstractMapAndTrackActivity implements
		LocationListener {

	PointProviderService boundService;
	boolean isServiceBound = false;
	private Set<LatLng> relativePoints;
	private ExecutorService singleThreadExecutor;
	GoogleMap map;
	private MapFragment mapFragment;
	private GoogleApiClient googleApiClient;
	private LocationRequest locationRequest;
	private Location lastKnownLocation;
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
		// setContentView(R.layout.activity_map);
		if (servicesConnected()) {
			mapFragment = (MapFragment) getFragmentManager().findFragmentById(
					R.id.map);
			if (savedInstanceState == null) {
				// First incarnation of this activity.
				mapFragment.setRetainInstance(true);
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
		}
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.my_map, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.legal) {
			startActivity(new Intent(this, LegalInfoActivity.class));

			return (true);
		}
		return super.onOptionsItemSelected(item);
	}

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

	private Callable<Set<LatLng>> task = new Callable<Set<LatLng>>() {

		@Override
		public Set<LatLng> call() {
			Set<LatLng> relativePoints = boundService
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
				Future<Set<LatLng>> future = singleThreadExecutor.submit(task);

				try {
					for (LatLng point : future.get()) {
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
			} else {
				Toast.makeText(this, R.string.no_maps, Toast.LENGTH_LONG)
						.show();
			}
		}
	}

	private void addMarker(GoogleMap map, LatLng position) {
		map.addMarker(new MarkerOptions().position(position));
	}
}
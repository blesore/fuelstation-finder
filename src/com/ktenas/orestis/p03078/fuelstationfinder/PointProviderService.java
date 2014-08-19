package com.ktenas.orestis.p03078.fuelstationfinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.ktenas.orestis.p03078.fuelstationfinder.entities.Fuel;
import com.ktenas.orestis.p03078.fuelstationfinder.entities.FuelStation;
import com.ktenas.orestis.p03078.fuelstationfinder.entities.FuelType;
import com.ktenas.orestis.p03078.fuelstationfinder.entities.StationBrand;

public class PointProviderService extends Service {
	private final IBinder localBinder = new LocalBinder();
	private static Set<FuelStation> points = new HashSet<>();

	public Set<FuelStation> getPoints(Location location) {
		double lat;
		double lng;
		if (points.isEmpty()) {
			try {
				InputStream is = getAssets().open("randomPoints.txt");
				BufferedReader br = new BufferedReader(
						new InputStreamReader(is));
				String line;
				String[] temp = new String[2];

				while ((line = br.readLine()) != null) {
					temp = line.split(",");
					lat = Double.parseDouble(temp[0]);
					lng = Double.parseDouble(temp[1]);
					points.add(new FuelStation(0, null, null, StationBrand.AEGEAN, new LatLng(lat, lng), Arrays.asList(new Fuel[]{new Fuel(FuelType.UNLEADED_95, (float) 1.685)}), new Date(2014, 8, 18)));
				}
				br.close();
			} catch (IOException e) {
				Log.e("Parsing Error",
						"Something went wrong while parsing the file");
				e.printStackTrace();
			}
		}
		return points;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return localBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		super.onStartCommand(intent, flags, startId);
		return Service.START_NOT_STICKY;
	}

	public class LocalBinder extends Binder {
		PointProviderService getService() {
			return PointProviderService.this;
		}
	}
}

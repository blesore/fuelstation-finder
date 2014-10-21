package com.ktenas.orestis.p03078.fuelstationfinder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.ktenas.orestis.p03078.fuelstationfinder.entities.Fuel;
import com.ktenas.orestis.p03078.fuelstationfinder.entities.FuelStation;
import com.ktenas.orestis.p03078.fuelstationfinder.enums.FuelType;

public class PointProviderService extends Service {
    private final IBinder localBinder = new LocalBinder();

    public List<FuelStation> getPoints(Location location, String fuelType, String stationBrand, int numOfPoints) {
        //request url
        final String url = "http://62.38.27.218:8087/fuelstations?lat=" + location.getLatitude() + "&lon=" + location.getLongitude() + "&fuelType=" + fuelType
                + "&brand=" + stationBrand + "&numOfPoints=" + numOfPoints;

        // Set the Accept header
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(Collections.singletonList(new MediaType("application", "json")));
        HttpEntity<?> requestEntity = new HttpEntity<>(requestHeaders);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        Log.i("REST Call", "Sending request to remote server for points");
        ResponseEntity<FuelStation[]> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, FuelStation[].class);
        Log.i("REST Call", "Response received.");
        List<FuelStation> points = Arrays.asList(responseEntity.getBody());
        // find cheapest
        FuelStation cheapest = null;
        float lowestPrice = Float.MAX_VALUE;
        for (FuelStation fs : points) {
            for (Fuel f : fs.getAvailableFuel()) {
                if (decodeFuelType(fuelType) == f.getFuelType() && f.getPrice() <= lowestPrice) {
                    cheapest = fs;
                }
            }
        }
        cheapest.setCheapestInRange(true);
        return points;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }

    public class LocalBinder extends Binder {
        PointProviderService getService() {
            return PointProviderService.this;
        }
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

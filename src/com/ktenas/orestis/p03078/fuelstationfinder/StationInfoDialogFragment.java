package com.ktenas.orestis.p03078.fuelstationfinder;

import java.text.SimpleDateFormat;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.ktenas.orestis.p03078.fuelstationfinder.entities.Fuel;
import com.ktenas.orestis.p03078.fuelstationfinder.entities.FuelStation;
import com.ktenas.orestis.p03078.fuelstationfinder.entities.FuelType;

public class StationInfoDialogFragment extends DialogFragment implements
		DialogInterface.OnClickListener {

	private View mainContainer = null;
	private FuelStation station;
	private Location myLocation;

	public static StationInfoDialogFragment newInstance(Location myLocation,
			FuelStation station) {
		StationInfoDialogFragment fragment = new StationInfoDialogFragment();
		// set Fragmentclass Arguments
		Bundle args = new Bundle();
		args.putParcelable("my location", myLocation);
		args.putParcelable("clicked marker", station);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		station = getArguments().getParcelable("clicked marker");
		myLocation = getArguments().getParcelable("my location");
		mainContainer = getActivity().getLayoutInflater().inflate(
				R.layout.info_window, null);
		List<Fuel> fuelTypes = station.getAvailableFuel();
		int i = 0;
		for(Fuel fuel : fuelTypes) {
			// put values into container
			RelativeLayout listItem = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.fuel_list_item, null);
			TextView label = (TextView) listItem.findViewById(R.id.fuel_type_label);
			label.setText(fuel.getFuelType().toString());
			TextView value = (TextView) listItem.findViewById(R.id.fuel_type_title);
			value.setText(Float.toString(fuel.getPrice()));
			LinearLayout ll = (LinearLayout) mainContainer.findViewById(R.id.fuel_info_container);
			ll.addView(listItem);
			// set date of last update
			TextView lastUpdated = (TextView) mainContainer.findViewById(R.id.last_updated_value);
			lastUpdated.setText(station.getLastUpdated().toString());
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(station.getBrand().toString()).setView(mainContainer)
				.setIcon(station.getBrand().getLogo()).setPositiveButton(R.string.drive_me_btn, this)
				.setNegativeButton(R.string.close_btn, this);
		return builder.create();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			driveMe();
		}
	}

	public void driveMe() {
		LatLng stationPosition = station.getPosition();
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
				Uri.parse("http://maps.google.com/maps?saddr="
						+ myLocation.getLatitude() + ","
						+ myLocation.getLongitude() + "&daddr="
						+ stationPosition.latitude + ","
						+ stationPosition.longitude));
		startActivity(intent);
	}
}

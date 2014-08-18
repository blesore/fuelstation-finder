package com.ktenas.orestis.p03078.fuelstationfinder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;
import com.ktenas.orestis.p03078.fuelstationfinder.entities.FuelStation;

public class StationInfoDialogFragment extends DialogFragment implements
		DialogInterface.OnClickListener {

	private View layout = null;
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
		layout = getActivity().getLayoutInflater().inflate(
				R.layout.info_window, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(station.getBrand().toString()).setView(layout)
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

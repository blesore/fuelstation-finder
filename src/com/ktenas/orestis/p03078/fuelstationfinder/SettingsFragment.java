package com.ktenas.orestis.p03078.fuelstationfinder;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.text.TextUtils;

public class SettingsFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);
		// Initialize pref summary label.
		setPreferenceSummary(findPreference("driving_mode"));
		setPreferenceSummary(findPreference("fuel_type"));
		setPreferenceSummary(findPreference("station_brand"));
	}

	@Override
	public void onResume() {
		super.onResume();
		getPreferenceManager().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
		getPreferenceManager().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Preference preference = findPreference(key);
		if (preference instanceof ListPreference) {
			setPreferenceSummary(preference);
		} else {
			// For all other preferences, set the summary to the value's
			// simple string representation.
			// preference.setSummary(stringValue);
		}
	}

	// Set the summary to reflect the new value.
	private void setPreferenceSummary(Preference preference) {

		if (preference instanceof ListPreference) {
			ListPreference listPref = (ListPreference) preference;
			CharSequence entry = listPref.getEntry();
			preference.setSummary(!TextUtils.isEmpty(entry) ? entry : null);
		}
	}
}

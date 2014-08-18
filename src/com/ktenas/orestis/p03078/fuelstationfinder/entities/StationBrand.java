package com.ktenas.orestis.p03078.fuelstationfinder.entities;

import com.ktenas.orestis.p03078.fuelstationfinder.R;

public enum StationBrand {
	AEGEAN(R.drawable.aegean_logo),
	AVIN(R.drawable.avin_logo),
	BP(R.drawable.bp_logo),
	CYCLON(R.drawable.cyclon_logo),
	EKO(R.drawable.eko_logo),
	ELIN(R.drawable.elin_logo),
	ETEKA(R.drawable.eteka_logo),
	JETOIL(R.drawable.jetoil_logo),
	REVOIL(R.drawable.revoil_logo),
	SHELL(R.drawable.shell_logo);
	
	private int logo;

	private StationBrand(int logo) {
		this.setLogo(logo);
	}

	public int getLogo() {
		return logo;
	}

	public void setLogo(int logo) {
		this.logo = logo;
	}
}

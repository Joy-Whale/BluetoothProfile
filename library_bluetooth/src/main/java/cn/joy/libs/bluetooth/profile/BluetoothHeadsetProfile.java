package cn.joy.libs.bluetooth.profile;

import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

/**
 * **********************
 * Author: J
 * Date:   2016/5/11
 * Time:   17:07
 * **********************
 */
 class BluetoothHeadsetProfile extends BaseBluetoothProfileImpl<BluetoothHeadset> {

	public BluetoothHeadsetProfile(Context context) {
		super(context);
	}

	@Override
	public int getProfileCode() {
		return BluetoothProfile.HEADSET;
	}
}

package cn.joy.libs.bluetooth.profile;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

/**
 * **********************
 * Author: J
 * Date:   2016/5/11
 * Time:   17:38
 * **********************
 */
class BluetoothA2dpProfile extends BaseBluetoothProfileImpl<BluetoothA2dp> {

	public BluetoothA2dpProfile(Context context) {
		super(context);
	}

	@Override
	public int getProfileCode() {
		return BluetoothProfile.A2DP;
	}
}

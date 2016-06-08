package cn.joy.libs.bluetooth.profile;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import static cn.joy.libs.bluetooth.profile.Profile.*;

/**
 * **********************
 * Author: J
 * Date:   2016/5/11
 * Time:   17:43
 * **********************
 */
public class DeviceBluetoothProfileManager {

	private Context context;
	private Map<Profile, IBluetoothProfile> profileMap = new HashMap<>();

	public DeviceBluetoothProfileManager(Context context) {
		initProfiles(this.context = context);
	}

	private void initProfiles(Context context) {
		profileMap.put(A2dp, new BluetoothA2dpProfile(context));
		profileMap.put(Headset, new BluetoothHeadsetProfile(context));
	}

	public void setProfiles(Profile... profiles) {
		profileMap.clear();
		for (Profile profile : profiles) {
			profileMap.put(profile, profile.create(context));
		}
	}

	public boolean addProfile(Profile profile) {
		return profileMap.containsKey(profile) || profileMap.put(profile, profile.create(context)) != null;
	}

	public boolean removeProfile(Profile profile) {
		return profileMap.remove(profile) != null;
	}

	public void connect(BluetoothDevice device) {
		for (IBluetoothProfile profile : profileMap.values()) {
			profile.connect(device);
		}
	}

	public boolean connect(BluetoothDevice device, Profile profile) {
		if (!profileMap.containsKey(profile)) {
			profileMap.put(profile, profile.create(context));
		}
		return profileMap.get(profile) != null && profileMap.get(profile).connect(device);
	}

	public void disconnect(BluetoothDevice device) {
		for (IBluetoothProfile profile : profileMap.values()) {
			profile.disconnect(device);
		}
	}

	public boolean disconnect(BluetoothDevice device, Profile profile) {
		return profileMap.containsKey(profile) && profileMap.get(profile) != null && profileMap.get(profile).disconnect(device);
	}

	public void close() {
		for (IBluetoothProfile profile : profileMap.values()) {
			profile.close();
		}
		profileMap.clear();
		profileMap = null;
		context = null;
	}
}

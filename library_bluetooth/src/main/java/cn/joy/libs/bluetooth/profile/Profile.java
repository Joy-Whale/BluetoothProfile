package cn.joy.libs.bluetooth.profile;

/**
 * **********************
 * Author: J
 * Date:   2016/5/11
 * Time:   17:51
 * **********************
 */
public enum Profile {

	A2dp(BluetoothA2dpProfile.class),
	//	Dun,
	//	Gatt,
	Headset(BluetoothHeadsetProfile.class),
	//	Map,
	//	Opp,
	//	Pan,
	//	Pbap,
	//	Sap
	;

	private Class<? extends IBluetoothProfile> profileClass;

	Profile(Class<? extends IBluetoothProfile> profileClass) {
		this.profileClass = profileClass;
	}

	public IBluetoothProfile create(Object... obj) {
		try {
			return profileClass.getConstructor(obj.getClass()).newInstance(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}

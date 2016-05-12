package cn.joy.libs.bluetooth.profile;

/**
 * **********************
 * Author: J
 * Date:   2016/5/11
 * Time:   17:51
 * 蓝牙profile的枚举
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

	/**
	 * 根据当前的profile创建一个蓝牙profile实例
	 * @param obj 实例化该profile所需要的参数
	 * @return 一个新的profile实例，如果实例化失败，则为null
	 */
	public IBluetoothProfile create(Object... obj) {
		try {
			return profileClass.getConstructor(obj.getClass()).newInstance(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}

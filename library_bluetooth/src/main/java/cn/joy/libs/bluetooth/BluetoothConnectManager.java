package cn.joy.libs.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.lang.reflect.Method;

import cn.joy.libs.bluetooth.profile.DeviceBluetoothProfileManager;
import cn.joy.libs.bluetooth.profile.Profile;
import cn.joy.libs.bluetooth.spp.BluetoothSppHelper;

/**
 * **********************
 * Author: J
 * Date:   2016/6/1
 * Time:   11:50
 * **********************
 */
public class BluetoothConnectManager implements BluetoothSppHelper.BluetoothSocketListener {

	/** HFP状态改变广播 {@see android.bluetooth.BluetoothHeadset} */
	private static final String ACTION_HFP_CONNECT_STATE = "android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED";
	private static final String EXTRA_HFP_CONNECT_STATE = "android.bluetooth.profile.extra.STATE";
	private static final int HPF_STATE_DISCONNECTED = 0;
	private static final int HPF_STATE_CONNECTED = 2;

	private Context context;
	private DeviceBluetoothProfileManager mProfileManager;
	private BluetoothSppHelper mSpp;

	private BluetoothCallback mCallback;

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			switch (intent.getAction()) {
				case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
					switch (intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE)) {
						case BluetoothDevice.BOND_BONDED:
							if (mCallback != null) {
								mCallback.onBondedStateChanged(device, true);
							}
							break;
						case BluetoothDevice.BOND_NONE:
							if (mCallback != null) {
								mCallback.onBondedStateChanged(device, false);
							}
							break;
					}
					break;
				case ACTION_HFP_CONNECT_STATE:
					switch (intent.getIntExtra(EXTRA_HFP_CONNECT_STATE, HPF_STATE_DISCONNECTED)) {
						case HPF_STATE_CONNECTED:
							if (mCallback != null) {
								mCallback.onProfileConnectedStateChanged(device, Profile.Headset, true);
							}
							break;
						case HPF_STATE_DISCONNECTED:
							if (mCallback != null) {
								mCallback.onProfileConnectedStateChanged(device, Profile.Headset, false);
							}
							break;
					}
					break;
			}
		}
	};

	public BluetoothConnectManager(Context context) {
		this.context = context;
		this.mProfileManager = new DeviceBluetoothProfileManager(context);
		this.mSpp = new BluetoothSppHelper();

		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		filter.addAction(ACTION_HFP_CONNECT_STATE);
		context.registerReceiver(mReceiver, filter);
	}

	/**
	 * 绑定远程设备
	 * @param device 远程设备
	 */
	public void bindDevice(BluetoothDevice device) {
		if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
			if (mCallback != null) {
				mCallback.onBondedStateChanged(device, true);
			}
			return;
		}
		try {
			Method method = BluetoothDevice.class.getMethod("createBond");
			method.invoke(device);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 连接设备handset profile
	 * @param device 远程设备
	 */
	public void connectHandSet(BluetoothDevice device) {
		connectProfile(device, Profile.Headset);
	}

	public void connectProfile(BluetoothDevice device, Profile profile) {
		mProfileManager.connect(device, profile);
	}

	/**
	 * 连接socket
	 * @param device 远程设备
	 */
	public void connectSocket(BluetoothDevice device) {
		mSpp.connect(device);
	}

	/**
	 * socket状态改变回调
	 * @param status       see {@link cn.joy.libs.bluetooth.spp.BluetoothSocketBaseThread.SocketStatus}
	 * @param remoteDevice 远程设备
	 */
	@Override
	public void onStateChanged(int status, BluetoothDevice remoteDevice) {
		if (status == 2) {
			mCallback.onSocketConnectedStateChanged(remoteDevice, true);
		} else if (status == 0) {
			mCallback.onSocketConnectedStateChanged(remoteDevice, false);
		}
	}

	/**
	 * socket接收到数据时回调
	 * @param message 数据字符串
	 */
	@Override
	public void onReceiveMessage(String message) {
		if (mCallback != null) {
			mCallback.onSocketReceivedMessage(message);
		}
	}

	public void destroy() {
		context.unregisterReceiver(mReceiver);
	}

	public void setBluetoothCallback(BluetoothCallback callback) {
		this.mCallback = callback;
	}

	public interface BluetoothCallback {
		void onBondedStateChanged(BluetoothDevice device, boolean bonded);

		void onProfileConnectedStateChanged(BluetoothDevice device, Profile profile, boolean connected);

		void onSocketConnectedStateChanged(BluetoothDevice device, boolean connected);

		void onSocketReceivedMessage(String message);
	}
}

package cn.joy.libs.bluetooth;

import android.bluetooth.BluetoothA2dp;
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
	private static final String EXTRA_PROFILE_CONNECT_STATE = "android.bluetooth.profile.extra.STATE";
	private static final String ACTION_A2DP_CONNECT_STATE = "android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED";
	private static final int PROFILE_STATE_DISCONNECTED = 0;
	private static final int PROFILE_STATE_CONNECTED = 2;
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
				case ACTION_A2DP_CONNECT_STATE:
					int state = intent.getIntExtra(EXTRA_PROFILE_CONNECT_STATE, PROFILE_STATE_DISCONNECTED);
					if (state != PROFILE_STATE_DISCONNECTED && state != PROFILE_STATE_CONNECTED) {
						return;
					}
					boolean connect = intent.getIntExtra(EXTRA_PROFILE_CONNECT_STATE, PROFILE_STATE_DISCONNECTED) == PROFILE_STATE_CONNECTED;
					Profile profile = intent.getAction().equals(ACTION_HFP_CONNECT_STATE) ? Profile.Headset : Profile.A2dp;
					if (mCallback != null) {
						mCallback.onProfileConnectedStateChanged(device, profile, connect);
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
	 * 链接Profile
	 * @param device  远程设备
	 * @param profile 需要链接的profile
	 */
	public void connectProfile(BluetoothDevice device, Profile profile) {
		mProfileManager.connect(device, profile);
	}

	/**
	 * 断开Profile
	 * @param device  远程设备
	 * @param profile 需要断开的profile
	 */
	public void disconnectProfile(BluetoothDevice device, Profile profile) {
		mProfileManager.disconnect(device, profile);
	}

	/**
	 * 断开所有Profile
	 * @param device 远程设备
	 */
	public void disconnectAllProfile(BluetoothDevice device) {
		mProfileManager.disconnect(device);
	}

	/**
	 * 连接socket
	 * @param device 远程设备
	 */
	public void connectSocket(BluetoothDevice device) {
		mSpp.connect(device);
	}

	/**
	 * 断开socket
	 */
	public void disconnectSocket() {
		mSpp.stop();
	}

	public void sendMessage(String msg) {
		mSpp.write(msg);
	}

	/**
	 * 启动socket服务，服务端使用
	 */
	public void startSocket() {
		mSpp.start();
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

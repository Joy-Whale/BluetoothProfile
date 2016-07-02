package cn.joy.libs.bluetooth.profile;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * **********************
 * Author: J
 * Date:   2016/5/11
 * Time:   16:58
 * **********************
 */
abstract class BaseBluetoothProfileImpl<T extends BluetoothProfile> implements IBluetoothProfile, BluetoothProfile.ServiceListener {

	private Context context;
	private T mProxy;
	private BluetoothProfileReflect mReflect;

	 BaseBluetoothProfileImpl(Context context) {
		this.context = context;
		BluetoothAdapter.getDefaultAdapter().getProfileProxy(context, this, getProfileCode());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onServiceConnected(int profile, BluetoothProfile proxy) {
		mProxy = (T) proxy;
		try {
			mReflect = new BluetoothProfileReflect(mProxy);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onServiceDisconnected(int profile) {
		mProxy = null;
	}

	@Override
	public boolean isAvailable() {
		return mProxy != null;
	}

	@Override
	public boolean connect(BluetoothDevice device) {
		if (!isEnabled())
			return false;
		mReflect.setPriority(device, PRIORITY_ON);
		return mReflect.connect(device);
	}

	@Override
	public boolean disconnect(BluetoothDevice device) {
		if (!isEnabled())
			return false;
		mReflect.setPriority(device, PRIORITY_OFF);
		return mReflect.disconnect(device);
	}

	@Override
	public int getPriority(BluetoothDevice device) {
		if (!isEnabled()) {
			return PRIORITY_OFF;
		}
		return mReflect.getPriority(device);
	}

	@Override
	public boolean setPriority(BluetoothDevice device, int priority) {
		return isEnabled() && mReflect.setPriority(device, priority);
	}

	@Override
	public boolean isPreferred(BluetoothDevice device) {
		return getPriority(device) == PRIORITY_ON;
	}

	protected T getProxy() {
		return mProxy;
	}

	protected Context getContext() {
		return context;
	}

	/**
	 * 当前profile是否可以使用
	 */
	private boolean isEnabled() {
		return mProxy != null && mReflect != null;
	}

	@Override
	public void close() {
		BluetoothAdapter.getDefaultAdapter().closeProfileProxy(getProfileCode(), mProxy);
	}

	protected static class BluetoothProfileReflect {
		private BluetoothProfile profile;
		Class<?> mClass;

		protected BluetoothProfileReflect(BluetoothProfile obj) throws ClassNotFoundException {
			setProxy(obj);
		}

		protected final void setProxy(BluetoothProfile obj) throws ClassNotFoundException {
			this.profile = obj;
			this.mClass = Class.forName(obj.getClass().getName());
		}

		protected final BluetoothProfile getProxy() {
			return profile;
		}

		protected final boolean connect(BluetoothDevice device) {
			Object object = invoke("connect", new Object[]{device}, BluetoothDevice.class);
			if (object != null) {
				return (Boolean) object;
			}
			return false;
		}

		protected final boolean disconnect(BluetoothDevice device) {
			Object object = invoke("disconnect", new Object[]{device}, BluetoothDevice.class);
			return object != null && ((object instanceof Boolean) ? (Boolean) object : false);
		}

		/**
		 * 设置设备优先级
		 * @param device   设备
		 * @param priority 优先级
		 */
		protected final boolean setPriority(BluetoothDevice device, int priority) {
			Object object = invoke("setPriority", new Object[]{device, priority}, BluetoothDevice.class, int.class);
			return object != null && (object instanceof Boolean) ? (Boolean) object : false;
		}

		/**
		 * 获取设备优先权
		 * @param device 设备
		 */
		protected final int getPriority(BluetoothDevice device) {
			Object object = invoke("getPriority", new Object[]{device}, BluetoothDevice.class);
			return object != null && (object instanceof Integer) ? (Integer) object : PRIORITY_OFF;
		}

		/**
		 * 关闭所有设备的连接
		 */
		protected final void disconnectAll() {
			List<BluetoothDevice> devices = getConnectDevices();
			if (devices == null)
				return;
			for (BluetoothDevice device : devices) {
				disconnect(device);
			}
		}

		protected final int getConnectionState(BluetoothDevice device) {
			Object object = invoke("getConnectionState", new Object[]{device}, BluetoothDevice.class);
			if (object != null) {
				return (object instanceof Integer) ? (Integer) object : -1;
			}
			return -1;
		}

		@SuppressWarnings("unchecked")
		protected final List<BluetoothDevice> getConnectDevices() {
			Object object = invoke("getConnectedDevices", null);
			if (object != null) {
				return ((ArrayList<BluetoothDevice>) object);
			}
			return new ArrayList<>();
		}

		private Object invoke(String methodName, Object[] objects, Class<?>... parameterTypes) {
			if (mClass == null || profile == null)
				return null;
			try {
				Method method = mClass.getMethod(methodName, parameterTypes);
				if (objects == null || objects.length == 0) {
					return method.invoke(profile);
				}
				return method.invoke(profile, objects);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}
}

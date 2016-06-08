package cn.joy.libs.bluetooth.spp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.util.UUID;

public abstract class BluetoothSocketBaseThread extends Thread {

	public static final String NAME_SECURE = "Bluetooth Secure";

	//  蓝牙配对需要的UUID
	public static final UUID UUID_ANDROID_DEVICE = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
	public static final UUID UUID_OTHER_DEVICE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	public enum SocketStatus {
		NONE(-1),           //初始状态
		DISCONNECTED(0),  //断开连接
		CONNECTING(1),  //正在建立连接通道
		CONNECTED(2),   //已经连接
		LISTENING(3),      //服务端正在监听被连接状态
		ACCEPTED(4),       //服务端接受到连接申请   或者客户端响应到服务端的连接
		RECEIVED_MESSAGE(8);  //消息达到

		private int code;

		SocketStatus(int code) {
			this.code = code;
		}

		public int getCode() {
			return code;
		}
	}

	/**
	 * 此handler 用来和 manager 通讯
	 */
	protected Handler mHandler;
	/**
	 * 是否还在运行
	 */
	protected boolean isRunning;

	protected BluetoothAdapter mBluetoothAdapter;

	protected BluetoothSocketBaseThread(Handler handler) {
		super();
		mHandler = handler;
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	}


	/**
	 * 获取当前的链接的socket 对象
	 */
	public abstract BluetoothSocket getSocket();

	/**
	 * 取消当前线程,释放内存
	 */
	public void cancel() {
		mHandler = null;
		isRunning = false;
		mBluetoothAdapter = null;
	}

	public void sendMessage(SocketStatus status) {
		if (mHandler != null)
			mHandler.obtainMessage(status.ordinal()).sendToTarget();
	}

	public void sendMessage(SocketStatus status, Object object) {
		if (mHandler != null)
			mHandler.obtainMessage(status.ordinal(), object).sendToTarget();
	}


	@Override
	public synchronized void start() {
		isRunning = true;
		super.start();
	}
}

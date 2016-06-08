package cn.joy.libs.bluetooth.spp;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;

import cn.joy.libs.bluetooth.spp.BluetoothSocketBaseThread.SocketStatus;

import java.util.concurrent.ConcurrentLinkedQueue;

import static cn.joy.libs.bluetooth.spp.BluetoothSocketBaseThread.SocketStatus.*;

public class BluetoothSppHelper {

	private BluetoothSocketBaseThread mTargetThread;
	private BluetoothDataThread mDataThread;
	private SocketStatus mNowStatus = NONE;
	private BluetoothSocketListener mStatusListener;
	/**
	 * 消息队列
	 */
	private ConcurrentLinkedQueue<String> mQueue = new ConcurrentLinkedQueue<>();

	private Handler mSocketHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			SocketStatus status = BluetoothSocketBaseThread.SocketStatus.values()[msg.what];
			if (status != RECEIVED_MESSAGE) {
				mNowStatus = status;
				if (mStatusListener != null) {
					mStatusListener.onStateChanged(status.getCode(), (BluetoothDevice) msg.obj);
				}
			}

			switch (values()[msg.what]) {
				case LISTENING:
					break;
				case ACCEPTED:  //当服务端或者客户端监听到对方已经同意连接,则分别开启数据通道线程,接受数据
					mQueue.clear();
					mDataThread = new BluetoothDataThread(mQueue, mTargetThread.getSocket(), this);
					mDataThread.start();
					break;
				case CONNECTING:
					break;
				case CONNECTED:
					break;
				case DISCONNECTED:

					//如果连接断开,则停止数据通道线程
					if (mDataThread != null) {
						mDataThread.cancel();
						mDataThread = null;
					}
					//如果当前是服务端,则重新启动服务,等待被连接
					if (mTargetThread instanceof BluetoothServiceConnectThread) {
						start();
					}
					break;
				case RECEIVED_MESSAGE:
					String message = (String) msg.obj;
					if (mStatusListener != null) {
						mStatusListener.onReceiveMessage(message);
					}
					break;
			}
		}
	};

	public BluetoothSppHelper() {

	}

	/**
	 * 开始服务端监听线程,等待客户端连接
	 */
	public void start() {
		if (mTargetThread != null) {
			mTargetThread.cancel();
			mTargetThread = null;
		}

		if (mDataThread != null) {
			mDataThread.cancel();
			mDataThread = null;
		}

		mTargetThread = new BluetoothServiceConnectThread(mSocketHandler);
		mTargetThread.start();
	}

	/**
	 * 客户端主动发起连接,去连接服务端
	 * @param serviceDevice 服务端的蓝牙设备
	 */
	public void connect(BluetoothDevice serviceDevice) {
		if (mTargetThread != null) {
			mTargetThread.cancel();
		}

		if (mDataThread != null) {
			mDataThread.cancel();
		}
		mTargetThread = new BluetoothClientConnectThread(serviceDevice, mSocketHandler);
		mTargetThread.start();
	}


	public synchronized boolean write(String message) {
		if (mNowStatus == CONNECTED) {
			mQueue.offer(message);
			mDataThread.startQueue();
			return true;
		}
		return false;
	}


	public void setBluetoothSocketListener(BluetoothSocketListener statusListener) {
		mStatusListener = statusListener;
	}


	public interface BluetoothSocketListener {

		/**
		 * 蓝牙socket连接状态该改变
		 * 连接成功的时候,device 是代表连接的设备,其他状态remoteDevice 为null
		 * @param state
		 */
		void onStateChanged(int state, BluetoothDevice remoteDevice);

		/**
		 * 蓝牙socket消息到达
		 */
		void onReceiveMessage(String message);
	}

	/**
	 * 出去时候停止掉所有线程
	 */
	public void stop() {
		if (mTargetThread != null) {
			mTargetThread.cancel();
			mTargetThread = null;
		}
		if (mDataThread != null) {
			mDataThread.cancel();
			mDataThread = null;
		}
		if (mStatusListener != null)
			mStatusListener.onStateChanged(DISCONNECTED.getCode(), null);
	}
}

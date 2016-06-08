package cn.joy.libs.bluetooth.spp;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;

/**
 * Service 端等待被连接的线程
 */
class BluetoothServiceConnectThread extends BluetoothSocketBaseThread {


	private BluetoothServerSocket mBlueServiceSocket;
	private BluetoothSocket mBlueSocket;

	public BluetoothServiceConnectThread(Handler handler) {
		super(handler);
	}

	@Override
	public BluetoothSocket getSocket() {
		return mBlueSocket;
	}

	@Override
	public void run() {

		if (!isRunning)
			return;

		try {
			mBlueServiceSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, UUID_ANDROID_DEVICE);
			sendMessage(SocketStatus.LISTENING);
			//监听连接,等待客户端连接,此处会阻塞线程,如果客户端没有连接服务端,此处一直会等待,知道有设备连接
			mBlueSocket = mBlueServiceSocket.accept();
			if (mBlueSocket != null) {
				sendMessage(SocketStatus.ACCEPTED);
			} else {
				sendMessage(SocketStatus.DISCONNECTED);
			}
		} catch (IOException e) {
			sendMessage(SocketStatus.DISCONNECTED);
		}
	}

	@Override
	public void cancel() {
		super.cancel();
		try {
			if (mBlueServiceSocket != null)
				mBlueServiceSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			if (mBlueSocket != null)
				mBlueSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mBlueServiceSocket = null;
		mBlueSocket = null;
	}
}

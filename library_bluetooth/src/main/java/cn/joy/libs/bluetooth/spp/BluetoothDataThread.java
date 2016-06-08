package cn.joy.libs.bluetooth.spp;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.SystemClock;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 服务端和客户端共用的数据传输线程
 */
 class BluetoothDataThread extends BluetoothSocketBaseThread {

	private BluetoothSocket mBlueSocket;
	private OutputStream mBlueSocketOutputStream;
	private InputStream mBlueSocketInputStream;
	private ConcurrentLinkedQueue<String> mQueue;
	private boolean isSendFinish = true;  //数据是否发送完成
	private SendMessageThread mSendThread;

	protected BluetoothDataThread(ConcurrentLinkedQueue<String> queue, BluetoothSocket bluetoothSocket, Handler handler) {
		super(handler);
		mBlueSocket = bluetoothSocket;
		mQueue = queue;
	}

	@Override
	public void run() {
		super.run();
		if (!isRunning)
			return;
		try {
			mBlueSocketOutputStream = mBlueSocket.getOutputStream();
			mBlueSocketInputStream = mBlueSocket.getInputStream();
			sendMessage(SocketStatus.CONNECTED, mBlueSocket.getRemoteDevice());
		} catch (IOException e) {
			e.printStackTrace();
			sendMessage(SocketStatus.DISCONNECTED);
			return;
		}

		//监听通道后,死循环监听通道,去读取message ,
		while (isRunning) {
			try {
				String result = readString(mBlueSocketInputStream);
				sendMessage(SocketStatus.RECEIVED_MESSAGE, result);
			} catch (IOException e) {
				e.printStackTrace();
				sendMessage(SocketStatus.DISCONNECTED);
				return;
			}

		}
	}

	/**
	 * 从流里面读取
	 * @throws IOException
	 */
	private String readString(InputStream inputStream) throws IOException {
		ArrayList<Integer> arrayList = new ArrayList<>();
		while (true) {
			int data = inputStream.read();
			if (data == 0x0A) {   //\r  不增加

			} else if (data == 0x0D) { //\n
				byte[] buffer = new byte[arrayList.size()];
				for (int i = 0; i < arrayList.size(); i++) {
					buffer[i] = arrayList.get(i).byteValue();
				}
				return new String(buffer);
			} else {
				arrayList.add(data);
			}
		}
	}


	/**
	 * 开始消息队列
	 */
	public void startQueue() {
		if (mQueue != null && !mQueue.isEmpty() && isSendFinish) {
			if (mSendThread != null) {
				mSendThread.cancel();
			}
			mSendThread = new SendMessageThread();
			mSendThread.start();
		}

	}


	@Override
	public BluetoothSocket getSocket() {
		return null;
	}

	@Override
	public void cancel() {
		super.cancel();

		if (mSendThread != null) {
			mSendThread.cancel();
			mSendThread = null;
			mQueue = null;
		}
		try {
			if (mBlueSocketInputStream != null)
				mBlueSocketInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			if (mBlueSocket != null)
				mBlueSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			if (mBlueSocketOutputStream != null)
				mBlueSocketOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		mBlueSocketInputStream = null;
		mBlueSocketOutputStream = null;
		mBlueSocket = null;

	}

	private class SendMessageThread extends Thread {
		private boolean isCancle = false;

		@Override
		public void run() {
			super.run();
			isSendFinish = false;
			while (mQueue != null && !mQueue.isEmpty() && !isCancle) {
				try {
					String message = mQueue.poll() + "\r\n";
					mBlueSocketOutputStream.write(message.getBytes());
				} catch (IOException e) {
					e.printStackTrace();
					sendMessage(SocketStatus.DISCONNECTED);
					isCancle = true;
				}
				SystemClock.sleep(50);
			}
			isSendFinish = true;
		}

		public void cancel() {
			isCancle = true;
		}
	}
}

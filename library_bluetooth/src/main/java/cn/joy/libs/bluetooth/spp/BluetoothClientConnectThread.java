package cn.joy.libs.bluetooth.spp;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;

/**
 * 蓝牙连接客户端连接线程
 */
 class BluetoothClientConnectThread extends BluetoothSocketBaseThread {

    private BluetoothDevice mServiceDevice;
    private BluetoothSocket mBlueSocket;

    protected BluetoothClientConnectThread(BluetoothDevice serviceDevice, Handler handler) {
        super(handler);
        mServiceDevice = serviceDevice;
    }


    @Override
    public void run() {
        super.run();
        if (!isRunning)return;
        try {
            sendMessage(SocketStatus.CONNECTING);
            mBlueSocket = mServiceDevice.createRfcommSocketToServiceRecord(UUID_ANDROID_DEVICE);
            mBlueSocket.connect();
            sendMessage(SocketStatus.ACCEPTED);
        } catch (IOException e) {
//            e.printStackTrace();
            sendMessage(SocketStatus.DISCONNECTED);
        }
    }


    @Override
    public BluetoothSocket getSocket() {
        return mBlueSocket;
    }

    @Override
    public void cancel() {
        super.cancel();

        try {
            if (mBlueSocket != null)
                mBlueSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mServiceDevice = null;
        mBlueSocket = null;
    }
}

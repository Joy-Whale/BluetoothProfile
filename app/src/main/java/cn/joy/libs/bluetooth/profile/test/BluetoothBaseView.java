package cn.joy.libs.bluetooth.profile.test;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.joy.libs.bluetooth.BluetoothConnectManager;
import cn.joy.libs.bluetooth.profile.Profile;

/**
 * **********************
 * Author: yu
 * Date:   2016-06-29
 * Time:   9:54
 * **********************
 */

class BluetoothBaseView extends FrameLayout {

	@BindView(R.id.remote_profile_view)
	View mProfileView;

	@BindView(R.id.remote_socket_view)
	View mSocketView;

	@BindView(R.id.remote_name)
	TextView mTextRemote;
	@BindView(R.id.remote_handset)
	TextView mTextProfileHs;
	@BindView(R.id.remote_a2dp)
	TextView mTextProfileA2dp;
	@BindView(R.id.remote_socket)
	TextView mTextSocket;
	@BindView(R.id.remote_socket_input)
	EditText mEdit;
	@BindView(R.id.remote_profile_handset_conn)
	Button mBtnProfileHandset;
	@BindView(R.id.remote_profile_a2dp_conn)
	Button mBtnProfileA2dp;
	@BindView(R.id.remote_socket_conn)
	Button mBtnSocket;
	@BindView(R.id.remote_socket_send)
	Button mBtnSocketSend;
	@BindView(R.id.remote_socket_message)
	RecyclerView mRecycler;

	private MyAdapter mAdapter;
	private List<String> mMessages;

	private AlertDialog mDialog;

	private BluetoothConnectManager mBluetoothManager;

	private BluetoothDevice mCurrentDevice;
	private boolean isHandsetConnected;
	private boolean isA2dpConnected;
	private boolean isSocketConnected;


	public BluetoothBaseView(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.layout_bluetooth_operate, this);
		ButterKnife.bind(this);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		mBluetoothManager = new BluetoothConnectManager(getContext());
		mBluetoothManager.setBluetoothCallback(new BluetoothConnectManager.BluetoothCallback() {
			@Override
			public void onBondedStateChanged(BluetoothDevice device, boolean bonded) {
				onDeviceBindStateChanged(device, bonded);
			}

			@Override
			public void onProfileConnectedStateChanged(BluetoothDevice device, Profile profile, boolean connected) {
				switch (profile) {
					case A2dp:
						onProfileA2dpConnectedStateChanged(connected);
						break;
					case Headset:
						onProfileHandsetConnectedStateChanged(connected);
						break;
				}
			}

			@Override
			public void onSocketConnectedStateChanged(BluetoothDevice device, boolean connected) {
				BluetoothBaseView.this.onSocketConnectedStateChanged(connected);
			}

			@Override
			public void onSocketReceivedMessage(String message) {
				BluetoothBaseView.this.onSocketReceivedMessage(message);
			}
		});

		onDeviceBindStateChanged(null, false);

		mRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
		mRecycler.setAdapter(mAdapter = new MyAdapter(mMessages = new ArrayList<>()));
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mBluetoothManager.destroy();
		mBluetoothManager = null;
	}

	/***
	 * 作为服务端使用
	 */
	void asService() {
		mBtnProfileHandset.setVisibility(GONE);
		mBtnProfileA2dp.setVisibility(GONE);
		mBtnSocket.setVisibility(GONE);
		mBluetoothManager.startSocket();
	}

	void bindDevice(BluetoothDevice device) {
		mBluetoothManager.bindDevice(device);
	}


	@OnClick(R.id.remote_profile_handset_conn)
	void toggleProfileHandset() {
		if (isHandsetConnected) {
			mBluetoothManager.disconnectProfile(mCurrentDevice, Profile.Headset);
		} else {
			showDialog(getString(R.string.remote_connect_format, "HandSet"));
			mBluetoothManager.connectProfile(mCurrentDevice, Profile.Headset);
		}
	}

	@OnClick(R.id.remote_profile_a2dp_conn)
	void toggleProfileA2dp() {
		if (isA2dpConnected) {
			mBluetoothManager.disconnectProfile(mCurrentDevice, Profile.A2dp);
		} else {
			showDialog(getString(R.string.remote_connect_format, "A2dp"));
			mBluetoothManager.connectProfile(mCurrentDevice, Profile.A2dp);
		}
	}

	@OnClick(R.id.remote_socket_conn)
	void toggleSocket() {
		if (isSocketConnected) {
			mBluetoothManager.disconnectSocket();
		} else {
			showDialog(getString(R.string.remote_connect_format, "Socket"));
			mBluetoothManager.connectSocket(mCurrentDevice);
		}
	}

	@OnClick(R.id.remote_socket_send)
	void sendSocketMessage() {
		if (TextUtils.isEmpty(mEdit.getText())) {
			Toast.makeText(getContext(), R.string.remote_socket_send_empty, Toast.LENGTH_SHORT).show();
			return;
		}
		mBluetoothManager.sendMessage(mEdit.getText().toString());
		Toast.makeText(getContext(), R.string.remote_socket_send_success, Toast.LENGTH_SHORT).show();
	}

	private void toggleProfileEnable(boolean enabled) {
		mBtnProfileA2dp.setEnabled(enabled);
		mBtnProfileHandset.setEnabled(enabled);
	}

	private void toggleSocketEnable(boolean enabled) {
		mBtnSocketSend.setEnabled(enabled);
		mBtnSocket.setEnabled(enabled);
	}

	private void onDeviceBindStateChanged(BluetoothDevice device, boolean bind) {
		if (bind) {
			mCurrentDevice = device;
		} else {
			mCurrentDevice = null;
			onProfileHandsetConnectedStateChanged(false);
			onProfileA2dpConnectedStateChanged(false);
			onSocketConnectedStateChanged(false);
		}
		toggleProfileEnable(bind);
		mTextRemote.setText(getString(R.string.remote_device_format, bind ? device.getName() : "null"));
		dismissDialog();
	}

	private void onProfileHandsetConnectedStateChanged(boolean connected) {
		isHandsetConnected = connected;
		mTextProfileHs.setText(getString(R.string.remote_profile_handset, getString(connected ? R.string.remote_profile_connected : R.string.remote_profile_disconnected)));
		mBtnProfileHandset.setText(getString(connected ? R.string.remote_profile_disconnect : R.string.remote_profile_connect));
		dismissDialog();
	}

	private void onProfileA2dpConnectedStateChanged(boolean connected) {
		isA2dpConnected = connected;
		mTextProfileA2dp.setText(getString(R.string.remote_profile_a2dp, getString(connected ? R.string.remote_profile_connected : R.string.remote_profile_disconnected)));
		mBtnProfileA2dp.setText(getString(connected ? R.string.remote_profile_disconnect : R.string.remote_profile_connect));
		dismissDialog();
	}

	private void onSocketConnectedStateChanged(boolean connected) {
		isSocketConnected = connected;
		mTextSocket.setText(getString(R.string.remote_socket, getString(connected ? R.string.remote_profile_connected : R.string.remote_profile_disconnected)));
		mBtnSocket.setText(getString(connected ? R.string.remote_profile_disconnect : R.string.remote_profile_connect));
		toggleSocketEnable(connected);
		dismissDialog();
 	}

	private void onSocketReceivedMessage(String message) {
		mMessages.add(new SimpleDateFormat("hh:mm:ss: ", Locale.getDefault()).format(new Date(System.currentTimeMillis())) + message);
		mAdapter.notifyDataSetChanged();
	}

	private void showDialog(String msg) {
		mDialog = new AlertDialog.Builder(getContext()).setView(R.layout.widget_loading).setTitle(msg).setCancelable(false).create();
		mDialog.show();
	}

	private void dismissDialog() {
		if (mDialog != null && mDialog.isShowing()) {
			mDialog.dismiss();
		}
		mDialog = null;
	}

	private String getString(int resId, Object... objs) {
		return getContext().getString(resId, objs);
	}

	class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyHolder> {
		private List<String> msgs;

		MyAdapter(List<String> msgs) {
			this.msgs = msgs;
		}

		@Override
		public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			return new MyHolder(parent);
		}

		@Override
		public void onBindViewHolder(MyHolder holder, int position) {
			holder.onBind(msgs.get(position));
		}

		@Override
		public int getItemCount() {
			return msgs.size();
		}

		class MyHolder extends RecyclerView.ViewHolder {

			@BindView(R.id.li_text)
			TextView txt;

			MyHolder(View itemView) {
				super(itemView);
			}

			void onBind(String msg) {
				txt.setText(msg);
			}
		}
	}
}

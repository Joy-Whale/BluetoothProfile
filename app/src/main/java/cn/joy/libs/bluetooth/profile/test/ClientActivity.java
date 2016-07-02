package cn.joy.libs.bluetooth.profile.test;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.joy.libs.bluetooth.BluetoothConnectManager;

/**
 * **********************
 * Author: J
 * Date:   2016/6/1
 * Time:   12:16
 * **********************
 */
public class ClientActivity extends AppCompatActivity {


	private BluetoothConnectManager mManager;
	private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	private BluetoothDevice mCurrentDevice;

	@BindView(R.id.bluetooth_view)
	BluetoothBaseView mBaseView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetooth);
		mManager = new BluetoothConnectManager(this);
		ButterKnife.bind(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mManager.destroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_device, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_client_search) {
			search();
		}
		return super.onOptionsItemSelected(item);
	}

	private void search() {

		final Dialog dialog = new Dialog(this);
		dialog.setContentView(new DialogView(this).onSelectedDevice(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				dialog.dismiss();
			}
		}));
		dialog.setTitle(R.string.remote_device_connect_title);
		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				mBluetoothAdapter.cancelDiscovery();
			}
		});
		dialog.show();
	}

	 class DialogView extends FrameLayout {

		@BindView(R.id.remote_list)
		RecyclerView mRecycler;

		OnClickListener mDeviceClickListener;

		private MyAdapter mAdapter;
		private List<BluetoothDevice> devices;

		private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		private BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				switch (intent.getAction()) {
					case BluetoothDevice.ACTION_FOUND:
						BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
						if (!devices.contains(device)) {
							devices.add(device);
							mAdapter.notifyDataSetChanged();
						}
						break;
				}
			}
		};

		public DialogView(Context context) {
			super(context);
			inflate(context, R.layout.layout_remote_list, this);
			ButterKnife.bind(this);

			mRecycler.setLayoutManager(new LinearLayoutManager(context));
			mRecycler.setAdapter(mAdapter = new MyAdapter(devices = new ArrayList<>()));
		}

		 @Override
		 protected void onAttachedToWindow() {
			 super.onAttachedToWindow();
			 getContext().registerReceiver(mBluetoothReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));

			 if (!mBluetoothAdapter.isEnabled()) {
				 mBluetoothAdapter.enable();
			 }
			 mBluetoothAdapter.cancelDiscovery();
			 mBluetoothAdapter.startDiscovery();
		 }

		@Override
		protected void onDetachedFromWindow() {
			super.onDetachedFromWindow();
			getContext().unregisterReceiver(mBluetoothReceiver);
		}

		DialogView onSelectedDevice(OnClickListener listener) {
			mDeviceClickListener = listener;
			return this;
		}

		class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyHolder> {

			private List<BluetoothDevice> devices;

			MyAdapter(List<BluetoothDevice> devices) {
				this.devices = devices;
			}

			@Override
			public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
				return new MyHolder(parent);
			}

			@Override
			public void onBindViewHolder(MyHolder holder, int position) {
				holder.onBind(devices.get(position));
			}

			@Override
			public int getItemCount() {
				return devices.size();
			}

			class MyHolder extends RecyclerView.ViewHolder {
				@BindView(R.id.li_text)
				TextView mTextName;

				MyHolder(View itemView) {
					super(itemView);
				}

				void onBind(final BluetoothDevice device) {
					mTextName.setText(device.getName());
					itemView.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View view) {
							mBaseView.bindDevice(device);
							if (mDeviceClickListener != null) {
								mDeviceClickListener.onClick(view);
							}
						}
					});
				}
			}
		}
	}
}

package cn.joy.libs.bluetooth.profile.test;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import cn.joy.libs.bluetooth.BluetoothConnectManager;

/**
 * **********************
 * Author: J
 * Date:   2016/6/1
 * Time:   12:16
 * **********************
 */
public class ClientActivity extends Activity {


	private BluetoothConnectManager mManager;
	private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	private BluetoothDevice mCurrentDevice;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mManager = new BluetoothConnectManager(this);
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
		if (!mBluetoothAdapter.isEnabled()) {
			mBluetoothAdapter.enable();
		} else {
			mBluetoothAdapter.startDiscovery();
		}

		Dialog dialog = new Dialog(this);
		dialog.setContentView(new DialogView(this));
		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				mBluetoothAdapter.cancelDiscovery();
			}
		});
		dialog.show();
	}

	class DialogView extends FrameLayout {

		public DialogView(Context context) {
			super(context);
		}
	}
}

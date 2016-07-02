package cn.joy.libs.bluetooth.profile.test;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * **********************
 * Author: J
 * Date:   2016/6/1
 * Time:   12:16
 * **********************
 */
public class ServiceActivity extends AppCompatActivity {

	@BindView(R.id.bluetooth_view)
	BluetoothBaseView mBluetoothView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetooth);
		ButterKnife.bind(this);
		mBluetoothView.post(new Runnable() {
			@Override
			public void run() {
				mBluetoothView.asService();
			}
		});
	}
}

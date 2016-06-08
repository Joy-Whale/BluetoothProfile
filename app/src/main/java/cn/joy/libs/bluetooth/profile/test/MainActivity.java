package cn.joy.libs.bluetooth.profile.test;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * **********************
 * Author: J
 * Date:   2016/5/12
 * Time:   12:36
 * **********************
 */
public class MainActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViewById(R.id.main_btn_client).setOnClickListener(this);
		findViewById(R.id.main_btn_service).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.main_btn_client:
				break;
			case R.id.main_btn_service:
				break;
		}
	}
}

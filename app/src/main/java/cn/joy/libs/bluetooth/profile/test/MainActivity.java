package cn.joy.libs.bluetooth.profile.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * **********************
 * Author: J
 * Date:   2016/5/12
 * Time:   12:36
 * **********************
 */
public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);
	}

	@OnClick(R.id.main_btn_service)
	void asService() {
		startActivity(new Intent(this, ServiceActivity.class));
	}

	@OnClick(R.id.main_btn_client)
	void asClient() {
		startActivity(new Intent(this, ClientActivity.class));
	}
}

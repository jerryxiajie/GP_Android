package com.example.myandroid_pro;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class start_page extends Activity {

	private Button start_btn;
	private Button finish_btn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.start_page);

		start_btn = (Button) findViewById(R.id.start_scan_btn);
		finish_btn = (Button) findViewById(R.id.finish_scan_btn);

		start_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				final Intent intent = new Intent();
				intent.setClass(start_page.this, DeviceScanActivity.class);
				intent.putExtra("FIND_FLAG", true);
//				Toast.makeText(start_page.this, "START", Toast.LENGTH_SHORT).show();
				startActivity(intent);
			}
		});

		finish_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				final Intent intent = new Intent();
				intent.setClass(start_page.this, MainActivity.class);
				startActivity(intent);
			}
		});
	}
}

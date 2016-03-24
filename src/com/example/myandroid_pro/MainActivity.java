package com.example.myandroid_pro;

import android.R.integer;
import android.support.v7.app.ActionBarActivity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	private TextView tv_cnt_timer;
	private TextView tv_pre_timer;
	private TextView tv_steps;
	private Button btn_start;
	private Button btn_pause;
	private TextView tv_total_steps;
	private TextView tv_week_day;
	private int StPa_flag = 0;/* 0: 暂停不能按； 1：暂停可以 */
	private CustomDialog dialog;
	private int steps = 90;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		tv_cnt_timer = (TextView) findViewById(R.id.tv_cnt_time);
		tv_pre_timer = (TextView) findViewById(R.id.tv_pre_time);
		tv_steps = (TextView) findViewById(R.id.tv_steps);
		btn_start = (Button) findViewById(R.id.start_btn);
		btn_pause = (Button) findViewById(R.id.pause_btn);
		tv_total_steps = (TextView) findViewById(R.id.Total_steps);
		tv_week_day = (TextView) findViewById(R.id.week_day);

		btn_pause.getBackground().setAlpha(80);

		btn_start.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// tv_cnt_timer.setText("00:00:00");
				// tv_pre_timer.setText("00:00:00");
				if (StPa_flag != 1) { /* 避免重复按开始键 */
					tv_steps.setText("88");
					btn_pause.setText("暂停");
					btn_start.getBackground().setAlpha(80);
					btn_pause.getBackground().setAlpha(255);
					StPa_flag = 1;
				} else {
					Toast.makeText(getApplicationContext(), "已经开始计步",
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		btn_pause.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				if (StPa_flag == 1) { /* 暂停按下 */
					btn_pause.setText("清除");
					tv_steps.setText("666");
					btn_start.getBackground().setAlpha(255);
					StPa_flag = 2; /* 表明清除按键的状态： 2：可以按下；3：不能按下 */
					
					alert_show();
					
				} else if (StPa_flag == 2) {
					tv_pre_timer.setText("00:00:00");
					tv_cnt_timer.setText("00:00:00");
					btn_pause.getBackground().setAlpha(80);
					tv_steps.setText("0");
					StPa_flag = 0;
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void alert_show() {
		CustomDialog.Builder customBuilder = new CustomDialog.Builder(
				MainActivity.this);
		customBuilder
				.setTitle("保存？")
				.setMessage("本次共计" + steps + "步")
				.setNegativeButton("保存", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Toast.makeText(getApplicationContext(), "保存",
								Toast.LENGTH_SHORT).show();
						dialog.dismiss();
					}
				})
				.setPositiveButton("不保存",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int which) {
								Toast.makeText(getApplicationContext(), "不保存",
										Toast.LENGTH_SHORT).show();
								dialog.dismiss();
							}
						});
		dialog = customBuilder.create();
		dialog.show();
	}

}

/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.myandroid_pro;

import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * For a given BLE device, this Activity provides the user interface to connect,
 * display data, and display GATT services and characteristics supported by the
 * device. The Activity communicates with {@code BluetoothLeService}, which in
 * turn interacts with the Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity {
	private final static String TAG = DeviceControlActivity.class
			.getSimpleName();

	public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
	public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

	private TextView mDataField;
	private String mDeviceName;
	private String mDeviceAddress;
	private BluetoothLeService mBluetoothLeService;
	private boolean mConnected = false;

	EditText edtSend;
	ScrollView svResult;
	
	Button btnStart;
	Button btnPause;
	
	private TextView tv_cnt_timer;
	private TextView tv_pre_timer;
	private TextView tv_steps;
	private Button btn_start;
	private Button btn_pause;
	private TextView tv_total_steps;
	private TextView tv_week_day;
	private int StPa_flag = 0;/* 0: ��ͣ���ܰ��� 1����ͣ���� */
	private CustomDialog dialog;
	private int steps = 0;
	private int temp = 0;

	public long second_all = 0;

	private long timer = 0;// �˶�ʱ��
	private long startTimer = 0;// ��ʼʱ��
	private long tempTime = 0;
	private long nextTime = 0;
	
	private long seconds;
	private long minutes;
	private long hours;

	private Thread thread; // �����̶߳���

	public volatile boolean exit = false;

	Handler handler = new Handler() {// Handler�������ڸ��µ�ǰ����,��ʱ������Ϣ�����÷�����ѯ����������ʾ��������������������
		// ��Ҫ�������̷߳��͵�����, ���ô�����������̸߳���UI
		// Handler���������߳���(UI�߳���), �������߳̿���ͨ��Message��������������,
		// Handler�ͳе��Ž������̴߳�������(���߳���sendMessage()��������Message����(�����������)
		// ����Щ��Ϣ�������̶߳����У�������߳̽��и���UI��

		@Override
		// ��������ǴӸ���/�ӿ� �̳й����ģ���Ҫ��дһ��
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg); // �˴����Ը���UI

			tv_cnt_timer.setText(getFormatTime(timer));// ��ʾ��ǰ����ʱ��
			tv_steps.setText(""+steps);

		}
	};

	
	// Code to manage Service lifecycle.
	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName,
				IBinder service) {
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service)
					.getService();
			if (!mBluetoothLeService.initialize()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
				finish();
			}
			Log.e(TAG, "mBluetoothLeService is okay");
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBluetoothLeService = null;
		}
	};

	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@SuppressLint("NewApi")
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) { // ���ӳɹ�
				Log.e(TAG, "Only gatt, just wait");
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED
					.equals(action)) { // �Ͽ�����
				mConnected = false;
				invalidateOptionsMenu();
				clearUI();
			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
					.equals(action)) // ���Կ�ʼ�ɻ���
			{
				mConnected = true;
				ShowDialog();
				Log.e(TAG, "In what we need");
				invalidateOptionsMenu();
			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) { // �յ�����
				Log.e(TAG, "RECV DATA");
				String data = intent
						.getStringExtra(BluetoothLeService.EXTRA_DATA);
				if (data != null) {
					steps++;
				//	tv_steps.setText(data);
				}
			}
		}
	};

	private void clearUI() {
		tv_steps.setText(""+steps);
	}

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) { // ��ʼ��
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		tv_cnt_timer = (TextView) findViewById(R.id.tv_cnt_time);
		tv_pre_timer = (TextView) findViewById(R.id.tv_pre_time);
		tv_steps = (TextView) findViewById(R.id.tv_steps);
		btn_start = (Button) findViewById(R.id.start_btn);
		btn_pause = (Button) findViewById(R.id.pause_btn);
		tv_total_steps = (TextView) findViewById(R.id.Total_steps);
		tv_week_day = (TextView) findViewById(R.id.week_day);

		btn_pause.getBackground().setAlpha(80);
		tv_steps.setText("0");
		setDate();
		
		final Intent intent = getIntent();
		mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
		mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

		getActionBar().setTitle(mDeviceName);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
		Log.d(TAG,
				"Try to bindService="
						+ bindService(gattServiceIntent, mServiceConnection,
								BIND_AUTO_CREATE));

		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		
		btn_start.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub			
				if (StPa_flag == 0) { /* �����ظ�����ʼ�� */
					btn_pause.setText("��ͣ");
					btn_start.getBackground().setAlpha(80);
					btn_pause.getBackground().setAlpha(255);
					mBluetoothLeService.connect(mDeviceAddress);
					StPa_flag = 1;
					exit = false;
					startTimer = System.currentTimeMillis();
					nextTime = startTimer;
					tempTime = 0;
					timer = 0;
					steps = 0;
					thread_fuc();

				} else if (StPa_flag == 2) {  // �������״̬
					mBluetoothLeService.connect(mDeviceAddress);
					exit = false;
					startTimer = System.currentTimeMillis(); // ��ʼʱ�����
					btn_start.getBackground().setAlpha(80);  // ͸��������
					tempTime = timer;
					thread_fuc();
					btn_pause.setText("��ͣ");
					StPa_flag = 1;

				} else {
					Toast.makeText(getApplicationContext(), "�Ѿ���ʼ�Ʋ�",
							Toast.LENGTH_SHORT).show();
				}
				
			}
		});
		
		btn_pause.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				if (StPa_flag == 1) { /* ��ͣ���£� �Ʋ��ͼ�ʱ��ͣ */
					mBluetoothLeService.disconnect();
					exit = true;
					btn_pause.setText("���");
					btn_start.setText("����");
					btn_start.getBackground().setAlpha(255);
					StPa_flag = 2; /* �������������״̬�� 2�����԰��£�3�����ܰ���  */
					alert_show();
				} else if (StPa_flag == 2) { /* Clear ��������ʱ���Ʋ����� */
					mBluetoothLeService.disconnect();
					exit = false;
					btn_start.setText("��ʼ");
					btn_pause.getBackground().setAlpha(80);
					tv_cnt_timer.setText("00:00:00");
					steps = 0;
					tv_steps.setText("0");
					StPa_flag = 0;
				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		/*
		 * registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		 * if (mBluetoothLeService != null) { final boolean result =
		 * mBluetoothLeService.connect(mDeviceAddress); Log.d(TAG,
		 * "Connect request result=" + result); }
		 */
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mGattUpdateReceiver);
		unbindService(mServiceConnection);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// this.unregisterReceiver(mGattUpdateReceiver);
		// unbindService(mServiceConnection);
		if (mBluetoothLeService != null) {
			mBluetoothLeService.close();
			mBluetoothLeService = null;
		}
		Log.d(TAG, "We are in destroy");
	}

	/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.gatt_services, menu);
		if (mConnected) {
			menu.findItem(R.id.menu_connect).setVisible(false);
			menu.findItem(R.id.menu_disconnect).setVisible(true);
		} else {
			menu.findItem(R.id.menu_connect).setVisible(true);
			menu.findItem(R.id.menu_disconnect).setVisible(false);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) { // �����ť
		switch (item.getItemId()) {
		case R.id.menu_connect:
			Toast.makeText(DeviceControlActivity.this, mDeviceAddress,
					Toast.LENGTH_SHORT).show();
			mBluetoothLeService.connect(mDeviceAddress);
			return true;
		case R.id.menu_disconnect:
			mBluetoothLeService.disconnect();
			return true;
		case android.R.id.home:
			if (mConnected) {
				mBluetoothLeService.disconnect();
				mConnected = false;
			}
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
*/
	
	private void ShowDialog() {
		Toast.makeText(this, "���ӳɹ������ڿ�������ͨ�ţ�", Toast.LENGTH_SHORT).show();
	}

	private static IntentFilter makeGattUpdateIntentFilter() { // ע����յ��¼�
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
		intentFilter.addAction(BluetoothDevice.ACTION_UUID);
		return intentFilter;
	}
	
	public void alert_show() {
		CustomDialog.Builder customBuilder = new CustomDialog.Builder(
				DeviceControlActivity.this);
		//temp = steps+1;
		
		if(hours > 0)
		{
			customBuilder.setMessage("��������" + hours+ "ʱ" + minutes+"��"+second_all + "�룬����" + steps + "��");
		}
		else if (minutes > 0) {
			customBuilder.setMessage("��������" + minutes+"��"+second_all + "�룬����" + steps + "��");
		}
		else {
			customBuilder.setMessage("��������" + second_all + "�룬����" + steps + "��");
		}
		
		customBuilder
				.setTitle("���棿")
//				.setMessage("��������" + second_all + "�룬����" + steps + "��")
				.setNegativeButton("����", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Toast.makeText(getApplicationContext(), "����",
								Toast.LENGTH_SHORT).show();
						dialog.dismiss();
					}
				})
				.setPositiveButton("������",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								Toast.makeText(getApplicationContext(), "������",
										Toast.LENGTH_SHORT).show();
								dialog.dismiss();
							}
						});
		dialog = customBuilder.create();
		dialog.show();
	}

	private String getFormatTime(long time) {
		time = time / 1000; /* ������ */
		long second = time % 60;
		long minute = (time % 3600) / 60;
		long hour = time / 3600;
		
		seconds = second;
		minutes = minute;
		hours = hour;

		second_all = second;

		// ��������ʾ��λ
		// String strMillisecond = "" + (millisecond / 10);
		// ����ʾ��λ
		String strSecond = ("00" + second)
				.substring(("00" + second).length() - 2);
		// ����ʾ��λ
		String strMinute = ("00" + minute)
				.substring(("00" + minute).length() - 2);
		// ʱ��ʾ��λ
		String strHour = ("00" + hour).substring(("00" + hour).length() - 2);

		return strHour + ":" + strMinute + ":" + strSecond;
		// + strMillisecond;
	}

	private void thread_fuc() {

		thread = new Thread() {// ���߳����ڼ�����ǰ�����ı仯
			@Override
			public void run() {
				// TODO Auto-generated method stub
				while (!exit) {
					super.run();
					int temp = 0;
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Message msg = new Message();
					if (startTimer != System.currentTimeMillis()) {
						timer = tempTime + System.currentTimeMillis()
								- startTimer;
					}
					handler.sendMessage(msg);// ֪ͨ���߳�
				}
			}
		};
		thread.start();
	}
	
	private void setDate() {
		Calendar mCalendar = Calendar.getInstance();// ��ȡ����Calendar����
		int weekDay = mCalendar.get(Calendar.DAY_OF_WEEK);// ���������
		int month = mCalendar.get(Calendar.MONTH) + 1;// ��ǰ�·�
		int day = mCalendar.get(Calendar.DAY_OF_MONTH);// ��ǰ����

		String week_day_str = new String();
		switch (weekDay) {
		case Calendar.SUNDAY:// ������
			week_day_str = getString(R.string.sunday);
			break;

		case Calendar.MONDAY:// ����һ
			week_day_str = getString(R.string.monday);
			break;

		case Calendar.TUESDAY:// ���ڶ�
			week_day_str = getString(R.string.tuesday);
			break;

		case Calendar.WEDNESDAY:// ������
			week_day_str = getString(R.string.wednesday);
			break;

		case Calendar.THURSDAY:// ������
			week_day_str = getString(R.string.thursday);
			break;

		case Calendar.FRIDAY:// ������
			week_day_str = getString(R.string.friday);
			break;

		case Calendar.SATURDAY:// ������
			week_day_str = getString(R.string.saturday);
			break;
		}
		tv_week_day.setText(month + getString(R.string.month) + day
				+ getString(R.string.day)+" "+week_day_str);
	}
	
}

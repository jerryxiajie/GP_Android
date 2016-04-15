package com.example.myandroid_pro;

import java.util.Calendar;

import android.R.integer;
import android.support.v7.app.ActionBarActivity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewDebug.FlagToString;
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
	private int StPa_flag = 0;/* 0: ��ͣ���ܰ��� 1����ͣ���� */
	private CustomDialog dialog;
	private int steps = 0;
	private int temp = 0;

	public long second_all = 0;

	private long timer = 0;// �˶�ʱ��
	private long startTimer = 0;// ��ʼʱ��
	private long tempTime = 0;
	private long nextTime = 0;

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
		tv_steps.setText("0");
		setDate();

		btn_start.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				if (StPa_flag == 0) { /* �����ظ�����ʼ�� */
					btn_pause.setText("��ͣ");
					btn_start.getBackground().setAlpha(80);
					btn_pause.getBackground().setAlpha(255);
					StPa_flag = 1;
					exit = false;
					startTimer = System.currentTimeMillis();
					nextTime = startTimer;
					tempTime = 0;
					timer = 0;
					steps = 0;
					thread_fuc();

				} else if (StPa_flag == 2) {
					exit = false;
					startTimer = System.currentTimeMillis();
					btn_start.getBackground().setAlpha(80);
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
					exit = true;
					btn_pause.setText("���");
					btn_start.setText("����");
					btn_start.getBackground().setAlpha(255);
					StPa_flag = 2; /* �������������״̬�� 2�����԰��£�3�����ܰ���  */
					alert_show();
				} else if (StPa_flag == 2) { /* Clear ��������ʱ���Ʋ����� */
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
		temp = steps+1;
		customBuilder
				.setTitle("���棿")
				.setMessage("��������" + second_all + "�룬����" + temp + "��")
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
						
						steps++;
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

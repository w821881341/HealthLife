package com.healthslife.activitys;


import com.healthslife.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class SplashActivity extends BaseActivity {
	private static final String TAG = "SplashActivity";
	private static final String LoginFirstPreference = "loginFirstPreference";
	private static final String LoginFirst = "loginFirst";
	Handler handler;
	private SharedPreferences sp;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_splash_activity);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		handler = new Handler(){
			public void handleMessage(Message msg) {
	              switch (msg.what) {
	              	case 0:
	              		Editor editor =  sp.edit();
	              		editor.putBoolean(LoginFirst, false);
	              		editor.commit();
	              		Intent intent = new Intent(SplashActivity.this,LoginActivity.class);
	              		startActivity(intent);
	              		finish();
	              		overridePendingTransition(R.anim.in_from_right,R.anim.out_to_left);
	              		break;
	              	default:
	              }  
	              super.handleMessage(msg);   
	          }   
		};
		
		
	}
	
	
	@Override
	protected void onStart() {
		super.onStart();
		sp = getSharedPreferences(LoginFirstPreference, MODE_PRIVATE);
		sp.getBoolean(LoginFirst, true);
		handler.sendEmptyMessageDelayed(0, 2000);
	}
	
	
	
	
	
	
}
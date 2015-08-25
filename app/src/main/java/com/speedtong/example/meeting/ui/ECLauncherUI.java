/*
 *  Copyright (c) 2013 The CCP project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a Beijing Speedtong Information Technology Co.,Ltd license
 *  that can be found in the LICENSE file in the root of the web site.
 *
 *   http://www.yuntongxun.com
 *
 *  An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */
package com.speedtong.example.meeting.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;

import com.screen.main.R;
import com.speedtong.example.meeting.common.utils.ECPreferenceSettings;
import com.speedtong.example.meeting.common.utils.ECPreferences;
import com.speedtong.example.meeting.common.utils.LogUtil;
import com.speedtong.example.meeting.core.CCPConfig;
import com.speedtong.example.meeting.core.ClientUser;
import com.speedtong.example.meeting.core.SDKCoreHelper;
import com.speedtong.example.meeting.ui.interphonemeeting.InterPhoneActivity;
import com.speedtong.example.meeting.ui.manager.CCPAppManager;
import com.speedtong.example.meeting.ui.videomeeting.VideoconferenceConversation;
import com.speedtong.example.meeting.ui.voicemeeting.ChatroomConversation;
import com.speedtong.sdk.ECDevice;
import com.umeng.analytics.MobclickAgent;

import java.io.InvalidClassException;

/**
 * 应用主界面（初始化二个Tab功能界面）
 * 
 * @author 容联•云通讯
 * @date 2014-12-4
 * @version 4.0
 */
public class ECLauncherUI extends Activity implements View.OnClickListener {

	private KickOffReceiver kickoffReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		initWelcome();

		// umeng
		MobclickAgent.updateOnlineConfig(this);
		MobclickAgent.setDebugMode(true);
		// 设置页面默认为竖屏
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		registerReceiver(new String[] { getPackageName() + "kickedoff" });
	}

	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.bu_interphone:

			startActivity(new Intent(this, InterPhoneActivity.class));

			break;
		case R.id.bu_videomeeting:

			startActivity(new Intent(this, VideoconferenceConversation.class));

			break;

		case R.id.bu_voicemeeting:

			startActivity(new Intent(this, ChatroomConversation.class));

			break;
		case R.id.bu_logout:
			
			try {
				ECPreferences
						.savePreference(
								ECPreferenceSettings.SETTINGS_REGIST_AUTO,
								"", true);
				SDKCoreHelper.logout();

				ECDevice.unInitial();

				finish();
				startActivity(new Intent(ECLauncherUI.this,
						ECLoginActivity.class));
			} catch (InvalidClassException e) {

				e.printStackTrace();
			}
			
			break;
		case R.id.bu_quit:
			
			try {

				SDKCoreHelper.logout();

				ECDevice.unInitial();
				finish();
				android.os.Process.killProcess(android.os.Process.myPid());

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			break;

		default:
			break;
		}
	}

	private class KickOffReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent != null
					&& intent.getAction().equalsIgnoreCase(
							getPackageName() + "kickedoff")) {

				finish();
			}

		}

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(kickoffReceiver);
	}

	protected final void registerReceiver(String[] actionArray) {
		if (actionArray == null) {
			return;
		}
		IntentFilter intentfilter = new IntentFilter();
		for (String action : actionArray) {
			intentfilter.addAction(action);
		}
		if (kickoffReceiver == null) {
			kickoffReceiver = new KickOffReceiver();
		}
		registerReceiver(kickoffReceiver, intentfilter);
	}

	private boolean mInit = false;

	private void initWelcome() {
		if (!mInit) {
			mInit = true;
			setContentView(R.layout.splash_activity);
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					mInit = false;

					setContentView(R.layout.activity_main);
				}
			}, 3000);
		}
	}

	@Override
	protected void onResume() {
		LogUtil.e(LogUtil.getLogUtilsTag(ECLauncherUI.class), "onResume start");
		super.onResume();
		// 统计时长
		MobclickAgent.onResume(this);

		String account = getAutoRegistAccount();
		if (TextUtils.isEmpty(account)) {
			startActivity(new Intent(this, ECLoginActivity.class));
			finish();
			return;
		}
		String[] split = account.split(",");
		ClientUser user = new ClientUser(split[2]);
		user.setSubSid(split[0]);
		user.setSubToken(split[1]);
		user.setUserToken(split[3]);
		user.setUserName(split[4]);
		CCPAppManager.setClientUser(user);
		SDKCoreHelper.init(this);

		// 暂时保存appId ??待改
		CCPConfig.VoIP_ID = CCPAppManager.getClientUser().getUserId();
		CCPConfig.App_ID = getAutoRegistAppid();

	}

	/**
	 * 检查是否需要自动登录
	 * 
	 * @return
	 */
	private String getAutoRegistAccount() {
		SharedPreferences sharedPreferences = ECPreferences
				.getSharedPreferences();
		ECPreferenceSettings registAuto = ECPreferenceSettings.SETTINGS_REGIST_AUTO;
		String registAccount = sharedPreferences.getString(registAuto.getId(),
				(String) registAuto.getDefaultValue());
		return registAccount;
	}

	private String getAutoRegistAppid() {
		SharedPreferences sharedPreferences = ECPreferences
				.getSharedPreferences();
		ECPreferenceSettings registAuto = ECPreferenceSettings.SETTINGS_YUNTONGXUN_APPID;
		String registAccount = sharedPreferences.getString(registAuto.getId(),
				(String) registAuto.getDefaultValue());
		return registAccount;
	}

	@Override
	protected void onPause() {
		LogUtil.d(LogUtil.getLogUtilsTag(getClass()), "KEVIN Launcher onPause");
		super.onPause();
		// 友盟统计API
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

	}

}

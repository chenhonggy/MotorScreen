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
package com.speedtong.example.meeting;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.speedtong.example.meeting.common.utils.DemoUtils;
import com.speedtong.example.meeting.common.utils.LogUtil;
import com.speedtong.example.meeting.common.utils.OrgJosnUtils;
import com.speedtong.example.meeting.common.utils.ToastUtil;
import com.speedtong.example.meeting.core.CCPConfig;
import com.speedtong.example.meeting.ui.manager.CCPAppManager;
import com.speedtong.sdk.core.CCPParameters;
import com.speedtong.sdk.debug.ECLog4Util;
import com.speedtong.sdk.exception.CCPException;
import com.speedtong.sdk.net.AsyncECRequestRunner;
import com.speedtong.sdk.net.InnerRequestListener;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;



/**
 * @author 容联•云通讯
 * @date 2014-12-4
 * @version 4.0
 */
public class ECApplication extends Application {

	private static ECApplication instance;
	
	public final static String VALUE_DIAL_MODE_FREE = "voip_talk";
	public final static String VALUE_DIAL_MODE_BACK = "back_talk";
	public final static String VALUE_DIAL_MODE_DIRECT = "direct_talk";
	public final static String VALUE_DIAL_MODE = "mode";
	public final static String VALUE_DIAL_SOURCE_PHONE = "srcPhone";
	public final static String VALUE_DIAL_VOIP_INPUT = "VoIPInput";
	public final static String VALUE_DIAL_MODE_VEDIO = "vedio_talk";
	public final static String VALUE_DIAL_NAME = "voip_name";
	
	public static ArrayList<String> interphoneIds = null;
	public static ArrayList<String> chatRoomIds;
	
	/**
	 * Activity Action: Start a VoIP incomeing call.
	 */
	public static final String ACTION_VOIP_INCALL = "com.voip.demo.ACTION_VOIP_INCALL";

	/**
	 * Activity Action: Start a VoIP outgoing call.
	 */
	public static final String ACTION_VOIP_OUTCALL = "com.voip.demo.ACTION_VOIP_OUTCALL";

	/**
	 * Activity Action: Start a Video incomeing call.
	 */
	public static final String ACTION_VIDEO_INTCALL = "com.voip.demo.ACTION_VIDEO_INTCALL";

	/**
	 * Activity Action: Start a Video outgoing call.
	 */
	public static final String ACTION_VIDEO_OUTCALL = "com.voip.demo.ACTION_VIDEO_OUTCALL";

	private static final String TAG = "ECApplication";

	/**
	 * 单例，返回一个实例
	 * 
	 * @return
	 */
	public static ECApplication getInstance() {
		if (instance == null) {
			LogUtil.w("[ECApplication] instance is null.");
		}
		return instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		CCPAppManager.setContext(instance);
		
		ECLog4Util.i(TAG, "ECApplication onCreate");
		
		if (interphoneIds == null) {
			interphoneIds = new ArrayList<String>();
		}
		if (chatRoomIds == null) {
			chatRoomIds = new ArrayList<String>();
		}
		
		
		
	}
	
	public void showToast(String text) {
		Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT)
				.show();
	}

	public void showToast(int resId) {
		Toast.makeText(getApplicationContext(), resId, Toast.LENGTH_SHORT)
				.show();
	}
	
	public void setAudioMode(int mode) {
		AudioManager audioManager = (AudioManager) getApplicationContext()
				.getSystemService(Context.AUDIO_SERVICE);
		if (audioManager != null) {
			audioManager.setMode(mode);
		}
	}
	
	
	
	public static boolean isNetWorkConnect(Activity act) {

		ConnectivityManager manager = (ConnectivityManager) act
				.getApplicationContext().getSystemService(
						Context.CONNECTIVITY_SERVICE);

		if (manager == null) {
			return false;
		}

		NetworkInfo networkinfo = manager.getActiveNetworkInfo();

		if (networkinfo == null || !networkinfo.isAvailable()) {
			return false;
		}

		return true;
	}  

	/**
	 * 返回配置文件的日志开关
	 * 
	 * @return
	 */
	public boolean getLoggingSwitch() {
		try {
			ApplicationInfo appInfo = getPackageManager().getApplicationInfo(
					getPackageName(), PackageManager.GET_META_DATA);
			boolean b = appInfo.metaData.getBoolean("LOGGING");
			LogUtil.w("[ECApplication - getLogging] logging is: " + b);
			return b;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return false;
	}

	public boolean getAlphaSwitch() {
		try {
			ApplicationInfo appInfo = getPackageManager().getApplicationInfo(
					getPackageName(), PackageManager.GET_META_DATA);
			boolean b = appInfo.metaData.getBoolean("ALPHA");
			LogUtil.w("[ECApplication - getAlpha] Alpha is: " + b);
			return b;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return false;
	}
}

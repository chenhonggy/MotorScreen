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
package com.speedtong.example.meeting.core;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.speedtong.example.meeting.ECApplication;
import com.speedtong.example.meeting.common.utils.CCPNotificationManager;
import com.speedtong.example.meeting.common.utils.ECPreferenceSettings;
import com.speedtong.example.meeting.common.utils.ECPreferences;
import com.speedtong.example.meeting.common.utils.ToastUtil;
import com.speedtong.example.meeting.db.ContactSqlManager;
import com.speedtong.example.meeting.ui.ECLauncherUI;
import com.speedtong.example.meeting.ui.ECLoginActivity;
import com.speedtong.example.meeting.ui.manager.CCPAppManager;
import com.speedtong.example.meeting.ui.videomeeting.VideoconferenceBaseActivity;
import com.speedtong.sdk.ECDevice;
import com.speedtong.sdk.ECDevice.InitListener;
import com.speedtong.sdk.ECError;
import com.speedtong.sdk.ECInitialize;
import com.speedtong.sdk.core.ECGlobalConstants;
import com.speedtong.sdk.core.interphone.ECInterphoneInviteMsg;
import com.speedtong.sdk.core.interphone.ECInterphoneMeetingMsg;
import com.speedtong.sdk.core.interphone.ECInterphoneOverMsg;
import com.speedtong.sdk.core.meeting.listener.OnMeetingListener;
import com.speedtong.sdk.core.videomeeting.ECVideoMeetingMsg;
import com.speedtong.sdk.core.voicemeeting.ECVoiceMeetingMsg;
import com.speedtong.sdk.debug.ECLog4Util;
import com.speedtong.sdk.platformtools.SdkErrorCode;

import java.io.IOException;
import java.io.InvalidClassException;

/**
 * @author 容联•云通讯
 * @date 2014-12-8
 * @version 4.0
 */
public class SDKCoreHelper implements InitListener,
		ECDevice.OnECDeviceConnectListener, ECDevice.OnLogoutListener,
		OnMeetingListener {

	public static final String ACTION_LOGOUT = "com.speedtong.example.ECDemo_logout";
	public static final String TAG = "SDKCoreHelper";
	private static SDKCoreHelper sInstance;
	private Context mContext;
	private ECInitialize params;
	private Connect mConnect = Connect.ERROR;

	public static final int WHAT_SHOW_PROGRESS = 0x101A;
	public static final int WHAT_CLOSE_PROGRESS = 0x101B;

	public static final int WHAT_ON_CALL_ALERTING = 0x2003;
	public static final int WHAT_ON_CALL_ANSWERED = 0x2004;
	public static final int WHAT_ON_CALL_PAUSED = 0x2005;
	public static final int WHAT_ON_CALL_PAUSED_REMOTE = 0x2006;
	public static final int WHAT_ON_CALL_RELEASED = 0x2007;
	public static final int WHAT_ON_CALL_PROCEEDING = 0x2008;
	public static final int WHAT_ON_CALL_TRANSFERED = 0x2009;
	public static final int WHAT_ON_CALL_MAKECALL_FAILED = 0x2010;
	public static final int WHAT_ON_TEXT_MESSAGE_RECEIVED = 0x2011;
	public static final int WHAT_ON_TEXT_REPORT_RECEIVED = 0x2012;
	public static final int WHAT_ON_CALL_BACKING = 0x2013;
	public static final int WHAT_ON_CALL_VIDEO_RATIO_CHANGED = 0x2014;

	public static final int WHAT_ON_NEW_VOICE = 0x201C;
	public static final int WHAT_ON_AMPLITUDE = 0x201D;
	public static final int WHAT_ON_RECODE_TIMEOUT = 0x202A;
	public static final int WHAT_ON_UPLOAD_VOICE_RES = 0x202B;
	public static final int WHAT_ON_PLAY_VOICE_FINSHING = 0x202C;

	public static final int WHAT_ON_INTERPHONE = 0x203A;
	public static final int WHAT_ON_CONTROL_MIC = 0x203B;
	public static final int WHAT_ON_RELEASE_MIC = 0x203C;
	public static final int WHAT_ON_INTERPHONE_MEMBERS = 0x203D;
	public static final int WHAT_ON_INTERPHONE_SIP_MESSAGE = 0x203E;
	public static final int WHAT_ON_DIMISS_DIALOG = 0x204A;;

	public static final int WHAT_ON_REQUEST_MIC_CONTROL = 0x204C;
	public static final int WHAT_ON_RELESE_MIC_CONTROL = 0x204D;
	public static final int WHAT_ON_PLAY_MUSIC = 0x204E;
	public static final int WHAT_ON_STOP_MUSIC = 0x204F;

	public static final int WHAT_ON_VERIFY_CODE_SUCCESS = 0x205A;
	public static final int WHAT_ON_VERIFY_CODE_FAILED = 0x205B;

	// Chatroom
	public static final int WHAT_ON_CHATROOM_SIP_MESSAGE = 0x205C;
	public static final int WHAT_ON_CHATROOM_MEMBERS = 0x205D;
	public static final int WHAT_ON_CHATROOM_LIST = 0x205E;
	public static final int WHAT_ON_CHATROOM = 0x206A;
	public static final int WHAT_ON_CHATROOM_INVITE = 0x206B;
	public static final int WHAT_ON_MIKE_ANIM = 0x206C;
	public static final int WHAT_ON_CNETER_ANIM = 0x206D;
	public static final int WHAT_ON_VERIFY_CODE = 0x206E;
	public static final int WHAT_ON_CHATROOMING = 0x207A;
	public static final int WHAT_ON_CHATROOM_KICKMEMBER = 0x207B;
	public static final int WHAT_ON_SET_MEMBER_SPEAK = 0x207C;
	public static final String NO_ERROR = "000000";

	private SDKCoreHelper() {

	}

	public static Connect getConnectState() {
		return getInstance().mConnect;
	}

	public static boolean hasFullSize(String inStr) {
		if (inStr.getBytes().length != inStr.length()) {
			return true;
		}
		return false;
	}

	public ECInitialize getParams() {
		return params;
	}

	public void setParams(ECInitialize params) {
		this.params = params;
	}

	public static SDKCoreHelper getInstance() {
		if (sInstance == null) {
			sInstance = new SDKCoreHelper();
		}
		return sInstance;
	}

	/**
	 * 初始化sdk后、成功会回调onInitialized() 失败回调onError(Exception exception)
	 * 
	 * @param ctx
	 */
	public static void init(Context ctx) {
		getInstance().mContext = ctx;
		if (!ECDevice.isInitialized()) {
			getInstance().mConnect = Connect.CONNECTING;
			ECDevice.initial(ctx, getInstance());// 正式开始初始化sdk
			postConnectNotify();
		}
	}

	@Override
	public void onInitialized() {

		if (params == null || params.getInitializeParams() == null
				|| params.getInitializeParams().isEmpty()) {
			ClientUser clientUser = CCPAppManager.getClientUser();
			params = new ECInitialize();
			params.setServerIP("sandboxapp.cloopen.com");
			params.setServerPort(8883);
			params.setSid(clientUser.getUserId()); // voip账号
			params.setSidToken(clientUser.getUserToken());// voip密码
			params.setSubId(clientUser.getSubSid());// 子账号
			params.setSubToken(clientUser.getSubToken());// 子账号密码
			params.setOnECDeviceConnectListener(this);
			params.setOnMeetingListener(this);
			CCPAppManager.setClientUser(clientUser);
			ECLog4Util.i(TAG, "onInitialized");

		}
		ECDevice.login(params);

	}

	@Override
	public void onError(Exception exception) {
		ECLog4Util.e(TAG, "onerror");
		ECDevice.unInitial(); // 释放sdk
	}

	@Override
	public void onConnect() {
		// SDK与云通讯平台连接成功
		getInstance().mConnect = Connect.SUCCESS;
		Intent intent = new Intent();
		intent.setAction(mContext.getPackageName() + ".inited");
		mContext.sendBroadcast(intent);
		postConnectNotify();
		ToastUtil.showMessage("连接成功，可以开始操作啦！");
	}

	@Override
	public void onDisconnect(ECError error) {
		// SDK与云通讯平台断开连接
		getInstance().mConnect = Connect.ERROR;
		postConnectNotify();

		if (error != null
				&& error.errorCode.equals(String
						.valueOf(SdkErrorCode.SDK_KickedOff))) {
			Intent intent = new Intent();
			intent.setAction(mContext.getPackageName() + "kickedoff");
			mContext.sendBroadcast(intent);

			ToastUtil.showMessage("您账号已在另外一台设备登陆");

			try {
				ECPreferences.savePreference(
						ECPreferenceSettings.SETTINGS_REGIST_AUTO, "", true);
				logout();

				ECDevice.unInitial();

				CCPAppManager.clearActivity();
				mContext.startActivity(new Intent(mContext,
						ECLoginActivity.class));
			} catch (InvalidClassException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public static void logout() {
		ECDevice.logout(getInstance());

		release();
	}

	/**
	 * 状态通知
	 */
	private static void postConnectNotify() {
		if (getInstance().mContext instanceof ECLauncherUI) {
			
		}
	}

	@Override
	public void onLogout() {
		if (params != null && params.getInitializeParams() != null) {
			params.getInitializeParams().clear();
		}
		params = null;

	}

	public enum Connect {
		ERROR, CONNECTING, SUCCESS
	}

	public static void release() {

		ContactSqlManager.reset();

	}

	long t = 0;
	private Handler handler;

	public synchronized void setHandler(final Handler handler) {
		this.handler = handler;
		ECLog4Util.w(TAG, "[setHandler] BaseActivity handler was set.");
	}

	private void sendTarget(int what, Object obj) {
		t = System.currentTimeMillis();
		while (handler == null && (System.currentTimeMillis() - t < 3500)) {

			try {
				Thread.sleep(80L);
			} catch (InterruptedException e) {
			}
		}

		if (handler == null) {
			ECLog4Util
					.w(TAG,
							"[RLVoiceHelper] handler is null, activity maybe destory, wait...");
			return;
		}

		Message msg = Message.obtain(handler);
		msg.what = what;
		msg.obj = obj;
		msg.sendToTarget();
	}

	@Override
	public void onReceiveInterphoneMeetingMsg(ECInterphoneMeetingMsg body) {

		ECLog4Util.e(
				TAG,
				"[onReceiveInterphoneMsg ] Receive inter phone message  , id :"
						+ body.interphoneMeetingNo + ",type="
						+ body.getMsgType());

		if (body instanceof ECInterphoneOverMsg) {
			ECApplication.interphoneIds.remove(body.interphoneMeetingNo);
			Intent intent = new Intent(CCPIntentUtils.INTENT_RECIVE_INTER_PHONE);
			mContext.sendBroadcast(intent);
		} else if (body instanceof ECInterphoneInviteMsg) {
			if (ECApplication.interphoneIds.indexOf(body.interphoneMeetingNo) < 0) {
				ECApplication.interphoneIds.add(body.interphoneMeetingNo);
			}
			Intent intent = new Intent(CCPIntentUtils.INTENT_RECIVE_INTER_PHONE);
			try {
				CCPNotificationManager.showNewInterPhoneNoti(mContext,
						body.interphoneMeetingNo);
			} catch (IOException e) {
				e.printStackTrace();
			}
			mContext.sendBroadcast(intent);
		}
		Bundle b = new Bundle();
		b.putParcelable(ECGlobalConstants.INTERPHONEMSG, body);
		sendTarget(WHAT_ON_INTERPHONE_SIP_MESSAGE, b);

	}

	@Override
	public void onReceiveVoiceMeetingMsg(ECVoiceMeetingMsg msg) {

		ECLog4Util.d(TAG,
				"[onReceiveChatRoomMsg ] Receive Chat Room message  , id :"
						+ msg.getVoiceMeetingNo());
		Bundle b = new Bundle();
		b.putParcelable(ECGlobalConstants.CHATROOM_MSG, msg);
		sendTarget(WHAT_ON_CHATROOM_SIP_MESSAGE, b);
	}

	@Override
	public void onReceiveVideoMeetingMsg(ECVideoMeetingMsg msg) {

		ECLog4Util
				.e(TAG,
						"[onReceivevideomsg ] Receive video phone message  , id :"
								+ msg.getVideoMeetingNo() + ",type="
								+ msg.getMsgType());
		Bundle b = new Bundle();
		b.putParcelable("VideoConferenceMsg", msg);

		sendTarget(VideoconferenceBaseActivity.KEY_VIDEO_RECEIVE_MESSAGE, b);

	}

	@Override
	public void onVideoRatioChanged(String voip, int width, int height) {

		Bundle b = new Bundle();
		b.putString("voip", voip);
		b.putInt("width", width);
		b.putInt("height", height);
		
		sendTarget(VideoconferenceBaseActivity.KEY_VIDEO_RATIO_CHANGED, b);
		
		
	}

}

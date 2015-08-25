/*
 *  Copyright (c) 2013 The CCP project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a Beijing Speedtong Information Technology Co.,Ltd license
 *  that can be found in the LICENSE file in the root of the web site.
 *
 *   http://www.cloopen.com
 *
 *  An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */package com.speedtong.example.meeting.ui.videomeeting;

import android.app.ProgressDialog;import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.screen.main.R;
import com.speedtong.example.meeting.core.SDKCoreHelper;
import com.speedtong.example.meeting.ui.CCPBaseActivity;
import com.speedtong.sdk.CloopenReason;
import com.speedtong.sdk.core.ECGlobalConstants;
import com.speedtong.sdk.core.videomeeting.ECVideoMeeting;
import com.speedtong.sdk.core.videomeeting.ECVideoMeetingList;
import com.speedtong.sdk.core.videomeeting.ECVideoMeetingMember;
import com.speedtong.sdk.core.videomeeting.ECVideoMeetingMemberList;
import com.speedtong.sdk.core.videomeeting.ECVideoMeetingMsg;
import com.speedtong.sdk.core.videomeeting.ECVideoPartnerPortrait;
import com.speedtong.sdk.core.videomeeting.ECVideoPartnerPortraitList;
import com.speedtong.sdk.debug.ECLog4Util;

import java.util.List;

/**
 * 
 * <p>
 * Title: VideoConfBaseActivity.java
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2012
 * </p>
 * <p>
 * Company: http://www.cloopen.com
 * </p>
 * 
 * @author Jorstin Chan
 * @date 2013-10-28
 * @version 3.5
 */
public abstract class VideoconferenceBaseActivity extends CCPBaseActivity {

	/**
	 * defined message code so that the recipient can identify what this message
	 * is about. Each {@link android.os.Handler} has its own name-space for message codes,
	 * so you do not need to worry about yours conflicting with other handlers.
	 */
	public static final int KEY_VIDEO_RECEIVE_MESSAGE = 0x1;

	/**
	 * defined message code for Receive Video Conference Message that you create
	 * or Join.
	 */
	public static final int KEY_VIDEO_CONFERENCE_STATE = 0x2;

	/**
	 * defined message code for query members
	 */
	public static final int KEY_VIDEO_CONFERENCE_MEMBERS = 0x3;
	public static final int KEY_VIDEO_RATIO_CHANGED = 0x111;

	/**
	 * defined message code for query Video Conference list.
	 */
	public static final int KEY_VIDEO_CONFERENCE_LIST = 0x4;

	/**
	 * defined message code for invite member join Video Conference.
	 */
	public static final int KEY_VIDEO_CONFERENCE_INVITE_MEMBER = 0x5;

	/**
	 * defined message code for dismiss a Video Conference.
	 */
	public static final int KEY_VIDEO_CONFERENCE_DISMISS = 0x6;

	/**
	 * defined message code for remove member from a Video Conference.
	 */
	public static final int KEY_VIDEO_REMOVE_MEMBER = 0x7;

	/**
	 * defined message code for download video conference member portrait.
	 */
	public static final int KEY_VIDEO_DOWNLOAD_PORTRAIT = 0x8;

	/**
	 * defined message code for query video conference member portrait list.
	 */
	public static final int KEY_VIDEO_GET_PORPRTAIT = 0x9;

	/**
	 * defined message code for get local porprtait of Video Conference.
	 * 
	 * @deprecated
	 */
	public static final int KEY_DELIVER_VIDEO_FRAME = 0x10;

	/**
	 * defined message code for Sswitch main screen of Video Conference.
	 */
	public static final int KEY_SWITCH_VIDEO_SCREEN = 0x11;

	/**
	 * defined message code for do something in background
	 * 
	 * @see ThreadPoolManager#addTask(ITask)
	 */
	public static final int KEY_TASK_DOWNLOAD_PORPRTAIT = 0x12;

	/**
	 * defined message code for init members to VideoUI
	 * 
	 * @see ThreadPoolManager#addTask(ITask)
	 */
	public static final int KEY_TASK_INIT_VIDEOUI_MEMBERS = 0x13;

	/**
	 * defined message code for Sswitch main screen of Video Conference.
	 */
	public static final int KEY_SEND_LOCAL_PORTRAIT = 0x14;

	private ProgressDialog pVideoDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SDKCoreHelper.getInstance().setHandler(handler);
	}

	@Override
	protected void onResume() {
		super.onResume();

		SDKCoreHelper.getInstance().setHandler(handler);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	private final Handler handler = new Handler() {

		public void handleMessage(Message msg) {

			if (msg.what == SDKCoreHelper.WHAT_SHOW_PROGRESS) {

			} else if (msg.what == SDKCoreHelper.WHAT_CLOSE_PROGRESS) {
				
			} else {
				Bundle b = (Bundle) msg.obj;
				int what = msg.what;
				ECLog4Util.e("handler---", "what=====" + what);
				int reason = -1;
				String conferenceId = "";
				if (b.containsKey(ECGlobalConstants.REASON)) {
					reason = b.getInt(ECGlobalConstants.REASON);
				}
				if (b.containsKey(ECGlobalConstants.REASON)) {
					conferenceId = b.getString(ECGlobalConstants.CONFERENCE_ID);
				}

				switch (what) {
				case KEY_VIDEO_RECEIVE_MESSAGE:
					ECVideoMeetingMsg vConferenceMsg = (ECVideoMeetingMsg) b
							.getParcelable("VideoConferenceMsg");
					handleReceiveVideoConferenceMsg(vConferenceMsg);
					break;
				case KEY_VIDEO_CONFERENCE_STATE:

					handleVideoConferenceState(reason, conferenceId);
					break;
				case KEY_VIDEO_CONFERENCE_MEMBERS:

					ECVideoMeetingMemberList videoMembers = (ECVideoMeetingMemberList) b
							.getParcelable("members");
					handleVideoConferenceMembers(reason,
							videoMembers.videoConferenceMembers);
					break;
				case KEY_VIDEO_CONFERENCE_LIST:

					ECVideoMeetingList videoCongferences = (ECVideoMeetingList) b
							.getParcelable("conferences");
					handleVideoConferences(reason,
							videoCongferences.videoConferences);
					break;
				case KEY_VIDEO_CONFERENCE_INVITE_MEMBER:

					handleVideoConferenceInviteMembers(reason, conferenceId);
					break;
				case KEY_VIDEO_CONFERENCE_DISMISS:

					handleVideoConferenceDismiss(reason, conferenceId);

					break;
				case KEY_VIDEO_REMOVE_MEMBER:

					String member = b.getString("member");
					handleVideoConferenceRemoveMember(reason, member);
					break;

				case KEY_VIDEO_GET_PORPRTAIT:

					ECVideoPartnerPortraitList vPortraitLists = (ECVideoPartnerPortraitList) b
							.getParcelable("vPortraitList");
					handleGetPortraitsFromVideoConference(reason,
							vPortraitLists.videoPartnerPortraits);
					break;

				case KEY_VIDEO_DOWNLOAD_PORTRAIT:

					ECVideoPartnerPortrait vPortrait = (ECVideoPartnerPortrait) b
							.getParcelable("portrait");
					handleDownloadVideoConferencePortraits(reason, vPortrait);
					break;
				case KEY_SWITCH_VIDEO_SCREEN:

					handleSwitchRealScreenToVoip(reason);

					break;
				case KEY_SEND_LOCAL_PORTRAIT:

					handleSendLocalPortrait(reason, conferenceId);

					break;
				case KEY_VIDEO_RATIO_CHANGED:

					String voip = b.getString("voip");
					int width = b.getInt("width");
					int height = b.getInt("height");
					handleVideoRatioChanged(voip, width, height);

					break;
				default:
					break;
				}

			}
		}

	};

	

	protected void handleVideoRatioChanged(String voip, int width, int height) {

	}



	/**
	 * @return the handler
	 */
	public Handler getHandler() {
		return handler;
	}

	public final Message getHandleMessage() {
		
		Message msg = getHandler().obtainMessage();
		return msg;
	}

	public final void sendHandleMessage(Message msg) {
		getHandler().sendMessage(msg);
	}

	

	@Deprecated
	public void onVideoConferenceInviteMembers(CloopenReason reason,
			String conferenceId) {

		ECLog4Util.d(SDKCoreHelper.TAG,
				"Video Conference Invite Members reason " + reason
						+ " , conferenceId " + conferenceId);
		showRequestErrorToast(reason);
		Message msg = getHandleMessage();

		Bundle b = new Bundle();
		b.putInt(ECGlobalConstants.REASON, reason.getReasonCode());
		b.putString(ECGlobalConstants.CONFERENCE_ID, conferenceId);
		msg.obj = b;
		msg.arg1 = KEY_VIDEO_CONFERENCE_INVITE_MEMBER;
		sendHandleMessage(msg);
	}

	

	/**
	 * 
	 * <p>
	 * Title: handleReceiveVideoConferenceMsg
	 * </p>
	 * <p>
	 * Description: SIP message callback, when the application receives the
	 * message of the video conference then trigger this method.
	 * {@link com.speedtong.example.meeting.ui.videomeeting.VideoconferenceBaseActivity#KEY_VIDEO_RECEIVE_MESSAGE} , and
	 * {@link com.speedtong.example.meeting.ui.videomeeting.VideoconferenceBaseActivity#onReceiveVideoConferenceMsg(VideoConferenceMsg)}
	 * 
	 * The application can realize their own the code to handle the message The
	 * method runs in the main thread
	 * </p>
	 * 
	 * @param VideoMsg
	 * 
	 * @see #onReceiveVideoConferenceMsg(VideoConferenceMsg)
	 * @see #KEY_VIDEO_RECEIVE_MESSAGE
	 */
	protected void handleReceiveVideoConferenceMsg(ECVideoMeetingMsg VideoMsg) {
	}

	/**
	 * 
	 * <p>
	 * Title: handleVideoConferenceState
	 * </p>
	 * <p>
	 * Description: The method trigger by the application calls the video
	 * conference interface For example: When the application calls for creating
	 * video conferencing interface or calls join into the video interface, SDK
	 * will be Execution the callback method notification application processing
	 * results. The application can realize their own the code to handle the
	 * message
	 * 
	 * The method runs in the main thread
	 * </p>
	 * 
	 * @param reason
	 *            Return 0 if successful ,Others return error code
	 * @param conferenceId
	 * 
	 * @see #onVideoConferenceState(int, String)
	 * @see #KEY_VIDEO_CONFERENCE_STATE
	 */
	protected void handleVideoConferenceState(int reason, String conferenceId) {
	}

	/**
	 * 
	 * <p>
	 * Title: handleVideoConferenceMembers
	 * </p>
	 * <p>
	 * Description: When the application calls the interface for query member of
	 * the video conference SDK will be Execution the callback method
	 * notification ,application processing results. The application can realize
	 * their own the code to handle the message
	 * 
	 * The method runs in the main thread
	 * </p>
	 * 
	 * @param reason
	 *            Return 0 if successful ,Others return error code
	 * @param members
	 * 
	 * @see #onVideoConferenceMembers(int, java.util.List)
	 * @see #KEY_VIDEO_CONFERENCE_MEMBERS
	 */
	protected void handleVideoConferenceMembers(int reason,
			List<ECVideoMeetingMember> members) {
	}

	/**
	 * 
	 * <p>
	 * Title: handleVideoConferences
	 * </p>
	 * <p>
	 * Description: When the application calls the interface for query list of
	 * the video conference SDK will be Execution the callback method
	 * notification ,application processing results. The application can realize
	 * their own the code to handle the message
	 * 
	 * The method runs in the main thread
	 * </p>
	 * 
	 * @param reason
	 *            Return 0 if successful ,Others return error code
	 * @param conferences
	 * 
	 * @see #onVideoConferences(int, java.util.List)
	 * @see #KEY_VIDEO_CONFERENCE_LIST
	 */
	protected void handleVideoConferences(int reason,
			List<ECVideoMeeting> conferences) {
	}

	/**
	 * 
	 * <p>
	 * Title: handleVideoConferenceInviteMembers
	 * </p>
	 * <p>
	 * Description: When the application calls the interface for invite member
	 * join the existing video conference , SDK will be Execution the callback
	 * method notification , application processing results. The application can
	 * realize their own the code to handle the message
	 * 
	 * The method runs in the main thread
	 * </p>
	 * 
	 * @param reason
	 *            Return 0 if successful ,Others return error code
	 * @param conferenceId
	 * 
	 * @see #onVideoConferenceInviteMembers(int, String)
	 * @see #KEY_VIDEO_CONFERENCE_INVITE_MEMBER
	 * 
	 * @deprecated
	 */
	protected void handleVideoConferenceInviteMembers(int reason,
			String conferenceId) {
	}

	/**
	 * 
	 * <p>
	 * Title: handleVideoConferenceDismiss
	 * </p>
	 * <p>
	 * Description: When the creator of the Video Conference calls the interface
	 * to dismiss existing video conference , SDK will be Execution the callback
	 * method notification Interface caller, that processing results.However,
	 * video conference member will be notified video conference was dismiss by
	 * SIP messages {@link #onReceiveVideoConferenceMsg(VideoConferenceMsg)} The
	 * application can realize their own the code to handle the message
	 * 
	 * The method runs in the main thread
	 * </p>
	 * 
	 * @param reason
	 *            Return 0 if successful ,Others return error code
	 * @param conferenceId
	 * 
	 * @see #onReceiveVideoConferenceMsg(VideoConferenceMsg)
	 * @see #onVideoConferenceDismiss(int, String)
	 * @see #KEY_VIDEO_CONFERENCE_DISMISS
	 */
	protected void handleVideoConferenceDismiss(int reason, String conferenceId) {
	}

	/**
	 * 
	 * <p>
	 * Title: handleVideoConferenceRemoveMember
	 * </p>
	 * <p>
	 * Description: When the creator of the Video Conference calls the interface
	 * to remove a member from existing video conference ,SDK will be Execution
	 * the callback method notification Interface caller, that processing
	 * results.However, video conference member will be notified that one member
	 * ha's be remove by SIP messages The application can realize their own the
	 * code to handle the message
	 * 
	 * The method runs in the main thread
	 * </p>
	 * 
	 * @param reason
	 *            Return 0 if successful ,Others return error code
	 * @param member
	 * 
	 * @see #onReceiveVideoConferenceMsg(VideoConferenceMsg)
	 * @see #onVideoConferenceRemoveMember(int, String)
	 * @see #KEY_VIDEO_REMOVE_MEMBER
	 */
	protected void handleVideoConferenceRemoveMember(int reason, String member) {
	}

	/**
	 * 
	 * <p>
	 * Title: handleDownloadVideoConferencePortraits
	 * </p>
	 * <p>
	 * Description: When the caller of download the portrait of voip from
	 * existing video conference SDK will be Execution the callback method
	 * notification Interface caller, that processing results.However, video
	 * conference member will be notified that this file is download by SIP
	 * messages The application can realize their own the code to handle the
	 * message
	 * 
	 * The method runs in the main thread
	 * </p>
	 * 
	 * @param reason
	 *            Return 0 if successful ,Others return error code
	 * @param portrait
	 * 
	 * @see #onDownloadVideoConferencePortraits(int, VideoPartnerPortrait)
	 * @see com.speedtong.sdk.core.ECGlobalConstants#executeCCPDownload(java.util.ArrayList)
	 * @see #KEY_VIDEO_DOWNLOAD_PORTRAIT
	 */
	protected void handleDownloadVideoConferencePortraits(int reason,
			ECVideoPartnerPortrait portrait) {
	}

	/**
	 * 
	 * <p>
	 * Title: handleGetPortraitsFromVideoConference
	 * </p>
	 * <p>
	 * Description: When the caller of query the portrait of voip from existing
	 * video conference SDK will be Execution the callback method notification
	 * Interface caller, that processing results.However, video conference
	 * member will be notified that this list of porprtait by SIP messages The
	 * application can realize their own the code to handle the message
	 * 
	 * The method runs in the main thread
	 * </p>
	 * 
	 * @param reason
	 *            Return 0 if successful ,Others return error code
	 * @param videoPortraits
	 * 
	 * @see #onGetPortraitsFromVideoConference(int, java.util.List)
	 * @see com.speedtong.sdk.core.ECGlobalConstants#getPortraitsFromVideoConference(String)
	 * @see #KEY_VIDEO_GET_PORPRTAIT
	 */
	protected void handleGetPortraitsFromVideoConference(int reason,
			List<ECVideoPartnerPortrait> videoPortraits) {
	}

	/**
	 * 
	 * <p>
	 * Title: handleSwitchRealScreenToVoip
	 * </p>
	 * <p>
	 * Description: When the caller of switch main screen from existing video
	 * conference SDK will be Execution the callback method notification
	 * Interface caller, that processing results.However, video conference
	 * member will be notified that the switch result by SIP messages
	 * 
	 * The application can realize their own the code to handle the message
	 * 
	 * The method runs in the main thread
	 * </p>
	 * 
	 * @param reason
	 */
	protected void handleSwitchRealScreenToVoip(int reason) {
	}

	/**
	 * 
	 * <p>
	 * Title: handleSendLocalPortrait
	 * </p>
	 * <p>
	 * Description: When the caller of send the portrait of voip from existing
	 * video conference SDK will be Execution the callback method notification
	 * Interface caller, that processing results.However, video conference
	 * member will be notified that the Video Frame send result of porprtait by
	 * SIP messages
	 * 
	 * The application can realize their own the code to handle the message
	 * 
	 * The method runs in the main thread
	 * </p>
	 * 
	 * @param reason
	 * @param conferenceId
	 */
	protected void handleSendLocalPortrait(int reason, String conferenceId) {
	};

	public void setVideoTitleBackground() {
		findViewById(R.id.nav_title).setBackgroundResource(
				R.drawable.video_title_bg);
	}

	@Deprecated
	public void setVideoBackSelector() {
		findViewById(R.id.voice_btn_back).setBackgroundResource(
				R.drawable.video_back_button_selector);
		findViewById(R.id.voice_btn_back).setVisibility(View.VISIBLE);
	}

	public void setVideoTitle(String title) {
		((TextView) findViewById(R.id.title)).setText(title);
	}

	// @Override
	// public void onRequestConferenceMemberVideoFailed(final int reason,
	// String conferenceId, final String voip) {
	// getHandler().post(new Runnable() {
	//
	// @Override
	// public void run() {
	// Toast.makeText(VideoconferenceBaseActivity.this, "请求成员[" + voip +
	// "]视频数据失败（错误码：" + reason+ "）", Toast.LENGTH_LONG).show();
	// }
	// });
	//
	// }
	//
	// @Override
	// public void onCancelConferenceMemberVideo(final int reason, String
	// conferenceId,
	// final String voip) {
	// getHandler().post(new Runnable() {
	//
	// @Override
	// public void run() {
	// if(reason == 0) {
	// Toast.makeText(VideoconferenceBaseActivity.this, "取消成员[" + voip +
	// "]视频数据成功", Toast.LENGTH_LONG).show();
	// return ;
	// }
	// Toast.makeText(VideoconferenceBaseActivity.this, "取消成员[" + voip +
	// "]视频数据失败（错误码：" + reason+ "）", Toast.LENGTH_LONG).show();
	// }
	// });
	// }

	@Override
	public int getTitleLayout() {
		// TODO Auto-generated method stub
		return -1;
	}

}
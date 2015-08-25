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
 */
package com.speedtong.example.meeting.ui.videomeeting;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.CCP.phone.CameraInfo;
import com.screen.main.R;
import com.speedtong.example.meeting.common.utils.CCPUtil;
import com.speedtong.example.meeting.common.utils.ToastUtil;
import com.speedtong.example.meeting.core.CCPConfig;
import com.speedtong.example.meeting.core.CCPIntentUtils;
import com.speedtong.example.meeting.core.CCPPreferenceSettings;
import com.speedtong.example.meeting.core.CcpPreferences;
import com.speedtong.example.meeting.core.SDKCoreHelper;
import com.speedtong.example.meeting.ui.manager.CCPAppManager;
import com.speedtong.example.meeting.ui.voicemeeting.ChatroomName;
import com.speedtong.example.meeting.view.CCPAlertDialog;
import com.speedtong.sdk.ECDevice;
import com.speedtong.sdk.ECError;
import com.speedtong.sdk.ECMultMeetingType;
import com.speedtong.sdk.Rotate;
import com.speedtong.sdk.core.ECCreateMeetingParams;
import com.speedtong.sdk.core.ECGlobalConstants;
import com.speedtong.sdk.core.ECMeetingType;
import com.speedtong.sdk.core.interphone.ECInterphoneMeetingMember;
import com.speedtong.sdk.core.meeting.listener.OnCreateOrJoinMeetingListener;
import com.speedtong.sdk.core.meeting.listener.OnDeleteMeetingListener;
import com.speedtong.sdk.core.meeting.listener.OnQueryMeetingMembersListener;
import com.speedtong.sdk.core.meeting.listener.OnRemoveMemberFromMeetingListener;
import com.speedtong.sdk.core.meeting.video.listener.OnCancelRequestMemberVideoInVideoMeetingListener;
import com.speedtong.sdk.core.meeting.video.listener.OnRequestMemberVideoInVideoMeetingListener;
import com.speedtong.sdk.core.meeting.video.listener.OnSelfVideoFrameChangedListener;
import com.speedtong.sdk.core.videomeeting.ECVideoMeetingDeleteMsg;
import com.speedtong.sdk.core.videomeeting.ECVideoMeetingExitMsg;
import com.speedtong.sdk.core.videomeeting.ECVideoMeetingJoinMsg;
import com.speedtong.sdk.core.videomeeting.ECVideoMeetingMember;
import com.speedtong.sdk.core.videomeeting.ECVideoMeetingMsg;
import com.speedtong.sdk.core.videomeeting.ECVideoMeetingRemoveMemberMsg;
import com.speedtong.sdk.core.videomeeting.ECVideoPartnerPortrait;
import com.speedtong.sdk.core.voicemeeting.ECVoiceMeetingMember;
import com.speedtong.sdk.debug.ECLog4Util;

import org.webrtc.videoengine.ViERenderer;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Title: MultiVideoconference.java
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2014
 * </p>
 * <p>
 * Company: Beijing Speedtong Information Technology Co.,Ltd
 * </p>
 * 
 * @author Jorstin Chan
 * @date 2014-8-27
 * @version 1.0
 */
public class MultiVideoconference extends VideoconferenceBaseActivity implements
		View.OnClickListener, CCPMulitVideoUI.OnVideoUIItemClickListener,
		CCPAlertDialog.OnPopuItemClickListener {

	/**
	 * The definition of video conference pattern of joining if the creator or
	 * join Invitation model
	 * 
	 * @see #modeType
	 * @see #MODE_VIDEO_C_INITIATED_INTERCOM
	 */
	private static final int MODE_VIDEO_C_INVITATION = 0x0;

	/**
	 * Creator pattern model
	 * 
	 * @see #modeType
	 * @see #MODE_VIDEO_C_INVITATION
	 */
	private static final int MODE_VIDEO_C_INITIATED_INTERCOM = 0x1;

	/**
	 * Unique identifier defined message queue
	 * 
	 * @see #getBaseHandle()
	 */
	public static final int WHAT_ON_VIDEO_NOTIFY_TIPS = 0X2;

	/**
	 * Unique identifier defined message queue
	 * 
	 * @see #getBaseHandle()
	 */
	public static final int WHAT_ON_VIDEO_REFRESH_VIDEOUI = 0X3;

	/**
	 * The definition of the status bar at the top of the transition time to
	 * update the state background
	 */
	public static final int ANIMATION_DURATION = 2000;

	/**
	 * The definition of the status bar at the top of the transition time to
	 * update the state background
	 */
	public static final int ANIMATION_DURATION_RESET = 1000;

	/**
	 * 
	 */
	public static final String PREFIX_LOCAL_VIDEO = "local_";

	protected static final String TAG = "MultiVideoconference";

	public HashMap<String, Integer> mVideoMemberUI = new HashMap<String, Integer>();
	public HashMap<String, ECVideoPartnerPortrait> mVideoPorprtaitCache = new HashMap<String, ECVideoPartnerPortrait>();

	private TextView mVideoTips;
	private ImageButton mCameraControl;
	private CCPMulitVideoUI mVideoConUI;
	private ImageButton mMuteControl;
	private ImageButton mVideoControl;

	private FrameLayout mVideoUIRoot;
	private View instructionsView;
	private View videoMainView;
	private Button mExitVideoCon;

	private String mVideoMainScreenVoIP;
	private String mVideoConferenceId;
	private String mVideoCreate;
	private CameraInfo[] cameraInfos;
	// The first rear facing camera
	int defaultCameraId;
	int numberOfCameras;
	int cameraCurrentlyLocked;

	/**
	 * Capbility index of pixel.
	 */
	int mCameraCapbilityIndex;

	private int modeType;
	private boolean isMute = false;
	private boolean isVideoConCreate = false;
	private boolean isVideoChatting = false;
	private boolean mPubish = true;

	// Whether to display all the members including frequency
	@Deprecated
	private boolean isDisplayAllMembers = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mVideoUIRoot = addFeatureGuide();
		setContentView(mVideoUIRoot);

		initResourceRefs();

		initialize(savedInstanceState);
		cameraInfos = ECDevice.getECVoipSetManager().getCameraInfos();

		// Find the ID of the default camera
		if (cameraInfos != null) {
			numberOfCameras = cameraInfos.length;
		}

		// Find the total number of cameras available
		for (int i = 0; i < numberOfCameras; i++) {
			if (cameraInfos[i].index == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
				defaultCameraId = i;
				mCameraCapbilityIndex = CCPUtil
						.comportCapbilityIndex(cameraInfos[i].caps);
			}
		}

		CCPConfig.VoIP_ID = CCPAppManager.getClientUser().getUserId();

		ECDevice.getECVoipSetManager().enableLoudSpeaker(true);

	}

	private void initResourceRefs() {
		mVideoTips = (TextView) findViewById(R.id.notice_tips);

		mVideoConUI = (CCPMulitVideoUI) findViewById(R.id.video_ui);
		mVideoConUI.setOnVideoUIItemClickListener(this);

		mCameraControl = (ImageButton) findViewById(R.id.camera_control);
		mMuteControl = (ImageButton) findViewById(R.id.mute_control);
		mVideoControl = (ImageButton) findViewById(R.id.video_control);
		mVideoControl.setVisibility(View.GONE);
		mCameraControl.setOnClickListener(this);
		mMuteControl.setOnClickListener(this);
		mMuteControl.setEnabled(false);
		mCameraControl.setEnabled(false);
		mVideoControl.setOnClickListener(this);

		mExitVideoCon = (Button) findViewById(R.id.out_video_c_submit);
		mExitVideoCon.setOnClickListener(this);

		initNewInstructionResourceRefs();
	}

	private void initNewInstructionResourceRefs() {
		if (instructionsView != null)
			findViewById(R.id.begin_video_conference).setOnClickListener(this);
	}

	private int count = 0;

	private void initialize(Bundle savedInstanceState) {
		Intent intent = getIntent();
		String roomName = null;
		boolean is_auto_close = true;
		int autoDelete = 1;
		int voiceMode = 1;
		if (intent.hasExtra(ChatroomName.AUTO_DELETE)) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				autoDelete = extras.getInt(ChatroomName.AUTO_DELETE);
			}
		}
		if (intent.hasExtra(ChatroomName.VOICE_MOD)) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				voiceMode = extras.getInt(ChatroomName.VOICE_MOD);
			}
		}
		if (intent.hasExtra(ChatroomName.IS_AUTO_CLOSE)) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				is_auto_close = extras.getBoolean(ChatroomName.IS_AUTO_CLOSE);
			}
		}
		if (intent.hasExtra(ChatroomName.IS_AUTO_CLOSE)) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				is_auto_close = extras.getBoolean(ChatroomName.IS_AUTO_CLOSE);
			}
		}
		if (intent.hasExtra(ChatroomName.CHATROOM_NAME)) {
			modeType = MODE_VIDEO_C_INITIATED_INTERCOM;
			Bundle extras = intent.getExtras();
			if (extras != null) {
				roomName = extras.getString(ChatroomName.CHATROOM_NAME);
				if (TextUtils.isEmpty(roomName)) {
					finish();
				} else {
					mVideoCreate = extras
							.getString(VideoconferenceConversation.CONFERENCE_CREATOR);
					isVideoConCreate = CCPConfig.VoIP_ID.equals(mVideoCreate);
					// mVideoConUI.setOperableEnable(isVideoConCreate);
					if (!isVideoConCreate && instructionsView != null)
						instructionsView.setVisibility(View.GONE);
				}

			}
		}

		if (intent.hasExtra(ECGlobalConstants.CONFERENCE_ID)) {
			// To invite voice group chat
			modeType = MODE_VIDEO_C_INVITATION;
			Bundle extras = intent.getExtras();
			if (extras != null) {
				mVideoConferenceId = extras
						.getString(ECGlobalConstants.CONFERENCE_ID);
				if (TextUtils.isEmpty(mVideoConferenceId)) {
					finish();
				}
			}

		}

		// init VideoUI View key
		initUIKey();

		mVideoTips.setText(R.string.top_tips_connecting_wait);

		ECDevice.getECVoipSetManager().setVideoView(
				mVideoConUI.getMainSurfaceView(), null);
		// Launched a Viode Conference request, waiting for SDK to return.
		if (modeType == MODE_VIDEO_C_INITIATED_INTERCOM) {// 自动创建、加入

			ECCreateMeetingParams.Builder builder = new ECCreateMeetingParams.Builder();
			builder.setAppId(CCPConfig.App_ID).setMeetingName(roomName)
					.setSquare(5).setVoiceMod(voiceMode)
					.setAutoDelete(autoDelete == 1 ? true : false)
					.setAutoJoin(true).setKeywords("").setMeetingPwd("")
					.setAutoClose(is_auto_close);
			ECCreateMeetingParams params = builder.build();

			ECDevice.getECMeetingManager().createMultMeetingByType(params,
					ECMultMeetingType.ECMeetingVideo,
					new OnCreateOrJoinMeetingListener() {

						@Override
						public void onCreateOrJoinMeeting(ECError reason,
								String meetingNo) {

							ECLog4Util.e(TAG, reason.toString() + "---"
									+ meetingNo);
							count++;
							mVideoConferenceId = meetingNo;
							if (count == 2) {
								refreshUIAfterjoinSuccess(reason, meetingNo);
							}
						}
					});

		} else if (modeType == MODE_VIDEO_C_INVITATION) {

			
			ECDevice.getECMeetingManager().joinMeetingByType(
					mVideoConferenceId, ECMeetingType.ECMeetingVideo, "1234",
					new OnCreateOrJoinMeetingListener() {

						@Override
						public void onCreateOrJoinMeeting(ECError reason,
								String meetingNo) {

							ECLog4Util.d(TAG, reason.toString() + "---"
									+ meetingNo);
							ECDevice.getECVoipSetManager().enableLoudSpeaker(
									true);
							refreshUIAfterjoinSuccess(reason, meetingNo);
						}
					});
		}

	}

	protected void refreshUIAfterjoinSuccess(ECError reason, String conferenceId) {

		if (!reason.isError()) {

			isVideoChatting = true;
			mVideoConferenceId = conferenceId;
			mExitVideoCon.setEnabled(true);
			mCameraControl.setEnabled(true);
			mMuteControl.setEnabled(true);
			isMute = ECDevice.getECVoipSetManager().getMuteStatus();
			initMute();

			updateVideoNoticeTipsUI(getString(R.string.video_tips_joining,
					conferenceId));

			ECDevice.getECMeetingManager().queryMeetingMembersByType(
					mVideoConferenceId, ECMeetingType.ECMeetingVideo,
					new OnQueryMeetingMembersListener() {

						@Override
						public void onQueryVoiceMeetingMembers(ECError reason,
								List<ECVoiceMeetingMember> members) {

						}

						@Override
						public void onQueryVideoMeetingMembers(ECError reason,
								List<ECVideoMeetingMember> members) {

							ECLog4Util.d(TAG, reason.toString() + "--"
									+ members);

							if (reason.errorCode.equals(SDKCoreHelper.NO_ERROR)) {

								if (mulitMembers == null) {
									mulitMembers = new ArrayList<MulitVideoMember>();
								}
								mulitMembers.clear();
								for (ECVideoMeetingMember member : members) {
									MulitVideoMember mulitMember = new MulitVideoMember(
											member);
									mulitMembers.add(mulitMember);
								}
								initMembersOnVideoUI(mulitMembers);
							}

						}

						@Override
						public void onQueryInterphoneMeetingMembers(
								ECError reason,
								List<ECInterphoneMeetingMember> members) {

						}
					});

		} else {
			isVideoChatting = false;
			ECDevice.getECMeetingManager().exitMeeting();

			ToastUtil.showMessage(R.string.str_join_video_c_failed_content);
			finish();
		}

	}

	private void initMute() {
		if (isMute) {
			mMuteControl.setImageResource(R.drawable.mute_forbid_selector);
		} else {
			mMuteControl.setImageResource(R.drawable.mute_enable_selector);
		}
	}

	/**
	 * 
	 * <p>
	 * Title: setMuteUI
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 */
	private void setMuteUI() {

		try {
			ECDevice.getECVoipSetManager().setMute(!isMute);
			isMute = ECDevice.getECVoipSetManager().getMuteStatus();
			initMute();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * <p>
	 * Title: updateVideoNoticeTipsUI
	 * </p>
	 * <p>
	 * Description: Display a background update the status bar, as well as text
	 * between update and normal.
	 * </p>
	 * 
	 * @param text
	 */
	public void updateVideoNoticeTipsUI(CharSequence text) {

		if (!TextUtils.isEmpty(text)) {
			getBaseHandle().removeMessages(WHAT_ON_VIDEO_NOTIFY_TIPS);
			mVideoTips.setText(text);
			TransitionDrawable transition = (TransitionDrawable) mVideoTips
					.getBackground();
			transition.resetTransition();
			transition.startTransition(ANIMATION_DURATION);
			Message msg = getBaseHandle().obtainMessage(
					WHAT_ON_VIDEO_NOTIFY_TIPS);
			getBaseHandle().sendMessageDelayed(msg, 6000);
		}
	}

	ArrayList<Integer> UIKey = new ArrayList<Integer>();

	private List<MulitVideoMember> mulitMembers;

	/**
	 * 
	 * <p>
	 * Title: getCCPMulitVideoUIKey
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 * 
	 * @return
	 */
	public synchronized Integer getCCPMulitVideoUIKey() {

		if (UIKey.isEmpty()) {
			return null;
		}
		return UIKey.remove(0);
	}

	public synchronized void putCCPMulitVideoUIKey(Integer key) {

		if (UIKey.size() > 4) {
			return;
		}

		if (key <= 2) {
			return;
		}

		UIKey.add(/* key - 3, */key);
		Collections.sort(UIKey, new Comparator<Integer>() {
			@Override
			public int compare(Integer lsdKey, Integer rsdKey) {

				// Apply sort mode
				return lsdKey.compareTo(rsdKey);
			}
		});
	}

	public void putVideoUIMemberCache(MulitVideoMember member, Integer key) {
		synchronized (mVideoMemberUI) {
			putVideoUIMemberCache(member.getNumber(), key);
			mulitMembers.add(member);
		}
	}

	/**
	 * 
	 * <p>
	 * Title: putVideoUIMemberCache
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 * 
	 * @param who
	 * @param key
	 */
	public void putVideoUIMemberCache(String who, Integer key) {
		synchronized (mVideoMemberUI) {
			if (key == CCPMulitVideoUI.LAYOUT_KEY_MAIN_SURFACEVIEW) {
				mVideoMainScreenVoIP = who;
			} else {
				mVideoMemberUI.put(who, key);
			}
		}
	}

	/**
	 * 
	 * <p>
	 * Title: isVideoUIMemberCacheEmpty
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 * 
	 * @return
	 */
	public boolean isVideoUIMemberCacheEmpty() {
		synchronized (mVideoMemberUI) {
			return mVideoMemberUI.isEmpty();
		}
	}

	/**
	 * 
	 * <p>
	 * Title: removeVideoUIMemberFormCache
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 * 
	 * @param who
	 * @return
	 */
	public Integer removeVideoUIMemberFormCache(String who) {
		synchronized (mVideoMemberUI) {
			Integer key = mVideoMemberUI.remove(who);
			return key;
		}
	}

	/**
	 * 
	 * <p>
	 * Title: queryVideoUIMemberFormCache
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 * 
	 * @param who
	 * @return
	 */
	public Integer queryVideoUIMemberFormCache(String who) {
		synchronized (mVideoMemberUI) {
			if (!mVideoMemberUI.containsKey(who)
					&& !who.equals(mVideoMainScreenVoIP)) {
				return null;
			}
			Integer key = null;

			if (mVideoMemberUI.containsKey(who)) {
				key = mVideoMemberUI.get(who);
			} else {

				if (who.equals(mVideoMainScreenVoIP)) {
					key = CCPMulitVideoUI.LAYOUT_KEY_MAIN_SURFACEVIEW;
				}
			}

			return key;
		}
	}

	/**
	 * 
	 * <p>
	 * Title: getVideoVoIPByCCPUIKey
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 * 
	 * @param CCPUIKey
	 * @return
	 */
	private String getVideoVoIPByCCPUIKey(Integer CCPUIKey) {
		synchronized (mVideoMemberUI) {
			if (CCPUIKey == CCPMulitVideoUI.LAYOUT_KEY_MAIN_SURFACEVIEW) {
				return mVideoMainScreenVoIP;
			}
			if (CCPUIKey != null) {
				for (Map.Entry<String, Integer> entry : mVideoMemberUI
						.entrySet()) {
					if (CCPUIKey.intValue() == entry.getValue().intValue()) {
						return entry.getKey();
					}
				}
			}
			return null;
		}
	}

	/**
	 * 
	 * <p>
	 * Title: isVideoUIMemberExist
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 * 
	 * @param who
	 * @return
	 */
	private boolean isVideoUIMemberExist(String who) {
		synchronized (mVideoMemberUI) {
			if (TextUtils.isEmpty(who)) {
				return false;
			}

			if (mVideoMemberUI.containsKey(who)) {
				return true;
			}

			return false;
		}
	}

	public synchronized void initUIKey() {
		UIKey.clear();
		UIKey.add(CCPMulitVideoUI.LAYOUT_KEY_SUB_VIEW_1);
		UIKey.add(CCPMulitVideoUI.LAYOUT_KEY_SUB_VIEW_2);
		UIKey.add(CCPMulitVideoUI.LAYOUT_KEY_SUB_VIEW_3);
		UIKey.add(CCPMulitVideoUI.LAYOUT_KEY_SUB_VIEW_4);

	}

	/**
	 * 
	 * @Title: addFeatureGuide
	 * @Description: TODO
	 * @param @return
	 * @return FrameLayout
	 * @throws
	 */
	private FrameLayout addFeatureGuide() {
		FrameLayout.LayoutParams iViewFLayoutParams = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT);
		iViewFLayoutParams.gravity = 17;

		videoMainView = getLayoutInflater().inflate(
				R.layout.mulit_video_conference, null);
		videoMainView.setLayoutParams(iViewFLayoutParams);

		FrameLayout frameLayout = new FrameLayout(this);
		frameLayout.addView(videoMainView);

		boolean instrucViewEnable = CcpPreferences
				.getSharedPreferences()
				.getBoolean(
						CCPPreferenceSettings.SETTING_SHOW_INSTRUCTIONS_VIEW
								.getId(),
						Boolean.TRUE);
		if (instrucViewEnable && instructionsView == null) {
			instructionsView = getLayoutInflater().inflate(
					R.layout.new_instructions, null);
			instructionsView.setLayoutParams(iViewFLayoutParams);
			instructionsView.setVisibility(View.VISIBLE);
			frameLayout.addView(instructionsView);
		}

		return frameLayout;
	}

	@Override
	protected void onResume() {
		super.onResume();
		DisplayLocalSurfaceView();

		lockScreen();

	}

	private void DisplayLocalSurfaceView() {
		SurfaceView localView = ViERenderer.CreateLocalRenderer(this);
		localView.setZOrderOnTop(false);
		mVideoConUI.setSubSurfaceView(localView);

		cameraCurrentlyLocked = defaultCameraId;

		ECDevice.getECVoipSetManager().selectCamera(cameraCurrentlyLocked,
				mCameraCapbilityIndex, 15, Rotate.Rotate_Auto, true);

	}

	/**
	 * 
	 * <p>
	 * Title: exitOrDismissVideoConference
	 * </p>
	 * <p>
	 * Description: dismiss or exit of Video Conference
	 * </p>
	 * 
	 * @param exit
	 */
	public void exitOrDismissVideoConference(boolean dismiss) {

		if (dismiss && isVideoConCreate) {

			showConnectionProgress(getString(R.string.str_dialog_message_default));

			ECDevice.getECMeetingManager().deleteMultMeetingByType(
					CCPConfig.App_ID, ECMultMeetingType.ECMeetingVideo,
					mVideoConferenceId, new OnDeleteMeetingListener() {

						@Override
						public void onDismissMeeting(ECError reason,
								String meetingNo) {
							closeConnectionProgress();
							ECLog4Util.d(TAG, reason.toString() + "--"
									+ meetingNo);
						}
					});

		} else {
			ECDevice.getECMeetingManager().exitMeeting();

			// If it is the creator of the video conference, then don't send
			// broadcast
			// when exit of the video conference.
			// Because the creators of Video Conference in create a video
			// conference
			// don't add to video conference list. And the video conference
			// creator exit that is
			// dismiss video conference, without notice to refresh the list
			if (!isVideoConCreate && dismiss) {
				Intent disIntent = new Intent(
						CCPIntentUtils.INTENT_VIDEO_CONFERENCE_DISMISS);
				disIntent.putExtra(ECGlobalConstants.CONFERENCE_ID,
						mVideoConferenceId);
				sendBroadcast(disIntent);
			}

		}
		finish();
	}

	@Override
	protected void onPause() {
		super.onPause();

		releaseLockScreen();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.out_video_c_submit:

			mExitVideoCon.setEnabled(false);
			doVideoConferenceDisconnect();
			mExitVideoCon.setEnabled(true);
			break;

		case R.id.camera_control:

			mCameraControl.setEnabled(false);
			// check for availability of multiple cameras
			if (cameraInfos.length == 1) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(this.getString(R.string.camera_alert))
						.setNeutralButton(R.string.dialog_alert_close, null);
				AlertDialog alert = builder.create();
				alert.show();
				return;
			}

			// OK, we have multiple cameras.
			// Release this camera -> cameraCurrentlyLocked
			cameraCurrentlyLocked = (cameraCurrentlyLocked + 1)
					% numberOfCameras;
			mCameraCapbilityIndex = CCPUtil
					.comportCapbilityIndex(cameraInfos[cameraCurrentlyLocked].caps);

			if (cameraCurrentlyLocked == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
				defaultCameraId = android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT;
				Toast.makeText(MultiVideoconference.this,
						R.string.camera_switch_front, Toast.LENGTH_SHORT)
						.show();
				mCameraControl
						.setImageResource(R.drawable.camera_switch_back_selector);
			} else {
				Toast.makeText(MultiVideoconference.this,
						R.string.camera_switch_back, Toast.LENGTH_SHORT).show();
				defaultCameraId = android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK;
				mCameraControl
						.setImageResource(R.drawable.camera_switch_font_selector);

			}

			ECDevice.getECVoipSetManager().selectCamera(cameraCurrentlyLocked,
					mCameraCapbilityIndex, 15, Rotate.Rotate_Auto, false);

			mCameraControl.setEnabled(true);
			break;

		case R.id.mute_control:

			setMuteUI();
			break;
		case R.id.begin_video_conference:
			try {
				if (mVideoUIRoot != null) {
					mVideoUIRoot.removeView(instructionsView);
					CcpPreferences
							.savePreference(
									CCPPreferenceSettings.SETTING_SHOW_INSTRUCTIONS_VIEW,
									Boolean.FALSE, true);
				}
			} catch (Exception e) {
			}
			break;
		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mVideoMemberUI != null) {
			mVideoMemberUI.clear();
			mVideoMemberUI = null;
		}

		if (mVideoPorprtaitCache != null) {
			mVideoPorprtaitCache.clear();
			mVideoPorprtaitCache = null;
		}

		if (mVideoConUI != null) {
			mVideoConUI.release();
			mVideoConUI = null;
		}

		instructionsView = null;
		videoMainView = null;

		mVideoConferenceId = null;
		mVideoCreate = null;
		cameraInfos = null;

		// The first rear facing camera
		isMute = false;
		isVideoConCreate = false;
		isVideoChatting = false;
		ECDevice.getECVoipSetManager().enableLoudSpeaker(false);

	}

	/**
	 * Set the main video, if the main video new set of main video server ID and
	 * current ID the same, will cancel the compulsory primary video, becomes
	 * automatic switching mode. So you can also use this method to achieve the
	 * "abolition of compulsory primary video" function.
	 */
	@Override
	public void onVideoUIItemClick(int CCPUIKey) {

		int[] ccpAlertResArray = new int[2];
		int title = 0;
		if (CCPUIKey == CCPMulitVideoUI.LAYOUT_KEY_SUB_SURFACEVIEW) {
			// If he is the create of Video Conference
			// The main object is not myself
			if (!mPubish) {
				ccpAlertResArray[0] = R.string.video_publish_video_frame;
			} else {
				ccpAlertResArray[0] = R.string.video_unpublish_video_frame;
			}
			if (isVideoConCreate) {
				ccpAlertResArray[1] = R.string.video_c_dismiss;
			} else {
				ccpAlertResArray[1] = R.string.video_c_logout;
			}
		} else {
			String who = getVideoVoIPByCCPUIKey(CCPUIKey);
			MulitVideoMember mulitVideoMember = getMulitVideoMember(who);
			if (mulitVideoMember == null) {
				return;
			}

			ccpAlertResArray = new int[2];
			if (mulitVideoMember.isRequestVideoFrame()) {
				ccpAlertResArray[0] = R.string.str_quxiao_sp;
			} else {
				ccpAlertResArray[0] = R.string.str_request_sp;
			}
			if (who.equals(mVideoMainScreenVoIP)) {
				ccpAlertResArray[1] = R.string.str_xiao;
			} else {
				ccpAlertResArray[1] = R.string.str_da;
			}
			if (isVideoConCreate) {
				int[] _arr = new int[] { ccpAlertResArray[0],
						ccpAlertResArray[1], R.string.str_video_manager_remove };
				ccpAlertResArray = _arr;
			}
		}
		CCPAlertDialog ccpAlertDialog = new CCPAlertDialog(
				MultiVideoconference.this, title, ccpAlertResArray, 0,
				R.string.dialog_cancle_btn);

		// set CCP UIKey
		ccpAlertDialog.setUserData(CCPUIKey);
		ccpAlertDialog.setOnItemClickListener(this);
		ccpAlertDialog.create();
		ccpAlertDialog.show();
	}

	/**
	 * 
	 * @param who
	 * @return
	 */
	public MulitVideoMember getMulitVideoMember(String who) {
		for (MulitVideoMember member : mulitMembers) {
			if (member != null && who.equals(member.getNumber())) {
				return member;
			}
		}
		return null;
	}

	@Override
	public void onItemClick(ListView parent, View view, int position,
			int resourceId) {
		switch (resourceId) {
		case R.string.video_c_logout:
			exitOrDismissVideoConference(false);
			break;
		case R.string.video_c_dismiss:
			exitOrDismissVideoConference(isVideoConCreate);
			break;
		case R.string.video_publish_video_frame:
		case R.string.video_unpublish_video_frame:

			if (mPubish) {
				showConnectionProgress(getString(R.string.video_unpublish_video_frame));

				ECDevice.getECMeetingManager()
						.cancelPublishSelfVideoFrameInVideoMeeting(
								CCPConfig.App_ID, mVideoConferenceId,
								new OnSelfVideoFrameChangedListener() {

									@Override
									public void onPublishSelfVideoFrameResult(
											ECError reason) {

									}

									@Override
									public void onCancelPublishSelfVideoFrameResult(
											ECError reason) {
										ECLog4Util.d(TAG, reason.toString());
										closeConnectionProgress();
										mPubish = false;

									}
								});

			} else {
				showConnectionProgress(getString(R.string.video_publish_video_frame));

				ECDevice.getECMeetingManager()
						.publishSelfVideoFrameInVideoMeeting(CCPConfig.App_ID,
								mVideoConferenceId,
								new OnSelfVideoFrameChangedListener() {

									@Override
									public void onPublishSelfVideoFrameResult(
											ECError reason) {
										closeConnectionProgress();
										mPubish = true;
										ECLog4Util.d(TAG, reason.toString());
									}

									@Override
									public void onCancelPublishSelfVideoFrameResult(
											ECError reason) {

									}
								});
			}

			break;
		case R.string.str_da:
		case R.string.str_xiao:

			Integer CCPUIKey = (Integer) view.getTag();
			String voipSwitch = getVideoVoIPByCCPUIKey(CCPUIKey);
			if (TextUtils.isEmpty(voipSwitch)) {
				return;
			}
			MulitVideoMember mulitVideoMember = getMulitVideoMember(voipSwitch);
			if (mulitVideoMember == null) {
				return;
			}
			if (TextUtils.isEmpty(mVideoMainScreenVoIP)) {
				doChangeVideoFrameSurfaceViewRequest(
						mVideoConUI.getMainSurfaceView(), mulitVideoMember);
				SurfaceView surfaceView = mVideoConUI.getSurfaceView(CCPUIKey);
				clearScreen(surfaceView);
				mVideoMainScreenVoIP = mulitVideoMember.getNumber();
				return;
			}

			if (!TextUtils.isEmpty(mVideoMainScreenVoIP)
					&& mVideoMainScreenVoIP.equals(voipSwitch)) {
				SurfaceView surfaceView = mVideoConUI.getSurfaceView(CCPUIKey
						.intValue());
				clearScreen(mVideoConUI.getMainSurfaceView());
				doChangeVideoFrameSurfaceViewRequest(surfaceView,
						mulitVideoMember);
				clearScreen(mVideoConUI.getMainSurfaceView());
				mVideoMainScreenVoIP = null;
				surfaceView.refreshDrawableState();
				return;
			}

			if (!TextUtils.isEmpty(mVideoMainScreenVoIP)
					&& !mVideoMainScreenVoIP.equals(voipSwitch)) {
				// remove src
				Integer indexKey = queryVideoUIMemberFormCache(mVideoMainScreenVoIP);
				SurfaceView surfaceView = mVideoConUI.getSurfaceView(indexKey);
				doChangeVideoFrameSurfaceViewRequest(surfaceView,
						getMulitVideoMember(mVideoMainScreenVoIP));

				// set
				doChangeVideoFrameSurfaceViewRequest(
						mVideoConUI.getMainSurfaceView(), mulitVideoMember);
				SurfaceView srcSurfaceView = mVideoConUI
						.getSurfaceView(CCPUIKey);
				clearScreen(srcSurfaceView);
				mVideoMainScreenVoIP = mulitVideoMember.getNumber();

			}
			break;
		case R.string.str_quxiao_sp:
		case R.string.str_request_sp: // ??
			Integer CCPUIKey2 = (Integer) view.getTag();
			String voipSwitch2 = null;
			if (CCPUIKey2.intValue() == CCPMulitVideoUI.LAYOUT_KEY_SUB_SURFACEVIEW) {
				voipSwitch2 = CCPConfig.VoIP_ID;
			} else {
				voipSwitch2 = getVideoVoIPByCCPUIKey(CCPUIKey2);
			}

			if (TextUtils.isEmpty(voipSwitch2)) {
				return;
			}
			MulitVideoMember mulitVideoMember2 = getMulitVideoMember(voipSwitch2);
			doHandlerMemberVideoFrameRequest(CCPUIKey2, mulitVideoMember2);
			break;
		// The members will be removed from the video conference
		case R.string.str_video_manager_remove:
			Integer CCPUIKeyRemove = (Integer) view.getTag();
			String voipRemove = getVideoVoIPByCCPUIKey(CCPUIKeyRemove);
			if (!TextUtils.isEmpty(voipRemove)) {
				showConnectionProgress(getString(R.string.str_dialog_message_default));

				ECDevice.getECMeetingManager()
						.removeMemberFromMultMeetingByType(CCPConfig.App_ID,
								ECMultMeetingType.ECMeetingVideo,
								mVideoConferenceId, voipRemove,
								new OnRemoveMemberFromMeetingListener() {

									@Override
									public void onRemoveMemberFromMeeting(
											ECError reason, String member) {
										closeConnectionProgress();
										if (!reason.isError()) {

										}
									}
								});

			}
			break;
		case R.string.dialog_cancle_btn:

			break;
		}
	}

	/**
	 * @param CCPUIKey
	 * @param mulitVideoMember
	 */
	private void doHandlerMemberVideoFrameRequest(Integer CCPUIKey,
			MulitVideoMember mulitVideoMember) {
		if (mulitVideoMember != null) {
			if (mVideoConferenceId.length() > 8) {

				String id = formatConferenceId(mVideoConferenceId);
				if (!mulitVideoMember.isRequestVideoFrame()) {
					SurfaceView surfaceView = mVideoConUI
							.getSurfaceView(CCPUIKey.intValue());

					int result = ECDevice
							.getECMeetingManager()
							.requestMemberVideoInVideoMeeting(
									id,
									"1234",
									mulitVideoMember.getNumber(),
									surfaceView,
									mulitVideoMember.getIp(),
									mulitVideoMember.getPort(),
									new OnRequestMemberVideoInVideoMeetingListener() {

										@Override
										public void onRequestMemberVideoResultFailed(
												int reason,
												String conferenceId, String voip) {
											ECLog4Util.e(TAG, "reason="
													+ reason + ",conferenceId="
													+ conferenceId + ",voip="
													+ voip);

										}
									});
					ECLog4Util.e(TAG, "result=" + result);
					if (result == 0) {
						mulitVideoMember.setRequestVideoFrame(true);
					}

				} else {

					int num = ECDevice
							.getECMeetingManager()
							.cancelRequestMemberVideoInVideoMeeting(
									id,
									"1234",
									mulitVideoMember.getNumber(),
									new OnCancelRequestMemberVideoInVideoMeetingListener() {

										@Override
										public void onCancelRequestMemberVideoFalied(
												int reason,
												String conferenceId, String voip) {
											ECLog4Util.e(TAG, "reason="
													+ reason + ",conferenceId="
													+ conferenceId + ",voip="
													+ voip);

										}
									});
					ECLog4Util.e(TAG, "num=" + num);
					if (num == 0) {
						mulitVideoMember.setRequestVideoFrame(false);
					}

				}
			}

		}
	}

	private String formatConferenceId(String id) {
		if (id.length() >= 30) {
			return id.substring(14, 30);
		} else if (id.length() >= 22) {
			return id.substring(14, 22);
		} else {
			return id;
		}
	}

	private void clearScreen(SurfaceView surfaceView) {
		Paint p = new Paint();
		// 清屏
		p.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
		Canvas lockCanvas = surfaceView.getHolder().lockCanvas();
		lockCanvas.drawPaint(p);
		p.setXfermode(new PorterDuffXfermode(Mode.SRC));
		surfaceView.getHolder().unlockCanvasAndPost(lockCanvas);
		surfaceView.invalidate();
	}

	/**
	 * 
	 * @param view
	 * @param mulitVideoMember
	 */
	private void doChangeVideoFrameSurfaceViewRequest(SurfaceView view,
			MulitVideoMember mulitVideoMember) {
		if (mulitVideoMember != null) {
			if (mVideoConferenceId.length() > 8) {
				String id = mVideoConferenceId.substring(
						mVideoConferenceId.length() - 8,
						mVideoConferenceId.length());
				if (mulitVideoMember.isRequestVideoFrame()) {
					SurfaceView surfaceView = view;

					surfaceView.getHolder().setFixedSize(
							mulitVideoMember.getWidth(),
							mulitVideoMember.getHeight());
					ECDevice.getECMeetingManager().resetVideoConfWindow(
							mulitVideoMember.getNumber(), surfaceView);
				}

			}

		}
	}

	/**
	 * 
	 * <p>
	 * Title: doVideoConferenceDisconnect
	 * </p>
	 * <p>
	 * Description: The end of processing video conference popu menu list
	 * </p>
	 * 
	 * @see CCPAlertDialog#CCPAlertDialog(android.content.Context, int, int[], int, int)
	 */
	private void doVideoConferenceDisconnect() {
		int videoTips = R.string.video_c_logout_warning_tip;
		int videoExit = R.string.video_c_logout;

		CCPAlertDialog ccpAlertDialog = new CCPAlertDialog(
				MultiVideoconference.this, videoTips, null, videoExit,
				R.string.dialog_cancle_btn);
		ccpAlertDialog.setOnItemClickListener(this);
		ccpAlertDialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
			}
		});
		ccpAlertDialog.create();
		ccpAlertDialog.show();
	}

	/**
	 * 
	 * <p>
	 * Title: setVideoUITextOperable
	 * </p>
	 * <p>
	 * Description: Unified setting for VideoUI text display, and set whether
	 * can click operation
	 * </p>
	 * 
	 * @param CCPMulitVideoUIKey
	 * @param who
	 * 
	 * @see com.speedtong.example.meeting.ui.videomeeting.CCPMulitVideoUI#setOnVideoUIItemClickListener(OnVideoUIItemClickListener
	 *      l)
	 */
	private void setVideoUITextOperable(Integer CCPMulitVideoUIKey,
			MulitVideoMember member) {

		mVideoConUI.setVideoMember(CCPMulitVideoUIKey, member);

	}

	/**
	 * 
	 * <p>
	 * Title: removeMemberFormVideoUI
	 * </p>
	 * <p>
	 * Description: remove the member of Video Conference form VideoUI
	 * </p>
	 * 
	 * @param who
	 */
	private void removeMemberFormVideoUI(String who) {
		Integer CCPMulitVideoUIKey = removeVideoUIMemberFormCache(who);
		removeMemberMulitCache(who);
		removeMemberFromVideoUI(CCPMulitVideoUIKey);

	}

	private void removeMemberMulitCache(String who) {
		MulitVideoMember removeMember = null;
		for (MulitVideoMember mulitVideoMember : mulitMembers) {
			if (mulitVideoMember != null
					&& mulitVideoMember.getNumber().equals(who)) {
				removeMember = mulitVideoMember;
				break;
			}
		}

		if (removeMember != null) {
			mulitMembers.remove(removeMember);
		}
	}

	private void removeMemberFromVideoUI(Integer CCPMulitVideoUIKey) {
		if (CCPMulitVideoUIKey != null) {
			// The layout of ID release where the rooms,
			putCCPMulitVideoUIKey(CCPMulitVideoUIKey);
			// mVideoConUI.setImageViewDrawable(CCPMulitVideoUIKey, null);
			setVideoUITextOperable(CCPMulitVideoUIKey, null);
		}
	}

	private void initMembersOnVideoUI(List<MulitVideoMember> members) {
		synchronized (mVideoMemberUI) {

			for (final MulitVideoMember member : members) {
				Integer CCPMulitVideoUIKey = null;
				if (CCPConfig.VoIP_ID.equals(member.getNumber())) {
					CCPMulitVideoUIKey = CCPMulitVideoUI.LAYOUT_KEY_SUB_SURFACEVIEW;
				} else {

					CCPMulitVideoUIKey = getCCPMulitVideoUIKey();
				}

				if (CCPMulitVideoUIKey == null) {
					break;
				}

				putVideoUIMemberCache(member.getNumber(), CCPMulitVideoUIKey);
				if (!CCPConfig.VoIP_ID.equals(member.getNumber())) {
					doHandlerMemberVideoFrameRequest(CCPMulitVideoUIKey, member);

				}

				final int key = CCPMulitVideoUIKey.intValue();
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						setVideoUITextOperable(key, member);
					}
				});
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

			if (isVideoChatting) {
				doVideoConferenceDisconnect();
			}
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void handleDialogOkEvent(int requestKey) {
		super.handleDialogOkEvent(requestKey);

		if (DIALOG_SHOW_KEY_DISSMISS_VIDEO == requestKey) {
			exitOrDismissVideoConference(true);
		} else if (DIALOG_SHOW_KEY_REMOVE_VIDEO == requestKey) {
			// The removed member of the video conference is self.
			exitOrDismissVideoConference(false);
		}
	}

	// -----------------------------------------------------SDK Callback
	// ---------------------------

	@Override
	protected void handleVideoConferenceDismiss(int reason, String conferenceId) {
		super.handleVideoConferenceDismiss(reason, conferenceId);

		closeConnectionProgress();

		// 111805【视频群聊】房间未找到
		if (reason != 0 && (reason != 111805)) {
			Toast.makeText(MultiVideoconference.this,
					getString(R.string.toast_video_dismiss_result, reason),
					Toast.LENGTH_SHORT).show();
			return;
		}

		exitOrDismissVideoConference(false);
	}

	@Override
	protected void handleReceiveVideoConferenceMsg(ECVideoMeetingMsg VideoMsg) {
		super.handleReceiveVideoConferenceMsg(VideoMsg);

		synchronized (MultiVideoconference.class) {
			try {
				// if the Video Conference ID is empty .then The next step
				if (VideoMsg == null
						|| !mVideoConferenceId.equals(VideoMsg
								.getVideoMeetingNo())) {

					// not current Video Conference . then do nothing.
					return;
				}

				if (VideoMsg instanceof ECVideoMeetingJoinMsg) {

					ECVideoMeetingJoinMsg videoJoinMessage = (ECVideoMeetingJoinMsg) VideoMsg;
					String[] whos = videoJoinMessage.getWhos();

					for (String who : whos) {

						if (isVideoUIMemberExist(who)) {
							continue;
						}
						if (CCPConfig.VoIP_ID.equals(who)) {
							continue;
						}

						// has Somebody join
						Integer CCPMulitVideoUIKey = getCCPMulitVideoUIKey();
						if (CCPMulitVideoUIKey == null) {
							return;
						}

						MulitVideoMember member = new MulitVideoMember();
						member.setNumber(who);
						member.setIp(videoJoinMessage.getIp());
						member.setPort(videoJoinMessage.getPort());
						member.setPublishStatus(videoJoinMessage
								.getPublishStatus());
						putVideoUIMemberCache(member, CCPMulitVideoUIKey);

						// queryVideoMembersPorprtait(CCPMulitVideoUIKey, who);

						// If there is no image, then show the account
						// information also.
						setVideoUITextOperable(CCPMulitVideoUIKey, member);
						updateVideoNoticeTipsUI(getString(
								R.string.str_video_conference_join, who));
						doHandlerMemberVideoFrameRequest(CCPMulitVideoUIKey,
								member);
					}

					// some one exit Video Conference..
				} else if (VideoMsg instanceof ECVideoMeetingExitMsg) {
					ECVideoMeetingExitMsg videoExitMessage = (ECVideoMeetingExitMsg) VideoMsg;
					String[] whos = videoExitMessage.getWhos();
					for (String who : whos) {

						// remove the member of Video Conference form VideoUI
						removeMemberFormVideoUI(who);
						updateVideoNoticeTipsUI(getString(
								R.string.str_video_conference_exit, who));

					}

				} else if (VideoMsg instanceof ECVideoMeetingDeleteMsg) {

					// If it is the creator of the video conference, then don't
					// send broadcast
					// when exit of the video conference.
					// Because the creators of Video Conference in create a
					// video conference
					// don't add to video conference list. And the video
					// conference creator exit that is
					// dismiss video conference, without notice to refresh the
					// list
					if (isVideoConCreate) {

						return;
					}

					// The creator to dismiss of video conference (PUSH to all
					// staff room)
					ECVideoMeetingDeleteMsg videoConferenceDismissMsg = (ECVideoMeetingDeleteMsg) VideoMsg;
					if (videoConferenceDismissMsg.getVideoMeetingNo().equals(
							mVideoConferenceId)) {
						showAlertTipsDialog(
								DIALOG_SHOW_KEY_DISSMISS_VIDEO,
								getString(R.string.dialog_title_be_dissmiss_video_conference),
								getString(R.string.dialog_message_be_dissmiss_video_conference),
								getString(R.string.dialog_btn), null);
					}

				} else if (VideoMsg instanceof ECVideoMeetingRemoveMemberMsg) {
					// The creator to remove a member(PUSH to all staff room)
					ECVideoMeetingRemoveMemberMsg vCRemoveMemberMsg = (ECVideoMeetingRemoveMemberMsg) VideoMsg;

					if (CCPConfig.VoIP_ID.equals(vCRemoveMemberMsg.getWho())) {
						// The removed member of the video conference is self.
						showAlertTipsDialog(
								DIALOG_SHOW_KEY_REMOVE_VIDEO,
								getString(R.string.str_system_message_remove_v_title),
								getString(R.string.str_system_message_remove_v_message),
								getString(R.string.dialog_btn), null);
					} else {
						removeMemberFormVideoUI(vCRemoveMemberMsg.getWho());
					}

				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void handleSwitchRealScreenToVoip(int reason) {
		super.handleSwitchRealScreenToVoip(reason);
		closeConnectionProgress();
		if (reason != 0) {
			Toast.makeText(getApplicationContext(),
					getString(R.string.str_video_switch_failed, reason),
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void handleVideoConferenceRemoveMember(int reason, String member) {
		super.handleVideoConferenceRemoveMember(reason, member);
		closeConnectionProgress();
		if (reason != 0) {
			Toast.makeText(
					getApplicationContext(),
					getString(R.string.str_video_remove_failed, member, reason),
					Toast.LENGTH_SHORT).show();
			return;
		}
	}

	@Override
	protected void handleNotifyMessage(Message msg) {
		super.handleNotifyMessage(msg);

		int what = msg.what;
		if (what == WHAT_ON_VIDEO_NOTIFY_TIPS) {
			mVideoTips.setText(getString(R.string.video_tips_joining,
					mVideoConferenceId));
			TransitionDrawable transition = (TransitionDrawable) mVideoTips
					.getBackground();
			transition.reverseTransition(ANIMATION_DURATION_RESET);

		}

	}

	/**
	 * 
	 * <p>
	 * Title: MultiVideoconference.java
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 * <p>
	 * Copyright: Copyright (c) 2007
	 * </p>
	 * <p>
	 * Company: http://www.cloopen.com
	 * </p>
	 * 
	 * @author zhanjichun
	 * @date 2013-11-7
	 * @version 1.0
	 */
	class CCPFilenameFilter implements FilenameFilter {

		String fileName = null;

		public CCPFilenameFilter(String fileNoExtensionNoDot) {
			fileName = fileNoExtensionNoDot;
		}

		@Override
		public boolean accept(File dir, String filename) {

			return filename.startsWith(fileName);
		}

	}

	@Override
	protected int getLayoutId() {
		return R.layout.video_conference;
	}

	@Override
	protected void handleVideoRatioChanged(String voip, int width, int height) {

		if (TextUtils.isEmpty(voip)) {
			return;
		}
		final Integer key = queryVideoUIMemberFormCache(voip);
		MulitVideoMember mulitVideoMember = getMulitVideoMember(voip);
		if (mulitVideoMember != null) {
			mulitVideoMember.setWidth(width);
			mulitVideoMember.setHeight(height);
		}
		final int _width = width;
		final int _height = height;
		getHandler().post(new Runnable() {

			@Override
			public void run() {
				if (mVideoConUI != null) {
					SurfaceView surfaceView = mVideoConUI.getSurfaceView(key
							.intValue());
					if (surfaceView != null) {
						surfaceView.getHolder().setFixedSize(_width, _height);
						LayoutParams layoutParams = surfaceView
								.getLayoutParams();
						// layoutParams.width=_width;
						// layoutParams.height=_height;
						// surfaceView.setLayoutParams(layoutParams);
					}
				}
			}
		});
	}

}

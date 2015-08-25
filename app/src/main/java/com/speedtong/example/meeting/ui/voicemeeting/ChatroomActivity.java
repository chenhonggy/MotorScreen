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
package com.speedtong.example.meeting.ui.voicemeeting;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.screen.main.R;
import com.speedtong.example.meeting.ECApplication;
import com.speedtong.example.meeting.common.utils.ToastUtil;
import com.speedtong.example.meeting.core.CCPConfig;
import com.speedtong.example.meeting.core.CCPIntentUtils;
import com.speedtong.example.meeting.core.SDKCoreHelper;
import com.speedtong.example.meeting.ui.manager.CCPAppManager;
import com.speedtong.sdk.ECDevice;
import com.speedtong.sdk.ECError;
import com.speedtong.sdk.ECMultMeetingType;
import com.speedtong.sdk.ECVoipSetManager;
import com.speedtong.sdk.core.ECCreateMeetingParams;
import com.speedtong.sdk.core.ECGlobalConstants;
import com.speedtong.sdk.core.ECMeetingType;
import com.speedtong.sdk.core.interphone.ECInterphoneMeetingMember;
import com.speedtong.sdk.core.meeting.listener.OnCreateOrJoinMeetingListener;
import com.speedtong.sdk.core.meeting.listener.OnDeleteMeetingListener;
import com.speedtong.sdk.core.meeting.listener.OnQueryMeetingMembersListener;
import com.speedtong.sdk.core.meeting.voice.listener.OnInviteMembersJoinToVoiceMeetingListener;
import com.speedtong.sdk.core.model.NetworkStatistic;
import com.speedtong.sdk.core.videomeeting.ECVideoMeetingMember;
import com.speedtong.sdk.core.voicemeeting.ECVoiceMeeting;
import com.speedtong.sdk.core.voicemeeting.ECVoiceMeetingDeleteMsg;
import com.speedtong.sdk.core.voicemeeting.ECVoiceMeetingExitMsg;
import com.speedtong.sdk.core.voicemeeting.ECVoiceMeetingJoinMsg;
import com.speedtong.sdk.core.voicemeeting.ECVoiceMeetingMember;
import com.speedtong.sdk.core.voicemeeting.ECVoiceMeetingMemberForbidOpt;
import com.speedtong.sdk.core.voicemeeting.ECVoiceMeetingMsg;
import com.speedtong.sdk.core.voicemeeting.ECVoiceMeetingMsg.ForbidOptions;
import com.speedtong.sdk.core.voicemeeting.ECVoiceMeetingRemoveMemberMsg;
import com.speedtong.sdk.debug.ECLog4Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Voice ChatRoom 1、Display in the group chat member list. 2、Manual control is
 * mute (whether the person can hear the sound).
 * 
 */
public class ChatroomActivity extends VoiceMeetingBaseActivity implements
		View.OnClickListener {

	protected static final String TAG = "ChatroomActivity";

	private static int[] STATUS_ICON = new int[] { R.drawable.animation_box01,
			R.drawable.animation_box02, R.drawable.animation_box03,
			R.drawable.animation_box04 };

	private ArrayList<ECVoiceMeetingMember> mCRoomMembers;

	private LinearLayout mChatMmber;
	private TextView mNoticeTips;
	private TextView mPersonCount;
	private TextView mMikeToast;
	private TextView mTrafficStats;
	private ImageButton mChatRoomMike;

	private LinearLayout mCRoomStatusL;
	private LinearLayout mCRoomStatusR;
	private LinearLayout mCRoomCenterIcn;

	private XQuickActionBar xQuickActionBar;

	// Whether the mute
	private boolean isMikeEnable = true;
	// Join Success
	private boolean isJion = false;

	private boolean isChatroomCreate = false;
	private boolean isChatroomDismiss = false;
	private boolean isValidate = false;

	private String mCurrentRoomNum;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// add by able for soft keyboard show

		setContentView(R.layout.layout_chatroom_activity);

		initResourceRefs();

		SDKCoreHelper.getInstance().setHandler(mChatRoomHandler);

		registerReceiver(new String[] { CCPIntentUtils.INTENT_CHAT_ROOM_DISMISS });
		initialize(savedInstanceState);

		CCPConfig.VoIP_ID = CCPAppManager.getClientUser().getUserId();

	}

	boolean isSpeakerphoneOn = true;

	@Override
	protected void onResume() {
		if (SDKCoreHelper.getInstance() != null && mChatRoomHandler != null) {
			SDKCoreHelper.getInstance().setHandler(mChatRoomHandler);
			lockScreen();
		} else {
			finish();
		}
		queryMembers();
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		releaseLockScreen();

	}

	private void initialize(Bundle savedInstanceState) {
		Intent intent = getIntent();
		String roomName = null;
		String roomPwd = null;
		String creator = null;
		boolean isAutoClose = true;
		boolean isAutoJoin = true;
		int autoDelete = 1;
		int voiceMode = 1;

		if (intent.hasExtra(ChatroomName.CHATROOM_NAME)) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				roomName = extras.getString(ChatroomName.CHATROOM_NAME);
				if (TextUtils.isEmpty(roomName)) {
					finish();
				} else {
					creator = extras.getString(ChatroomName.CHATROOM_CREATOR);
					if (CCPConfig.VoIP_ID.equals(creator)) {
						isChatroomCreate = true;
						handleTitleDisplay(
								getString(R.string.btn_title_back),
								roomName,
								getString(R.string.app_title_right_button_pull_down));
					} else {
						handleTitleDisplay(
								getString(R.string.btn_title_back),
								roomName,
								getString(R.string.app_title_right_button_pull_down_1));
					}

					if (extras.containsKey(ChatroomName.CHATROOM_PWD)) {
						roomPwd = extras.getString(ChatroomName.CHATROOM_PWD);
						if (!TextUtils.isEmpty(roomPwd))
							isValidate = true;
					}
					if (extras.containsKey(ChatroomName.IS_AUTO_CLOSE)) {
						isAutoClose = extras
								.getBoolean(ChatroomName.IS_AUTO_CLOSE);
					}
					if (extras.containsKey(ChatroomName.IS_AUTO_JOIN)) {
						isAutoJoin = extras
								.getBoolean(ChatroomName.IS_AUTO_JOIN);
					}
					if (intent.hasExtra(ChatroomName.AUTO_DELETE)) {
						autoDelete = extras.getInt(ChatroomName.AUTO_DELETE);
					}
					if (intent.hasExtra(ChatroomName.VOICE_MOD)) {
						voiceMode = extras.getInt(ChatroomName.VOICE_MOD);
					}
				}

			}
		}

		boolean create = true;
		if (intent.hasExtra(ECGlobalConstants.CONFNO)) {
			// To invite voice group chat
			Bundle extras = intent.getExtras();
			if (extras != null) {
				mCurrentRoomNum = extras.getString(ECGlobalConstants.CONFNO);
				create = TextUtils.isEmpty(mCurrentRoomNum) ? true : false;

			}

		}
		mNoticeTips.setText(R.string.top_tips_connecting_wait);

		// Launched a group chat room request, waiting for SDK to return.
		if (getIntent() != null && getIntent().getBooleanExtra("flag", false)) {

			queryMembers();
			return;
		}
		if (create) {

			ECCreateMeetingParams.Builder builder = new ECCreateMeetingParams.Builder();
			builder.setAppId(CCPConfig.App_ID).setMeetingName(roomName)
					.setSquare(8).setVoiceMod(voiceMode)
					.setAutoDelete(autoDelete == 1 ? true : false)
					.setAutoJoin(isAutoJoin).setKeywords("")
					.setMeetingPwd(roomPwd).setAutoClose(isAutoClose);
			ECCreateMeetingParams params = builder.build();
			showConnectionProgress(getString(R.string.str_dialog_message_default));
			ECDevice.getECMeetingManager().createMultMeetingByType(params,
					ECMultMeetingType.ECMeetingVoice,
					new OnCreateOrJoinMeetingListener() {

						@Override
						public void onCreateOrJoinMeeting(ECError reason,
								String meetingNo) {
							
							ECLog4Util.e(TAG, reason.toString() + ","
									+ meetingNo);

							count++;
							if (count == 2) {
								closeConnectionProgress();
								doRefreshUI(reason, meetingNo);
							}
						}
					});

		} else {
			showConnectionProgress(getString(R.string.str_dialog_message_default));
			ECDevice.getECMeetingManager().joinMeetingByType(mCurrentRoomNum,
					ECMeetingType.ECMeetingVoice, roomPwd,
					new OnCreateOrJoinMeetingListener() {

						@Override
						public void onCreateOrJoinMeeting(ECError reason,
								String meetingNo) {
							closeConnectionProgress();
							ECLog4Util.e(TAG, reason.toString() + ","
									+ meetingNo);

							doRefreshUI(reason, meetingNo);

						}
					});

		}

	}

	private int count = 0;

	void doRefreshUI(ECError error, String meetingNo) {

		if (!error.isError()) {
			ECDevice.getECVoipSetManager().enableLoudSpeaker(true);

			mCurrentRoomNum = meetingNo;
			mNoticeTips.setText(CCPConfig.VoIP_ID
					+ getString(R.string.str_join_chatroom_success));

			isJion = true;
			new Thread(mikeAnimRunnable).start();
			new Thread(CenterAnimRunnable).start();

			if (isChatroomCreate) {
				ECVoiceMeeting chatRoomInfo = new ECVoiceMeeting();
				chatRoomInfo.setCreator(CCPConfig.VoIP_ID);
				chatRoomInfo.setJoined("1");
				chatRoomInfo.setRoomNo(mCurrentRoomNum);
				chatRoomInfo.setRoomName(getActivityTitle());
				chatRoomInfo.setSquare("8");
				chatRoomInfo.setValidate(isValidate ? "1" : "0");
				Intent intent = new Intent(
						CCPIntentUtils.INTENT_RECIVE_CHAT_ROOM);
				intent.putExtra("ChatRoomInfo", chatRoomInfo);
				sendBroadcast(intent);
			}

			isMikeEnable = ECDevice.getECVoipSetManager().getMuteStatus();
			// synchronized (mChatRoomHandler) {
			// this.notifyAll();
			// }
		} else {

			isJion = false;
			ECDevice.getECMeetingManager().exitMeeting();
			ECApplication.getInstance().showToast(error.errorMsg);
			finish();
		}

	}

	private void initResourceRefs() {
		// mChatMmber = (ListView) findViewById(R.id.chatroom_member_list);
		mChatMmber = (LinearLayout) findViewById(R.id.member_list);
		mNoticeTips = (TextView) findViewById(R.id.chatroom_notice_tips);
		mChatRoomMike = (ImageButton) findViewById(R.id.chatroom_mike);
		mPersonCount = (TextView) findViewById(R.id.count_tv);
		mMikeToast = (TextView) findViewById(R.id.mute_tips);

		mChatRoomMike.setOnClickListener(this);

		mCRoomStatusL = (LinearLayout) findViewById(R.id.chatroom_l_status);
		mCRoomStatusR = (LinearLayout) findViewById(R.id.chatroom_r_status);
		mCRoomCenterIcn = (LinearLayout) findViewById(R.id.chatroom_center_status);

		mTrafficStats = (TextView) findViewById(R.id.trafficStats);

		initBottomStatus(0);
	}

	@Override
	protected void handleTitleAction(int direction) {
		if (direction == TITLE_RIGHT_ACTION) {

			if (xQuickActionBar == null) {
				xQuickActionBar = new XQuickActionBar(
						findViewById(R.id.voice_right_btn));
				xQuickActionBar.setOnPopClickListener(popListener);
			}

			int switchId = -1;

			boolean speakerStatus = getvoipSetManager().getLoudSpeakerStatus();
			switchId = speakerStatus ? R.string.pull_mode_earpiece
					: R.string.pull_mode_speaker;

			int[] arrays = null;
			if (isChatroomCreate) {
				arrays = new int[4];
				arrays[0] = R.string.pull_invited_phone_member;
				arrays[1] = switchId;
				arrays[2] = R.string.pull_manager_member;
				arrays[3] = R.string.pull_dissolution_room;

			} else {
				arrays = new int[] { switchId };
			}

			xQuickActionBar.setArrays(arrays);
			xQuickActionBar.show();
		} else {
			// super.handleTitleAction(direction);
			if (getIntent() != null
					&& getIntent().getBooleanExtra("flag", false)) {
				finish();
				return;
			}
			if (isChatroomCreate) {
				showAlertTipsDialog(DIALOG_REQUEST_KEY_EXIT_CHATROOM,
						getString(R.string.dialog_chatroom_title),
						getString(R.string.dialog_chatroom_exit_message)
						// , getString(R.string.dialog_chatroom_message)
						, getString(R.string.dialog_cancle_btn)
						// , getString(R.string.dialog_p_dissolution)
						, getString(R.string.dialog_n_exit));
			} else {
				exitOrDismissChatroom(false);
			}
		}
	}

	public void exitOrDismissChatroom(boolean exit) {
		if (!exit) {

			ECDevice.getECMeetingManager().exitMeeting();
			finish();
		} else {
			showConnectionProgress(getString(R.string.str_dialog_message_default));

			ECDevice.getECMeetingManager().deleteMultMeetingByType(
					CCPConfig.App_ID, ECMultMeetingType.ECMeetingVoice,
					mCurrentRoomNum, new OnDeleteMeetingListener() {

						@Override
						public void onDismissMeeting(ECError reason,
								String meetingNo) {

							closeConnectionProgress();
							ECLog4Util.e(TAG, reason.toString() + ","
									+ meetingNo);

							if (!reason.isError()) {

								finish();
							} else {

								ToastUtil.showMessage(reason.errorMsg);
							}

						}
					});
			isChatroomDismiss = true;

		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

			handleTitleAction(TITLE_LEFT_ACTION);
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.chatroom_mike:

			try {
				mChatRoomMike.setEnabled(false);
				getvoipSetManager().setMute(!isMikeEnable);
				isMikeEnable = getvoipSetManager().getMuteStatus();
				if (isMikeEnable) {
					initBottomStatus(0);
				} else {
					synchronized (mChatRoomHandler) {
						// new Thread(mikeAnimRunnable).start();
						mChatRoomHandler.notify();

					}

				}
				mMikeToast
						.setText(isMikeEnable ? R.string.str_chatroom_mike_disenable
								: R.string.str_chatroom_mike_enable);
				mChatRoomMike.setEnabled(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();

		isJion = false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		getvoipSetManager().enableLoudSpeaker(false);

		if (getvoipSetManager().getMuteStatus()) {
			getvoipSetManager().setMute(false);
			SDKCoreHelper.getInstance().setHandler(null);
		}

		if (mChatRoomHandler != null) {
			mChatRoomHandler = null;
		}

		if (mCRoomMembers != null) {
			mCRoomMembers.clear();
			mCRoomMembers = null;
		}

		isMikeEnable = true;
		isJion = false;
		mCurrentRoomNum = null;
	}

	private void queryMembers() {
		ECDevice.getECMeetingManager().queryMeetingMembersByType(
				mCurrentRoomNum, ECMeetingType.ECMeetingVoice,
				new OnQueryMeetingMembersListener() {

					@Override
					public void onQueryVoiceMeetingMembers(ECError reason,
							List<ECVoiceMeetingMember> members) {

						if (!isError(reason)) {
							if (members != null) {
								if (mCRoomMembers == null) {
									mCRoomMembers = new ArrayList<ECVoiceMeetingMember>();
								}
								mCRoomMembers.clear();
								for (ECVoiceMeetingMember i : members) {
									if (i.getNumber().equals(CCPConfig.VoIP_ID)) {
										mCRoomMembers.add(0, i);
									} else {
										mCRoomMembers.add(i);
									}
								}
								mPersonCount.setText(members.size() + "");
								initChatRoomListView(mCRoomMembers);
							}
						}

					}

					@Override
					public void onQueryVideoMeetingMembers(ECError reason,
							List<ECVideoMeetingMember> members) {

					}

					@Override
					public void onQueryInterphoneMeetingMembers(ECError reason,
							List<ECInterphoneMeetingMember> members) {

					}
				});
	}

	private android.os.Handler mChatRoomHandler = new android.os.Handler() {

		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle b = null;
			int reason = 0;
			ECVoiceMeetingMsg crmsg = null;
			ArrayList<ECVoiceMeetingMember> members = null;
			if (msg.obj instanceof Bundle) {
				b = (Bundle) msg.obj;
				reason = b.getInt(ECGlobalConstants.REASON);
				if (b.getString(ECGlobalConstants.CONFNO) != null)
					mCurrentRoomNum = b.getString(ECGlobalConstants.CONFNO);

				if (b.getParcelable(ECGlobalConstants.CHATROOM_MSG) != null)
					crmsg = (ECVoiceMeetingMsg) b
							.getParcelable(ECGlobalConstants.CHATROOM_MSG);

				if (b.getParcelable(ECGlobalConstants.CHATROOM_MEMBERS) != null)
					members = (ArrayList<ECVoiceMeetingMember>) b
							.getParcelable(ECGlobalConstants.CHATROOM_MEMBERS);
			}

			switch (msg.what) {
			case SDKCoreHelper.WHAT_ON_CHATROOM:

				break;
			case SDKCoreHelper.WHAT_ON_CHATROOM_INVITE:

				
			case SDKCoreHelper.WHAT_ON_CHATROOM_MEMBERS:

				break;
			case SDKCoreHelper.WHAT_ON_CHATROOM_SIP_MESSAGE:
				try {
					if (crmsg != null) {
						if (crmsg instanceof ECVoiceMeetingJoinMsg) {
							ECVoiceMeetingJoinMsg crj = (ECVoiceMeetingJoinMsg) crmsg;
							if (mCRoomMembers != null) {
								StringBuilder builder = new StringBuilder();
								String[] whos = crj.getWhos();
								for (int i = 0; i < whos.length; i++) {
									builder.append(whos[i]).append(",");

									//
									boolean isHas = false;
									for (ECVoiceMeetingMember cm : mCRoomMembers) {
										if (cm.getNumber().equals(whos[i])) {
											isHas = true;
											break;
										}
									}
									if (!isHas) {
										mCRoomMembers
												.add(new ECVoiceMeetingMember(
														whos[i], "0"));
									}
								}

								String joinV = builder.toString();
								if (joinV != null && joinV.length() > 0) {
									joinV = joinV.substring(0,
											joinV.length() - 1);
								}
								mNoticeTips.setText(getString(
										R.string.str_chatroom_join, joinV));
								initChatRoomListView(mCRoomMembers);
							}
						} else if (crmsg instanceof ECVoiceMeetingExitMsg) {
							ECVoiceMeetingExitMsg cre = (ECVoiceMeetingExitMsg) crmsg;
							if (mCRoomMembers != null) {
								StringBuilder builder = new StringBuilder();
								ArrayList<ECVoiceMeetingMember> mExitMember = new ArrayList<ECVoiceMeetingMember>();
								String[] whos = cre.getWhos();
								for (int i = 0; i < whos.length; i++) {
									builder.append(whos[i]).append(",");
									for (ECVoiceMeetingMember eMember : mCRoomMembers) {
										if (eMember.getNumber().equals(whos[i])) {
											mExitMember.add(eMember);
											break;
										}
									}
								}

								mCRoomMembers.removeAll(mExitMember);
								initChatRoomListView(mCRoomMembers);
								String joinV = builder.toString();
								if (joinV != null && joinV.length() > 0) {
									joinV = joinV.substring(0,
											joinV.length() - 1);
								}
								mNoticeTips.setText(getString(
										R.string.str_chatroom_exit, joinV));
							}
						} else if (crmsg instanceof ECVoiceMeetingDeleteMsg) {

							// If it is the creator of the chatroom, then don't
							// send broadcast
							// when exit of the chatroom.
							if (isChatroomCreate && isChatroomDismiss) {

								// do nothing .
								return;
							}

							ECVoiceMeetingDeleteMsg dismissMsg = (ECVoiceMeetingDeleteMsg) crmsg;
							if (dismissMsg.getVoiceMeetingNo().equals(
									mCurrentRoomNum)) {
								showAlertTipsDialog(
										DIALOG_SHOW_KEY_DISSMISS_CHATROOM,
										getString(R.string.dialog_title_be_dissmiss_chatroom),
										getString(R.string.dialog_message_be_dissmiss_chatroom),
										getString(R.string.dialog_btn), null);

								// Set the mute mode
								isMikeEnable = true;
							}
						} else if (crmsg instanceof ECVoiceMeetingRemoveMemberMsg) {
							ECVoiceMeetingRemoveMemberMsg crRemoveMemberMsg = (ECVoiceMeetingRemoveMemberMsg) crmsg;
							if (CCPConfig.VoIP_ID.equals(crRemoveMemberMsg
									.getWho())
									&& mCurrentRoomNum.equals(crRemoveMemberMsg
											.getVoiceMeetingNo())) {
								// if sel..
								showAlertTipsDialog(
										DIALOG_SHOW_KEY_REMOVE_CHATROOM,
										getString(R.string.dialog_title_be_kick_chatroom),
										getString(R.string.dialog_message_be_kick_chatroom),
										getString(R.string.dialog_btn), null);
							} else {
								mNoticeTips.setText(getString(
										R.string.str_chatroom_kick,
										crRemoveMemberMsg.getWho()));

								queryMembers();

							}
						} else if (crmsg instanceof ECVoiceMeetingMemberForbidOpt) {
							ECVoiceMeetingMemberForbidOpt forbidOpt = (ECVoiceMeetingMemberForbidOpt) crmsg;
							ForbidOptions options = forbidOpt.getOptions();
							if (options == null) {
								return;
							}
							if (options.inSpeak == ForbidOptions.OPTION_SPEAK_FREE) {

								mNoticeTips.setText(getString(
										R.string.str_chatroom_can_speak,
										forbidOpt.getMember()));
							} else {
								mNoticeTips.setText(getString(
										R.string.str_chatroom_forbid_speak,
										forbidOpt.getMember()));
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;

			case SDKCoreHelper.WHAT_ON_MIKE_ANIM:

				int abs = randomNum(6);
				initBottomStatus(abs);
				break;

			case SDKCoreHelper.WHAT_ON_CNETER_ANIM:
				initCenterStatus(15);

				NetworkStatistic trafficStats = ECDevice.getECVoipSetManager()
						.getNetworkStatistic(mCurrentRoomNum);
				if (trafficStats != null) {
					mTrafficStats.setText("统计时长：" + trafficStats.getDuration()
							+ " 秒, " + "流量发送" + trafficStats.getTxBytes()
							/ 1024 + "KB," + "流量接收" + trafficStats.getRxBytes()
							/ 1024 + "KB," + "wifi发送"
							+ trafficStats.getSendWifi() / 1024 + "KB,"
							+ "wifi接收" + trafficStats.getRecvWifi() / 1024
							+ "KB"

					);
				}

				break;
			case SDKCoreHelper.WHAT_ON_CHATROOMING:

				mNoticeTips.setText(R.string.top_tips_chatroom_ing);
				break;
			default:
				break;
			}
		}

	};

	private int randomNum(int num) {
		Random rand = new Random();
		return Math.abs(rand.nextInt() % num);
	}

	private void initChatRoomListView(
			ArrayList<ECVoiceMeetingMember> mCRoomMembers) {
		if (mCRoomMembers == null) {
			return;
		}
		mChatMmber.removeAllViews();
		View view = null;
		ArrayList<String> list = new ArrayList<String>();
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < mCRoomMembers.size(); i++) {
			if (isOddCorrect(i)) {
				builder.append(mCRoomMembers.get(i).getNumber());
				// if(!list.contains(builder.toString()))
				list.add(builder.toString());
				builder = new StringBuilder();
			} else {
				if (i == mCRoomMembers.size() - 1) {
					builder.append(mCRoomMembers.get(i).getNumber());
					// if(!list.contains(builder.toString()))
					list.add(builder.toString());
					builder = new StringBuilder();
				} else {
					builder.append(mCRoomMembers.get(i).getNumber())
							.append(",");
				}
			}
		}
		for (String members : list) {
			view = getLayoutInflater().inflate(R.layout.list_chatroom_item,
					null);
			ImageView cRoomStatusL = (ImageView) view
					.findViewById(R.id.chatroom_join_statu_l);
			TextView cRoomNameL = (TextView) view
					.findViewById(R.id.chatroom_name_l);
			ImageView cRoomStatusR = (ImageView) view
					.findViewById(R.id.chatroom_join_statu_r);
			TextView cRoomNameR = (TextView) view
					.findViewById(R.id.chatroom_name_r);
			String[] split = members.split(",");
			if (split.length > 0 && !TextUtils.isEmpty(split[0])) {
				if (CCPConfig.VoIP_ID.equals(split[0])) {
					cRoomStatusL.setImageResource(R.drawable.touxiang);
				} else {
					cRoomStatusL.setImageResource(R.drawable.status_uncreateor);
				}
				cRoomNameL.setText(split[0]);
			}
			if (split.length > 1 && !TextUtils.isEmpty(split[1])) {
				if (CCPConfig.VoIP_ID.equals(split[1])) {
					cRoomStatusR.setImageResource(R.drawable.touxiang);
				} else {
					cRoomStatusR.setImageResource(R.drawable.status_uncreateor);
				}
				cRoomNameR.setText(split[1]);

			}
			mChatMmber.addView(view);
		}

		mPersonCount.setText(mCRoomMembers.size() + "");

	}

	private boolean isError(ECError error) {

		return error.isError();
	}

	boolean isOddCorrect(int i) {
		return i % 2 != 0;

	}

	@Override
	protected void handleEditDialogOkEvent(int requestKey, String editText,
			boolean checked) {
		super.handleEditDialogOkEvent(requestKey, editText, checked);

		if (DIALOG_SHOW_KEY_INVITE == requestKey) {
			String mPhoneNumber = editText;
			if (mPhoneNumber != null && !TextUtils.isEmpty(mPhoneNumber)) {
				

				ECDevice.getECMeetingManager().inviteMembersJoinToVoiceMeeting(CCPConfig.App_ID, mCurrentRoomNum, new String[]{mPhoneNumber}, new OnInviteMembersJoinToVoiceMeetingListener() {
					
					@Override
					public void onInviteMembersJoinToChatroom(ECError reason, String roomNo) {

						ECLog4Util.e(TAG, reason.toString()+"--"+roomNo);
						
						if (!reason.isError()) {
							Toast.makeText(ChatroomActivity.this,
									R.string.toast_invite_join_room_success,
									Toast.LENGTH_SHORT).show();
						} else {
							ToastUtil.showMessage(reason.errorMsg);
						}

						Message obtainMessage = mChatRoomHandler
								.obtainMessage(SDKCoreHelper.WHAT_ON_CHATROOMING);
						mChatRoomHandler.sendMessageDelayed(obtainMessage, 2000);

						
						
						
						
					}
				});
				
				
				mNoticeTips.setText(getString(R.string.str_invite_join_room,
						CCPConfig.VoIP_ID, mPhoneNumber));
			}
		}
	}

	@Override
	protected void handleDialogOkEvent(int requestKey) {
		super.handleDialogOkEvent(requestKey);

		isMikeEnable = true;
		if (DIALOG_SHOW_KEY_DISSMISS_CHATROOM == requestKey
				|| DIALOG_REQUEST_KEY_EXIT_CHATROOM == requestKey) {
			if (isChatroomCreate) {
				// getDevice().dismissChatroom(CCPConfig.App_ID,
				// mCurrentRoomNum);
				exitOrDismissChatroom(true);
			} else {

				// Here is the receipt dissolution news, not so directly off the
				// Page Creator
				finish();
				Intent intent = new Intent(
						CCPIntentUtils.INTENT_CHAT_ROOM_DISMISS);
				intent.putExtra("roomNo", mCurrentRoomNum);
				sendBroadcast(intent);
			}
		} else if (DIALOG_SHOW_KEY_REMOVE_CHATROOM == requestKey) {
			// Here is the receipt dissolution news, not so directly off the
			// Page Creator
			finish();
		}

	}

	@Override
	protected void handleDialogCancelEvent(int requestKey) {
		super.handleDialogCancelEvent(requestKey);
		if (requestKey == DIALOG_REQUEST_KEY_EXIT_CHATROOM) {
			isChatroomDismiss = true;
			exitOrDismissChatroom(false);
		}
	}

	@Override
	protected void onReceiveBroadcast(Intent intent) {
		super.onReceiveBroadcast(intent);
		closeConnectionProgress();
		if (intent.getAction().equals(CCPIntentUtils.INTENT_CHAT_ROOM_DISMISS)) {
			if (intent.hasExtra("roomNo")) {
				String roomNo = intent.getStringExtra("roomNo");
				if (!TextUtils.isEmpty(roomNo)
						&& roomNo.equals(mCurrentRoomNum)) {
					finish();
				}
			}
		}
	}

	synchronized void initBottomStatus(int num) {// 4
		mCRoomStatusL.removeAllViews();
		mCRoomStatusR.removeAllViews();
		for (int i = 0; i < 6; i++) {
			ImageView imageViewl_i = new ImageView(this);
			ImageView imageViewR_i = new ImageView(this);
			if (i > (6 - num - 1)) {// 1
				imageViewl_i.setImageResource(R.drawable.chatroom_speaker);
			} else {
				imageViewl_i.setImageResource(R.drawable.chatroom_unspeaker);

			}
			if (i >= num) {// 4
				imageViewR_i.setImageResource(R.drawable.chatroom_unspeaker);
			} else {
				imageViewR_i.setImageResource(R.drawable.chatroom_speaker);
			}
			mCRoomStatusL
					.addView(imageViewl_i, new LinearLayout.LayoutParams(
							LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT, 1.0f));
			mCRoomStatusR
					.addView(imageViewR_i, new LinearLayout.LayoutParams(
							LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT, 1.0f));
		}
	}

	synchronized void initCenterStatus(int num) {// 4
		mCRoomCenterIcn.removeAllViews();
		for (int i = 0; i < num; i++) {
			ImageView imageView = new ImageView(this);
			if (STATUS_ICON != null) {
				imageView.setImageResource(STATUS_ICON[randomNum(4)]);
				mCRoomCenterIcn.addView(imageView,
						new LinearLayout.LayoutParams(
								LayoutParams.WRAP_CONTENT,
								LayoutParams.WRAP_CONTENT, 1.0f));
			}
		}
	}

	Runnable mikeAnimRunnable = new Runnable() {

		@Override
		public void run() {
			while (isJion) {
				if (isMikeEnable) {
					synchronized (mChatRoomHandler) {
						try {
							mChatRoomHandler.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				if (mChatRoomHandler != null) {
					mChatRoomHandler
							.sendEmptyMessage(SDKCoreHelper.WHAT_ON_MIKE_ANIM);
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};

	Runnable CenterAnimRunnable = new Runnable() {

		@Override
		public void run() {
			while (isJion) {
				if (mChatRoomHandler != null) {
					mChatRoomHandler
							.sendEmptyMessage(SDKCoreHelper.WHAT_ON_CNETER_ANIM);
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};

	private static final int REQUEST_CODE_KICK_MEMBER = 0x1;
	private XQuickActionBar.OnPopClickListener popListener = new XQuickActionBar.OnPopClickListener() {

		@Override
		public void onPopClick(int index) {
			SDKCoreHelper.getInstance().setHandler(mChatRoomHandler);
			switch (index) {
			case R.string.pull_invited_phone_member:

				showEditTextDialog(DIALOG_SHOW_KEY_INVITE,
						getString(R.string.dialog_title_invite),
						getString(R.string.dialog_title_summary));
				break;
			case R.string.pull_manager_member:
				Intent intent = new Intent(ChatroomActivity.this,
						ChatroomMemberManagerActivity.class);
				intent.putExtra(ECGlobalConstants.CONFNO, mCurrentRoomNum);
				startActivityForResult(intent, REQUEST_CODE_KICK_MEMBER);
				break;
			case R.string.pull_dissolution_room:

				showAlertTipsDialog(DIALOG_SHOW_KEY_DISSMISS_CHATROOM,
						getString(R.string.dialog_title_dissmiss_chatroom),
						getString(R.string.dialog_message_dissmiss_chatroom),
						getString(R.string.dailog_button_dissmiss_chatroom),
						getString(R.string.dialog_cancle_btn));
				break;
			case R.string.pull_mode_earpiece:
			case R.string.pull_mode_speaker:

				boolean enable = getvoipSetManager().getLoudSpeakerStatus();
				getvoipSetManager().enableLoudSpeaker(!enable);

				break;

			default:

				break;
			}
			xQuickActionBar.dismissBar();
		}
	};

	private ECVoipSetManager getvoipSetManager() {

		return ECDevice.getECVoipSetManager();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// If there's no data (because the user didn't select a number and
		// just hit BACK, for example), there's nothing to do.
		if (requestCode != REQUEST_CODE_KICK_MEMBER) {
			if (data == null) {
				return;
			}
		} else if (resultCode != RESULT_OK) {

			return;
		}

		switch (requestCode) {
		case REQUEST_CODE_KICK_MEMBER:
			if (data.hasExtra("isKicked")) {
				Bundle extras = data.getExtras();
				if (extras != null) {
					boolean isKicked = extras.getBoolean("isKicked");

					if (isKicked) {
						SDKCoreHelper.getInstance()
								.setHandler(mChatRoomHandler);

						queryMembers();
					}
				}
			}

			break;
		default:
			break;
		}
	}

}

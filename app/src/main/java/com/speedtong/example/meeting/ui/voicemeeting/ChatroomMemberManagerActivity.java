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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.screen.main.R;
import com.speedtong.example.meeting.ECApplication;
import com.speedtong.example.meeting.core.CCPConfig;
import com.speedtong.example.meeting.core.SDKCoreHelper;
import com.speedtong.sdk.ECDevice;
import com.speedtong.sdk.ECError;
import com.speedtong.sdk.ECMultMeetingType;
import com.speedtong.sdk.core.ECGlobalConstants;
import com.speedtong.sdk.core.ECMeetingType;
import com.speedtong.sdk.core.interphone.ECInterphoneMeetingMember;
import com.speedtong.sdk.core.meeting.listener.OnQueryMeetingMembersListener;
import com.speedtong.sdk.core.meeting.listener.OnRemoveMemberFromMeetingListener;
import com.speedtong.sdk.core.videomeeting.ECVideoMeetingMember;
import com.speedtong.sdk.core.voicemeeting.ECVoiceMeetingMember;

import java.util.ArrayList;
import java.util.List;

public class ChatroomMemberManagerActivity extends VoiceMeetingBaseActivity {

	private ListView mKickMemList;
	private String roomNo;

	private KickAdapter mKickAdapter;

	private boolean isKicked = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.chatroom_member_manager);

		handleTitleDisplay(getString(R.string.btn_title_back),
				getString(R.string.app_title_manager_member), null);

		initResourceRefs();

		SDKCoreHelper.getInstance().setHandler(mChatRoomHandler);
		initialize(savedInstanceState);

	}

	private void doRemoveMember(ECVoiceMeetingMember member) {
		ECDevice.getECMeetingManager().removeMemberFromMultMeetingByType(
				CCPConfig.App_ID, ECMultMeetingType.ECMeetingVoice, roomNo,
				member.getNumber(), new OnRemoveMemberFromMeetingListener() {

					@Override
					public void onRemoveMemberFromMeeting(ECError reason,
							String member) {

						closeConnectionProgress();
						if (!reason.isError()) {

							try {

								if (mKickAdapter != null) {
									ECVoiceMeetingMember item = mKickAdapter
											.getItem(kickPosition);
									if (item != null
											&& member.equals(item.getNumber())) {
										mKickAdapter.remove(item);
										isKicked = true;
									}
								}

							} catch (Exception e) {
								e.printStackTrace();
							}

						} else {
							ECApplication
									.getInstance()
									.showToast(
											getString(
													R.string.str_member_kicked_out_failed,
													reason + ""));
							return;

						}
					}
				});

	}

	private void initResourceRefs() {

		mKickMemList = (ListView) findViewById(R.id.kicked_member_list);
	}

	private void initialize(Bundle savedInstanceState) {
		Intent intent = getIntent();
		if (intent.hasExtra(ECGlobalConstants.CONFNO)) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				roomNo = extras.getString(ECGlobalConstants.CONFNO);
			}

		}

		if (TextUtils.isEmpty(roomNo)) {
			finish();
		}

		showConnectionProgress(getString(R.string.str_dialog_message_default));

		ECDevice.getECMeetingManager().queryMeetingMembersByType(roomNo,
				ECMeetingType.ECMeetingVoice,
				new OnQueryMeetingMembersListener() {

					@Override
					public void onQueryVoiceMeetingMembers(ECError reason,
							List<ECVoiceMeetingMember> members) {

						closeConnectionProgress();
						if (!reason.isError()) {

							if (members != null) {
								mKickAdapter = new KickAdapter(
										getApplicationContext(), members);
								mKickMemList.setAdapter(mKickAdapter);
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

	@Override
	protected void handleTitleAction(int direction) {
		if (direction == TITLE_LEFT_ACTION) {
			setResultOk();
		} else {
			super.handleTitleAction(direction);
		}
	}

	int kickPosition;

	class KickAdapter extends ArrayAdapter<ECVoiceMeetingMember> {

		public KickAdapter(Context context, List<ECVoiceMeetingMember> objects) {
			super(context, 0, objects);

		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {

			KickedHolder holder;
			if (convertView == null || convertView.getTag() == null) {
				convertView = getLayoutInflater().inflate(
						R.layout.list_kicked_member_item, null);
				holder = new KickedHolder();

				holder.name = (TextView) convertView.findViewById(R.id.name);
				holder.kickButton = (Button) convertView
						.findViewById(R.id.kicked_btn);
			} else {
				holder = (KickedHolder) convertView.getTag();
			}

			// do ..
			final ECVoiceMeetingMember chatroomMember = getItem(position);
			if (chatroomMember != null) {
				holder.name.setText(chatroomMember.getNumber());

				if (CCPConfig.VoIP_ID.equals(chatroomMember.getNumber())) {
					holder.kickButton.setVisibility(View.GONE);
				} else {
					holder.kickButton.setVisibility(View.VISIBLE);
				}

				holder.kickButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (mKickAdapter != null) {
							showConnectionProgress(getString(R.string.str_dialog_message_default));

							kickPosition = position;

							doRemoveMember(chatroomMember);
						}
					}
				});

			}
			return convertView;
		}

		class KickedHolder {
			TextView name;
			Button kickButton;
		}

	}

	private android.os.Handler mChatRoomHandler = new android.os.Handler() {

		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle b = null;
			int reason = -1;
			ArrayList<ECVoiceMeetingMember> members = null;
			if (msg.obj instanceof Bundle) {
				b = (Bundle) msg.obj;
				reason = b.getInt(ECGlobalConstants.REASON);
				if (b.getParcelable(ECGlobalConstants.CHATROOM_MEMBERS) != null)
					members = (ArrayList<ECVoiceMeetingMember>) b
							.getParcelable(ECGlobalConstants.CHATROOM_MEMBERS);
			}

			switch (msg.what) {
			case SDKCoreHelper.WHAT_ON_CHATROOM_MEMBERS:

				break;

			case SDKCoreHelper.WHAT_ON_CHATROOM_KICKMEMBER:

				break;

			}
		}

	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

			setResultOk();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	private void setResultOk() {
		Intent intent = new Intent(ChatroomMemberManagerActivity.this,
				ChatroomActivity.class);
		intent.putExtra("isKicked", isKicked);
		setResult(RESULT_OK, intent);
		this.finish();
	}
}

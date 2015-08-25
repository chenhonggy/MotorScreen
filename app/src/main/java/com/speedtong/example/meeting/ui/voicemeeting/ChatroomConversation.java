package com.speedtong.example.meeting.ui.voicemeeting;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.screen.main.R;
import com.speedtong.example.meeting.core.CCPConfig;
import com.speedtong.example.meeting.core.CCPIntentUtils;
import com.speedtong.example.meeting.core.SDKCoreHelper;
import com.speedtong.example.meeting.ui.CCPBaseActivity;
import com.speedtong.example.meeting.ui.manager.CCPAppManager;
import com.speedtong.sdk.ECDevice;
import com.speedtong.sdk.ECError;
import com.speedtong.sdk.ECMultMeetingType;
import com.speedtong.sdk.core.ECGlobalConstants;
import com.speedtong.sdk.core.meeting.listener.OnDeleteMeetingListener;
import com.speedtong.sdk.core.meeting.listener.OnListAllMeetingsListener;
import com.speedtong.sdk.core.videomeeting.ECVideoMeeting;
import com.speedtong.sdk.core.voicemeeting.ECVoiceMeeting;
import com.speedtong.sdk.core.voicemeeting.ECVoiceMeetingMsg;
import com.speedtong.sdk.debug.ECLog4Util;

import java.util.ArrayList;
import java.util.List;

public class ChatroomConversation extends CCPBaseActivity implements
		View.OnClickListener, OnItemClickListener,
		CCPAlertDialog.OnPopuItemClickListener {

	private LinearLayout mChatRoomEmpty;
	private ListView mChatRoomLv;
	private ArrayList<ECVoiceMeeting> chatRoomList;
	private ChatRoomConvAdapter mRoomAdapter;

	protected static final String TAG = "ChatroomConversation";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		handleTitleDisplay(getString(R.string.btn_title_back),
				getString(R.string.app_title_chatroom_conv),
				getString(R.string.str_button_create_chatroom));

		initResourceRefs();

		registerReceiver(new String[] { CCPIntentUtils.INTENT_RECIVE_CHAT_ROOM,
				CCPIntentUtils.INTENT_CHAT_ROOM_DISMISS });
	}

	// 获取所有的语音会议
	public void listAllVoiceMeetings() {

		showConnectionProgress(getString(R.string.str_dialog_message_default));
		ECDevice.getECMeetingManager().listAllMultMeetingsByType(
				CCPConfig.App_ID, null, ECMultMeetingType.ECMeetingVoice,
				new OnListAllMeetingsListener() {

					@Override
					public void onListAllVoiceMeetings(ECError reason,
							List<ECVoiceMeeting> list) {

						closeConnectionProgress();
						if (list != null) {
							System.out.println(list.size());
							chatRoomList = (ArrayList<ECVoiceMeeting>) list;
							initListView();
						}
					}

					@Override
					public void onListAllVideoMeetings(ECError reason,
							List<ECVideoMeeting> list) {

					}
				});
	}

	private void initResourceRefs() {
		mChatRoomLv = (ListView) findViewById(R.id.chatroom_list);
		mChatRoomLv.setOnItemClickListener(this);
		findViewById(R.id.begin_create_chatroom).setOnClickListener(this);
		mChatRoomEmpty = (LinearLayout) findViewById(R.id.chatroom_empty);
		initListView();
	}

	private void initListView() {
		if (chatRoomList != null && !chatRoomList.isEmpty()) {
			mRoomAdapter = new ChatRoomConvAdapter(this, chatRoomList);
			mChatRoomLv.setAdapter(mRoomAdapter);
			mChatRoomLv.setVisibility(View.VISIBLE);
			mChatRoomEmpty.setVisibility(View.GONE);
		} else {
			mChatRoomEmpty.setVisibility(View.VISIBLE);
			mChatRoomLv.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		SDKCoreHelper.getInstance().setHandler(mChatRoomHandler);
		listAllVoiceMeetings();
	}

	private android.os.Handler mChatRoomHandler = new android.os.Handler() {
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			Bundle b = null;
			int reason = 0;
			// 获取通话ID
			if (msg.obj instanceof Bundle) {
				b = (Bundle) msg.obj;
				reason = b.getInt(ECGlobalConstants.REASON);
			}

			switch (msg.what) {
			case SDKCoreHelper.WHAT_ON_CHATROOM_SIP_MESSAGE:
				if (b.getParcelable(ECGlobalConstants.CHATROOM_MSG) != null) {
					ECVoiceMeetingMsg crmsg = (ECVoiceMeetingMsg) b
							.getParcelable(ECGlobalConstants.CHATROOM_MSG);
					if (crmsg != null && crmsg instanceof ECVoiceMeetingMsg) {
						listAllVoiceMeetings();
					}
				}
				break;
			// case SDKCoreHelper.WHAT_ON_CHATROOM_LIST:
			// if(reason == 0 ) {
			// chatRoomList = (ArrayList<ECVoiceMeeting>)
			// b.getSerializable(ECGlobalConstants.CHATROOM_LIST);
			// initListView();
			// } else {
			// Toast.makeText(getApplicationContext(),
			// getString(R.string.toast_get_chatroom_list_failed, reason , 0),
			// Toast.LENGTH_SHORT).show();
			// }
			// break;

			default:
				break;
			}
		};
	};

	@Override
	protected int getLayoutId() {
		return R.layout.layout_chatroom_conversation_activity;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.begin_create_chatroom:
			// 创建语音房间
			handleTitleAction(TITLE_RIGHT_ACTION);
			break;
		default:
			break;
		}
	}

	@Override
	protected void handleTitleAction(int direction) {
		if (direction == TITLE_RIGHT_ACTION) {
			// 创建语音房间
			startActivity(new Intent(ChatroomConversation.this,
					ChatroomName.class));
			// 翻页动画效果
			overridePendingTransition(R.anim.video_push_up_in,
					R.anim.push_empty_out);
		} else {
			super.handleTitleAction(direction);
		}
	}

	ECVoiceMeeting mVoiceMeeting = null;

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (mRoomAdapter != null && mRoomAdapter.getItem(position) != null) {

			ECVoiceMeeting voiceMeeting = mRoomAdapter.getItem(position);
			if (TextUtils.isEmpty(voiceMeeting.getRoomNo())) {
				Toast.makeText(this,
						"加入语音群聊失败,房间号:" + voiceMeeting.getRoomNo(),
						Toast.LENGTH_SHORT).show();
				return;
			}

			if (voiceMeeting.getCreator().equals(
					CCPAppManager.getClientUser().getUserId())) {
				// 语音会议 加入会议 解散会议 成员管理 取消（弹出选择框）
				mVoiceMeeting = voiceMeeting;
				doChatRoomControl();
				return;
			}

			// 验证密码是否有效
			if ("1".equals(voiceMeeting.getValidate())) {
				mVoiceMeeting = voiceMeeting;
				showEditTextDialog(DIALOG_SHOW_KEY_INVITE,
						InputType.TYPE_CLASS_TEXT, 1,
						getString(R.string.dialog_title_auth),
						getString(R.string.dialog_message_chatroom_auth_reason));
				return;
			}
			mVoiceMeeting = voiceMeeting;
			doChatroomAction(voiceMeeting, null);
		}
	}

	private void doChatroomAction(ECVoiceMeeting voiceMeeting, String roomPwd) {
		Intent intent = new Intent(ChatroomConversation.this,
				ChatroomActivity.class);
		// 房间号 房间名称 房间密码
		intent.putExtra(ECGlobalConstants.CONFNO, voiceMeeting.getRoomNo());
		intent.putExtra(ChatroomName.CHATROOM_CREATOR,
				voiceMeeting.getCreator());
		if (TextUtils.isEmpty(voiceMeeting.getRoomName())) {
			if (TextUtils.isEmpty(voiceMeeting.getCreator())) {
				return;
			}
			intent.putExtra(
					ChatroomName.CHATROOM_NAME,
					getString(
							R.string.app_title_default,
							voiceMeeting.getCreator().substring(
									voiceMeeting.getCreator().length() - 3,
									voiceMeeting.getCreator().length())));
		} else {
			intent.putExtra(ChatroomName.CHATROOM_NAME,
					voiceMeeting.getRoomName());
		}

		if (!TextUtils.isEmpty(roomPwd)) {
			intent.putExtra(ChatroomName.CHATROOM_PWD, roomPwd);
		}
		startActivity(intent);
	}

	// 弹出选择框：加入语音会议 解散会议 管理会议
	private void doChatRoomControl() {

		int[] ccpAlertResArray = null;
		int title = R.string.chatroom_control_tip;
		ccpAlertResArray = new int[] { R.string.chatroom_c_join,
				R.string.chatroom_c_dismiss };
		CCPAlertDialog ccpAlertDialog = new CCPAlertDialog(
				ChatroomConversation.this, title, ccpAlertResArray, 0,
				R.string.dialog_cancle_btn);

		ccpAlertDialog.setOnItemClickListener(this);
		ccpAlertDialog.create();
		ccpAlertDialog.show();
	}

	class ChatRoomConvAdapter extends ArrayAdapter<ECVoiceMeeting> {

		LayoutInflater mInflater;

		public ChatRoomConvAdapter(Context context,
				ArrayList<ECVoiceMeeting> objects) {
			super(context, 0, objects);
			mInflater = getLayoutInflater();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ChatRoomHolder holder;
			if (convertView == null || convertView.getTag() == null) {
				convertView = mInflater.inflate(R.layout.list_item_chatroom,
						null);
				holder = new ChatRoomHolder();
				convertView.setTag(holder);

				holder.chatRoomName = (TextView) convertView
						.findViewById(R.id.chatroom_name);
				holder.chatRoomTips = (TextView) convertView
						.findViewById(R.id.chatroom_tips);
				holder.mLock = (ImageView) convertView.findViewById(R.id.lock);
			} else {
				holder = (ChatRoomHolder) convertView.getTag();
			}

			try {
				ECVoiceMeeting item = getItem(position);
				if (item != null) {
					holder.chatRoomName.setText(item.getRoomName());
					int resourceId;
					if ("8".equals(item.getJoined())) {
						resourceId = R.string.str_chatroom_list_join_full;
					} else {
						resourceId = R.string.str_chatroom_list_join_unfull;
					}
					holder.chatRoomTips.setText(getString(resourceId,
							item.getJoined(), item.getCreator()));

					if (!TextUtils.isEmpty(item.getValidate())
							&& "1".equals(item.getValidate())) {
						holder.mLock.setVisibility(View.VISIBLE);
					} else {
						holder.mLock.setVisibility(View.GONE);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return convertView;
		}

		class ChatRoomHolder {
			TextView chatRoomName;
			TextView chatRoomTips;
			ImageView mLock;
		}
	}

	@Override
	public void onItemClick(ListView parent, View view, int position,
			int resourceId) {
		switch (resourceId) {
		case R.string.chatroom_c_join:
			if ("1".equals(mVoiceMeeting.getValidate())) {
				showEditTextDialog(DIALOG_SHOW_KEY_INVITE,
						InputType.TYPE_CLASS_TEXT, 1,
						getString(R.string.dialog_title_auth),
						getString(R.string.dialog_message_chatroom_auth_reason));
				return;
			}
			doChatroomAction(mVoiceMeeting, null);
			break;
		case R.string.chatroom_c_dismiss:
			showConnectionProgress(getString(R.string.str_dialog_message_default));
			ECDevice.getECMeetingManager().deleteMultMeetingByType(
					CCPConfig.App_ID, ECMultMeetingType.ECMeetingVoice,
					mVoiceMeeting.getRoomNo(), new OnDeleteMeetingListener() {

						@Override
						public void onDismissMeeting(ECError reason,
								String meetingNo) {
							closeConnectionProgress();
							ECLog4Util.d(TAG, reason.toString() + "---"
									+ meetingNo);
							listAllVoiceMeetings();
						}
					});

			break;
		case R.string.chatroom_c_managemenber:

		}
	}

	@Override
	protected void handleEditDialogOkEvent(int requestKey, String editText,
			boolean checked) {
		super.handleEditDialogOkEvent(requestKey, editText, checked);
		// 加入语音群聊时的密码验证
		if (requestKey == DIALOG_SHOW_KEY_INVITE) {
			if (mVoiceMeeting != null) {
				doChatroomAction(mVoiceMeeting, editText);
			}
		}
	}

	@Override
	protected void onReceiveBroadcast(Intent intent) {
		super.onReceiveBroadcast(intent);
		if (intent != null
				&& CCPIntentUtils.INTENT_RECIVE_CHAT_ROOM.equals(intent
						.getAction())) {
			if (intent.hasExtra("ChatRoomInfo")) {
				ECVoiceMeeting cRoomInfo = (ECVoiceMeeting) intent
						.getSerializableExtra("ChatRoomInfo");
				if (cRoomInfo != null) {
					if (chatRoomList == null) {
						chatRoomList = new ArrayList<ECVoiceMeeting>();
					}
					chatRoomList.add(cRoomInfo);
					initListView();
				}
			} else {
				listAllVoiceMeetings();
			}
		} else if (intent.getAction().equals(
				CCPIntentUtils.INTENT_CHAT_ROOM_DISMISS)) {
			if (intent.hasExtra("roomNo")) {
				String roomNo = intent.getStringExtra("roomNo");
				if (!TextUtils.isEmpty(roomNo) && chatRoomList != null) {
					for (ECVoiceMeeting chatroom : chatRoomList) {
						if (roomNo.equals(chatroom.getRoomNo())) {
							chatRoomList.remove(chatroom);
							break;
						}
					}
					initListView();
				}
			} else {
				listAllVoiceMeetings();
			}
		}
	}

	@Override
	public int getTitleLayout() {
		return -1;
	}

}

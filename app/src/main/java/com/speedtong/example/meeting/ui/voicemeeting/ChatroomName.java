package com.speedtong.example.meeting.ui.voicemeeting;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.screen.main.R;
import com.speedtong.example.meeting.common.utils.ToastUtil;
import com.speedtong.example.meeting.core.CCPConfig;
import com.speedtong.example.meeting.ui.CCPBaseActivity;
import com.speedtong.sdk.ECDevice;
import com.speedtong.sdk.ECError;
import com.speedtong.sdk.ECMultMeetingType;
import com.speedtong.sdk.core.ECCreateMeetingParams;
import com.speedtong.sdk.core.meeting.listener.OnCreateOrJoinMeetingListener;
import com.speedtong.sdk.debug.ECLog4Util;

;

public class ChatroomName extends CCPBaseActivity implements
		View.OnClickListener {
	public static final String IS_AUTO_JOIN = "isAutoJoin";
	public static final String VOICE_MOD = "voiceMod";
	public static final String AUTO_DELETE = "autoDelete";
	public static final String IS_AUTO_CLOSE = "isAutoClose";
	public static final String CHATROOM_NAME = "ChatroomName";
	public static final String CHATROOM_PWD = "ChatroomPwd";
	public static final String CHATROOM_CREATOR = "ChatroomCreator";

	private EditText mChatroomName;
	private EditText mChatroomPwd;
	private Button mSubmit;
	private CheckBox cb_autoclose;
	private CheckBox cb_autojoin;
	private int voiceMod = 1;
	private int autoDelete = 1;

	private ProgressDialog pDialog = null;

	protected static final String TAG = "CreateVoiceConference";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		handleTitleDisplay(getString(R.string.btn_title_back),
				getString(R.string.app_title_chatroom_create), null);
		initResourceRefs();
	}

	private void initResourceRefs() {

		// 房间类型 自动删除房间 不自动删除房间
		RadioGroup rg_autoDelete = (RadioGroup) findViewById(R.id.rg1);
		rg_autoDelete.check(R.id.rb1);
		rg_autoDelete.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup arg0, int id) {
				if (id == R.id.rb1) {
					autoDelete = 1;
				} else {
					autoDelete = 0;
				}
			}
		});

		// 声音模式
		RadioGroup rg_2 = (RadioGroup) findViewById(R.id.rg2);
		rg_2.check(R.id.rb3);
		rg_2.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup arg0, int id) {
				switch (id) {
				case R.id.rb3:
					// 全部提示音
					voiceMod = 1;
					break;
				case R.id.rb4:
					// 没有提示音 有背景音
					voiceMod = 0;
					break;
				case R.id.rb5:
					// 无提示音 无背景音
					voiceMod = 2;
					break;
				}
			}
		});

		// 创建人退出时是否自动解散
		View ll_cb = findViewById(R.id.ll_cb_autoclose);
		ll_cb.setOnClickListener(this);
		cb_autoclose = (CheckBox) findViewById(R.id.cb_autoclose);

		// 创建人是否自动加入
		View ll_cb2 = findViewById(R.id.ll_cb_autojoin);
		ll_cb2.setOnClickListener(this);
		cb_autojoin = (CheckBox) findViewById(R.id.cb_autojoin);

		mChatroomName = (EditText) findViewById(R.id.chatroom_name);
		mChatroomPwd = (EditText) findViewById(R.id.chatroom_pwd);
		mChatroomName.setSelection(mChatroomName.getText().length());
		mChatroomName.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (mChatroomName.getText().length() <= 0) {
					mSubmit.setEnabled(false);
				} else {
					mSubmit.setEnabled(true);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		mSubmit = (Button) findViewById(R.id.create_chatroom_submit);
		mSubmit.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ll_cb_autojoin:
			cb_autojoin.setChecked(!cb_autojoin.isChecked());
			break;
		case R.id.ll_cb_autoclose:
			cb_autoclose.setChecked(!cb_autoclose.isChecked());
			break;
		case R.id.create_chatroom_submit:
			if (TextUtils.isEmpty(mChatroomName.getText())) {
				Toast.makeText(getApplicationContext(), "请输入房间名称.",
						Toast.LENGTH_LONG).show();
				return;
			}
			HideSoftKeyboard();
			if (!cb_autojoin.isChecked()) {

				ECCreateMeetingParams.Builder builder = new ECCreateMeetingParams.Builder();

				ECCreateMeetingParams infoForCreate = builder
						.setAppId(CCPConfig.App_ID)
						.setAutoClose(cb_autoclose.isChecked())
						.setAutoDelete(autoDelete == 1 ? true : false)
						.setAutoJoin(false).setKeywords("")
						.setMeetingName(mChatroomName.getText().toString())
						.setMeetingPwd(mChatroomPwd.getText().toString())
						.setSquare(8).setVoiceMod(voiceMod).build();

				showConnectionProgress(getString(R.string.str_dialog_message_default));
				ECDevice.getECMeetingManager().createMultMeetingByType(
						infoForCreate, ECMultMeetingType.ECMeetingVoice,
						new OnCreateOrJoinMeetingListener() {

							@Override
							public void onCreateOrJoinMeeting(ECError reason,
									String meetingNo) {
								closeConnectionProgress();
								ECLog4Util.d(TAG, reason.toString() + "---"
										+ meetingNo);
								if (!reason.isError()) {

									finish();
								} else {
									ToastUtil.showMessage(reason.errorMsg);
								}

							}
						});
				return;
			}
			String pwd = mChatroomPwd.getText().toString();
			Intent intent = new Intent(ChatroomName.this,
					ChatroomActivity.class);
			intent.putExtra(CHATROOM_NAME, mChatroomName.getText().toString());
			if (!TextUtils.isEmpty(pwd)) {
				intent.putExtra(CHATROOM_PWD, pwd);
			}
			intent.putExtra(CHATROOM_CREATOR, CCPConfig.VoIP_ID);
			intent.putExtra(IS_AUTO_CLOSE, cb_autoclose.isChecked());
			intent.putExtra(IS_AUTO_JOIN, cb_autojoin.isChecked());
			intent.putExtra(ChatroomName.AUTO_DELETE, autoDelete);
			intent.putExtra(ChatroomName.VOICE_MOD, voiceMod);
			startActivity(intent);
			finish();
			break;

		default:
			break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		DisplaySoftKeyboard();
	}

	@Override
	protected void handleTitleAction(int direction) {
		if (direction == TITLE_LEFT_ACTION) {
			finishChatroom();
		} else {
			super.handleTitleAction(direction);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			HideSoftKeyboard();
			finishChatroom();
		}
		return super.onKeyDown(keyCode, event);
	}

	private void finishChatroom() {
		finish();
		overridePendingTransition(R.anim.push_empty_out,
				R.anim.video_push_down_out);
	}

	@Override
	protected int getLayoutId() {
		return R.layout.layout_set_chatroom_name_activity;
	}

	@Override
	public int getTitleLayout() {
		// TODO Auto-generated method stub
		return -1;
	}
}

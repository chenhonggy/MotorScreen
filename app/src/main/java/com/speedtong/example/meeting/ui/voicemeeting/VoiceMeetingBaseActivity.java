package com.speedtong.example.meeting.ui.voicemeeting;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;

import com.screen.main.R;
import com.speedtong.example.meeting.ui.CCPBaseActivity;

import java.util.HashMap;

public class VoiceMeetingBaseActivity extends CCPBaseActivity  {

	public static final String KEY_GROUP_ID = "groupId";
	public static final String KEY_MESSAGE_ID = "messageId";
	
	private final android.os.Handler handler = new android.os.Handler() {
		@SuppressWarnings("unchecked")
		public void handleMessage(Message msg) {
			
			if(msg.what == WHAT_SHOW_PROGRESS) {
				pDialog = new ProgressDialog(VoiceMeetingBaseActivity.this);
				pDialog.setTitle(R.string.str_dialog_title);
				pDialog.setMessage(getString(R.string.str_dialog_message_default));
				pDialog.setCanceledOnTouchOutside(false);
				String message = (String) msg.obj;
				if(!TextUtils.isEmpty(message))
					pDialog.setMessage(message);
				pDialog.show();
			} else if (msg.what == WHAT_CLOSE_PROGRESS) {
				if(pDialog != null && pDialog.isShowing()) {
					pDialog.dismiss();
					pDialog = null;
				}
			} else {
				// Normally we would do some work here.
				Bundle b = (Bundle) msg.obj;
				int what = msg.arg1;
//				ERequestState reason = (ERequestState) b.getSerializable("ERequestState");
				
				
				switch (what) {
				
					
//				case RestGroupManagerHelper.KEY_FORBIDS_PEAK:
//					
//					handleForbidSpeakForUser(reason);
//					
//					break;
				default:
//					doHandleExpertCallback(msg);
					break;
				}
			};
		}
	};
	
	/**
	 * @return the handler
	 */
	public android.os.Handler getHandler() {
		return handler;
	}
	
	
	public final Message getHandleMessage() {
		// For each start request, send a message to start a job and deliver the
		// start ID so we know which request we're stopping when we finish the
		// job
		Message msg = getHandler().obtainMessage();
		return msg;
	}

	public final void sendHandleMessage(Message msg) {
		getHandler().sendMessage(msg);
	}

	private ProgressDialog pDialog = null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//ThreadPoolManager.getInstance().setOnTaskDoingLinstener(this);
		
	}
	
	/* (non-Javadoc)
	 * @see com.voice.demo.ui.CCPBaseActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	/**
	 * when sub class can't show progress, then you can override it.
	 */
	public void showConnectionProgress(String messageContent) {
		Message message = Message.obtain();
		message.obj = messageContent;
		message.what = WHAT_SHOW_PROGRESS;
		handler.sendMessage(message);
	}

	public void closeConnectionProgress() {
		handler.sendEmptyMessage(WHAT_CLOSE_PROGRESS);
	}
	
	
	
	
	private HashMap<String, Object> parameters = new HashMap<String, Object>();

	
	public void setTaskParameters(String key , Object value) {
		parameters.put(key, value);
		
	}
	public Object getTaskParameters(String key) {
		return parameters.remove(key);
	}



	


	@Override
	protected int getLayoutId() {
		return -1;
	}
	
	
}

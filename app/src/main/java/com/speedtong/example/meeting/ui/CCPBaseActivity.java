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
package com.speedtong.example.meeting.ui;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.screen.main.R;
import com.speedtong.example.meeting.core.CCPIntentUtils;
import com.speedtong.example.meeting.core.SDKCoreHelper;
import com.speedtong.example.meeting.view.CCPLayoutListenerView;
import com.speedtong.example.meeting.view.CCPLayoutListenerView.OnCCPViewLayoutListener;
import com.speedtong.sdk.CloopenReason;

@SuppressWarnings("deprecation")
public abstract class CCPBaseActivity extends Activity {

	// The tone volume relative to other sounds in the stream
	private static final float TONE_RELATIVE_VOLUME = 100.0F;

	private static final int STREAM_TYPE = AudioManager.STREAM_MUSIC;
	// Stream type used to play the DTMF tones off call,
	// and mapped to the volume control keys

	// The length of tones in milliseconds
	public static final int TONE_LENGTH_MS = 200;

	public static final int TITLE_LEFT_ACTION = 1;
	public static final int TITLE_RIGHT_ACTION = 2;

	private Object mToneGeneratorLock = new Object();
	private KeyguardLock kl = null;
	private WakeLock mWakeLock;
	private WindowManager mWindowManager;
	private PowerManager mPowerManager;
	private AudioManager mAudioManager = null;

	private LayoutInflater mLayoutInflater;
	private LinearLayout mCCPRootView;

	private View mLayoutListenerView;
	private View mTitleLayout;
	private View mContentView = null;

	private Vibrator mVibrator; // Vibration (haptic feedback) for dialer key
								// presses.
	private ToneGenerator mToneGenerator;
	private Animation inAnimation, outAnimation; // The status bar notice the
													// animation of the show and
													// hide

	InternalReceiver internalReceiver = null;

	private Button titleLeftButton;
	private Button titleRightButton;
	private TextView titleTextView;

	protected TextView mVtalkNumber;

	private int mDuration = 6000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		registerReceiver(new String[] { CCPIntentUtils.INTENT_KICKEDOFF,
				Intent.ACTION_MEDIA_MOUNTED, Intent.ACTION_MEDIA_REMOVED });
		// if (!CCPApplication.getInstance().isChecknet()) {
		// CCPApplication.getInstance().setChecknet(true);
		// // CCPUtil.networkMonitor(this ,mHandler);
		// }

		mLayoutInflater = LayoutInflater.from(this);
		mLayoutListenerView = mLayoutInflater.inflate(R.layout.ccp_activity,
				null);
		mCCPRootView = (LinearLayout) mLayoutListenerView
				.findViewById(R.id.ccp_root_view);

		if (getTitleLayout() != -1) {
			mTitleLayout = mLayoutInflater.inflate(getTitleLayout(), null);
			mCCPRootView.addView(mTitleLayout,
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
		}

		if (getLayoutId() != -1) {
			mContentView = mLayoutInflater.inflate(getLayoutId(), null);
			mCCPRootView.addView(mContentView,
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.MATCH_PARENT);
			setContentView(mLayoutListenerView);
		}

		ScrollView scrollView = (ScrollView) findViewById(R.id.ccp_body_sv);
		if (scrollView != null) {
			scrollView.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					HideSoftKeyboard();
					return false;
				}
			});
		}

		CCPLayoutListenerView listenerView = (CCPLayoutListenerView) findViewById(R.id.ccp_base_view);
		if (listenerView != null) {
			listenerView.setOnLayoutListener(new OnCCPViewLayoutListener() {

				@Override
				public void onViewLayout() {
					// TODO Auto-generated method stub

				}
			});

			if (getWindow().getAttributes().softInputMode == WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE) {
				listenerView
						.setOnSizeChangedListener(new CCPLayoutListenerView.OnCCPViewSizeChangedListener() {

							@Override
							public void onSizeChanged(int w, int h, int oldw,
									int oldh) {

								if ((w != 0) && (h != 0) && (oldw != 0)
										&& (oldh != 0) && (w == oldw)) {
									if ((h <= oldh) || (h - oldh <= 50)) {

									} else if ((oldh > h) && (oldh - h > 50)) {

									}

								}
							}
						});
			}
		}

		initScreenStates();
	}

	protected final void handleTitleDisplay(CharSequence leftButton,
			CharSequence titleText, CharSequence rightButton) {
		handleTitleDisplay(leftButton, -1, titleText, rightButton, -1);
	}

	public void showConnectionProgress(String messageContent) {
		Message message = Message.obtain();
		message.obj = messageContent;
		message.what = WHAT_SHOW_PROGRESS;
		handler.sendMessage(message);
	}

	public void closeConnectionProgress() {
		handler.sendEmptyMessage(WHAT_CLOSE_PROGRESS);
	}

	/**
	 * 
	 * @param leftButton
	 * @param leftResId
	 * @param titleText
	 * @param rightButton
	 * @param rightResId
	 */
	protected final void handleTitleDisplay(CharSequence leftButton,
			int leftResId, CharSequence titleText, CharSequence rightButton,
			int rightResId) {
		titleLeftButton = (Button) findViewById(R.id.voice_btn_back);
		if (titleLeftButton != null) {
			titleLeftButton.setOnClickListener(titleButtonOnClickListener);
			if (leftResId != -1) {
				titleLeftButton.setBackgroundResource(leftResId);
				titleLeftButton.setVisibility(View.VISIBLE);
			}
			if (leftButton != null) {
				titleLeftButton.setText(leftButton);
				titleLeftButton.setVisibility(View.VISIBLE);
			}
		}

		titleTextView = (TextView) findViewById(R.id.voice_title);
		if (titleTextView != null) {
			if (titleText != null) {
				titleTextView.setText(titleText);
			}
		}

		titleRightButton = (Button) findViewById(R.id.voice_right_btn);
		if (titleRightButton != null) {
			titleRightButton.setOnClickListener(titleButtonOnClickListener);
			if (rightResId != -1) {
				titleRightButton.setBackgroundResource(rightResId);
				titleRightButton.setVisibility(View.VISIBLE);
			}
			if (rightButton != null) {
				titleRightButton.setText(rightButton);
				titleRightButton.setVisibility(View.VISIBLE);
			}
		}
	}

	public void showVoIPWifiWarnning() {
		showAlertTipsDialog(DIALOG_REQUEST_VOIP_NOT_WIFI_WARNNING,
				getString(R.string.voip_not_wifi_warnning_title),
				getString(R.string.voip_not_wifi_warnning_message),
				getString(R.string.dialog_btn),
				getString(R.string.dialog_cancle_btn));
	}

	/**
	 * 
	 * <p>
	 * Title: showVoIPWifiWarnning
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 */
	public void showNetworkNotAvailable() {
		showAlertTipsDialog(DIALOG_REQUEST_LOAD_FAILED_NETWORK,
				getString(R.string.voip_not_wifi_warnning_title),
				getString(R.string.load_failed_network),
				getString(R.string.dialog_btn), null);
	}

	protected void handleTitleAction(int direction) {
		finish();
	}

	protected void setActivityTitle(String title) {
		if (!TextUtils.isEmpty(title)) {
			titleTextView.setText(title);
		}
	}

	protected void setActivityTitle(int id) {
		setActivityTitle(getResources().getString(id));
	}

	public String getActivityTitle() {
		return titleTextView.getText().toString();
	}

	public Button getTitleRightButton() {
		return titleRightButton;
	}

	private final Button.OnClickListener titleButtonOnClickListener = new Button.OnClickListener() {

		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.voice_btn_back:
				handleTitleAction(TITLE_LEFT_ACTION);
				break;
			case R.id.voice_right_btn:
				handleTitleAction(TITLE_RIGHT_ACTION);
				break;
			}
		}

	};

	public static final String INTETN_ACTION_EXIT_CCP_DEMO = "exit_demo";

	private class InternalReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null || intent.getAction() == null) {
				return;
			}
			handleReceiver(context, intent);
		}
	}

	boolean mExternalStorageAvailable = false;
	boolean mExternalStorageWriteable = false;

	void updateExternalStorageState() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else {
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		// handleExternalStorageState(mExternalStorageAvailable,
		// mExternalStorageWriteable);
	}

	protected final void registerReceiver(String[] actionArray) {
		if (actionArray == null) {
			return;
		}
		IntentFilter intentfilter = new IntentFilter(
				INTETN_ACTION_EXIT_CCP_DEMO);
		intentfilter.addAction(CCPIntentUtils.INTENT_CONNECT_CCP);
		intentfilter.addAction(CCPIntentUtils.INTENT_DISCONNECT_CCP);
		for (String action : actionArray) {
			intentfilter.addAction(action);
		}

		if (internalReceiver == null) {
			internalReceiver = new InternalReceiver();
		}
		registerReceiver(internalReceiver, intentfilter);
	}

	protected void handleReceiver(Context context, Intent intent) {
		// 广播处理
	}

	public void showAlertTipsDialog(final int requestKey, String title,
			String message, final String posButton, String NegButton) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		if (!TextUtils.isEmpty(title))
			builder.setTitle(title);
		builder.setMessage(message);
		if (!TextUtils.isEmpty(posButton)) {
			builder.setPositiveButton(posButton, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

					dialog.dismiss();
					if (!posButton
							.equals(getString(R.string.dialog_cancle_btn)))
						handleDialogOkEvent(requestKey);

				}
			});
		}
		if (!TextUtils.isEmpty(NegButton)) {
			builder.setNegativeButton(NegButton, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					handleDialogCancelEvent(requestKey);
				}
			});
		}

		builder.create().show();
	}

	protected void handleDialogOkEvent(int requestKey) {

	}

	protected void handleDialogCancelEvent(int requestKey) {
	}

	protected void onReceiveBroadcast(Intent intent) {

	}

	protected void handleNotifyMessage(Message msg) {

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		synchronized (mToneGeneratorLock) {
			if (mToneGenerator != null) {
				mToneGenerator.release();
				mToneGenerator = null;
			}
		}
		mAudioManager = null;
		unregisterReceiver(internalReceiver);

	}

	public void HideSoftKeyboard() {
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (inputMethodManager != null) {
			View localView = getCurrentFocus();
			if (localView != null && localView.getWindowToken() != null) {
				IBinder windowToken = localView.getWindowToken();
				inputMethodManager.hideSoftInputFromWindow(windowToken, 0);
			}
		}
	}

	/**
	 * 
	 * <p>
	 * Title: HideSoftKeyboard
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 * 
	 * @param view
	 */
	public void HideSoftKeyboard(View view) {
		if (view == null) {
			return;
		}

		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (inputMethodManager != null) {
			IBinder localIBinder = view.getWindowToken();
			if (localIBinder != null)
				inputMethodManager.hideSoftInputFromWindow(localIBinder, 0);
		}
	}

	public void DisplaySoftKeyboard() {

		getBaseHandle().postDelayed(new Runnable() {

			@Override
			public void run() {
				// Display the soft keyboard
				InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				if (inputMethodManager != null) {
					View localView = getCurrentFocus();
					if (localView != null && localView.getWindowToken() != null) {
						inputMethodManager.toggleSoftInput(0,
								InputMethodManager.HIDE_NOT_ALWAYS);
					}
				}
			}
		}, 200);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// if (!checkeDeviceHelper()) {
		// launchCCP();
		// finish(); ??
		// }
	}

	//
	private CharSequence mMsgContent;
	private int msgContentCount = 200;
	public final TextWatcher mTextEditorWatcher = new TextWatcher() {
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			mMsgContent = s;
		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {

			if (SDKCoreHelper.hasFullSize(s.toString())) {
				msgContentCount = 100;
			}
		}

		public void afterTextChanged(Editable s) {
			if (mMsgContent.length() > msgContentCount) {

				s.delete(msgContentCount, mMsgContent.length());
			}
		}
	};

	public static final int DIALOG_SHOW_TIPS = 0x1;
	public static final int DIALOG_SHOW_KEY_INVITE = 0x2;
	public static final int DIALOG_SHOW_KEY_CHECKBOX = 0x3;
	public static final int DIALOG_SHOW_KEY_DISSMISS_CHATROOM = 0x4;
	public static final int DIALOG_SHOW_KEY_REMOVE_CHATROOM = 0x5;
	public static final int DIALOG_REQUEST_KEY_EXIT_CHATROOM = 0x6;
	public static final int DIALOG_REQUEST_VOIP_NOT_WIFI_WARNNING = 0x7;
	public static final int DIALOG_REQUEST_LOAD_FAILED_NETWORK = 0x8;

	public static final int WHAT_SHOW_PROGRESS = 0x101A;
	public static final int WHAT_CLOSE_PROGRESS = 0x101B;

	public static final int DIALOG_SHOW_KEY_DISSMISS_VIDEO = DIALOG_SHOW_KEY_DISSMISS_CHATROOM;
	public static final int DIALOG_SHOW_KEY_REMOVE_VIDEO = DIALOG_SHOW_KEY_REMOVE_CHATROOM;

	public void showEditTextDialog(final int requestKey, String title,
			String message) {
		showEditTextDialog(requestKey, InputType.TYPE_CLASS_PHONE, false, 1,
				title, message);

	}

	public void showEditTextDialog(final int requestKey, int inputType,
			int lines, String title, String message) {
		showEditTextDialog(requestKey, inputType, false, lines, title, message);

	}

	// display an input dialog .
	// Popup authentication input dialog box,
	// Need to enter the application to join the group of the reason
	public void showEditTextDialog(final int requestKey, int inputType,
			boolean ischeckBox, int lines, final String title, String message) {
		Dialog dialog = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		View view = getLayoutInflater().inflate(R.layout.dialog_edittext, null);
		TextView dialogText = (TextView) view.findViewById(R.id.id_dialog_tips);
		Button btn_choosecontact = (Button) view
				.findViewById(R.id.btn_choosecontact);

		if (title
				.equals(getResources().getString(R.string.dialog_title_invite))
				|| title.equals(getResources().getString(
						R.string.dialog_title_input_number_transfer))) {
			btn_choosecontact.setVisibility(View.VISIBLE);
			btn_choosecontact.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {

				}
			});
		} else {
			btn_choosecontact.setVisibility(View.INVISIBLE);
		}
		if (!TextUtils.isEmpty(message)) {
			dialogText.setVisibility(View.VISIBLE);
			dialogText.setText(message);
		} else {
			dialogText.setVisibility(View.GONE);
		}
		if (ischeckBox) {
			view.findViewById(R.id.checkbox_ly).setVisibility(View.VISIBLE);
		}
		final CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
		mInvitEt = (EditText) view.findViewById(R.id.invite_voip);

		if (title.equals(getResources().getString(
				R.string.dialog_title_input_number_transfer))) {
			mInvitEt.setInputType(InputType.TYPE_CLASS_PHONE);
		}

		if (lines == 1) {
			mInvitEt.setSingleLine();
		} else {
			mInvitEt.setMaxLines(lines);
		}
		mInvitEt.setInputType(inputType);
		mInvitEt.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, final boolean hasFocus) {

				if (hasFocus) {
					new Handler().postDelayed(new Runnable() {
						public void run() {
							InputMethodManager imm = (InputMethodManager) mInvitEt
									.getContext().getSystemService(
											Context.INPUT_METHOD_SERVICE);
							if (hasFocus) {
								imm.toggleSoftInput(0,
										InputMethodManager.HIDE_NOT_ALWAYS);
							} else {
								imm.hideSoftInputFromWindow(
										mInvitEt.getWindowToken(), 0);
							}
						}
					}, 300);
				}
			}
		});

		builder.setPositiveButton(R.string.dialog_btn,
				new OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						// if(title.equals(getResources().getString(R.string.dialog_title_input_number_transfer))){
						// //呼叫转移
						// if(checkeDeviceHelper()&&AudioVideoCallActivity.mCurrentCallId!=null)
						// {
						// int transferCall =
						// getDeviceHelper().transferCall(AudioVideoCallActivity.mCurrentCallId,mInvitEt.getText().toString());
						// if(transferCall==0){
						// Toast.makeText(getApplicationContext(), "转接成功",
						// 1).show();
						// if(mVtalkNumber!=null)
						// mVtalkNumber.setText(mInvitEt.getText());
						// finish();
						// }else
						// //呼叫转接失败
						// Toast.makeText(getApplicationContext(),
						// "转接失败 返回码"+transferCall, 1).show();
						// }
						// }else
						handleEditDialogOkEvent(requestKey, mInvitEt.getText()
								.toString(), checkBox.isChecked());
					}

				});

		builder.setNegativeButton(R.string.dialog_cancle_btn,
				new OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						handleDialogCancelEvent(requestKey);
					}

				});
		builder.setView(view);
		dialog = builder.create();
		dialog.show();
		dialog.setCanceledOnTouchOutside(false);
	}

	protected void handleEditDialogOkEvent(int requestKey, String editText,
			boolean checked) {

	}

	private void initScreenStates() {
		if (mAudioManager == null) {
			mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		}
		if (mWindowManager == null) {
			mWindowManager = ((WindowManager) getSystemService(Context.WINDOW_SERVICE));
			mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		}
		synchronized (mToneGeneratorLock) {
			if (mToneGenerator == null) {
				try {
					int streamVolume = mAudioManager
							.getStreamVolume(STREAM_TYPE);
					int streamMaxVolume = mAudioManager
							.getStreamMaxVolume(STREAM_TYPE);
					int volume = (int) (TONE_RELATIVE_VOLUME * (streamVolume / streamMaxVolume));
					mToneGenerator = new ToneGenerator(STREAM_TYPE, volume);

				} catch (RuntimeException e) {

					mToneGenerator = null;
				}
			}
		}
		mWakeLock = mPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK
				| PowerManager.ACQUIRE_CAUSES_WAKEUP, SDKCoreHelper.TAG);
	}

	// The initial screen lock screen and bright screen parameters
	// Set the screen lock screen to the closed
	public void lockScreen() {
		KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);

		// Get a keyboard lock manager object
		if (km.inKeyguardRestrictedInputMode()) {
			kl = km.newKeyguardLock(SDKCoreHelper.TAG);
			// Parameter is used by Tag LogCat.
			kl.disableKeyguard();// Unlock.
		}

		mWakeLock.acquire();
	}

	// Release the lock screen and the screen brightness manager,
	// reply to the system default state
	public void releaseLockScreen() {
		if (kl != null) {
			kl.reenableKeyguard();
		}

		if (mWakeLock != null) {
			try {
				mWakeLock.release();
			} catch (Exception e) {
				System.out.println("mWakeLock may already release");
			}
		}
	}

	/**
	 * Triggers haptic feedback Can also be based on the system settings to
	 * enable touch feedback
	 */
	public synchronized void vibrate(long milliseconds) {
		if (mVibrator == null) {
			mVibrator = (Vibrator) /* new Vibrator(); */getSystemService(Context.VIBRATOR_SERVICE);
		}
		mVibrator.vibrate(milliseconds);
	}

	// ------------------------------------------------------------------------------
	// Set the record button touch feedback events
	/**
	 * Plays the specified tone for TONE_LENGTH_MS milliseconds.
	 * 
	 * The tone is played locally, using the audio stream for phone calls. Tones
	 * are played only if the "Audible touch tones" user preference is checked,
	 * and are NOT played if the device is in silent mode.
	 * 
	 * @param tone
	 *            a tone code from {@link android.media.ToneGenerator}
	 */
	public void playTone(int tone, int durationMs) {

		// Also do nothing if the phone is in silent mode.
		// We need to re-check the ringer mode for *every* )
		// call, rather than keeping a local flag that's updated in
		// onResume(), since it's possible to toggle silent mode without
		// leaving the current activity (via the ENDCALL-longpress menu.)
		int ringerMode = mAudioManager.getRingerMode();
		if ((ringerMode == AudioManager.RINGER_MODE_SILENT)
				|| (ringerMode == AudioManager.RINGER_MODE_VIBRATE)) {
			return;
		}

		synchronized (mToneGeneratorLock) {
			if (mToneGenerator == null) {
				return;
			}

			// Start the new tone (will stop any playing tone)
			mToneGenerator.startTone(tone, durationMs);
		}
	}

	public void stopTone() {
		if (mToneGenerator != null)
			mToneGenerator.stopTone();
	}

	// ---------------------------------------------------------------
	// On the top of the screen to increase a toast display status notification
	/**
	 * default add to root view top
	 */
	public void addNotificatoinToView(CharSequence text) {
		LayoutParams layoutParams = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT,
				Gravity.TOP);
		// int margin = Math.round(50 *
		// getResources().getDisplayMetrics().densityDpi / 160.0F);
		// layoutParams.topMargin = margin;
		addNotificatoinToView(text, layoutParams);
	}

	/**
	 * Constructs and sets the layout parameters to have some gravity.
	 * 
	 * @param gravity
	 *            the gravity of the Crouton
	 * @return <code>this</code>, for chaining.
	 * @see android.view.Gravity
	 */
	public void addNotificatoinToView(CharSequence text, int titleGravity) {
		LayoutParams layoutParams = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT,
				titleGravity);
		int margin = Math
				.round(50 * getResources().getDisplayMetrics().densityDpi / 160.0F);
		layoutParams.topMargin = margin;
		addNotificatoinToView(text, layoutParams);
	}

	/**
	 * Constructs and sets the layout parameters to have some LayoutParams.
	 * 
	 * @param text
	 * @param params
	 */
	private void addNotificatoinToView(CharSequence text, LayoutParams params) {
		if (isNotificationVisiable()) {
			getBaseHandle().removeMessages(R.layout.ads_tops_view);
			removeNotificatoinView(findViewById(R.id.free_num_tip));
		}
		initNotificatoinAnimation();
		View view = getLayoutInflater().inflate(R.layout.ads_tops_view, null);
		if (view.getParent() == null) {
			addContentView(view, params);
		}
		((TextView) view.findViewById(R.id.tv_tips)).setText(text);
		view.startAnimation(inAnimation);
		if (view.getVisibility() != View.VISIBLE) {
			view.setVisibility(View.VISIBLE);
		}

		if (getDuration() != -1) {
			Message msg = getBaseHandle().obtainMessage(R.layout.ads_tops_view);
			msg.obj = view;
			getBaseHandle().sendMessageDelayed(msg, 6000);
		}
	}

	public boolean isNotificationVisiable() {

		return findViewById(R.id.free_num_tip) != null;
	}

	/**
	 * Set how long to show the view for. -1 VISIBLE alway
	 * 
	 * @see #LENGTH_SHORT
	 * @see #LENGTH_LONG
	 */
	public void setDuration(int duration) {
		mDuration = duration;
	}

	/**
	 * Return the duration.
	 * 
	 * @see #setDuration
	 */
	public int getDuration() {
		return mDuration;
	}

	/**
	 * Removes the {@link AppMsg}'s view after it's display duration.
	 * 
	 * @param appMsg
	 *            The {@link AppMsg} added to a {@link android.view.ViewGroup} and should be
	 *            removed.s
	 */
	public void removeNotificatoinView(final View view) {
		ViewGroup parent = ((ViewGroup) view.getParent());
		if (parent != null) {
			outAnimation.setAnimationListener(new OutAnimationListener(view));
			view.startAnimation(outAnimation);
			// Remove the AppMsg from the view's parent.
			parent.removeView(view);

		}
	}

	public void initNotificatoinAnimation() {
		if (inAnimation == null) {
			inAnimation = AnimationUtils.loadAnimation(CCPBaseActivity.this,
					android.R.anim.fade_in);
		}
		if (outAnimation == null) {
			outAnimation = AnimationUtils.loadAnimation(CCPBaseActivity.this,
					android.R.anim.fade_out);
		}
	}

	private ProgressDialog pVideoDialog = null;
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == WHAT_SHOW_PROGRESS) {
				pVideoDialog = new ProgressDialog(CCPBaseActivity.this);
				// pVideoDialog.setTitle(R.string.str_dialog_title);
				pVideoDialog
						.setMessage(getString(R.string.str_dialog_message_default));
				pVideoDialog.setCanceledOnTouchOutside(false);
				String message = (String) msg.obj;
				if (!TextUtils.isEmpty(message))
					pVideoDialog.setMessage(message);
				pVideoDialog.show();

			} else if (msg.what == WHAT_CLOSE_PROGRESS) {
				if (pVideoDialog != null) {
					pVideoDialog.dismiss();
					pVideoDialog = null;
				}

			} else {
				switch (msg.what) {
				case R.layout.ads_tops_view:
					if (msg.obj != null && msg.obj instanceof View) {
						removeNotificatoinView((View) msg.obj);
					}
					break;

				default:
					handleNotifyMessage(msg);
					break;
				}
			}
		};
	};

	private EditText mInvitEt;

	@SuppressLint("HandlerLeak")
	public Handler getBaseHandle() {
		return handler;
	}

	private static class OutAnimationListener implements
			Animation.AnimationListener {

		private View view;

		private OutAnimationListener(View view) {
			this.view = view;
		}

		@Override
		public void onAnimationStart(Animation animation) {

		}

		@Override
		public void onAnimationEnd(Animation animation) {
			view.setVisibility(View.GONE);
		}

		@Override
		public void onAnimationRepeat(Animation animation) {

		}
	}

	@Override
	protected void onStart() {
		super.onStart();

	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	public int getTitleLayout() {
		return R.layout.ccp_title;

	}

	protected abstract int getLayoutId();

	public void showRequestErrorToast(CloopenReason reason) {
		if (reason != null && reason.isError()) {

			Toast.makeText(this,
					reason.getMessage() + "[" + reason.getReasonCode() + "]",
					Toast.LENGTH_SHORT).show();
		}
	}
}

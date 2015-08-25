package com.speedtong.example.meeting.ui;//package com.speedtong.example.meeting.ui;
//
//import java.util.ArrayList;
//
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.Button;
//
//import com.speedtong.example.meeting.ECApplication;
//import com.speedtong.example.meeting.R;
//import com.speedtong.example.meeting.common.utils.ToastUtil;
//import com.speedtong.example.meeting.core.CCPConfig;
//import com.speedtong.example.meeting.ui.rest.LoginRestHelper;
//import com.speedtong.example.meeting.ui.rest.LoginRestHelper.OnRestManagerHelpListener;
//import com.speedtong.example.meeting.view.CCPFormInputView;
//import com.speedtong.example.meeting.view.ECProgressDialog;
//
//public class ECBindTestPhoneUI extends ECSuperActivity implements
//		OnClickListener, OnRestManagerHelpListener {
//
//	private CCPFormInputView etPhone;
//	private Button button;
//	private ECProgressDialog mPostingdialog;
//
//	@Override
//	protected int getLayoutId() {
//		// TODO Auto-generated method stub
//		return R.layout.binding_test_phone;
//	}
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		// TODO Auto-generated method stub
//		super.onCreate(savedInstanceState);
//
//		etPhone = (CCPFormInputView) findViewById(R.id.et_phonenum);
//		button = (Button) findViewById(R.id.bu_binding_submit);
//		getTopBarView().setTopBarToStatus(1, R.drawable.topbar_back_bt, -1,
//				R.string.bangding_test_phone, this);
//
//		button.setOnClickListener(this);
//
//	}
//
//	@Override
//	public void onClick(View v) {
//		// TODO Auto-generated method stub
//
//		switch (v.getId()) {
//		case R.id.btn_left:
//			hideSoftKeyboard();
//			finish();
//			break;
//
//		case R.id.bu_binding_submit:
//
//			final String phoneNum = etPhone.getText().toString().trim();
//			if (TextUtils.isEmpty(phoneNum)) {
//				ToastUtil.showMessage("绑定号码不能为空");
//				return;
//			}
//			String[] splitTestNumbers;
//			ArrayList<String> arrays = null;
//			if (!TextUtils.isEmpty(CCPConfig.test_number)) {
//				splitTestNumbers = CCPConfig.test_number.split(",");
//
//				arrays = new ArrayList<String>();
//				for (String str : splitTestNumbers) {
//					if (CCPConfig.mobile != null
//							&& CCPConfig.mobile.equals(str)) {
//
//					}
//					arrays.add(str);
//				}
//
//			}
//			if (arrays != null && arrays.size() > 0) {
//				if (arrays.contains(phoneNum)) {
//
//					ToastUtil.showMessage("当前号码已被绑定");
//					return;
//				}
//			}
//
//			mPostingdialog = new ECProgressDialog(this, R.string.binding);
//			mPostingdialog.show();
//
//			new Thread(new Runnable() {
//
//				@Override
//				public void run() {
//					// TODO Auto-generated method stub
//
//					LoginRestHelper.getInstance().setOnRestManagerHelpListener(
//							ECBindTestPhoneUI.this);
//					LoginRestHelper.getInstance().doTestNumber("", phoneNum);
//				}
//			}).start();
//
//			break;
//
//		default:
//			break;
//		}
//
//	}
//
//	@Override
//	public void onRequestFailed(int requestType, int errorCode,
//			String errorMessage) {
//		// TODO Auto-generated method stub
//		if (mPostingdialog != null && mPostingdialog.isShowing()) {
//			mPostingdialog.dismiss();
//		}
//		ToastUtil.showMessage(errorMessage);
//
//	}
//
//	@Override
//	public void onTestNumber(String number) {
//		// TODO Auto-generated method stub
//		if (mPostingdialog != null && mPostingdialog.isShowing()) {
//			mPostingdialog.dismiss();
//		}
//		ToastUtil.showMessage("绑定成功");
//		ECApplication.getInstance().getDemoAccountInfos();
//
//	}
//}

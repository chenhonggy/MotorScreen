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
package com.speedtong.example.meeting.ui.rest;

import java.io.ByteArrayInputStream;


import java.io.IOException;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.speedtong.sdk.core.Response;
import com.speedtong.sdk.net.HttpManager;
import com.speedtong.sdk.platformtools.SdkErrorCode;

import android.text.TextUtils;



/**
 * @ClassName: BaseRestHelper.java
 * @Description: TODO
 * @author Jorstin Chan 
 * @date 2013-12-17
 * @version 3.6
 */
public class LoginRestHelper extends BaseRestHelper {
	
	public static final String TAG = LoginRestHelper.class.getSimpleName();
	
	/**
	 * 
	 */
	public static final int REST_CLIENT_LOGIN = 0X1;
	
	/**
	 * 
	 */
	public static final int REST_BUILD_TEST_NUMBER= 0X5;
	
	private static LoginRestHelper mInstance = null;
	
	private OnRestManagerHelpListener mListener;
	
	public static LoginRestHelper getInstance(){
		if (mInstance == null) {
			mInstance = new LoginRestHelper();
		}
		return mInstance;
	}
	
	private LoginRestHelper(){
		super();
	}
	
	
	
	/**
	 * 
	 * @param oldNum
	 * @param newNum
	 */
	public void doTestNumber(String oldNum, String newNum) {
		int keyValue = REST_BUILD_TEST_NUMBER;

		StringBuffer requestURL = getAccountRequestURL(keyValue, getTimestamp());

		HashMap<String, String> requestHead = getAccountRequestHead(keyValue,
				getTimestamp());

		final StringBuffer requestBody = new StringBuffer("<Request>\r\n");
		if (!TextUtils.isEmpty(oldNum))
			requestBody.append("\t<oldNum>").append(oldNum)
					.append("</oldNum>\r\n");
		requestBody.append("\t<newNum>").append(newNum).append("</newNum>\r\n");
		requestBody.append("</Request>\r\n");

		try {

			String xml = HttpManager.httpPost(requestURL.toString(),
					requestHead, requestBody.toString());

			Response response = doParserResponse(keyValue,
					new ByteArrayInputStream(xml.getBytes()));

			if (response != null) {
				if (response.isError()) {
					handleRequestFailed(keyValue, response.statusCode,
							response.statusMsg);
				} else {
					if (mListener != null) {
						mListener.onTestNumber(newNum);
					}
				}
			} else {
				handleRequestFailed(keyValue, SdkErrorCode.SDK_XML_ERROR + "",
						null);
			}

		} catch (Exception e) {
			e.printStackTrace();
			handleRequestFailed(keyValue, SdkErrorCode.SDK_UNKNOW_ERROR + "",
					null);
		} finally {
			if (requestHead != null) {
				requestHead.clear();
				requestHead = null;
			}
		}
	}
	
	protected void handleRequestFailed(int requestType, String errorCode,
			String errorMessage) {

		if (mListener != null) {

			int code = -1;
			try {
				code = Integer.valueOf(errorCode);
			} catch (Exception e) {

			}
			if (TextUtils.isEmpty(errorMessage)) {
				errorMessage = "请求失败,";
			}
			mListener.onRequestFailed(requestType, code, errorMessage);
		}
	}

	
	@Override
	protected Response getResponseByKey(int key) {
		switch (key) {
		
		default:
			return new Response();
		}
	}

	
	@Override
	protected void formatURL(StringBuffer requestUrl, int requestType) {
		switch (requestType) {
		case REST_CLIENT_LOGIN:
			requestUrl.append("GetServiceNum");
			break;
			
		case REST_BUILD_TEST_NUMBER :
			requestUrl.append("TestNumEdit");
			break;
		default:
			break;
		}
	}

	/* (non-Javadoc)
	 * @see com.voice.demo.tools.net.RestRequestManagerHelper#handleParserXMLBody(int, org.xmlpull.v1.XmlPullParser, com.hisun.phone.core.voice.model.Response)
	 */
	@Override
	protected void handleParserXMLBody(int parseType, XmlPullParser xmlParser,
			Response response) throws Exception {
		if(parseType == REST_CLIENT_LOGIN) {
			
			
		} else {
			xmlParser.nextText();
		}
	}
	

	

	

	
	public void setOnRestManagerHelpListener(OnRestManagerHelpListener l) {
		this.mListener = l;
	}
	
	
	public interface OnRestManagerHelpListener extends BaseHelpListener{
	
		void onTestNumber(String number);
	}

	
	
	
}

/*
 *  Copyright (c) 2013 The CCP project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a Beijing Speedtong Information Technology Co.,Ltd license
 *  that can be found in the LICENSE file in the root of the web site.
 *
 *   http://www.yuntongxun.com
 *
 *  An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */
package com.speedtong.example.meeting.contacts;

import android.graphics.Bitmap;

import com.screen.main.R;
import com.speedtong.example.meeting.common.utils.BitmapUtil.InnerBitmapEntity;
import com.speedtong.example.meeting.common.utils.DemoUtils;
import com.speedtong.example.meeting.common.utils.ECPropertiesUtil;
import com.speedtong.example.meeting.common.utils.LogUtil;
import com.speedtong.example.meeting.plugin.ResourceHelper;
import com.speedtong.example.meeting.ui.manager.CCPAppManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Jorstin Chan@容联•云通讯
 * @date 2014-12-12
 * @version 4.0
 */
public class ContactLogic {

	public static final String ALPHA_ACCOUNT = "izhangjy@163.com";
	private static HashMap<String, Bitmap> photoCache = new HashMap<String, Bitmap>(
			20);
	public static final String[] CONVER_NAME = { "张三", "李四", "王五", "赵六", "钱七" };
	public static final String[] CONVER_PHONTO = {
			"select_account_photo_one.png", "select_account_photo_two.png",
			"select_account_photo_three.png", "select_account_photo_four.png",
			"select_account_photo_five.png" };

	private static Bitmap mDefaultBitmap = null;

	static {
		try {
			if (mDefaultBitmap == null) {
				mDefaultBitmap = DemoUtils.decodeStream(
						CCPAppManager.getContext().getAssets()
								.open("avatar/default_nor_avatar.png"),
						ResourceHelper.getDensity(null));
			}
		} catch (IOException e) {
		}
	}

	/**
	 * 查找头像
	 * 
	 * @param username
	 * @return
	 */
	public static Bitmap getPhoto(String username) {

		try {
			if (photoCache.containsKey(username)) {
				return photoCache.get(username);
			}

			Bitmap bitmap = DemoUtils.decodeStream(CCPAppManager.getContext()
					.getAssets().open("avatar/" + username),
					ResourceHelper.getDensity(null));
			photoCache.put(username, bitmap);
			return bitmap;
		} catch (IOException e) {
		}
		return mDefaultBitmap;
	}

	private static List<InnerBitmapEntity> getBitmapEntitys(int count) {
		List<InnerBitmapEntity> mList = new LinkedList<InnerBitmapEntity>();
		String value = ECPropertiesUtil.readData(CCPAppManager.getContext(),
				String.valueOf(count), R.raw.nine_rect);
		LogUtil.d("value=>" + value);
		String[] arr1 = value.split(";");
		int length = arr1.length;
		for (int i = 0; i < length; i++) {
			String content = arr1[i];
			String[] arr2 = content.split(",");
			InnerBitmapEntity entity = null;
			for (int j = 0; j < arr2.length; j++) {
				entity = new InnerBitmapEntity();
				entity.x = Float.valueOf(arr2[0]);
				entity.y = Float.valueOf(arr2[1]);
				entity.width = Float.valueOf(arr2[2]);
				entity.height = Float.valueOf(arr2[3]);
			}
			mList.add(entity);
		}
		return mList;
	}

	/**
	 * 随即设置用户昵称
	 * 
	 * @param beas
	 * @return
	 */
	public static ArrayList<ECContacts> converContacts(
			ArrayList<ECContacts> beas) {

		if (beas == null || beas.isEmpty()) {
			return null;
		}
		Collections.sort(beas, new Comparator<ECContacts>() {

			@Override
			public int compare(ECContacts lhs, ECContacts rhs) {

				return lhs.getContactid().compareTo(rhs.getContactid());
			}

		});

		boolean alphaTest = isAlphaTest();
		for (int i = 0; i < beas.size(); i++) {
			ECContacts accountBean = beas.get(i);

			if (i < 5) {
				accountBean.setNickname(CONVER_NAME[i]);
				accountBean.setRemark(ContactLogic.CONVER_PHONTO[i]);
			} else {
				accountBean.setNickname("云通讯" + i);
				accountBean.setRemark("personal_center_default_avatar.png");
			}
		}

		return beas;
	}

	private static boolean isAlphaTest() {
		return ALPHA_ACCOUNT.equals(DemoUtils.getLoginAccount());
	}
}

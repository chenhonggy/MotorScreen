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
package com.speedtong.example.meeting.view;

import com.speedtong.example.meeting.common.utils.DensityUtil;

import android.content.Context;

import android.util.AttributeSet;
import android.view.View;



/**
 * 标题右边下拉菜单
 * @author Jorstin Chan@容联•云通讯
 * @date 2014-12-26
 * @version 4.0
 */
public class PopupMenuListView extends SuperListView {

	private Context mContext;
	
	/**
	 * @param context
	 */
	public PopupMenuListView(Context context) {
		super(context);
		mContext = context;
	}
	
	/**
	 * @param context
	 * @param attrs
	 */
	public PopupMenuListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(MeasureSpec.makeMeasureSpec(measureWidth()
				+ getPaddingLeft() + getPaddingRight(),
				MeasureSpec.EXACTLY), heightMeasureSpec);
	}
	
	/**
	 * 计算宽度
	 * @return
	 */
	private int measureWidth() {
		int maxWidth = 0;
		View convertView = null;
		for(int i = 0 ; i < getAdapter().getCount() ; i ++) {
			convertView = getAdapter().getView(i, convertView, this);
			if(convertView == null) {
				continue;
			}
			convertView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
			if(convertView.getMeasuredWidth() <= maxWidth) {
				continue;
			}
			maxWidth = convertView.getMeasuredWidth();
		}
		
		int max = DensityUtil.dip2px(112.0F);
		if(maxWidth < max) {
			maxWidth = max;
		}
		return maxWidth;
	}

}

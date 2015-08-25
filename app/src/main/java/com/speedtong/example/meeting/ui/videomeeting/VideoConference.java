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
 */package com.speedtong.example.meeting.ui.videomeeting;


import android.os.Bundle;
import android.view.View;

import com.screen.main.R;

public class VideoConference extends VideoconferenceBaseActivity implements View.OnClickListener{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		findViewById(R.id.create).setOnClickListener(this);
		findViewById(R.id.querylist).setOnClickListener(this);
		findViewById(R.id.querymembers).setOnClickListener(this);
		findViewById(R.id.dismiss).setOnClickListener(this);
		findViewById(R.id.exit).setOnClickListener(this);
		findViewById(R.id.join).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		default:
			break;
		}
	}

	@Override
	protected int getLayoutId() {
		return R.layout.layout_video_conference;
	}

}

package com.speedtong.example.meeting.common.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.screen.main.R;
import com.speedtong.example.meeting.ECApplication;
import com.speedtong.example.meeting.ui.interphonemeeting.InterPhoneRoomActivity;
import com.speedtong.sdk.ECDevice.CallType;

import java.io.IOException;

public class CCPNotificationManager {
	
	public static final int CCP_NOTIFICATOIN_ID_CALLING = 0x1;
	
	private static NotificationManager mNotificationManager;

	public static void showInCallingNotication(Context context ,CallType callType ,String topic, String text) {
		
		try {
			
			checkNotification(context);
			
			Notification notification = new Notification(R.drawable.icon_call_small, text,
					System.currentTimeMillis());
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			notification.tickerText = topic;
			Intent intent = null;
			if(callType == CallType.VIDEO) {
				intent = new Intent(ECApplication.ACTION_VIDEO_INTCALL);
			} else {
				intent = new Intent(ECApplication.ACTION_VOIP_INCALL);
			}
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);  
			
			PendingIntent contentIntent = PendingIntent.getActivity(context, 
					R.string.app_name, 
					intent, 
					PendingIntent.FLAG_UPDATE_CURRENT);
			
			notification.setLatestEventInfo(context, 
					topic, 
					text, 
					contentIntent);
			
			mNotificationManager.notify(CCP_NOTIFICATOIN_ID_CALLING, notification);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void showOutCallingNotication(Context context ,CallType callType ,String topic, String text) {
		
		try {
			
			checkNotification(context);
			
			Notification notification = new Notification(R.drawable.icon_call_small, text,
					System.currentTimeMillis());
			notification.flags = Notification.FLAG_AUTO_CANCEL; 
			notification.tickerText = topic;
			Intent intent = null;
			if(callType == CallType.VIDEO) {
				intent = new Intent(ECApplication.ACTION_VIDEO_OUTCALL);
			} else {
				intent = new Intent(ECApplication.ACTION_VOIP_OUTCALL);
			}
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);  
			
			PendingIntent contentIntent = PendingIntent.getActivity(
					context, 
					R.string.app_name, 
					intent, 
					PendingIntent.FLAG_UPDATE_CURRENT);
			
			notification.setLatestEventInfo(context, 
					topic, 
					text, 
					contentIntent);
			
			mNotificationManager.notify(CCP_NOTIFICATOIN_ID_CALLING, notification);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void showNewInterPhoneNoti(Context context ,String config) throws IOException {
		NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);               
		Notification n = new Notification(R.drawable.icon, context.getString(R.string.str_notifycation_inter_phone)
				, System.currentTimeMillis());             
		n.flags = Notification.FLAG_AUTO_CANCEL;
		n.defaults = Notification.DEFAULT_SOUND;
		n.audioStreamType= android.media.AudioManager.ADJUST_LOWER;
		Intent intent = new Intent(context, InterPhoneRoomActivity.class);
		intent.putExtra("confNo",config);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);           
		//PendingIntent
		PendingIntent contentIntent = PendingIntent.getActivity(
				context, 
		        R.string.app_name, 
		        intent, 
		        PendingIntent.FLAG_UPDATE_CURRENT);
		                 
		n.setLatestEventInfo(
				context,
				config, 
		        context.getString(R.string.notifycation_new_inter_phone_title), 
		        contentIntent);
		nm.notify(R.string.app_name, n);
	}
	
	
	
	
	public static void cancleCCPNotification(Context context , int id) {
		checkNotification(context);
		
		mNotificationManager.cancel(id);
	}

	private static void checkNotification(Context context) {
		if(mNotificationManager == null) {
			mNotificationManager = (NotificationManager) context.getSystemService(
					Context.NOTIFICATION_SERVICE);
		}
	}
}

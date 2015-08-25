package com.speedtong.example.meeting.common.utils;

import org.json.JSONException;
import org.json.XML;

import org.json.JSONObject;





/**
 *
 */
public class OrgJosnUtils {
	
	/**
	 * json string convert to xml string
	 */
	public static String json2xml(String json){
		 
		return null; 
	}
	
	/**
	 * xml string convert to json string
	 */
	public static String xml2json(String xml){
		try {
			return XML.toJSONObject(xml).toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null; 
	}
	 
}



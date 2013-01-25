package com.jadn.cc.core;

import java.io.File;
import java.io.FileInputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public class PushToServer {

	public boolean pushFile(File file) {
		try {
			   HttpClient httpclient = new DefaultHttpClient();

			    HttpPost httppost = new HttpPost("http://192.168.1.2:8080/carcast/audio/post?bob@jadn.com");

			    InputStreamEntity reqEntity = new InputStreamEntity(
			            new FileInputStream(file), -1);
			    reqEntity.setContentType("binary/octet-stream");
			    reqEntity.setChunked(true); // Send in multiple parts if needed
			    httppost.setEntity(reqEntity);
			    HttpResponse response = httpclient.execute(httppost);
			    Log.i("carcast", response.getStatusLine().toString() );
			    return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}

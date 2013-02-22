package com.baixing.network.api;

import android.content.Context;
import android.util.Pair;

import com.baixing.network.impl.FileUploadRequest;
import com.baixing.network.impl.HttpNetworkConnector;

public class FileUploadCommand {
	
	static String HOST = "www.baixing.com";
	
	private String filePath;
	private FileUploadCommand(String filepath) {
		this.filePath = filepath;
	}
	
	public static  FileUploadCommand create(String filePath) {
		return new FileUploadCommand(filePath);
	}
	
	private String getUrl() {
		return "http://" + HOST + "/image_upload/";
	}
	
	public String doUpload(Context context) {
		FileUploadRequest request = new FileUploadRequest(getUrl(), this.filePath);
		
		Pair<Boolean, String> result = (Pair<Boolean, String>) HttpNetworkConnector.connect().sendHttpRequestSync(context, request, new PlainRespHandler());
		
		return result.second;
	}
}

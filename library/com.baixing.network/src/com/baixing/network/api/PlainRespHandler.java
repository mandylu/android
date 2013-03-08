package com.baixing.network.api;

import java.io.UnsupportedEncodingException;

import org.apache.http.util.ByteArrayBuffer;

import android.util.Log;
import android.util.Pair;

import com.baixing.network.impl.HttpNetworkConnector.IResponseHandler;

public class PlainRespHandler implements IResponseHandler<Pair<Boolean, String>> {

	private ByteArrayBuffer buf;// = new ByteArrayBuffer(4096);
	
	public PlainRespHandler() {
	}
	
	@Override
	public Pair<Boolean, String> networkError(int respCode, String serverMessage) {
		return createResponse(false, serverMessage);
	}

	@Override
	public Pair<Boolean, String> handleException(Exception ex) {
		return createResponse(false, ex.getMessage());
	}

	@Override
	public void handlePartialData(byte[] partOfResponseData, int len) {
		if (buf == null) {
			buf = new ByteArrayBuffer(4096);
		}
		if (len > 0) buf.append(partOfResponseData, 0, len);
	}

	@Override
	public Pair<Boolean, String> handleResponseEnd(String charset) {
		String response = "";
		try {
			response = new String(buf.toByteArray(), charset);
		} catch (UnsupportedEncodingException e) {
			Log.e("PlainResponseHandler", "fail to decode response " + e.getMessage());
		}
		return createResponse(true, response);
	}
	
	private Pair<Boolean, String> createResponse(boolean succed, String responseData) {
		Pair p = new Pair(Boolean.valueOf(succed), responseData != null ? responseData : "");
		buf.clear();
		return p;
	}
}


package com.baixing.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.baixing.data.GlobalDataManager;
import com.baixing.entity.UserBean;
import com.baixing.message.BxMessageCenter;
import com.baixing.message.IBxNotificationNames;

public class Communication implements Comparator<String> {

	public static interface CommandListener
	{
		void onServerResponse(String serverMessage);
		void onException(Exception ex);
	}
	
	private final static Communication COMPARATOR = new Communication();

	public static String apiKey = "api_mobile_android";
//	 public static String apiKey = "baixing_ios";
	public static String apiSecret = "c6dd9d408c0bcbeda381d42955e08a3f";
//	 public static String apiSecret = "f93bfd64405a641a7c8447fc50e55d6e";

	 public static String apiUrl = "http://www.baixing.com/api/mobile.";
	 
	 public static boolean isWifiConnection() {
		ConnectivityManager connectivityManager = 
				(ConnectivityManager) GlobalDataManager.getInstance().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo != null
				&& activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
			return true;
		}
		return false;
	}

	public static boolean isNetworkActive() {
		ConnectivityManager connectivityManager = (ConnectivityManager) GlobalDataManager.getInstance().getApplicationContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo != null) {
			return true;
		}
		return false;
	}
	
	@Deprecated
	public static String getApiUrl(String apiName, List<String> parameters) {
		String url = apiUrl + apiName + "/?" + getPostParameters(parameters);
		Log.d("Communication", url);
		return url;
	}

	public static String getTimeStamp() {
		return (System.currentTimeMillis() / 1000) + "";
	}

	// 业务逻辑API URL
	private static String getPostParameters(List<String> list) {
		/* 
		 * if(MyApplication.udid.equals("") ||
		 * MyApplication.version.equals("")){ getudid(); getversion(); }
		 */
		list.add("udid=" + Util.getDeviceUdid(GlobalDataManager.getInstance().getApplicationContext()));
		list.add("version=" + Util.getVersion(GlobalDataManager.getInstance().getApplicationContext()));
		list.add("api_key=" + apiKey);
		list.add("channel=" + GlobalDataManager.getInstance().getChannelId());
		list.add("timestamp=" + getTimeStamp());
		list.add("userId=" + GlobalDataManager.getInstance().getAccountManager().getMyId(GlobalDataManager.getInstance().getApplicationContext()) );
		if(GlobalDataManager.getInstance() != null){
			list.add("city=" + GlobalDataManager.getInstance().getCityEnglishName());
		}
		
		Collections.sort(list, COMPARATOR);

		String queryString = "";
		for (Object s : list) {
			queryString += "&" + urlEncode((String) s);
		}
		
		String parameter = urlEncode(queryString.substring(1));
		String md5String = getMD5(parameter + apiSecret);
		String p = "access_token=" + md5String + "&"
				+ parameter.replaceAll("image=", "image[]=");
		return p;
	}

	// Url编码
	public static String urlEncode(String str) {
		return str.replaceAll(":", "%3A").replaceAll(" ", "%20")
				.replaceAll("\\(", "%28").replaceAll("\\)", "%29")
				.replaceAll("/", "%2F").replaceAll("\\+", "%20")
				.replaceAll("\\*", "%2A").replaceAll("\\,", "%2C");
	}

	// Url编码
	public static String replaceAdd(String str) {
		return str.replaceAll("+", " ");
	}

	public static String getMD5(String str) {
		byte[] source = str.getBytes();
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(source);

			StringBuilder sb = new StringBuilder();
			for (byte b : md5.digest()) {
				sb.append(String.format("%02x", b));
			}
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// 将下载完的数据中的Unicode转成中文
	public static String decodeUnicode(String source) {
		if (null == source || " ".equals(source)) {
			return source;
		}
		StringBuffer sb = new StringBuffer();
		int i = 0;
		while (i < source.length()) {
			if (source.charAt(i) == '\\') {
				if (source.charAt(i + 1) == 'u') {
					int j = Integer
							.parseInt(source.substring(i + 2, i + 6), 16);
					sb.append((char) j);
					i += 6;
				} else {
					sb.append(source.charAt(i));
					i++;
					sb.append(source.charAt(i));
					i++;
				}
			} else {
				sb.append(source.charAt(i));
				i++;
			}
		}
		return sb.toString();

	}

	// 替换特殊字符
	public static String replace(String source) {
		if (null == source || " ".equals(source)) {
			return source;
		}
		StringBuffer sb = new StringBuffer();
		int i = 0;
		while (i < source.length()) {
			if (source.charAt(i) == '\\' || source.charAt(i) == '"') {
				i++;
			} else {
				sb.append(source.charAt(i));
				i++;
			}
		}
		return sb.toString();

	}

	// 将中文转成Unicode码
	public static String toUnicode(String theString, boolean escapeSpace) {
		int len = theString.length();
		int bufLen = len * 2;
		if (bufLen < 0) {
			bufLen = Integer.MAX_VALUE;
		}
		StringBuffer outBuffer = new StringBuffer(bufLen);

		for (int x = 0; x < len; x++) {
			char aChar = theString.charAt(x);
			if ((aChar > 61) && (aChar < 127)) {
				if (aChar == '\\') {
					outBuffer.append('\\');
					outBuffer.append('\\');
					continue;
				}
				outBuffer.append(aChar);
				continue;
			}
			switch (aChar) {
			case ' ':
				if (x == 0 || escapeSpace)
					outBuffer.append('\\');
				outBuffer.append(' ');
				break;
			case '\t':
				outBuffer.append('\\');
				outBuffer.append('t');
				break;
			case '\n':
				outBuffer.append('\\');
				outBuffer.append('n');
				break;
			case '\r':
				outBuffer.append('\\');
				outBuffer.append('r');
				break;
			case '\f':
				outBuffer.append('\\');
				outBuffer.append('f');
				break;
			case '=': // Fall through
			case ':': // Fall through
			case '#': // Fall through
			case '!':
				outBuffer.append('\\');
				outBuffer.append(aChar);
				break;
			default:
				if ((aChar < 0x0020) || (aChar > 0x007e)) {
					outBuffer.append('\\');
					outBuffer.append('u');
					outBuffer.append(toHex((aChar >> 12) & 0xF));
					outBuffer.append(toHex((aChar >> 8) & 0xF));
					outBuffer.append(toHex((aChar >> 4) & 0xF));
					outBuffer.append(toHex(aChar & 0xF));
				} else {
					outBuffer.append(aChar);
				}
			}
		}
		return outBuffer.toString().toLowerCase();
	}

	private static char toHex(int nibble) {
		return hexDigit[(nibble & 0xF)];
	}

	private static final char[] hexDigit = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	static public class BXHttpException extends Exception {
		public int errorCode = 0;
		public String msg = "";

		public BXHttpException(int errorCode, String msg) {
			this.errorCode = errorCode;
			this.msg = msg;
		}

		public BXHttpException() {

		}
	}

	public static String getDataByUrlGet(String url)
			throws UnsupportedEncodingException, IOException, BXHttpException {
		HttpClient httpClient = NetworkProtocols.getInstance().getHttpClient();

		HttpGet httpGet = new HttpGet(url);
		HttpResponse response = httpClient.execute(httpGet);

		if (response.getStatusLine() != null
				&& response.getStatusLine().getStatusCode() >= 400) {
			BXHttpException bxe = new BXHttpException(response.getStatusLine()
					.getStatusCode(), response.getStatusLine()
					.getReasonPhrase());
			throw bxe;
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				response.getEntity().getContent(), "utf-8"));// 设置编码,否则中文乱码

		String lines = "";
		String temp = "";
		while ((lines = reader.readLine()) != null) {
			temp += lines;
		}
		reader.close();
		// 断开连接

		httpClient.getConnectionManager().shutdown();
		return temp;
	}

	public enum E_DATA_POLICY {
		E_DATA_POLICY_ONLY_LOCAL,
		// E_DATA_POLICY_PREFER_LOCAL,
		E_DATA_POLICY_NETWORK_CACHEABLE, E_DATA_POLICY_NETWORK_UNCACHEABLE
	};
	
	public static void registerDevice(boolean async) {
		if (async) {
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					Communication.registerDevice(NetworkProtocols.getInstance().getHttpClient());
				}
			}).start();
		}else {
			Communication.registerDevice(NetworkProtocols.getInstance().getHttpClient());
		}
		
	}
	
	private static void registerDevice(HttpClient httpClient){
		UserBean currentUser = (UserBean) Util.loadDataFromLocate(GlobalDataManager.getInstance().getApplicationContext(), "user", UserBean.class);
		if(currentUser == null){
			UserBean anonymousUser = (UserBean) Util.loadDataFromLocate(GlobalDataManager.getInstance().getApplicationContext(), "anonymousUser", UserBean.class);
			if(anonymousUser != null){
				Util.saveDataToLocate(GlobalDataManager.getInstance().getApplicationContext(), "user", anonymousUser);
				BxMessageCenter.defaultMessageCenter().postNotification(IBxNotificationNames.NOTIFICATION_USER_CREATE, anonymousUser);
				return;
			}
		}else{
			return;
		}
		
		String apiName = "user_autoregister";
		ArrayList<String> list = new ArrayList<String>();

		String url = Communication.getApiUrl(apiName, list);
		try {
			String json_response = doRequest(httpClient, url, false);
			if (json_response != null) {
				JSONObject jsonObject = new JSONObject(json_response);

				JSONObject userObj = null;
				try {
					userObj = jsonObject.getJSONObject("user");
				} catch (Exception e) {
//					userObj = ";
					e.printStackTrace();
				}
				JSONObject json = jsonObject.getJSONObject("error");
//				String message = json.getString("message");

				if (userObj != null) {
					
					// 登录成功
					UserBean user = new UserBean();
//					JSONObject jb = jsonObject.getJSONObject("id");
					user.setId(userObj.getString("id"));
//					user.
//					user.setPhone(userObj.getString("mobile"));
					
					Util.saveDataToLocate(GlobalDataManager.getInstance().getApplicationContext(), "user", user);
					Util.saveDataToLocate(GlobalDataManager.getInstance().getApplicationContext(), "anonymousUser", user);
					BxMessageCenter.defaultMessageCenter().postNotification(IBxNotificationNames.NOTIFICATION_USER_CREATE, user);
				} 
				return;
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String doRequest(HttpClient httpClient, String url, boolean shutdown) throws UnsupportedEncodingException,
	IOException, BXHttpException{
//		Profiler.markStart("REQ_R");
		final int paramStart = url.indexOf("/?");
		HttpPost httpPost = new HttpPost(
				url.substring(0, paramStart + 2));
		StringEntity se = new StringEntity(url.substring(paramStart + 2));
		httpPost.setEntity(se);
		se.setContentType("application/x-www-form-urlencoded");
		
		httpPost.addHeader("Accept-Encoding", "gzip");

		HttpResponse response = null;
		android.util.Log.d("current url:  ", "current url is:  " + url);
		try {
			response = httpClient.execute(httpPost);
		} catch (IllegalStateException e) {
			return null;
		} catch (NullPointerException e) {
			return null;
		}
		
		StatusLine status = response.getStatusLine();
		if (status != null && status.getStatusCode() >= 400) {
			BXHttpException bxe = new BXHttpException(status.getStatusCode(),
					status.getReasonPhrase());
			throw bxe;
		}
		InputStream inputStream = response.getEntity().getContent();

		Header header = response.getFirstHeader("Content-Encoding");
		if (header != null
				&& header.getValue().toLowerCase().indexOf("gzip") > -1) {
			inputStream = new GZIPInputStream(inputStream);
		}

		InputStreamReader reader = new InputStreamReader(
				inputStream, "utf-8");
		StringBuilder sb = new StringBuilder();

		char[] buffer = new char[1024];
		int numRead = 0;
		while((numRead = reader.read(buffer)) > 0){			
			sb.append(buffer, 0, numRead);
		}
		
		reader.close();
		final String result = sb.toString(); 
//		Profiler.markEnd("REQ_READ");
//		Profiler.markEnd("REQ_R");
		// 断开连接
		if(shutdown){
//			Profiler.markStart("REQ_STORE");
			httpClient.getConnectionManager().shutdown();
			GlobalDataManager.getInstance().getNetworkCacheManager().putCacheNetworkRequest(Util.extractUrlWithoutSecret(url), result);
//			Profiler.markEnd("REQ_STORE");
		}
		return result;
	}
	
	public static String getCacheRequestIfExist(String url) {
		String extractedUrl = Util.extractUrlWithoutSecret(url);
		String result = GlobalDataManager
				.getInstance().getNetworkCacheManager().getCacheNetworkRequest(extractedUrl);
		if (result != null && !result.equals("")) {
			return result;
		}
		return null;
	}

	public static String getDataByGzipUrl(String url, boolean forceUpdate)
			throws UnsupportedEncodingException, IOException, BXHttpException {
		return getDataByUrlBasic(url, forceUpdate, true);
	} 
	
	// post提交数据方法
	public static String getDataByUrl(String url, boolean forceUpdate)
			throws UnsupportedEncodingException, IOException, BXHttpException {
		return getDataByUrlBasic(url, forceUpdate, false);
	}

	private static String getDataByUrlBasic(String url, boolean forceUpdate, boolean isGzipped)
			throws UnsupportedEncodingException, IOException, BXHttpException {

		// URL getUrl = new URL(url);
		if (!forceUpdate) {
			String cached = getCacheRequestIfExist(url);
			if (cached != null)
				return cached;
		}
		HttpClient httpClient = NetworkProtocols.getInstance().getHttpClient();

		HttpPost httpPost = new HttpPost(
				url.substring(0, url.indexOf("/?") + 2));
		StringEntity se;
		if (isGzipped) {			
			se = new StringEntity(GzipUtil.compress(url.substring(url.indexOf("/?") + 2)));
			httpPost.setEntity(se);
			se.setContentType("application/zip");//application/zip
		} else {
			se = new StringEntity(url.substring(url.indexOf("/?") + 2));
			httpPost.setEntity(se);
			se.setContentType("application/x-www-form-urlencoded");
		}
		httpPost.addHeader("Accept-Encoding", "gzip");
		HttpResponse response = httpClient.execute(httpPost);

		if (response.getStatusLine() != null
				&& response.getStatusLine().getStatusCode() >= 400) {
			BXHttpException bxe = new BXHttpException(response.getStatusLine()
					.getStatusCode(), response.getStatusLine()
					.getReasonPhrase());
			throw bxe;
		}

		InputStream inputStream = response.getEntity().getContent();

		Header header = response.getFirstHeader("Content-Encoding");
		if (header != null
				&& header.getValue().toLowerCase().indexOf("gzip") > -1) {
			inputStream = new GZIPInputStream(inputStream);
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				inputStream, "utf-8"));// 设置编码,否则中文乱码

		String lines = "";
		String temp = "";
		while ((lines = reader.readLine()) != null) {
			temp += lines;
		}
		reader.close();
		// 断开连接

		httpClient.getConnectionManager().shutdown();
		
		if (temp != null) //Simply validate json string.
		{
			temp = temp.trim();
			if ((temp.startsWith("[") && temp.endsWith("]")) || 
					(temp.startsWith("{") && temp.endsWith("}")))
			{
				GlobalDataManager.getInstance().getNetworkCacheManager().putCacheNetworkRequest(
						Util.extractUrlWithoutSecret(url), temp);
			}
		}

		return temp;
	}
	
	// Post提交数据方法
	/*
	 * private static String PostDataByUrl(String Url, ArrayList<String> list) {
	 * String word = Communication.getPostParameters(list); // BufferedWriter
	 * out; String line = ""; try { // URL url = new URL(Url); //
	 * HttpURLConnection httpURLConnection = (HttpURLConnection) url //
	 * .openConnection(); // httpURLConnection.setDoOutput(true); //
	 * httpURLConnection.setDoInput(true); //
	 * httpURLConnection.setRequestMethod("POST"); //
	 * httpURLConnection.setConnectTimeout(100000); //
	 * httpURLConnection.setReadTimeout(100000); // httpURLConnection.connect();
	 * 
	 * HttpClient httpClient = NetworkProtocols.getInstance().getHttpClient();
	 * 
	 * HttpPost httpPost = new HttpPost(Url);
	 * 
	 * StringEntity stringEntity = new StringEntity(word, "UTF-8");
	 * httpPost.setEntity(stringEntity);
	 * 
	 * HttpResponse response = httpClient.execute(httpPost); // HttpResponse
	 * response = httpClient.execute(httpPost); // // out = new
	 * BufferedWriter(new OutputStreamWriter( //
	 * httpURLConnection.getOutputStream(), "utf-8")); // // out.write(word); //
	 * out.flush(); // out.close();
	 * 
	 * BufferedReader rd = new BufferedReader(new InputStreamReader(
	 * response.getEntity().getContent()));
	 * 
	 * while ((line = rd.readLine()) != null) { word = line;
	 * 
	 * } QuanleimuApplication.resetViewCounter();//counter sent successfully
	 * httpClient.getConnectionManager().shutdown(); } catch
	 * (UnsupportedEncodingException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } catch (IOException e) { // TODO Auto-generated
	 * catch block e.printStackTrace(); }
	 * 
	 * return word; }
	 */
	// 老版本API

	public static final String POST_URL = "http://www.baixing.com/iphone/fabu/v2/?action=mobilePost&device=android";
	private static final String UPLOAD_PIC_URL = "http://www.baixing.com/image_upload/";
	public static final String DELETE_URL = "http://www.baixing.com/iphone/delete/v1/";
	public static final String FEEDBACK_URL = "http://www.baixing.com/iphone/feedback/v1/";
	private static final String BOUNDARY = "---------------------------19861025304733";

	public static String uploadPicture(Bitmap bmp) {
		// //System.out.println("uploadPicture");
		try {

			StringBuffer sb = new StringBuffer();
			sb = sb.append("--");
			sb = sb.append(BOUNDARY);
			sb = sb.append("\r\n");
			sb = sb.append("Content-Disposition: form-data; name=\"file\"; filename=\"iphonefile.jpg\"\r\n");
			sb = sb.append("Content-Type: Content-Type: image/jpeg\r\n\r\n");
			byte[] data = sb.toString().getBytes();
			byte[] end_data = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bmp.compress(Bitmap.CompressFormat.JPEG,
					(Communication.isWifiConnection() ? 100 : 50), bos);
			byte[] file = bos.toByteArray();
			// System.out.println(data.toString());
			// System.out.println("[Image upload] " + file.length + " bytes");

			HttpClient httpClient = NetworkProtocols.getInstance()
					.getHttpClient();

			HttpPost httpPost = new HttpPost(UPLOAD_PIC_URL);

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			out.write(data);
			out.write(file);
			out.write(end_data);
			out.flush();

			ByteArrayEntity stringEntity = new ByteArrayEntity(
					out.toByteArray());
			stringEntity.setContentType("multipart/form-data; boundary="
					+ BOUNDARY);
			httpPost.setEntity(stringEntity);

			HttpResponse response = httpClient.execute(httpPost);

			// URL url = new URL(UPLOAD_PIC_URL);
			// HttpURLConnection connection = (HttpURLConnection) url
			// .openConnection();
			// connection.setConnectTimeout(5000);
			// connection.setDoOutput(true);
			// connection.setRequestMethod("POST");
			// connection.setRequestProperty("Content-Type",
			// "multipart/form-data; boundary=" + BOUNDARY);
			// connection
			// .setRequestProperty(
			// "Content-Length",
			// String.valueOf(data.length + file.length
			// + end_data.length));
			// connection.setUseCaches(false);
			// connection.connect();

			out.close();

			InputStreamReader reader = new InputStreamReader(response
					.getEntity().getContent());
			BufferedReader buffer = new BufferedReader(reader);
			String content = "", line = null;
			while ((line = buffer.readLine()) != null) {
				content += line;
			}
			// System.out.println("uploaded image response: " + content);
			reader.close();
			httpClient.getConnectionManager().shutdown();
			String retUrl = null;
			JSONObject obj = new JSONObject(content);
			retUrl = obj.getString("url");
			if (retUrl != null) {
				return obj.getString("url");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int compare(String o1, String o2) {
		int key1Index = o1.indexOf("=");
		int key2Index = o2.indexOf("=");
		if (key1Index != -1 && key2Index != -1) {
			String key1 = o1.substring(0, key1Index);
			String key2 = o2.substring(0, key2Index);

			if (key1.equals(key2)) {
				String value1 = key1Index == o1.length() - 1 ? "" : o1
						.substring(key1Index + 1);
				String value2 = key2Index == o2.length() - 1 ? "" : o2
						.substring(key2Index + 1);

				return value1.compareTo(value2);
			} else {
				return key1.compareTo(key2);
			}
		}

		return o1.compareTo(o2);
	}

	/**
	 * @deprecated
	 * 
	 * @param apiName
	 * @param params
	 * @param listener
	 */
	public static void executeAsyncGetTask(final String apiName, final ParameterHolder params, final CommandListener listener) {
		executeAsyncTask(false, apiName, params, listener);
	}
	
	/**
	 * @deprecated
	 * @param isGet
	 * @param apiName
	 * @param params
	 * @param listener
	 */
	private static void executeAsyncTask(final boolean isGet, final String apiName, final ParameterHolder params, final CommandListener listener) {
		Thread t = new Thread(
				new Runnable() {
					public void run() {
						
						String url = Communication.getApiUrl(apiName, params.toParameterList());
						try {
							String result = isGet ? Communication.getDataByUrlGet(url) : Communication.getDataByUrl(url, true);
							if (listener != null)
							{
								listener.onServerResponse(result);
							}
						} catch (Exception e) {
							if (listener != null)
							{
								listener.onException(e);
							}
						}
					}
				});
		
		t.start();
	
	}
	
	/**
	 * @deprecated
	 * @param apiName
	 * @param params
	 * @param listener
	 */
	public static void executeAsyncPostTask(final String apiName, final ParameterHolder params, final CommandListener listener) {
		
		executeAsyncTask(false, apiName, params, listener);
	}
}

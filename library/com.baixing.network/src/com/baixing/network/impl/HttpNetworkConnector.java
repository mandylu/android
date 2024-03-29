package com.baixing.network.impl;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Pair;

import com.baixing.network.ICacheProxy;
import com.baixing.network.NetworkProfiler;
import com.baixing.network.NetworkUtil;
import com.baixing.network.impl.IHttpRequest.CACHE_POLICY;

/**
 * 
 * @author liuchong
 *
 */
public class HttpNetworkConnector {
	
    public interface IResponseHandler<T>
    {
        public T networkError(int respCode, String serverMessage);

        public T handleException(Exception ex);

        public void handlePartialData(byte[] partOfResponseData, int len);

        public T handleResponseEnd(String charset);
    }
    
	public static final String DEFAULT_CHARSET = "UTF-8";
	private static final String USER_AGENT = "BaixingMobileApi";
    
    public static final long DEFAULT_CONNECTION_TIMEOUT = 30000;
    public static final long DEFAULT_READ_TIMEOUT = 30000;
    
    public static final int READ_BUFFER_SIZE = 4096; 
    
    /**
     * Cache proxy used to store and load network response; HTTP POST request will never be cached, we will cache HTTP GET request due to the cache policy specified by the user of this class.
     * 
     * @see IHttpRequest#getCachePolicy()
     */
    public static ICacheProxy cacheProxy;
    
    long connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
    long readTimeout = DEFAULT_READ_TIMEOUT;
    
    private HttpNetworkConnector() {
    }
    
    public static HttpNetworkConnector connect() {
    	return new HttpNetworkConnector();
    }
	
	public void sendHttpRequest(final Context context, final IHttpRequest request, final IResponseHandler responseHandler, final IRequestStatusListener statusListener) {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				doSend(context, request, responseHandler, statusListener);
			}
		});
		
		t.start();
	}
	
	private Object syncRequestResponse;
	public Object sendHttpRequestSync(final Context context, final IHttpRequest request, final IResponseHandler responseHandler) {
		
		syncRequestResponse = null;
		doSend(context, request, responseHandler, new IRequestStatusListener() {

			@Override
			public void onConnectionStart() {
			}

			@Override
			public void onReceiveData(long cursor, long total) {
			}

			@Override
			public void onProcessingData() {
			}

			@Override
			public void onCancel() {
			}

			@Override
			public void onRequestDone(Object response) {
				syncRequestResponse = response;
			}
		});
		
		return syncRequestResponse;
	}
	
	static private String dealCacheKey(String url){
		if(TextUtils.isEmpty(url)) return "";
		String ret = new String(url);
		
		Pattern p = Pattern.compile("access_token=\\w+");
        Matcher m = p.matcher(url);
        if(m.find()){
        	ret = ret.replace(m.group(), "");
        }
        
        p = Pattern.compile("timestamp=\\w+");
        m = p.matcher(ret);
        if(m.find()){
        	ret = ret.replace(m.group(), "");
        }
        ret = ret.replaceAll("\\?\\&", "\\?");
        ret = ret.replaceAll("\\&\\&", "\\&");
        return ret;
	}

	private void doSend(Context context, IHttpRequest request, IResponseHandler responseHandler, IRequestStatusListener listener) {
		HttpURLConnection connection = null;
		Object businessResponse = null;
		
		final String targetUrl = request.getUrl();
		//Try to load data from cache.
		if (request.getCachePolicy() == CACHE_POLICY.CACHE_PREF_CACHE) { //If cache available, return it.
			if (cacheProxy != null) {
				String cacheData = cacheProxy.onLoad(dealCacheKey(targetUrl));
				
				if (cacheData != null) {
					try {
						if (!request.isCanceled()) {
							byte[] data = cacheData.getBytes(DEFAULT_CHARSET);
							responseHandler.handlePartialData(data, data.length);
							
							businessResponse = responseHandler.handleResponseEnd(DEFAULT_CHARSET);
							listener.onRequestDone(businessResponse);
						}
						
						return;
					} catch (Throwable t) {
						//Ignor.
					}
				}
			}
		}
		
		int requestSize = 0;
		int responseSize = 0;
		
		try {
			NetworkProfiler.startUrl(targetUrl);
			//Setup connection
			connection = getConnection(context, new URL(targetUrl), request.isGetRequest(), request.getContentType());
			if (listener != null) listener.onConnectionStart();
			
			//Step 1 : send data
			if (!request.isGetRequest()) {
				List<Pair<String, String>> headers = request.getHeaders();
				if (headers != null) {
					for (Pair<String, String> header : headers) {
						connection.addRequestProperty(header.first, header.second);
					}
				}
				
				connection.setConnectTimeout((int) this.connectionTimeout);
				connection.setReadTimeout((int) this.readTimeout);
				
				OutputStream os = connection.getOutputStream();
				requestSize += request.writeContent(os);
			} else {
				requestSize += targetUrl.length();
			}
			
			//Step 2 : read response data.
			String charset = getResponseCharset(connection.getContentType());
			String header = connection.getHeaderField("Content-Encoding");
			boolean isGzip = false;
			if (header != null && header.toLowerCase().contains("gzip")) {
				isGzip = true;
			}
			InputStream es = connection.getErrorStream();
			if (es == null) {
				InputStream input = connection.getInputStream();
				if (isGzip) {
					input = new GZIPInputStream(input);
				}
				
				long cursor = 0;
				final long totalLen = getResponseLen(connection.getHeaderField("Content-Length"), input.available());
				if (!request.isCanceled()) {
					if (listener != null) listener.onReceiveData(cursor, totalLen); 
				}
				else {
					return;
				}
				/*-------debug info-------
				if (targetUrl.startsWith("http://tu.baixing.net")) {
					StringBuffer buf = new StringBuffer();
					Map<String, List<String>> headers = connection.getHeaderFields();
					Iterator<String> keys = headers.keySet().iterator();
					while (keys.hasNext()) {
						String key = keys.next();
						List<String> value = headers.get(key);
						buf.append(key);
						for (String v : value) {
							buf.append(",").append(v);
						}
						buf.append("\r\n");
					}
					
					buf.toString();
				}
				//-----------------*/
				
				responseSize += totalLen;
				ByteArrayOutputStream cacheOs =  request.getCachePolicy() != CACHE_POLICY.CACHE_NOT_CACHEABLE ? new ByteArrayOutputStream() : null;
				byte[] buffer = new byte[READ_BUFFER_SIZE];
				int totalReadCount = 0;
				int count = input.read(buffer);
				while (count > 0) {
					totalReadCount += count;
					responseHandler.handlePartialData(buffer, count);
					if (cacheOs != null) {
						cacheOs.write(buffer, 0, count);
					}

					cursor += count;
					if (!request.isCanceled()) {
						if (listener != null) listener.onReceiveData(cursor, totalLen);
					}
					else {
						return;
					}
					count = input.read(buffer);
				}
				
				if (totalLen < totalReadCount) responseSize = totalReadCount;

				if (!request.isCanceled()) {
					if (listener != null) listener.onProcessingData();
				}
				else {
					return;
				}

				businessResponse = responseHandler.handleResponseEnd(charset);
				if(cacheOs != null){
					if (cacheProxy != null){
						cacheProxy.onSave(dealCacheKey(targetUrl), cacheOs.toString()); 
					}
				}
				
//				listener.onRequestDone(businessResponse);
			} else {
				if (isGzip) {
					es = new GZIPInputStream(es);
				}
				
				String msg = getStreamAsString(es, charset);
				if (TextUtils.isEmpty(msg)) {
					businessResponse = responseHandler.networkError(connection.getResponseCode(), connection.getResponseMessage());
				} else {
					businessResponse = responseHandler.handleException(new IOException(""));
				}
			}
			
		} catch (MalformedURLException e) {
//			e.printStackTrace();
			Exception ex = new Exception("请求URL出错, " + e.getMessage());
			businessResponse = responseHandler.handleException(ex);
		} catch (Throwable e) {
//			e.printStackTrace();
			String msg = "网络连接出错，请稍后重试";
			if (!NetworkUtil.isNetworkActive(context)) {
				msg = "网络连接失败，没有可用的网络连接";
			}
			Exception ex = new Exception(msg);
			businessResponse = responseHandler.handleException(ex);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
			
			NetworkProfiler.endUrl(targetUrl, requestSize, responseSize, "canceled:"+request.isCanceled());
		}
		
		if (!request.isCanceled()) {
			if (listener != null) listener.onRequestDone(businessResponse);
		}
	}
	
	private static HttpURLConnection getConnection(Context context, URL url,
			boolean isGet, String ctype) throws IOException {
		Proxy proxy = null;
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		if (!wifiManager.isWifiEnabled()) {
			Uri uri = Uri.parse("content://telephony/carriers/preferapn"); //FIXME: for device > 4.2, read APN setting may be forbidden 
			Cursor mCursor = context.getContentResolver().query(uri, null,
					null, null, null);
			if (mCursor != null && mCursor.moveToFirst()) {
				String proxyStr = mCursor.getString(mCursor
						.getColumnIndex("proxy"));
				if (proxyStr != null && proxyStr.trim().length() > 0) {
					proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
							proxyStr, 80));
				}
				mCursor.close();
			}
		}

		HttpURLConnection conn = null;
		if ("https".equals(url.getProtocol())) {
			SSLContext ctx = null;
			try {
				ctx = SSLContext.getInstance("TLS");
				ctx.init(new KeyManager[0],
						new TrustManager[] { new DefaultTrustManager() },
						new SecureRandom());
			} catch (Exception e) {
				throw new IOException(e.getMessage());
			}
			HttpsURLConnection connHttps = null;
			if (proxy == null) {
				connHttps = (HttpsURLConnection) url.openConnection();
			} else {
				connHttps = (HttpsURLConnection) url.openConnection(proxy);
			}
			connHttps.setSSLSocketFactory(ctx.getSocketFactory());
			connHttps.setHostnameVerifier(new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			});
			conn = connHttps;
		} else {
			if (proxy == null) {
				conn = (HttpURLConnection) url.openConnection();
			} else {
				conn = (HttpURLConnection) url.openConnection(proxy);
			}
		}

		conn.setRequestMethod(isGet ? "GET" : "POST");
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setRequestProperty("Accept", "text/xml,text/javascript,text/html");
		conn.setRequestProperty("User-Agent", USER_AGENT);
		conn.setRequestProperty("Content-Type", ctype);
		conn.setRequestProperty("Accept-Language", "zh-CN, en-us, en");
		conn.setRequestProperty("Accept-Encoding", "gzip");//support gzip
		return conn;
	}
	
	private static class DefaultTrustManager implements X509TrustManager {
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		public void checkClientTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
		}
	}
	
	private static int getResponseLen(String contentLen, int defaultValue) {
		try {
			return Integer.valueOf(contentLen);
		} catch(Throwable t) {
			
		}
		
		return defaultValue;
	}
	
	private static String getResponseCharset(String ctype) {
		String charset = DEFAULT_CHARSET;

		if (!TextUtils.isEmpty(ctype)) {
			String[] params = ctype.split(";");
			for (String param : params) {
				param = param.trim();
				if (param.startsWith("charset")) {
					String[] pair = param.split("=", 2);
					if (pair.length == 2) {
						if (!TextUtils.isEmpty(pair[1])) {
							charset = pair[1].trim();
						}
					}
					break;
				}
			}
		}

		return charset;
	}
	
	private static String getStreamAsString(InputStream stream, String charset)
			throws IOException {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					stream, charset));
			StringWriter writer = new StringWriter();

			char[] chars = new char[256];
			int count = 0;
			while ((count = reader.read(chars)) > 0) {
				writer.write(chars, 0, count);
			}

			return writer.toString();
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
	}
}

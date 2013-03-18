package com.baixing.network.api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;

import android.content.Context;

import com.baixing.network.impl.GetRequest;
import com.baixing.network.impl.HttpNetworkConnector;
import com.baixing.network.impl.HttpNetworkConnector.IResponseHandler;

/**
 * 
 * @author liuchong
 *
 */
public class FileDownloadCommand {
	
	private String targetUrl;
	public FileDownloadCommand(String url) {
		this.targetUrl = url;
	}
	
	/**
	 * download file on current thread.
	 * 
	 * @return true if file download is finished normally.
	 * 			false if file download failed.
	 */
	public boolean doDownload(Context context, File targetFile) {
		HttpNetworkConnector connector = HttpNetworkConnector.connect();
		
		FileDownloadHandler handler = new FileDownloadHandler(targetFile);
		
		Boolean result = (Boolean) connector.sendHttpRequestSync(context, new GetRequest(targetUrl, new HashMap<String, String>(), false), handler);
		return result.booleanValue();
	}
	
	class FileDownloadHandler implements IResponseHandler<Boolean> {

		private File targetFile;
		private OutputStream os;
		
		FileDownloadHandler(File f) {
			this.targetFile = f;
		}
		
		@Override
		public Boolean networkError(int respCode, String serverMessage) {
			return Boolean.FALSE;
		}

		@Override
		public Boolean handleException(Exception ex) {
			return Boolean.FALSE;
		}

		@Override
		public void handlePartialData(byte[] partOfResponseData, int len) {
			try {
				if (os == null) {
					os = new FileOutputStream(targetFile);
				}
				
				os.write(partOfResponseData, 0, len);
			} catch (Throwable t) {
				
			}
		}

		@Override
		public Boolean handleResponseEnd(String charset) {
			try {
				if (os != null) {
					os.flush();
					os.close();
				} else {
					return Boolean.FALSE;
				}
			} catch (Throwable t) {
				
			} finally {
				os = null;
			}
			
			return Boolean.TRUE;
		}
		
	}
}

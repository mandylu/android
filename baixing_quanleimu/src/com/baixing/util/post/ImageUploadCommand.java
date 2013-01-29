package com.baixing.util.post;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.baixing.tracking.Tracker;
import com.baixing.tracking.TrackConfig.TrackMobile.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.json.JSONObject;

import com.baixing.util.NetworkProtocols;

public class ImageUploadCommand {
	
	private static final String UPLOAD_PIC_URL = "http://www.baixing.com/image_upload/";
	private static final String BOUNDARY = "---------------------------19861025304733";
	
	private String imagePath;
	public ImageUploadCommand(String filePath) {
		this.imagePath = filePath;
	}
	
	public String doUpload() {
		String url = null;
		String failReason = null;
		long size = 0;
		long startTime = System.currentTimeMillis();
		try {

			HttpClient httpClient = NetworkProtocols.getInstance()
					.getHttpClient();

			HttpPost httpPost = new HttpPost(UPLOAD_PIC_URL);

//			EntityTemplate fileEntity = new EntityTemplate(new FileContentProvider(imagePath));
			FileContentProvider fileEntity = new FileContentProvider(imagePath);
			size = fileEntity.getContentLength();
			fileEntity.setContentType("multipart/form-data; boundary=" + BOUNDARY);
			httpPost.setEntity(fileEntity);

			HttpResponse response = httpClient.execute(httpPost);
			
			InputStreamReader reader = new InputStreamReader(response
					.getEntity().getContent());
			BufferedReader buffer = new BufferedReader(reader);
			String content = "", line = null;
			while ((line = buffer.readLine()) != null) {
				content += line;
			}
			
			reader.close();
			httpClient.getConnectionManager().shutdown();
			JSONObject obj = new JSONObject(content);
			url = obj.getString("url");
			if(url == null) {
				failReason = "url of json string in response is null";
			}
		} catch (Exception e) {
			e.printStackTrace();
			failReason = e.getMessage();
		}
		long timeInMill = System.currentTimeMillis() - startTime;
		Tracker.getInstance().event(BxEvent.POST_IMAGEUPLOAD)
				.append(Key.RESULT, url != null ? Value.YES : Value.NO)
				.append(Key.FAIL_REASON, failReason)
				.append(Key.SIZEINBYTES, size)
				.append(Key.UPLOADSECONDS, timeInMill / 1000.0)
				.end();
		return url;
	}

	class FileContentProvider extends AbstractHttpEntity {
		String filePath;
		byte[] prefix;
		byte[] suffix;
		
		public FileContentProvider(String file) {
			this.filePath = file;
			StringBuffer sb = new StringBuffer();
			sb = sb.append("--");
			sb = sb.append(BOUNDARY);
			sb = sb.append("\r\n");
			sb = sb.append("Content-Disposition: form-data; name=\"file\"; filename=\"iphonefile.jpg\"\r\n");
			sb = sb.append("Content-Type: Content-Type: image/jpeg\r\n\r\n");
			prefix = sb.toString().getBytes();
			suffix = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();
		}
		
		public void writeTo(OutputStream outstream) throws IOException {
			
			outstream.write(prefix);
			FileInputStream ins = new FileInputStream(filePath);
			byte[] buffer = new byte[2048];
			int count = 0;
			do {
				count = ins.read(buffer);
				if (count > 0) {
					outstream.write(buffer, 0, count);
				}
			} while (count > 0);
			ins.close();
			outstream.write(suffix);
			outstream.flush();
		}

		@Override
		public InputStream getContent() throws IOException,
				IllegalStateException {
			return null;
		}

		@Override
		public long getContentLength() {
			return prefix.length + new File(filePath).length() + suffix.length;
		}

		@Override
		public boolean isRepeatable() {
			return false;
		}

		@Override
		public boolean isStreaming() {
			return false;
		}
		
	}
}

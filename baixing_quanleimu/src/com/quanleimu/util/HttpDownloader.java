package com.quanleimu.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpDownloader {
	 private URL url = null;  
	   
	    
	   
	     // ���url�����ļ���ǰ��������ļ����ı��ļ�������ķ���ֵ���ļ��е�����  
	   
	     public String download(String urlStr) {  
	   
	        StringBuffer sb = new StringBuffer();  
	   
	        String line = null;  
	   
	        BufferedReader br = null;  
	   
	        try {  
	   
	            // ʹ��IO����ȡ���  
	   
	            br = new BufferedReader(new InputStreamReader(getUrl(urlStr)));  
	   
	            while ((line = br.readLine()) != null) {  
	   
	               // ����Щ�ַ���ӵ��������ĩ��  
	   
	               sb.append(line);  
	   
	            }  
	   
	    
	   
	            // �ر���  
	   
	            br.close();  
	   
	        } catch (MalformedURLException e) {  
	   
	            // TODO Auto-generated catch block  
	   
	            e.printStackTrace();  
	   
	        } catch (IOException e) {  
	   
	            // TODO Auto-generated catch block  
	   
	            e.printStackTrace();  
	   
	        }  
	   
	        return sb.toString();  
	   
	     }  
	   
	    
	   
	     // ���������ʽ���ļ�  
	   
	     //1���ļ��Ѿ����ڣ�-1���ļ�����ʧ�ܣ�0�����سɹ�  
	   
	     public int downFile(String urlStr, String path, String fileName) {  
	   
	        InputStream inputStream = null;  
	   
	        FileUtils fileUtils = new FileUtils();  
	   
	        if ((fileUtils.isFileExist(path + fileName))) {  
	   
	            return 1;  
	   
	        } else {  
	   
	            try {  
	   
	               inputStream = getUrl(urlStr);  
	   
	               File resultFile = fileUtils  
	   
	                      .writeSD(path, fileName, inputStream);  
	   
	               if (resultFile == null) {  
	   
	                   return -1;  
	   
	               }  
	   
	            } catch (MalformedURLException e) {  
	   
	               // TODO Auto-generated catch block  
	   
	               e.printStackTrace();  
	   
	            } catch (IOException e) {  
	   
	               // TODO Auto-generated catch block  
	   
	               e.printStackTrace();  
	   
	            } finally {  
	   
	               try {  
	   
	                   inputStream.close();  
	   
	               } catch (IOException e) {  
	   
	                   // TODO Auto-generated catch block  
	   
	                   e.printStackTrace();  
	   
	               }  
	   
	            }  
	   
	        }  
	   
	        return 0;  
	   
	     }  
	   
	    
	   
	     // ��װ���ص�3������ķ���  
	   
	     public InputStream getUrl(String urlStr) throws MalformedURLException,  
	 
	            IOException {  
	   
	        // ����url  
	   
	        url = new URL(urlStr);  
	   
	        // ����http  
	   
	        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();  
	   
	        // ��ȡ���  
	   
	        InputStream inputStream = urlConn.getInputStream();  
	   
	        return inputStream;  
	   
	     } 
}

package com.quanleimu.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Environment;

	public class FileUtils {
		private String SDPATH;  
	   
	       
	   
	     public String getSDPATH(){  
	   
	        return SDPATH;  
	   
	     }  
	   
	       
	   
	     public FileUtils(){  
	   
	        //�õ���ǰ�ⲿ�洢�豸��Ŀ¼  
	   
	        SDPATH=Environment.getExternalStorageDirectory()+"/";  
	   
	     }  
	   
	       
	   
	     //��SD���ϴ����ļ�  
	   
	     public File createSDFile(String fileName) throws IOException{  
	   
	        File file=new File(SDPATH+fileName);  
	   
	        file.createNewFile();  
	   
	        return file;  
	   
	     }  
	   
	       
	   
	     //��SD���ϴ���Ŀ¼  
	   
	     public File createSDDir(String dirName){  
	   
	        File dir = new File(SDPATH+dirName);  
	   
	        dir.mkdir();  
	   
	        return dir;  
	   
	     }  
	   
	       
	   
	     //�ж�SD���ϵ�Ŀ¼�Ƿ����  
	   
	     public boolean isFileExist(String fileName){  
	   
	        File file = new File(SDPATH+fileName);  
	   
	        return file.exists();  
	   
	     }  
	   
	       
	   
	     //��InputStream��������д�뵽SD����ȥ  
	   
	     public File writeSD(String path,String fileName,InputStream input){  
	   
	        File file = null;  
	   
	        OutputStream output = null;  
	   
	        try{  
	   
	            //���ô���SD��Ŀ¼����  
	   
	            createSDDir(path);  
	   
	            //���ô���SD���ļ��ķ���  
	   
	            file = createSDFile(path+fileName);  
	   
	            //�����ļ����������  
	   
	            output = new FileOutputStream(file);  
	   
	            //4���ֽڵĶ�ȡ  
	   
	            byte buffer[] = new byte[4*1024];  
	   
	            //���ļ������ݲ�Ϊ�յ�ʱ���ֹͣ���  
	   
	            while(input.read(buffer)!=-1){  
	   
	               output.write(buffer);  
	   
	            }  
	   
	            output.flush();  
	   
	        }catch(Exception e){  
	   
	            e.getMessage();  
	   
	        }finally{  
	   
	            try {  
	   
	              output.close();  
	   
	            } catch (IOException e) {  
	   
	               // TODO Auto-generated catch block  
	   
	               e.printStackTrace();  
	   
	            }  
	   
	        }  
	   
	        return file;  
	   
	     }  
}

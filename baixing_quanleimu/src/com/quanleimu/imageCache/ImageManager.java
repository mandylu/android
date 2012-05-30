package com.quanleimu.imageCache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import com.quanleimu.activity.R;
import com.quanleimu.util.NetworkProtocols;
import com.quanleimu.util.Util;

public class ImageManager
{

Map<String, SoftReference<Bitmap>> imgCache ;
	
	private Context context;
	
	
	
	public static Bitmap userDefualtHead;
	
	public ImageManager(Context context)
	{
		this.context = context;
		imgCache = new HashMap<String, SoftReference<Bitmap>>();
		userDefualtHead =drawabltToBitmap(context.getResources().getDrawable(R.drawable.moren));
		
	}
	
	public boolean contains(String url)
	{
		
		return imgCache.containsKey(url);
		
	}
	
	public Bitmap getFromCache(String url)
	{
		Bitmap bitmap = null;
		
		bitmap = this.getFromMapCache(url);
		
		if(null == bitmap)
		{
			
			bitmap =getFromFile(url);
		}
		
		return bitmap;
		
		
	}
	
	
	public Bitmap getFromFile(String url)
	{
		
		String fileName = this.getMd5(url);
		
		FileInputStream is=null;
		
		try
		{
			is=context.openFileInput(fileName);
			
			BitmapFactory.Options o =  new BitmapFactory.Options();
            o.inPurgeable = true;
            
			return BitmapFactory.decodeStream(is, null, o);
		} 
		catch (FileNotFoundException e)
		{
			try{
				is = context.getAssets().openFd(fileName).createInputStream();
				BitmapFactory.Options o =  new BitmapFactory.Options();
	            o.inPurgeable = true;
	            return BitmapFactory.decodeStream(is, null, o);
			}catch(FileNotFoundException ee){
				return null;
			}catch(IOException eee){
				return null;
			}
		}
		finally
		{
			if(null != is)
			{
				try{is.close();}catch(Exception ex){};
			}
		}
		
		
	}
	
	public Bitmap getFromMapCache(String url)
	{
		Bitmap bitmap = null;
		
		SoftReference<Bitmap> ref = null;
		
		synchronized (this)
		{
			ref = imgCache.get(url);
		}
		if(null != ref)
		{
			bitmap = ref.get();
			
		}
		return bitmap;
	}
	
	
	
	public Bitmap safeGetFromFile(String url)
	{
		Bitmap bitmap = this.getFromFile(url);
		if(null != bitmap)
		{
			synchronized (this)
			{
				imgCache.put(url, new SoftReference<Bitmap>(bitmap));
			}			
		}
		
		return bitmap;
	}
	
	
	public Bitmap safeGet(String url) throws HttpException
	{
		Bitmap bitmap = this.getFromFile(url);
		if(null != bitmap)
		{
			synchronized (this)
			{
				imgCache.put(url, new SoftReference<Bitmap>(bitmap));
			}
			return bitmap;
		}
		
		return downloadImg(url);
		
	}
	
	public Bitmap downloadImg(String urlStr) throws HttpException
	{
		
		try
		{
//			URL url = new URL(urlStr);
//			HttpURLConnection connection =(HttpURLConnection) url.openConnection();
			
			HttpClient httpClient = NetworkProtocols.getInstance().getHttpClient();
	        
	        HttpPost httpPost = new HttpPost(urlStr); 
	        HttpResponse response = httpClient.execute(httpPost);
	        
			String fileName=writerToFile(getMd5(urlStr),response.getEntity().getContent());
			
			
			httpClient.getConnectionManager().shutdown();
			
			BitmapFactory.Options o =  new BitmapFactory.Options();
            o.inPurgeable = true;
            
			return BitmapFactory.decodeFile(fileName, o);
			
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	
	public void clearall(){
		String[] str = context.fileList();
		for (int i = 0; i < str.length; i++) {
			context.deleteFile(str[i]);
		}
	}
	
	public static List<String> listNames= new ArrayList<String>();;
	public String  writerToFile(String fileName, InputStream is)
	{
		
		BufferedInputStream bis = null;
		
		BufferedOutputStream bos = null;
		
		try
		{
			bis = new BufferedInputStream(is);
			bos = new BufferedOutputStream(context.openFileOutput(fileName, Context.MODE_PRIVATE));
			
			byte[] buffer = new byte[1024];
			int length;
			while((length = bis.read(buffer)) != -1)
			{
				bos.write(buffer, 0, length);
			}
//			listNames = MyApplication.list;
//			if(listNames == null || listNames.size() == 0)
//			{
//				listNames = new ArrayList<String>();
//				listNames.add(fileName);
//			}
//			else if(!listNames.contains(fileName))
//			{
//				listNames.add(fileName);
//			}
//			MyApplication.list = listNames;
			
			
		} catch (Exception e)
		{
			
		}
		finally
		{
			
			try
			{
				if(null != bis)
				{
					bis.close();
				}
				
				if(null != bos)
					{
						bos.flush();
						bos.close();
					
					}
				
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		
		return context.getFilesDir() + "/" + fileName;
		
	}
	
	
	
	
	
	
	private String getMd5(String src)
	{
		return Util.MD5(src);
	}
	
	
	
	
	private Bitmap drawabltToBitmap(Drawable drawable)
	{
		
		BitmapDrawable tempDrawable = (BitmapDrawable)drawable;
		return tempDrawable.getBitmap();
		
	}
}

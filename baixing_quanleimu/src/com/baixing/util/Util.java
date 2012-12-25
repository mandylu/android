//liuchong@baixing.com
package com.baixing.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Environment;
import android.os.Message;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Pair;
import android.view.Display;

import com.baixing.data.GlobalDataManager;
import com.baixing.entity.UserBean;
import com.baixing.jsonutil.JsonUtil;
import com.baixing.message.BxMessageCenter;
import com.baixing.message.IBxNotificationNames;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
public class Util {
	
	public static final String TAG = "QLM";
	
	private static String[] keys;
	private static String[] values;
	
	public static String qq_access_key="";
	public static String qq_access_secret="";
	
//	private static String currentUserId;

	private static String getSdCardRoot() {
		String path = Environment.getExternalStorageDirectory().getAbsolutePath();
		return path.endsWith("/") ? path : path + "/";
	}
	
	//数据保存SD卡
	public static String saveDataToSdCard(String path, String file,
			Object object, boolean append) {
		String res = null;
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		if (Environment.getExternalStorageState() != null) {
			try {
				String sdcardRoot = getSdCardRoot();
				File p = new File(sdcardRoot + path); // 创建目录
				File f = new File(sdcardRoot + path + "/" + file + ".txt"); // 创建文件
				if (!p.exists()) {
					p.mkdir();
				}
				if (!f.exists()) {
					f.createNewFile();
				}
				fos = new FileOutputStream(f, append);
				oos = new ObjectOutputStream(fos);
				oos.writeObject(object);
			} catch (FileNotFoundException e) {
				res = "没有找到文件";
				e.printStackTrace();
			} catch (IOException e) {
				res = "没有数据";
				e.printStackTrace();
			} finally {
				try {
					if(null != oos){
						oos.close();
					}
					if(null != fos){
						fos.close();
					}
					res = "保存成功";
				} catch (IOException e) {
					res = "没有数据";
					e.printStackTrace();
				}
			}
		}
		return res;
	}

	//从SD卡读取数据
	public static Object loadDataFromSdCard(String path, String file) {
		Object obj = null;
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		if (Environment.getExternalStorageState() != null) {
			try {
				fis = new FileInputStream(getSdCardRoot() + path + "/" + file
						+ ".txt");
				ois = new ObjectInputStream(fis);
				obj = ois.readObject();
			} catch (FileNotFoundException e) {
				obj = null;
				e.printStackTrace();
			} catch (IOException e) {
				obj = null;
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				obj = null;
				e.printStackTrace();
			} finally {
				try {
					if(null != ois){
						ois.close();
					}
					if(null != fis){
						fis.close();
					}
				} catch (Exception e) {
					obj = null;
					e.printStackTrace();
				}
			}
		}
		return obj;
	}
	
	public static void saveDataToLocateDelay(final Context context, final String file, final Object obj)
	{
		Thread t = new Thread(new Runnable() {
			public void run() {
				saveDataToLocate(context, file, obj);
			}
		});
		t.start();
	}

	//将数据保存到手机内存中
	public static String saveDataToLocate(Context context, String file,
			Object object) {
//		Profiler.markStart("WRITE_OBJ");
		if(file != null && !file.equals("") && file.charAt(0) != '_'){
			file = "_" + file;
		}
		
		if (object == null)
		{
			context.deleteFile(file);
			return "保存成功";
		}
		
		String res = null;
		FileOutputStream fos = null;
		try {
			fos = context.openFileOutput(file, Activity.MODE_PRIVATE);
			ObjectWriter writer = mapper.writer();
			writer.writeValue(fos, object);
//			mapper.writeValue(fos, object);
			res = "保存成功";
		} catch (FileNotFoundException e) {
			res = "没有找到文件";
			e.printStackTrace();
		} catch (IOException e) {
			res = "没有数据";
			e.printStackTrace();
		} catch(NullPointerException e){
			res = "null pointer";
			e.printStackTrace();
		} finally {
			try {
				if(null != fos){
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
//		Profiler.markEnd("WRITE_OBJ");
//		Profiler.dump();
//		Profiler.clear();
		return res;
	}
	
	/**
	 * Return files under the specified dir.
	 * 
	 * @param context
	 * @param dir
	 * @return
	 */
	public static List<String> listFiles(Context context, String dir)
	{
		List<String> list = new ArrayList<String>();
		String dirPath = context.getFilesDir().getAbsolutePath();
		dirPath  = dir.startsWith(File.separator) ? dirPath + dir : dirPath + File.separator + dir;
		
		File dirF = new File(dirPath);
		if (dirF.exists() && dirF.isDirectory())
		{
			File[] fs = dirF.listFiles();
			for (File f : fs)
			{
				list.add(f.getAbsolutePath());
			}
		}
		
		return list;
	}
	
	public static byte[] loadData(Context context, String fileName) {
		if (context == null || fileName == null)
		{
			return null;
		}
		
		try {
			return loadBytes(context.openFileInput(fileName));
		} catch (FileNotFoundException e) {
			//Ignor
		}
		
		return null;
	}
	
	private static byte[] loadBytes(FileInputStream ins)
	{
		if (ins == null)
		{
			return null;
		}
		
		try
		{
			byte[] data = new byte[ins.available()];
			ins.read(data);
			
			return data;
		}
		catch(Throwable t)
		{
			
		}
		finally
		{
			try
			{
				if (ins != null)
				{
					ins.close();
				}
			}
			catch(Throwable t)
			{
				//Ignor.
			}
		}
		
		return null;
	}
	
	public static byte[] loadData(String absolutePath)
	{

		if (absolutePath == null)
		{
			return null;
		}
		
		File f = new File(absolutePath);
		if (!f.exists() || f.isDirectory())
		{
			return null;
		}
		
		try {
			return loadBytes(new FileInputStream(f));
		} catch (FileNotFoundException e) {
			//Ignor
		}
		
		
		return null;
	}
	
	/**
	 * Load serializable file from specified path.
	 * 
	 * @param absolutePath
	 * @return
	 */
	public static Object loadSerializable(String absolutePath)
	{
		if (absolutePath == null)
		{
			return null;
		}
		
		File f = new File(absolutePath);
		if (!f.exists() || f.isDirectory())
		{
			return null;
		}
		
		ObjectInputStream ins = null;
		try
		{
			ins = new ObjectInputStream(new FileInputStream(f));
			return ins.readObject();
		}
		catch(Throwable t)
		{
			
		}
		finally
		{
			try
			{
				if (ins != null)
				{
					ins.close();
				}
			}
			catch(Throwable t)
			{
				//Ignor.
			}
			
		}
		
		return null;
		
	}
	
	public static String saveDataToFile(Context context, String dir, String file, byte[] data)
	{
		return saveDataToFile( context,  dir,  file, data, false);
	}
	
	public static String saveDataToFile(Context context, String dir, String file, byte[] data, boolean append)
	{
		if (file == null || data == null || data.length == 0 || context == null)
		{
			return null;
		}
		
		String dirPath = context.getFilesDir().getAbsolutePath();
		if (dir != null)
		{
			dirPath  = dir.startsWith(File.separator) ? dirPath + dir : dirPath + File.separator + dir;
			
			File dirFile = new File(dirPath);
			dirFile.mkdirs();
			
			if (!dirFile.exists())
			{
				return null;
			}
		}
		
		
		String filePath = dirPath.endsWith(File.separator) ? dirPath + file : dirPath + File.separator + file; 
		FileOutputStream os =null;
		try
		{
			os = new FileOutputStream(new File(filePath), append);
			os.write(data);
			os.flush();
			os.close();
			
			return filePath;
		}
		catch(Throwable t)
		{
		}
		finally
		{
			if (os != null)
			{
				try
				{
					os.flush();
					os.close();
				}
				catch(Throwable t)
				{
					//Ignor.
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Save serializable file to speficied path.
	 * 
	 * @param context
	 * @param dir
	 * @param file
	 * @param obj
	 * @return
	 */
	public static String saveSerializableToPath(Context context, String dir, String file, Serializable obj)
	{
		if (file == null || obj == null || context == null)
		{
			return null;
		}
		
		String dirPath = context.getFilesDir().getAbsolutePath();
		dirPath  = dir.startsWith(File.separator) ? dirPath + dir : dirPath + File.separator + dir;
		
		File dirFile = new File(dirPath);
		dirFile.mkdirs();
		if (!dirFile.exists())
		{
			return null;
		}
		
		
		String filePath = dirPath.endsWith(File.separator) ? dirPath + file : dirPath + File.separator + file; 
		ObjectOutputStream os =null;
		try
		{
			os = new ObjectOutputStream(new FileOutputStream(new File(filePath)));
			os.writeObject(obj);
			os.flush();
			os.close();
			
			return filePath;
		}
		catch(Throwable t)
		{
		}
		finally
		{
			if (os != null)
			{
				try
				{
					os.flush();
					os.close();
				}
				catch(Throwable t)
				{
					//Ignor.
				}
			}
		}
		
		return null;
		
	}
	
	public static void clearFile(String absolutePath) {
		new File(absolutePath).delete();
	}
	
	public static void clearData(Context context, String file){
		if(file != null && !file.equals("") && file.charAt(0) != '_'){
			file = "_" + file;
		}
		
		context.deleteFile(file);
	}
	
	/**
	 * 保存json数据和timstamp(秒数)至手机内存。会检查json的完整性。
	 * @param context
	 * @param file     
	 * @param json      json String
	 * @param timestamp 秒数
	 * @return
	 */
	public static String saveJsonAndTimestampToLocate(Context context, String file, String json, long timestamp) {
		if (json == null)
			return "data invalid";
		
		json = json.trim();
		if ((json.startsWith("[") && json.endsWith("]")) ||
			(json.startsWith("{") && json.endsWith("}")) ) {
			String s = String.format("%d,%s", timestamp, json);
//			return saveDataToLocate(context, file, s);
			saveDataToFile(context, null, file, s.getBytes());
			return "保存成功";
		}else{
			return "data invalid";
		}
	}
	
	/**
	 * 将Json数据(String)从手机内存中读出来, 同时将timstamp返回(单位秒)。
	 * @param context
	 * @param filename
	 * @return Pair(LastModifiedTimeStamp, String): if file not exist, LastModifiedTimeStamp = 0;  
	 */
	public static Pair<Long, String> loadJsonAndTimestampFromLocate(Context context, String filename) {
//		File file = context.getFileStreamPath(filename);
//		long timestamp = file.lastModified()/1000;
		byte[] bytes = Util.loadData(context, filename);
		String s = bytes == null ? null : new String(bytes);
		
		if (s != null && s.length() > 0) {
			int index = s.indexOf(',');
			if (index != -1) {
				try {
					long timestamp = Long.parseLong(s.substring(0, index));
					String data = s.substring(index+1);
					return new Pair<Long, String>(timestamp, data);
				}catch (NumberFormatException e) { // old data format.
					e.printStackTrace();
				}
			}
		}
		return new Pair<Long, String>(0l, "");
	}
	
	/**
	 * 
	 * @param context
	 * @param filename
	 * @return
	 */
	public static Pair<Long, String> loadDataAndTimestampFromAssets(Context context, String filename) {
		InputStream is = null;
		Pair<Long, String> pair = new Pair<Long, String>(0l, "");
		try {
			is = context.getAssets().open(filename);
			byte[] b = new byte[is.available()];
			is.read(b);
			String s = new String(b);
			if (s != null && s.length() > 0) {
				int index = s.indexOf(',');
				if (index != -1) {
					long timestamp = Long.parseLong(s.substring(0, index));
					String data = s.substring(index+1);
					pair = new Pair<Long, String>(timestamp, data);
				}
			}
		}catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException e) {
			}
		}
		return pair;		
	}

	private static ObjectMapper mapper = new ObjectMapper();
	
	private static ObjectMapper getDefaultMapper()
	{
		if (mapper == null)
		{
			mapper = new ObjectMapper();
		}
		
		return mapper;
	}
	
	//将数据从手机内存中读出来
	public static Object loadDataFromLocate(Context context,String file, Class clsName) {
//		Profiler.markStart("READ_OBJ");
		if(file != null && !file.equals("") && file.charAt(0) != '_'){//ForArray
			file = "_" + file;
		}

		Object obj = null;
		FileInputStream fis = null;
		try {
			fis = context.openFileInput(file);
			ObjectReader reader = mapper.reader(clsName);
			obj = reader.readValue(fis);
		} catch (FileNotFoundException e) {
			obj = null;
		} catch (IOException e) {
			obj = null;
		}catch (Throwable e) {
			obj = null;
		} finally {
			try {
				if(null != fis){
					fis.close();
				}
			} catch (Exception e) {
				obj = null;
				e.printStackTrace();
			}
		}
//		Profiler.markEnd("READ_OBJ");
//		Profiler.dump();
//		Profiler.clear();
		return obj;
	}
	
	public String[] getKeys() {
		return keys;
	}
	public static void setKeys(Object...args) {
		keys = new String[args.length];
		for(int i=0;i<args.length;i++){
			keys[i] = (String) args[i];
		}
	}
	public static String[] getValues() {
		return values;
	}
	public static void setValues(Object...args) {
		values = new String[args.length];
		for(int i=0;i<args.length;i++){
			values[i] = (String) args[i];
		}
	}
	
	//MD5加密
	public  static String MD5(String inStr) {
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
		char[] charArray = inStr.toCharArray();
		byte[] byteArray = new byte[charArray.length];

		for (int i = 0; i < charArray.length; i++)
			byteArray[i] = (byte) charArray[i];
		byte[] md5Bytes = md5.digest(byteArray);
		StringBuffer hexValue = new StringBuffer();
		for (int i = 0; i < md5Bytes.length; i++) {
			int val = ((int) md5Bytes[i]) & 0xff;
			if (val < 16)
				hexValue.append("0");
			hexValue.append(Integer.toHexString(val));
		}
		return hexValue.toString();
	}
	
	public static String requestUserProfile(String usrId){
		String apiName = "user_profile";
		ArrayList<String> list = new ArrayList<String>();
		 
		list.add("rt=1");
		list.add("userId=" + usrId);
		
		String url = Communication.getApiUrl(apiName, list);
		try {
			return Communication.getDataByUrl(url, true);
		} catch (UnsupportedEncodingException e) {
			ErrorHandler.getInstance().handleError(ErrorHandler.ERROR_SERVICE_UNAVAILABLE, null);
		} catch (IOException e) {
			ErrorHandler.getInstance().handleError(ErrorHandler.ERROR_NETWORK_UNAVAILABLE, null);
		} catch (Communication.BXHttpException e) {
			ErrorHandler.getInstance().handleError(ErrorHandler.ERROR_NETWORK_UNAVAILABLE, null);
		}
		return null;
	}
	
	
	
	
	
	public static String getJsonDataFromURLByPost(String path,String params) throws SocketTimeoutException, UnknownHostException {
		//����һ��URL����
//		HttpURLConnection urlCon = null;
		BufferedReader reader = null ;
		String str = "";
		try {
			
			HttpClient httpClient = NetworkProtocols.getInstance().getHttpClient();
            
            HttpPost httpPost = new HttpPost(path); 
            
            ByteArrayEntity stringEntity = new ByteArrayEntity(params.getBytes());
            stringEntity.setContentType("application/x-www-form-urlencoded");
            httpPost.setEntity(stringEntity);
            
            HttpResponse response = httpClient.execute(httpPost);
            
//			DataOutputStream out = new DataOutputStream(urlCon.getOutputStream());
//			
//			out.writeBytes(params);
//			
//			out.flush();
//			out.close();
			reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String temp = "";
			while((temp=reader.readLine())!=null){
				str += (temp + "\n");
			}
			
			httpClient.getConnectionManager().shutdown();
		}catch(SocketTimeoutException ste){
			Log.e("��ʱ", "��ʱ��");
				throw ste; 
		}catch(UnknownHostException h){
			Log.e("��·", "���粻��ͨ");
			throw h;
		
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(reader!=null)
					reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
//			urlCon.disconnect();
		}
		//Log.d("json", "activityjson--->" + str);
		return str;
	}
	
	//post�ύ����
	public static String sendString(String str, String urlString) throws IOException{
		DataInputStream dis = null;
		String readUrl = "";
		try {
			HttpClient httpClient = NetworkProtocols.getInstance().getHttpClient();
            
            HttpPost httpPost = new HttpPost(urlString); 
            
            StringEntity stringEntity = new StringEntity(str, "UTF-8");
            httpPost.setEntity(stringEntity);
            
            HttpResponse response = httpClient.execute(httpPost);
            
			dis = new DataInputStream(response.getEntity().getContent());
			readUrl = dis.readLine();
			dis.close();
//			os.close();
			
			httpClient.getConnectionManager().shutdown();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return readUrl;
	} 
	
	public static String saveBitmapToSdCard(String path,String name,Bitmap bitmap) {
		String res = null;
		FileOutputStream fos = null; 
		if (Environment.getExternalStorageState() != null) {
			try {
				File p = new File("/sdcard/" + "quanleimu"); // 创建目录
				File s = new File("/sdcard/" + "quanleimu" + "/" + path); // 创建目录
				File f = new File("/sdcard/" + "quanleimu" + "/" + path + "/" + name + ".png"); // 创建文件
				if (!p.exists()) {
					p.mkdir();
				}
				if (!s.exists()) {
					s.mkdir();
				}
				if (!f.exists()) {
					f.createNewFile();
				}
				fos = new FileOutputStream(f);
				
				bitmap.compress(CompressFormat.JPEG, 100, fos);
				fos.close();
				res = f.getAbsolutePath();
			} catch (FileNotFoundException e) {
				res = "没有找到文件";
				e.printStackTrace();
			} catch (Exception e) {
				res = "SD卡未安装";
				e.printStackTrace();
			}
		}else{
			res = "无SD卡";
		}
		return res;
	}
	
	public static List<Bitmap> loadBitmapFromSdCard(String path,String name) {
		List<Bitmap> b = new ArrayList<Bitmap>();
		File file = new File("/sdcard/"+"quanleimu/"+path);
		if (file != null) {
			File[] files = file.listFiles();
			if (files != null) {
				for (File f : files) {
					if(f.getAbsolutePath().contains(name)){
					    BitmapFactory.Options o =  new BitmapFactory.Options();
	                    o.inPurgeable = true;
						b.add(BitmapFactory.decodeFile(f.getPath(), o));
					}
				}
			}
		}
		b = replaceList(b);
		return b;
	}
	
	public static List<Bitmap> replaceList(List<Bitmap> bit) {
		List<Bitmap> b = new ArrayList<Bitmap>();
		for (int i = bit.size() - 1; i > -1; i--) {
			b.add(bit.get(i));
		}
		return b;
	}
	
	//下载图片
	public static Bitmap getImage(String strURL)throws OutOfMemoryError{	
		//Log.d("img", strURL);
				Bitmap img = null;
				URLConnection conn = null;
				try {
					URL url = new URL(strURL);
					conn = (URLConnection) url.openConnection();
					BitmapFactory.Options o =  new BitmapFactory.Options();
					o.inPurgeable = true;
					//Log.e("o", o.toString());
					img = BitmapFactory.decodeStream(conn.getInputStream(), null, o);
				}
				catch (Exception e) { 
					img = null;
					e.printStackTrace();
					//conn.disconnect();
				}
		return img;
	}
	
	//将bitmap转成流存到file里面
	public static void saveImage2File(Context context,Bitmap bmp,String fileName)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
		byte[] file = bos.toByteArray();
		FileOutputStream fos = null;
		try {
			fos = context.openFileOutput(fileName, Activity.MODE_PRIVATE);
			fos.write(file);
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//将流转成bitmap里面
	public static Bitmap getBitmapFromInputstream(Context context,String fileName)
	{
		Bitmap bmp = null;
		FileInputStream fis = null;
		byte[] aa = null;
		try {
			fis = context.openFileInput(fileName);
			
			aa = new byte[fis.available()];
			fis.read(aa);
		} catch (FileNotFoundException e) {
			fis = null;
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(fis == null)
		{
			bmp = null;
		}
		else
		{
		    BitmapFactory.Options o =  new BitmapFactory.Options();
            o.inPurgeable = true;
			bmp = BitmapFactory.decodeByteArray(aa, 0, aa.length, o);
		}
		return bmp;
	}
	
	
	//手机分辨率宽
	public static int getWidth(Activity activity){
		Display display = activity.getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		int a = 0;
		if(width == 240)
		{
			a = 1;
		}
		else if(width == 320)
		{
			a = 2;
		}
		else if(width == 480)
		{
			a = 3;
		}
		else if(width == 540)
		{
			a = 4;
		}
		else if(width == 640)
		{
			a = 5;
		}else{
			a = 5;
		}
		
		return a;
	}
	
	public static int getWidthByContext(Context context){
		Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		return width;
	}
	
	public static int getHeightByContext(Context context){
		Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
		int height = display.getHeight();
		return height;
	}
	

	//获得手机屏幕焦点
	public static Point getSrccenPoint(Activity activity){
		Display display = activity.getWindowManager().getDefaultDisplay();
		return new Point(display.getWidth(), display.getHeight());
	}
	
    public static Bitmap newBitmap(Bitmap b, int w, int h)
    {
        float scaleWidth = 0;
        float scaleHeight = h;
        int width = b.getWidth();
        int height = b.getHeight();
        
        if (w == h)
        {
            int minValue = Math.min(b.getWidth(), b.getHeight());
            if (minValue >= w)
            {
                float ratio = (float) w / (float) minValue;
                scaleWidth = ratio;
                scaleHeight = ratio;
            }
            else
            {
                minValue = Math.max(b.getWidth(), b.getHeight());
                float ratio = (float) w / (float) minValue;
                scaleWidth = ratio;
                scaleHeight = ratio;
            }
        }
        else
        {
            scaleWidth = ((float) w) / width;
            scaleHeight = ((float) h) / height;
        }
        
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);// ����
        return Bitmap.createBitmap(b, 0, 0, width, height, matrix, true);
    }
	
	//图片旋转
	public static Bitmap rotate(Bitmap b, int degrees) {
		if (degrees != 0 && b != null) {
			Matrix m = new Matrix();
			m.setRotate(degrees);
			try {
				Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b
						.getHeight(), m, true);
				if (b != b2) {
					b = b2;
				}
			} catch (OutOfMemoryError ex) {
				ex.printStackTrace();
			}
		}
		return b;
	}
	
	//һ����ת
	public static List<Bitmap> rotateList(List<Bitmap> bitmaps) {
		List<Bitmap> bits = new ArrayList<Bitmap>();
		for(Bitmap b : bitmaps){
			bits.add(Util.rotate(b, -90));
		}
		return bits;
	}
	public static List<Bitmap> newBitmapList(List<Bitmap> bitmaps,Activity activity) {
		List<Bitmap> bits = new ArrayList<Bitmap>();
		for(Bitmap b : bitmaps){
			if(Util.getWidth(activity)==1){
				bits.add(Util.newBitmap(b, 90, 90));
			}else if(Util.getWidth(activity)== 2){
				bits.add(Util.newBitmap(b, 135, 135));
			}
			else if(Util.getWidth(activity)== 3){
				bits.add(Util.newBitmap(b, 60, 60));
			}
			else if(Util.getWidth(activity)== 4){
				bits.add(Util.newBitmap(b, 160, 160));
			}
			else if(Util.getWidth(activity)== 5){
				bits.add(Util.newBitmap(b, 180, 180));
			}
			
		}
		return bits;
	}
	
	
	//根据经纬度获取地址
	public static String GetAddr(double latitude, double longitude) {  
		String addr = "";  
		
		String url = String.format(  
		  "http://ditu.google.cn/maps/geo?output=csv&key=abcdef&q=%s,%s",  
		  latitude, longitude);  
		URL myURL = null;  
		URLConnection httpsConn = null;  
		try {  
			myURL = new URL(url);  
		} catch (MalformedURLException e) {  
			e.printStackTrace();  
		 return null;
		}  
		try {  
		 httpsConn = (URLConnection) myURL.openConnection();  
		 if (httpsConn != null) {  
		  InputStreamReader insr = new InputStreamReader(  
		    httpsConn.getInputStream(), "UTF-8");  
		  BufferedReader br = new BufferedReader(insr);  
		  String data = null;  
		  if ((data = br.readLine()) != null) {  
		   String[] retList = data.split(",");  
		   if (retList.length > 2 && ("200".equals(retList[0]))) {  
		    addr = retList[2];  
		    addr = addr.replace("/", "");  
		   } else {  
		    addr = "";  
		   }  
		  }  
		  insr.close();  
		 }  
		} catch (IOException e) {  
		 e.printStackTrace();  
		 return null;  
		}  
		return addr;  
	}
	
		
	public static String extractUrlWithoutSecret(String url){
		if(url == null || url.equals("")) return null;
		int index1 = url.indexOf("access_token=");
		int index2 = -1;
		if(index1 >= 0){
			index2 = url.indexOf("&", index1);
			if(index2 > index1){
				url = url.replace(url.substring(index1, index2 + 1), "");
			}
		}
		index1 = url.indexOf("timestamp=");
		if(index1 >= 0){
			index2 = url.indexOf("&", index1);
			if(index2 > index1){
				url = url.replace(url.substring(index1, index2 + 1), "");
			}
		}
		index1 = url.indexOf("adIds=");
		if(index1 >= 0){
			index2 = url.indexOf("&", index1);
			if(index2 > index1){
				url = url.replace(url.substring(index1, index2 + 1), "");
			}
		}		
		return url;
	}

    /**
     * logout 数据清理也放在这里，外部直接调用即可
     */
    public static void logout()
	{
        Util.clearData(GlobalDataManager.getInstance().getApplicationContext(), "user");
        Util.clearData(GlobalDataManager.getInstance().getApplicationContext(), "userProfile");
        
		UserBean anonymousUser = (UserBean) loadDataFromLocate(GlobalDataManager.getInstance().getApplicationContext(), "anonymousUser", UserBean.class);
        GlobalDataManager.getInstance().getAccountManager().logout();
		
		GlobalDataManager.getInstance().setPhoneNumber("");
		
		BxMessageCenter.defaultMessageCenter().postNotification(IBxNotificationNames.NOTIFICATION_LOGOUT, anonymousUser);
	}

	public static boolean isPushAlreadyThere(Context ctx, String pushCode){
		if(ctx == null) return true;
		if(pushCode == null || pushCode.equals("")) return false;
		byte[] objCode = Util.loadData(ctx, "pushCode");//Util.loadDataFromLocate(ctx, "pushCode", String.class);
		if(objCode != null){
			String code = new String(objCode);
			try{
				return Integer.valueOf(pushCode) <= Integer.valueOf(code);
			}catch(Throwable e){
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}
	
	public static String getVersion(Context ctx){
		PackageManager packageManager = ctx.getPackageManager();
		// getPackageName()是你当前类的包名，0代表是获取版本信息
		PackageInfo packInfo;
		try {
			packInfo = packageManager.getPackageInfo(ctx.getPackageName(), 0);
			return packInfo.versionName;
		} catch (NameNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return "";
	}
	
	
	private static String PREF_DEVICE_ID = "bx_share_deviceID";
	private static String PREF_KEY_DEVICE_ID = "pref_key_device";
	private static String DEVICE_ID = "";
	
    static public String getDeviceUdid(Context context) {
    	
    	/**
    	 * Firstly, check if memory exists. 
    	 */
    	if (DEVICE_ID != null && DEVICE_ID.length() > 0)
    	{
    		return DEVICE_ID;
    	}
    	
    	/**
    	 * Then, check if we have saved the preference.
    	 */
    	SharedPreferences pref = context.getSharedPreferences(PREF_DEVICE_ID, Context.MODE_PRIVATE);
    	if (pref != null && pref.contains(PREF_KEY_DEVICE_ID))
    	{
    		DEVICE_ID = pref.getString(PREF_KEY_DEVICE_ID, null);
    	}
    	
    	if (DEVICE_ID != null && DEVICE_ID.length() > 0)
    	{
    		return DEVICE_ID;
    	}

    	/**
    	 * And last, get ANDROID_ID or device id, or random id if we cannot get any unique id from android system. 
    	 */
    	try
    	{
    		DEVICE_ID = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
    		
    		/**
    		 * --> 9774d56d682e549c is an android system bug.
    		 * --> null or "null" means cannot get android id.
    		 */
    		if ("9774d56d682e549c".equals(DEVICE_ID) || DEVICE_ID == null || "null".equalsIgnoreCase(DEVICE_ID.trim())) {
    			final String deviceId = ((TelephonyManager) context.getSystemService( Context.TELEPHONY_SERVICE )).getDeviceId();
    			String uuid = deviceId!=null && !"null".equalsIgnoreCase(deviceId.trim()) ? UUID.nameUUIDFromBytes(deviceId.getBytes("utf8")).toString() : UUID.randomUUID().toString();
    			
    			DEVICE_ID = uuid;
    			return uuid;
    		}
    	}
    	catch(Throwable t)
    	{
    		DEVICE_ID = System.currentTimeMillis() + ""; //If any exception occur, use system current time as unique id.
    		t.printStackTrace();
    	}
    	finally
    	{
    		if (pref != null)
    		{
    			pref.edit().putString(PREF_KEY_DEVICE_ID, DEVICE_ID).commit(); 
    		}
    	}
    	
    	return DEVICE_ID;

    }	
    
    static public void makeupUserInfoParams(UserBean user, List<String> params){
		if(user != null && params != null){
			params.add("mobile=" + user.getPhone());
			params.add("userToken=" + generateUsertoken(user.getPassword()));
		}    	
    }
    
    static public String generateUsertoken(String password){
    		String password1 = Communication.getMD5(password.trim());
		password1 += Communication.apiSecret;
		return Communication.getMD5(password1);
    }
}

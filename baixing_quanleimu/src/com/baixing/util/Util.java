//liuchong@baixing.com
package com.baixing.util;

import java.io.BufferedReader;
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
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Pair;
import android.view.Display;

import com.baixing.data.GlobalDataManager;
import com.baixing.entity.UserBean;
import com.baixing.message.BxMessageCenter;
import com.baixing.message.IBxNotificationNames;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.tencent.mm.algorithm.Base64;
public class Util {
	
	public static final String TAG = "QLM";
	
	private static String[] keys;
	private static String[] values;
	
	public static String qq_access_key="";
	public static String qq_access_secret="";
	
//	private static String currentUserId;
	
	public static boolean isLoggable() {
		return new File(Environment.getExternalStorageDirectory()
				+ "/baixing_debug_log_crl.dat").exists();
	}

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
	
	public static void deleteDataFromLocate(Context context, String file){
		if(file != null && !file.equals("") && file.charAt(0) != '_'){//ForArray
			file = "_" + file;
		}
		context.deleteFile(file);
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
	
	public static boolean isExternalStorageWriteable() {
		String state = Environment.getExternalStorageState();
		
		return Environment.MEDIA_MOUNTED.equals(state);
	}
	
	public static boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		
		return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
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
		
//		GlobalDataManager.getInstance().setPhoneNumber("");
		
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
    
    public static String getDevicePhoneNumber(){
        TelephonyManager mTelephonyMgr = (TelephonyManager)GlobalDataManager.getInstance().getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        String number = null;
        if(mTelephonyMgr != null){
        	number = mTelephonyMgr.getLine1Number();
        	if(number != null && number.length() >= 11){
        		number = number.substring(number.length() - 11);
        	}
        }
        return number;
    }
    
    public static boolean isValidMobile(String mobile){
    	Pattern p = Pattern.compile("^(13[0-9]|15[0|3|6|7|8|9]|18[8|9|6])\\d{8}$");
        Matcher matcher = p.matcher(mobile);  
        System.out.println(matcher.matches() + "---");
        return matcher.matches();    	
    }
    
    static private byte[] decript(byte[] encryptedData, byte[] key)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
	    Cipher c = Cipher.getInstance("AES/ECB/ZeroBytePadding");
	    SecretKeySpec k = new SecretKeySpec(key, "AES");
	    c.init(Cipher.DECRYPT_MODE, k);
	    return c.doFinal(encryptedData);
    }
    
    static public String getDecryptedPassword(String encryptedPwd){
		try{
			String key = "c6dd9d408c0bcbeda381d42955e08a3f";
			key = key.substring(0, 16);
			byte[] pwd = decript(Base64.decode(encryptedPwd), key.getBytes("utf-8"));
			String str = new String(pwd);
			return str;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
    }
}

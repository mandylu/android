package com.quanleimu.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;

public class Helper {

	/**
	 * 
	 * @author henry_young
	 * @throws IOException
	 * 
	 * @保存方式：Stream 数据流方式
	 * 
	 *              writeUTF(String str); 但是用Data包装后就会支持。
	 * 
	 * @操作模式: Context.MODE_PRIVATE：新内容覆盖原内容
	 * 
	 *        Context.MODE_APPEND：新内容追加到原内容后
	 * 
	 *        Context.MODE_WORLD_READABLE：允许其他应用程序读取
	 * 
	 *        Context.MODE_WORLD_WRITEABLE：允许其他应用程序写入，会覆盖原数据。
	 */

	// 数据保存SD卡
	public static String saveDataToSdCard(String path, String file,
			Object object) {
		String res = null;
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		if (Environment.getExternalStorageState() != null) {
			try {
				File p = new File("/sdcard/" + path); // 创建目录
				File f = new File("/sdcard/" + path + "/" + file + ".txt"); // 创建文件
				if (!p.exists()) {
					p.mkdir();
				}
				if (!f.exists()) {
					f.createNewFile();
				}
				fos = new FileOutputStream(f);
				oos = new ObjectOutputStream(fos);
				oos.writeObject(object);
				System.out.println(fos + "fos");
				System.out.println(oos + "oos");
			} catch (FileNotFoundException e) {
				res = "没有找到文件";
				e.printStackTrace();
			} catch (IOException e) {
				res = "没有数据";
				e.printStackTrace();
			} finally {
				try {
					oos.close();
					fos.close();
					res = "保存成功";
				} catch (IOException e) {
					res = "没有数据";
					e.printStackTrace();
				}
			}
		}
		return res;
	}

	// 从SD卡读取数据
	public static Object loadDataFromSdCard(String path, String file) {
		Object obj = null;
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		if (Environment.getExternalStorageState() != null) {
			try {
				fis = new FileInputStream("/sdcard/" + path + "/" + file
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
					ois.close();
					fis.close();
				} catch (Exception e) {
					obj = null;
					e.printStackTrace();
				}
			}
		}
		return obj;
	}

	// 将数据保存到手机内存中
	public static String saveDataToLocate(Context context, String file,
			Object object) {
		if(file != null && !file.equals("") && file.charAt(0) != '_'){
			file = "_" + file;
		}
		String res = null;
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			fos = context.openFileOutput(file, Activity.MODE_PRIVATE);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(object);
		} catch (FileNotFoundException e) {
			res = "没有找到文件";
		} catch (IOException e) {
			res = "没有数据";
		} finally {
			try {
				if(oos != null)
				{
					oos.close();
				}
				if(fos != null)
				{
					fos.close();
				}
				
				res = "保存成功";
			} catch (IOException e) {
				res = "没有数据";
			}
		}
		return res;
	}

	// 将数据从手机内存中读出来
	public static Object loadDataFromLocate(Context context, String file) {
		if(file != null && !file.equals("") && file.charAt(0) != '_'){
			file = "_" + file;
		}
		Object obj = null;
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try {
			fis = context.openFileInput(file);
			ois = new ObjectInputStream(fis);
			obj = ois.readObject();

		} catch (FileNotFoundException e) {
			System.out.println("文件没有找到");
			obj = null;
		} catch (IOException e) {
			System.out.println("输入输出错误");
			obj = null;
		} catch (ClassNotFoundException e) {
			System.out.println("类型没有找到");
			obj = null;
		} catch (Exception e) {
			System.out.println("异常");
			obj = null;
		} finally {
			try {
				if(ois != null)
				{
					ois.close();
				}
				if(fis != null)
				{
					fis.close();
				}
			} catch (Exception e) {
				obj = null;
			}
		}
		return obj;
	}
	
	public static Bitmap addBorder(Bitmap bitmap, int borderWidth){
		if(bitmap == null) return null;
		Bitmap dest = Bitmap.createBitmap(bitmap.getWidth() + 2 * borderWidth, bitmap.getHeight() + 2 * borderWidth, bitmap.getConfig());
		Rect rec = new Rect(borderWidth, borderWidth, bitmap.getWidth(), bitmap.getHeight());
		Canvas canvas = new Canvas(dest);
		canvas.drawColor(Color.WHITE);
		canvas.drawBitmap(bitmap, null, rec, null);
		return dest;
	}

	//图片圆角
	public static Bitmap toRoundCorner(Bitmap bitmap, float pixels) {
		return bitmap;
		/*Bitmap output = null;
		if(bitmap!=null){
			try {
				output = Bitmap.createBitmap(bitmap.getWidth(),
						bitmap.getHeight(), Config.ARGB_4444);
			} catch (Exception e) {
				return null;
			}
			Canvas canvas = new Canvas(output);
			final int color = 0xff424242;
			final Paint paint = new Paint();
			final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
			final RectF rectF = new RectF(rect);
			final float roundPx = pixels;
			paint.setAntiAlias(true);
			canvas.drawARGB(0, 0, 0, 0);
			paint.setColor(color);
			canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
			paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
			canvas.drawBitmap(bitmap, rect, rect, paint);
		}
		return output;*/
	}
	public static Drawable bitmap2Drawable(Bitmap bitmap) {
		return new BitmapDrawable(bitmap);
	}
}

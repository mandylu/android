package com.quanleimu.activity.test;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.test.AndroidTestCase;
import android.util.Pair;

import com.baixing.data.GlobalDataManager;
import com.baixing.entity.BXLocation;
import com.baixing.entity.Ad;
import com.baixing.entity.UserBean;
import com.baixing.util.Util;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SaveLoadFileTest extends AndroidTestCase {
	public void setUp()
	{
		try {
			super.setUp();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void testSaveAndLoadSer()
	{
		//First check 
		
		String tmpDir = System.currentTimeMillis() + "";
		
		String ss = new String("abcd");
		String path = Util.saveSerializableToPath(getContext(), tmpDir, "abc.ser", ss);
		assertNotNull(path);
		assertTrue(new File(path).exists());
		
		List<String> files = Util.listFiles(getContext(), tmpDir);
		assertEquals(1, files.size());
		assertEquals(files.get(0), path);
		
		String ssResult = (String) Util.loadSerializable(path);
		assertEquals(ssResult, ss);
		
		File f = new File(path);
		f.delete();
	}
	
	public void testSaveAndLoadString()
	{
		String tmpDir = System.currentTimeMillis() + "";
		
		String ss = new String("abcd");
		String path = Util.saveDataToFile(getContext(), tmpDir, "abc.dat", ss.getBytes());
		assertNotNull(path);
		assertTrue(new File(path).exists());
		
		String path2 = Util.saveDataToFile(getContext(), tmpDir, "cd.dat", ss.getBytes());
		assertNotNull(path2);
		assertTrue(new File(path2).exists());
		
		List<String> files = Util.listFiles(getContext(), tmpDir);
		assertEquals(2, files.size());
		assertEquals(files.get(0), path);
		assertEquals(files.get(1), path2);
		
		String ssResult = new String(Util.loadData(path));
		assertEquals(ssResult, ss);
		
		File f = new File(path);
		f.delete();
	}
	
	public void testSaveAndLoadToLocate()
	{
		UserBean userBean = new UserBean();
		userBean.setId("123");
		userBean.setPassword("123", false);
		userBean.setPhone("123456");
		
		final String fileName = System.currentTimeMillis() + ".json";
		String retValue = Util.saveDataToLocate(getContext(), fileName, userBean);
		assertEquals("保存成功", retValue);
		
		UserBean resultBean = (UserBean) Util.loadDataFromLocate(getContext(), fileName, UserBean.class);
		assertNotNull(resultBean);
		assertEquals(userBean.getId(), resultBean.getId());
		assertEquals(userBean.getPassword(), resultBean.getPassword());
		assertEquals(userBean.getPhone(), resultBean.getPhone());
		
	}
	
	public void testSaveAndLoadLocation()
	{
		BXLocation location = new BXLocation(true);
		final String fileName = System.currentTimeMillis() + ".json";
		
		String retValue = Util.saveDataToLocate(getContext(), fileName, location);
		assertEquals("保存成功", retValue);
		
		BXLocation locationLoad = (BXLocation) Util.loadDataFromLocate(getContext(), fileName, BXLocation.class);
		assertNotNull(locationLoad);
		assertEquals(location.address, locationLoad.address);
		assertEquals(location.cityName, locationLoad.cityName);
	}
	
	public void testSaveAndLoadJson()
	{
		String json = "{abc:123}";
		long timestamp = System.currentTimeMillis()/1000;
		final String fileName = System.currentTimeMillis() + ".json";
		
		Util.saveJsonAndTimestampToLocate(getContext(), fileName, json, timestamp);
		
		Pair<Long, String> p = Util.loadJsonAndTimestampFromLocate(getContext(), fileName);
		assertNotNull(p);
		assertEquals(p.first.longValue(), timestamp);
	}
	
	public void testSaveAndLoadArray()
	{
		List<String> arrayList = new ArrayList<String>();
		for (int i=0; i<10; i++)
		{
			arrayList.add(String.valueOf(i));
		}
		final String fileName = System.currentTimeMillis() + ".json";
		Util.saveDataToLocate(getContext(), fileName, arrayList);
		
		String[] result = (String[]) Util.loadDataFromLocate(getContext(), fileName, String[].class);
		assertNotNull(result);
		for (int i=0; i<10; i++)
		{
			assertEquals(arrayList.get(i), result[i]);
		}
	}
	
	public void testSaveAndLoadFav()
	{
		GlobalDataManager.context = new WeakReference<Context>(getContext());
		
		final String fileName = System.currentTimeMillis()+ "";
		
		final int count = 5;
		Ad[] detail = new Ad[count];
		for (int i=0; i<count; i++)
		{
			detail[i] = new Ad();
			detail[i].setDistance(i);
			GlobalDataManager.getInstance().addFav(detail[i]);
		}

		List<Ad> list = GlobalDataManager.getInstance().getListMyStore();
		assertEquals(count, list.size());
		Util.saveDataToLocate(getContext(), fileName, list);

		
		Ad[] result = (Ad[]) Util.loadDataFromLocate(getContext(), fileName, Ad[].class);
		assertEquals(count, result.length);
		
		for (int i=0; i<count;i++)
		{
			assertEquals(list.get(i), result[i]);
			assertEquals(list.get(i).getDistance(), result[i].getDistance());
		}
	}
	
}

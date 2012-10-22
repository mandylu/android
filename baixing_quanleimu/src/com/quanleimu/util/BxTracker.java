package com.quanleimu.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

//单例类
public class BxTracker {
	private static final String SERIALIZABLE_TRACKDATA_PREFIX = "bx_trackdata";//记录文件
	private static final String SERIALIZABLE_TRACKDATA_SUFFIX = ".ser";
	
	private static final String SERIALIZABLE_TRACK_MANIFEST = "bx_track_manifest.ser";//花名册存放的文件
	
	private List<BxTrackData> dataList = new ArrayList<BxTrackData>();
	private BxSender sender = null;
	private List<String> manifest = new ArrayList<String>();//花名册
	
	private static BxTracker instance;
	
	//Constructor
	private BxTracker()
	{
		
	}
	
	public BxSender getBxSender() {
		return sender;
	}
	
	public void initialize(Context context, BxSender sender) {
		this.sender = sender;
		
		Object obj = Util.loadDataFromLocate(context, SERIALIZABLE_TRACK_MANIFEST);
		if (obj != null) {
			manifest = (ArrayList<String>)obj;//获取花名册
			if (manifest.size() > 0) {
				synchronized (this.sender) {
					this.sender.setFileFlag(true);
					this.sender.notifyAll();
				}
			}
		}
	}
	
	public List<String> getManifest() {
		return manifest;
	}

	public static BxTracker getInstance()
	{
		if (instance == null)
		{
			instance = new BxTracker();
		}
		return instance;
	}
	
	//需要记录时调用,参数为BxTrackData对象
	public void addTrackData(Context context, BxTrackData trackobj)
	{
		dataList.add(trackobj);
		if (dataList.size()>0) {//设置100次,写一组记录
			saveToLocal(context, dataList);
			clearDataList();
		}
	}
	
	public void clearDataList()
	{
		dataList.clear();
	}
	
	public int size()
	{
		return dataList.size();
	}
	
	public String saveToLocal(Context context, List<BxTrackData> dataList)
	{
		String filePath = SERIALIZABLE_TRACKDATA_PREFIX + System.currentTimeMillis()/1000 + SERIALIZABLE_TRACKDATA_SUFFIX;
		String result = Util.saveDataToLocate(context,  filePath , dataList);
		synchronized (sender) {
			manifest.add(filePath);
			result += "," +Util.saveDataToLocate(context, SERIALIZABLE_TRACK_MANIFEST, manifest);
			sender.setFileFlag(true);
			sender.notifyAll();//唤醒sender
		}
		return result;
	}
	
}

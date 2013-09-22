package com.baixing.util.post;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Bitmap;

import com.baixing.data.GlobalDataManager;
import com.baixing.imageCache.ImageLoaderManager;
import com.baixing.imageCache.ImageLoaderManager.DownloadCallback;
import com.baixing.network.api.FileUploadCommand;
import com.baixing.tracking.TrackConfig.TrackMobile.BxEvent;
import com.baixing.tracking.TrackConfig.TrackMobile.Key;
import com.baixing.tracking.TrackConfig.TrackMobile.Value;
import com.baixing.tracking.Tracker;
import com.baixing.util.BitmapUtils;

public class ImageUploader implements DownloadCallback {
	
	public static interface Callback {
		public void onUploadDone(String imagePath, String serverUrl, Bitmap thumbnail);
		public void onUploading(String imagePath, Bitmap thumbnail);
		public void onUploadFail(String imagePath, Bitmap thumbnail);
	}
	
	private class StateImage {
		public String imagePath;
		public String serverUrl;
		public ImageState state = ImageState.FAIL;
		public Bitmap thumbnail;
		
		public List<Callback> callbacks = new ArrayList<ImageUploader.Callback>();
	}
	
	private enum ImageState{
		SYNC, // upload to server.
		FAIL, //Fail upload to server.
		UPLOADING,
		DOWNLOADING
	}
	
	private ImageUploader() {}
	
	private static ImageUploader instance;
	private List<StateImage> imageList = new ArrayList<StateImage>();
	private Activity activity;
	public static ImageUploader getInstance() {
		if (instance == null) {
			instance = new ImageUploader();
		}
		
		return instance;
	}
	
	public void attachActivity(Activity activity) {
		this.activity = activity;
	}
	
	private StateImage findImage(String imagePath) {
		synchronized (imageList) {
			for (StateImage img : imageList) {
				if (img.imagePath.equals(imagePath)) {
					return img;
				}
			}
		}
		
		return null;
	}
	
	public void removeCallback(Callback callback) {
		if (callback == null) {
			return;
		}
		synchronized (imageList) {
			for (StateImage img : imageList) {
				for (int i=0; i<img.callbacks.size();) {
					Callback callbackW = img.callbacks.get(i);
					if (callback.equals(callbackW)) {
						img.callbacks.remove(i);
					}
					else
					{
						i++;
					}
				}
			}
		}
	}
	
	
	public boolean hasPendingJob() {
		for (StateImage img : this.imageList) {
			if (img.state != ImageState.SYNC && img.state != ImageState.FAIL) {
				return true;
			}
		}
		
		return false;
	}
	
	public List<String> getServerUrlList() {
		ArrayList<String> array = new ArrayList<String>();
		for (StateImage img : this.imageList) {
			if (img.state == ImageState.SYNC) {
				array.add(img.serverUrl);
			}
		}
		
		return array;
	}
	
	public void clearAll() {
		for (StateImage img : this.imageList) {
			img.callbacks.clear();
		}
		
		imageList.clear();
	}
	
	public void addDownloadImage(String thumbnailUrl, String url, Callback callback) {
		if (this.findImage(thumbnailUrl) != null) {
			return;
		}
		
		StateImage image = null;
		synchronized (imageList) {
			image = new StateImage();
			image.imagePath = thumbnailUrl;
			image.serverUrl = url;
			image.state = ImageState.DOWNLOADING;
			if (callback != null) {
				image.callbacks.add(callback);
			}
			imageList.add(image);
		}
		
		GlobalDataManager.getInstance().getImageLoaderMgr().loadImg(this, thumbnailUrl);
	}
	
	/**
	 * start to upload the specified image to server.  
	 */
	public void startUpload(String imagePath, Bitmap thumbnail, Callback callback) {
		
		if (this.findImage(imagePath) != null) { //If the specified image is uploading.
			return;
		}
		
		StateImage image = null;
		synchronized (imageList) {
			image = new StateImage();
			image.imagePath = imagePath;
			image.thumbnail = thumbnail;
			if (callback != null) {
				image.callbacks.add(callback);
			}
			imageList.add(image);
		}
		
		appendJob(image);
	}
	
	public Bitmap getThumbnail(String imagePath) {
		StateImage img = this.findImage(imagePath);
		if (img == null) {
			return null;
		}
		
		return img.thumbnail;
	}
	
	/**
	 * Listening the uploading state of the specified image.
	 * @param imagePath
	 * @param callback
	 */
	public void registerCallback(String imagePath, Callback callback) {
		boolean isNewTask = false;
		StateImage image = null;
		synchronized (imageList) {
			image = this.findImage(imagePath);
			if (image == null) {
				isNewTask = true;
				image = new StateImage();
				image.imagePath = imagePath;
				if (callback != null) {
					image.callbacks.add(callback);
				}
				imageList.add(image);
			}
			else
			{
				if (callback != null && !image.callbacks.contains(callback)) {
					image.callbacks.add(callback);
				}
				notifyState(callback, image);
			}
		}
		
		if (isNewTask) {
			appendJob(image);
		}
	}
	
	
	
	private void appendJob(final StateImage img) {
		Thread t = new Thread(new Runnable() { //TODO: check if we need thread pool?
			public void run() {

				if (img.thumbnail == null) {
					img.thumbnail = BitmapUtils.createThumbnail(img.imagePath, 200, 200);//FIXME: width, height 
				}
				
				img.state = ImageState.UPLOADING; //Set state
				notifyState(img);
				
				if(img.imagePath != null){
					String result = null;
					String url = null;
					String failReason = null;
					long timeInMill = 0;
					try {
						
						long startTime = System.currentTimeMillis();
						result = FileUploadCommand.create(img.imagePath).doUpload(activity);//new ImageUploadCommand(img.imagePath).doUpload();//Communication.uploadPicture(currentBmp);
						timeInMill = System.currentTimeMillis() - startTime;
						
						JSONObject obj = new JSONObject(result);
						url = obj.getString("url");
						if(url == null) {
							failReason = "url of json string in response is null";
						}
						
					} catch (Throwable t) {
						//Fail
					} finally {
						Tracker.getInstance().event(BxEvent.POST_IMAGEUPLOAD)
						.append(Key.RESULT, url != null ? Value.YES : Value.NO)
						.append(Key.FAIL_REASON, failReason)
						.append(Key.SIZEINBYTES, new File(img.imagePath).length())
						.append(Key.UPLOADSECONDS, timeInMill / 1000.0)
						.end();
					}
					
					if (url != null) {
						img.state = ImageState.SYNC;
						img.serverUrl = url;
						notifyState(img);
					}
					else {
						img.state = ImageState.FAIL;
						notifyFail(img);
					}
				}else{
					img.state = ImageState.FAIL;
					notifyFail(img);
				}
			}
		});
		t.start();
	}
	
	private void notifyState(Callback callback, StateImage img) {
		switch (img.state) {
		case UPLOADING:
		case DOWNLOADING:
			callback.onUploading(img.imagePath, img.thumbnail);
			break;
		case SYNC:
			callback.onUploadDone(img.imagePath, img.serverUrl, img.thumbnail);
			break;
		case FAIL:
			callback.onUploadFail(img.imagePath, img.thumbnail);
			break;
		default:
			break;
		}
	}
	
	private void notifyState(StateImage img) {
		for (Callback callbackE : img.callbacks) {
			Callback callback = callbackE;
			if (callback != null) {
				notifyState(callback, img);
			}
		}
	}
	
	public void cancel(String imagePath) {
		StateImage img = null;
		synchronized (imageList) {
			img = this.findImage(imagePath);
			if (img != null) {
				imageList.remove(img);
			}
		}
		
		if (img != null) {
			notifyFail(img);
		}
	}
	
	private void notifyFail(StateImage img) {
		for (Callback callback : img.callbacks) {
			if (callback != null) {
				callback.onUploadFail(img.imagePath, img.thumbnail);
			}
		}
	}

	@Override
	public void onFail(String url) {
		StateImage img = this.findImage(url);
		if (img != null) {
			img.state = ImageState.FAIL;
			this.notifyFail(img);
		}
	}

	@Override
	public void onSucced(String url, Bitmap bp) {
		StateImage img = this.findImage(url);
		if (img != null) {
			img.state = ImageState.SYNC;
			img.thumbnail = bp.copy(bp.getConfig(), true);
			this.notifyState(img);
		}
	}
	
}
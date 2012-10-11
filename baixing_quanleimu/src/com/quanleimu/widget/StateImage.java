package com.quanleimu.widget;

import java.io.File;
import java.io.Serializable;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.quanleimu.activity.R;
import com.quanleimu.imageCache.SimpleImageLoader;
import com.quanleimu.util.BitmapUtils;
import com.quanleimu.util.UploadImageCommand;
import com.quanleimu.util.UploadImageCommand.ProgressListener;

public class StateImage implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -629866660748357052L;
	
	public static interface UploadListener
	{
		public void onSucced(String serverPath);
		public void onFailed();
	}
	
	public enum ImageStatus{
		ImageStatus_SYNC, // upload to server.
		ImageStatus_DEFAULT, //
		ImageStatus_LOCAL, //Fail upload to server.
		ImageStatus_UPLOADING
	}
	
	private int defaultRes;
	
	private transient Bitmap defaultImg;
	
	private String localImage;
	
	private String serverImage;
	
	private transient Context context;
	
	private transient WeakReference<UploadListener> uploadListener;
	
	private ImageStatus status;
	
	private transient WeakReference<ImageView> attachedView;
	
	public StateImage(int defaultImgRes)
	{
		this.defaultRes = defaultImgRes;
		status = ImageStatus.ImageStatus_DEFAULT;
	}
	
	public void setDefault(int defaultImgRes)
	{
		this.defaultRes = defaultImgRes;
		
		updateImage();
	}
	
	public void setContext(Context context)
	{
		this.context  = context;
	}
	
	public void assignLocalImage(String localImgPath)
	{
		String newUri = localImgPath;
		if (newUri == null || !new File(newUri).exists())
		{
			return;
		}
		
//		final boolean changed = this.localImage != null && newUri.equals(this.localImage);
		this.localImage = localImgPath;//Uri.parse(localImgPath);
//		if (changed)
//		{
			changeStatus(ImageStatus.ImageStatus_LOCAL);
//		}
	}
	
	public void assignServerImage(String serverImgPath)
	{
		this.serverImage = serverImgPath;//Uri.parse(serverImgPath);
		if (this.localImage == null) //If we have local image to upload, do not change the status.
		{
			changeStatus(ImageStatus.ImageStatus_SYNC);
		}
	}
	
	public void resumeUpload(UploadListener listener)
	{
		if (status ==ImageStatus.ImageStatus_LOCAL)
		{
			startUpload(listener);
		}
	}
	
	private void changeStatus(ImageStatus newStatue)
	{
		synchronized (this) {
			status = newStatue;
		}
		
		updateImage();
	}
	
	public void attachView(ImageView view)
	{
		attachedView = new WeakReference<ImageView>(view);
		
		updateImage();
	}
	
	private void updateImage()
	{
		final ImageView imageView = attachedView == null ? null : attachedView.get();
		if (imageView == null)
		{
			return;
		}
		
		imageView.post(new Runnable() {

			@Override
			public void run() {
				switch(status)
				{
				case ImageStatus_DEFAULT:
					imageView.setImageResource(defaultRes);
					break;
				case ImageStatus_UPLOADING:
					imageView.setImageResource(R.drawable.u);
					break;
				case ImageStatus_SYNC:
					SimpleImageLoader.showImg(imageView, serverImage.toString(), null, imageView.getContext());
					break;
				case ImageStatus_LOCAL:
					if (defaultImg != null)
					{
						imageView.setImageBitmap(defaultImg);
					}
					else
					{
						Bitmap bp = BitmapUtils.createThumbnail(localImage.toString(), imageView.getWidth(), imageView.getHeight());
						if (bp != null)
						{
							defaultImg = bp;
							imageView.setImageBitmap(bp);
						}
					}
					break;
				}
			}
			
		});
	}
	
	public void cancelUpload()
	{
		synchronized (this) {
			this.uploadListener = null;
			
			if (status == ImageStatus.ImageStatus_UPLOADING)
			{
				changeStatus(ImageStatus.ImageStatus_LOCAL);
			}

		}
	}
	
	private boolean isLocalExists()
	{
		return localImage != null && new File(localImage).exists();
	}
	
	public void startUpload(UploadListener listener)
	{
		
		if (this.localImage == null)
		{
			return;
		}
		
		synchronized (this) {
			this.uploadListener = new WeakReference<StateImage.UploadListener>(listener);
		}
		
		new UploadImageCommand(context, localImage.toString()).startUpload(new ProgressListener() {
			public void onStart(String imagePath) {
				changeStatus(ImageStatus.ImageStatus_UPLOADING);
			}

			public void onCancel(String imagePath) {
				synchronized (StateImage.this) {
					if (isLocalExists())
					{
						changeStatus(ImageStatus.ImageStatus_LOCAL);
					}
					else
					{
						changeStatus(serverImage == null ? ImageStatus.ImageStatus_DEFAULT : ImageStatus.ImageStatus_SYNC);
					}
				}
				UploadListener listener = uploadListener.get();
				if (listener != null)
				{
					listener.onFailed();
				}
			}

			public void onFinish(Bitmap bmp, final String imagePath) {
				synchronized (StateImage.this) {
					serverImage = imagePath;//Uri.parse(imagePath);
					localImage = null; //Clear local image reference
					changeStatus(ImageStatus.ImageStatus_SYNC);
				}
				
				UploadListener listener = uploadListener.get();
				if (listener != null)
				{
					listener.onSucced(imagePath);
				}
			}
			
		});
	}
	
	
	public String getLocalUri()
	{
		return this.localImage;
	}
	
	public String getServerUri()
	{
		return this.serverImage;
	}
}

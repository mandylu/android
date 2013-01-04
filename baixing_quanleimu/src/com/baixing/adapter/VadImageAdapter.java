package com.baixing.adapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.baixing.data.GlobalDataManager;
import com.baixing.imageCache.ImageCacheManager;
import com.quanleimu.activity.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class VadImageAdapter extends BaseAdapter {
	Context context;
	List<String> listUrl;
	private int pageIndex;
	private WeakReference<Bitmap> mb_loading = null;
	private IImageProvider provider;
	
	public static interface IImageProvider {
		public void onShowView(ImageView imageView, String url, String previousUrl, final int index);
	} 
	
	public VadImageAdapter(Context context, List<String> listUrl, int detailPostion, IImageProvider imageLoader) {
		this.context = context;
		this.listUrl = listUrl;
		pageIndex = detailPostion;
		this.provider = imageLoader;
		
		mb_loading = new WeakReference<Bitmap>(ImageCacheManager.getInstance().loadBitmapFromResource(R.drawable.icon_vad_loading));
	}
	
	public void setContent(List<String> listUrl){
		this.listUrl = listUrl;
	}

	@Override
	public int getCount() {
		return listUrl.size();
	}
	
	public List<String> getImages()
	{
		return listUrl;
	}

	@Override
	public Object getItem(int arg0) {
		return listUrl.get(arg0);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View root = convertView;
		if (root == null)
		{
			root = LayoutInflater.from(context).inflate(R.layout.item_detailview, null);
		}
		ImageView iv = (ImageView) root.findViewById(R.id.ivGoods);
		iv.setImageBitmap(mb_loading.get());
		
		if (listUrl.size() != 0 && listUrl.get(position) != null && !listUrl.get(position).equals("")) {
			String prevTag = (String)iv.getTag();
			iv.setTag(listUrl.get(position));
			if (provider != null)
			{
				provider.onShowView(iv, listUrl.get(position), prevTag, pageIndex);
			}
		}
		
		return root;
	}
	
	

}

package com.quanleimu.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;

import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.activity.R;
import com.quanleimu.entity.GoodsDetail;
import com.quanleimu.imageCache.SimpleImageLoader;
import com.quanleimu.util.Communication;
import com.quanleimu.widget.CircleFlowIndicator;
import com.quanleimu.widget.ViewFlow;

public class BigGalleryView extends BaseView implements ViewFlow.ViewSwitchListener, MediaScannerConnectionClient{

	//int index = 0;
	private int postIndex = -1;
	public GoodsDetail goodsDetail;
	public List<String> listUrl = new ArrayList<String>();
	private Bitmap mb;
//	private HashMap<String, byte[]> imageData;
	
	private android.media.MediaScannerConnection scannerConnection = null;
	static private String mediaPath = Environment.getExternalStorageDirectory().getPath()+"/quanleimu/favorites/百姓网收藏图片/";
	private Vector<String> unScannedFiles = new Vector<String>();
	
	protected void Init(){
		QuanleimuApplication.lazyImageLoader.enableSampleSize();
		LayoutInflater inflater = LayoutInflater.from(getContext());
		this.addView(inflater.inflate(R.layout.biggallery, null));
		
		try {
			if(goodsDetail.getImageList().getBig() == null || goodsDetail.getImageList().getBig().equals(""))
			{
				if(null != m_viewInfoListener){
					TitleDef title = getTitleDef();
					title.m_title = "0/0";
					m_viewInfoListener.onTitleChanged(title);
				}
				Toast.makeText(getContext(), "图片未加载成功，请稍后重试", 3).show();
			}
			else
			{
				String b = (goodsDetail.getImageList().getBig()).substring(1, (goodsDetail.getImageList().getBig()).length()-1);
				b = Communication.replace(b);
				if(b.contains(","))
				{
					String[] c = b.split(",");
					for(int i=0;i<c.length;i++) 
					{
						listUrl.add(c[i]);
					}
				}
				else
				{
					listUrl.add(b);
				}
				
				BitmapFactory.Options o =  new BitmapFactory.Options();
                o.inPurgeable = true;
                mb = BitmapFactory.decodeResource(BigGalleryView.this.getResources(),R.drawable.loading_210_black, o);  
                
				ViewFlow vfCoupon = (ViewFlow)findViewById(R.id.vfCoupon);
				GalleryImageAdapter adapter = new GalleryImageAdapter(getContext(), listUrl);
				vfCoupon.setOnViewLazyInitializeListener(adapter);
				vfCoupon.setOnViewSwitchListener(this);
				vfCoupon.setAdapter(adapter, postIndex);

				CircleFlowIndicator indic = (CircleFlowIndicator) findViewById(R.id.viewflowindic);
				vfCoupon.setFlowIndicator(indic); 
			}
		} catch (Exception e) {
			
		}
	}
	
	public BigGalleryView(Context context, Bundle bundle){
		super(context);
		
		postIndex = bundle.getInt("postIndex");
		goodsDetail = (GoodsDetail) bundle.getSerializable("goodsDetail");
		
		Init();
	}

	
	public TitleDef getTitleDef(){
		TitleDef title = new TitleDef();
		title.m_visible = true;
		title.m_title = (postIndex+1)+"/"+listUrl.size();
		title.m_leftActionHint = "返回";
		
		title.m_rightActionHint = "保存";
		return title;
	}
	public TabDef getTabDef(){
		TabDef tab = new TabDef();
		tab.m_visible = false;
		return tab;
	}
	
    @Override
    public void onDestroy()
    {      
    	super.onDestroy();
    	
        ((ViewFlow)findViewById(R.id.vfCoupon)).finalize();
        
        //imageData = null;
        goodsDetail = null;
        QuanleimuApplication.lazyImageLoader.disableSampleSize();
  		SimpleImageLoader.Cancel(listUrl);           
        
        if(mb != null)
        {
            mb.recycle();
            mb = null;
        }
    }
    
    @Override
    public void onResume()
    {
    		QuanleimuApplication.lazyImageLoader.enableSampleSize();
	    	if(null == mb){
				BitmapFactory.Options o =  new BitmapFactory.Options();
	            o.inPurgeable = true;
				mb = BitmapFactory.decodeResource(BigGalleryView.this.getResources(),R.drawable.loading_210_black, o);
	    	}
    }
//    
//    @Override
//    public void onPause(){
//    	
//    }
    
    @Override
    public boolean onRightActionPressed(){
    	ViewFlow vfCoupon = (ViewFlow)findViewById(R.id.vfCoupon);
    	String filePath = SimpleImageLoader.getFileInDiskCache(vfCoupon.getSelectedView().getTag().toString());
    	
    	String title = goodsDetail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_TITLE)+postIndex;

        int index = filePath.lastIndexOf("/");
        String fileName = filePath.substring(index+1)+".png";
        ContentValues values = new ContentValues(8);
        String newname = (new SimpleDateFormat("MM月dd日 HH:mm:ss", Locale.SIMPLIFIED_CHINESE)).format(System.currentTimeMillis()).toString();
        values.put(MediaStore.Images.Media.TITLE, newname);//名称，随便
        values.put(MediaStore.Images.Media.DISPLAY_NAME, newname);
        values.put(MediaStore.Images.Media.DESCRIPTION, title);//描述，随便
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());//图像的拍摄时间，显示时根据这个排序
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");//默认为jpg格式
        values.put(MediaStore.Images.Media.ORIENTATION, 0);//
        values.put(MediaStore.Images.Media.IS_PRIVATE, 1);
        
        String mediaFileName = mediaPath+fileName.hashCode()+".png";
        values.put(Images.Media.DATA, mediaFileName);

        // 先得到新的URI
        Uri uri = null;
        try{
        	uri = getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        }catch(Exception e){
        	Toast.makeText(getContext(), "保存失败,请检查SD卡是否可>_<", Toast.LENGTH_LONG).show();
        	return true;
        }

        OutputStream outStream = null;
        InputStream inStream = null;
        
        try {
            outStream = getContext().getContentResolver().openOutputStream(uri);
            inStream = new FileInputStream(new File(filePath));
            
            //new BufferedInputStream(
            byte buffer[] = new byte[1024];
            int nRead = 0;
            while((nRead = inStream.read(buffer)) > 0){
            	outStream.write(buffer, 0, nRead);
            }
            
            //Log.d("BigGalleryView", "uri.path: "+uri.getPath()+", outStream.toString()"+outStream.toString());
            
            if(null == scannerConnection)
            	scannerConnection = new android.media.MediaScannerConnection(getContext(), this);
            
            if(!scannerConnection.isConnected()){
            	unScannedFiles.add(mediaFileName);
            	scannerConnection.connect();
            }else{
            	scannerConnection.scanFile(mediaFileName, "image/png");
            }
            
            Toast.makeText(getContext(), "成功！！您可以到手机相册查看该图 ^_^ \n也可以直接找文件："+mediaFileName, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "保存失败,请检查SD卡是否可>_<", Toast.LENGTH_LONG).show();
        }finally{
        	if(outStream != null){
        		try {
					outStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        	
        	if(inStream != null){
        		try {
					inStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        }
        
    	return true;
    }

    class GalleryImageAdapter extends BaseAdapter implements ViewFlow.ViewLazyInitializeListener
    {
        private Context context;

        private List<String> imageUrls;

        private int position = 0;

 //       private final ExecutorService pool;

        public GalleryImageAdapter(Context c, List<String> imageUrls)
        {
            this.context = c;
            this.imageUrls = imageUrls;

//            imageData = new HashMap<String, byte[]>();
//            pool = Executors.newFixedThreadPool(5);
        }

//        public void loadBitmap(final String url, final ImageView imageView)
//        {
//            final Bitmap bitmap = getBitmapFromCache(url);
//            if (bitmap != null)
//            {
//                imageView.setImageBitmap(bitmap);
//            }
//            else
//            {
//                imageView.setImageBitmap(mb);
//                pool.submit(new Runnable()
//                {
//                    @Override
//                    public void run()
//                    {
//                        byte[] data = downloadBitmap(url);
//                        imageData.put(url, data);
//                        
//                        BitmapFactory.Options o = new BitmapFactory.Options();
//                        o.inPurgeable = true;
//                        final Bitmap tmb = BitmapFactory.decodeByteArray(data, 0, data.length, o);
//                        
//                        ((Activity) context).runOnUiThread(new Runnable()
//                        {
//                            public void run()
//                            {
//                                imageView.setImageBitmap(tmb);
//                            }
//                        });
//                    }
//                });
//            }
//        }
//
//        private byte[] downloadBitmap(String url)
//        {
//            try
//            {
//                HttpClient httpClient = NetworkProtocols.getInstance().getHttpClient();
//
//                HttpPost httpPost = new HttpPost(url);
//                HttpResponse response = httpClient.execute(httpPost);
//
//                InputStream is = response.getEntity().getContent();
//                ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                byte[] buffer = new byte[1024];
//                int length;
//                while((length = is.read(buffer)) != -1)
//                {
//                    bos.write(buffer, 0, length);
//                }
//                httpClient.getConnectionManager().shutdown();
//
//                return bos.toByteArray();
//            }
//            catch (Exception e)
//            {
//                e.printStackTrace();
//            }
//
//            return null;
//        }
//
//        public Bitmap getBitmapFromCache(String url)
//        {
//            if (imageData != null && !imageData.containsKey(url))
//            {
//                return null;
//            }
//
//            byte[] data = imageData.get(url);
//            
//            if(data == null)
//            {
//                return null;
//            }
//            
//            BitmapFactory.Options o = new BitmapFactory.Options();
//            o.inPurgeable = true;
//            Bitmap tmb = BitmapFactory.decodeByteArray(data, 0, data.length, o);
//            return tmb;
//        }

        @Override
        public int getCount()
        {
            return this.imageUrls.size();
        }

        @Override
        public Object getItem(int position)
        {
            return this.position;
        }

        @Override
        public long getItemId(int position)
        {
            return this.position;
        }

        public View getView(int position, View convertView, ViewGroup parent)
        {
        		ImageView imageView = null;
			if (convertView != null) {
				imageView = (ImageView) convertView;
			} else {
				imageView = new ImageView(context);	            
	            imageView.setScaleType(ScaleType.FIT_CENTER);
	            imageView.setLayoutParams(new Gallery.LayoutParams(Gallery.LayoutParams.WRAP_CONTENT, Gallery.LayoutParams.FILL_PARENT));
			}

			if(null == imageView.getTag() || !imageView.getTag().equals(imageUrls.get(position)))
			{	
				imageView.setImageBitmap(mb);
				
			    SimpleImageLoader.showImg(imageView, imageUrls.get(position), BigGalleryView.this.getContext());
	            imageView.setTag(imageUrls.get(position));
			}

            return imageView;

        }
        
    	@Override
    	public void onViewLazyInitialize(View view, int position) {
    		
//    		if(null != view && position >= 0 && position < imageUrls.size()){
//    			ImageView imageView = (ImageView) view;
//    			
//    			if(null == imageView.getTag() || !imageView.getTag().equals(imageUrls.get(position)))
//    			{	
//    				imageView.setImageBitmap(mb);
//    	            SimpleImageLoader.showImg(imageView, imageUrls.get(position), BigGalleryView.this.getContext());
//    	            imageView.setTag(imageUrls.get(position));
//    			}
//    		}		
    	}
    	
    	@Override
    	public void onViewRecycled(View view){
    		
    		if(view instanceof ImageView){
    			//recycle the bitmap referred by the view
    			QuanleimuApplication.lazyImageLoader.forceRecycle((String)(((ImageView)view).getTag()));
    		}
    	}
    }

	@Override
	public void onSwitched(View view, int position) {
		postIndex = position;
		
		if(null != m_viewInfoListener){
			TitleDef title = getTitleDef();
			title.m_title = (position + 1)+"/"+listUrl.size();
			m_viewInfoListener.onTitleChanged(title);
		}
		
		//adjust download sequence
		ArrayList<String> urls = new ArrayList<String>();
		urls.add(listUrl.get(position));
		for(int index = 0; (index + position < listUrl.size() || position - index >= 0); ++index){
			if(index + position < listUrl.size())
				urls.add(listUrl.get(index+position));
			
			if(position - index >= 0)
				urls.add(listUrl.get(position-index));				
		}
		SimpleImageLoader.AdjustPriority(urls);		
	}

	@Override
	public void onMediaScannerConnected() {
		//Log.d("BigGalleryView", "onMediaScannerConnected() !!!");
		
		while(unScannedFiles.size() > 0){
			String mediaFileName = unScannedFiles.remove(0);
			if(mediaFileName != null && mediaFileName.length() > 0){
				scannerConnection.scanFile(mediaFileName, "image/png");
			}
		}
	}

	@Override
	public void onScanCompleted(String path, Uri uri) {
		//Log.d("BigGalleryView", "onScanCompleted(), path: "+path+", uri: "+uri.getPath());
	}

//    @Override
//    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
//    {
//		if(null != m_viewInfoListener){
//			TitleDef title = getTitleDef();
//			title.m_title = (position + 1)+"/"+listUrl.size();
//			m_viewInfoListener.onTitleChanged(title);
//		}
//		
//		//adjust download sequence
//		ArrayList<String> urls = new ArrayList<String>();
//		urls.add(listUrl.get(position));
//		for(int index = 0; (index + position < listUrl.size() || position - index >= 0); ++index){
//			if(index + position < listUrl.size())
//				urls.add(listUrl.get(index+position));
//			
//			if(position - index >= 0)
//				urls.add(listUrl.get(position-index));				
//		}
//		SimpleImageLoader.AdjustPriority(urls);
//    }
//
//    @Override
//    public void onNothingSelected(AdapterView<?> arg0)
//    {
//        // TODO Auto-generated method stub
//        
//    }
}

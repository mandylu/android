package com.quanleimu.activity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.quanleimu.entity.GoodsDetail;
import com.quanleimu.util.Communication;
import com.quanleimu.util.Helper;
import com.quanleimu.util.NetworkProtocols;

public class BigGallery extends BaseActivity implements OnItemSelectedListener{

	int index = 0;
	private TextView tvCount;
	private Button btnClose;
	private int postIndex = -1;
	public GoodsDetail goodsDetail;
	public List<String> listUrl = new ArrayList<String>();
	private Bitmap mb;
	private HashMap<String, byte[]> imageData;
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	@Override
	protected void onResume() {
		super.onResume();
	}
	
    protected void onDestroy()
    {
        super.onDestroy();
        if(mb != null)
        {
            mb.recycle();
        }
        
        imageData = null;
        goodsDetail = null;
    }
    
    public void onClick(View v) {
        if(v.getId() == btnClose.getId())
        {
            BigGallery.this.finish();
        }
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.biggallery);
		super.onCreate(savedInstanceState);
		
		try {
			postIndex = bundle.getInt("postIndex");
			goodsDetail = (GoodsDetail) bundle.getSerializable("goodsDetail");
			
			tvCount = (TextView)findViewById(R.id.tvCount);
			
			btnClose = (Button)findViewById(R.id.btnClose);
			btnClose.setOnClickListener(this);
			
			if(goodsDetail.getImageList().getBig() == null || goodsDetail.getImageList().getBig().equals(""))
			{
				tvCount.setText("0/0");
				Toast.makeText(BigGallery.this, "图片未加载成功，请稍后重试", 3).show();
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
				
				Gallery vfCoupon = (Gallery)findViewById(R.id.vfCoupon);
				vfCoupon.setAdapter(new GalleryImageAdapter(this,listUrl));
				vfCoupon.setOnItemSelectedListener(this);
				vfCoupon.setSelection(postIndex);
//				vfCoupon.setSpacing(20);
				//tvCountNum.setText((vfCoupon.getCurrentView().getId()+1)+"/"+listCup.size());
				BitmapFactory.Options o =  new BitmapFactory.Options();
                o.inPurgeable = true;
				Bitmap tmb = BitmapFactory.decodeResource(BigGallery.this.getResources(),R.drawable.loading_210_black, o);
				mb= Helper.toRoundCorner(tmb, 20);
				tmb.recycle();
				
				tvCount.setText((postIndex+1)+"/"+listUrl.size());
				
			}
		} catch (Exception e) {
			
		}
	}

    class GalleryImageAdapter extends BaseAdapter
    {
        private Context context;

        private List<String> imageUrls;

        private int position = 0;

        private final ExecutorService pool;

        public GalleryImageAdapter(Context c, List<String> imageUrls)
        {
            this.context = c;
            this.imageUrls = imageUrls;

            imageData = new HashMap<String, byte[]>();
            pool = Executors.newFixedThreadPool(5);
        }

        public void loadBitmap(final String url, final ImageView imageView)
        {
            final Bitmap bitmap = getBitmapFromCache(url);
            if (bitmap != null)
            {
                imageView.setImageBitmap(bitmap);
            }
            else
            {
                imageView.setImageBitmap(mb);
                pool.submit(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        byte[] data = downloadBitmap(url);
                        imageData.put(url, data);
                        
                        BitmapFactory.Options o = new BitmapFactory.Options();
                        o.inPurgeable = true;
                        final Bitmap tmb = BitmapFactory.decodeByteArray(data, 0, data.length, o);
                        
                        ((Activity) context).runOnUiThread(new Runnable()
                        {
                            public void run()
                            {
                                imageView.setImageBitmap(tmb);
                            }
                        });
                    }
                });
            }
        }

        private byte[] downloadBitmap(String url)
        {
            try
            {
                HttpClient httpClient = NetworkProtocols.getInstance().getHttpClient();

                HttpPost httpPost = new HttpPost(url);
                HttpResponse response = httpClient.execute(httpPost);

                InputStream is = response.getEntity().getContent();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while((length = is.read(buffer)) != -1)
                {
                    bos.write(buffer, 0, length);
                }
                httpClient.getConnectionManager().shutdown();

                return bos.toByteArray();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            return null;
        }

        public Bitmap getBitmapFromCache(String url)
        {
            if (!imageData.containsKey(url))
            {
                return null;
            }

            byte[] data = imageData.get(url);
            
            if(data == null)
            {
                return null;
            }
            
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inPurgeable = true;
            Bitmap tmb = BitmapFactory.decodeByteArray(data, 0, data.length, o);
            return tmb;
        }

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
            if(convertView instanceof ImageView)
            {
                ((BitmapDrawable)((ImageView)convertView).getDrawable()).getBitmap().recycle();
            }
            ImageView imageView = new ImageView(context);
            imageView.setScaleType(ScaleType.FIT_CENTER);
            imageView.setLayoutParams(new Gallery.LayoutParams(Gallery.LayoutParams.WRAP_CONTENT, Gallery.LayoutParams.FILL_PARENT));
            loadBitmap(imageUrls.get(position), imageView);

            return imageView;

        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        tvCount.setText((position + 1)+"/"+listUrl.size());
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0)
    {
        // TODO Auto-generated method stub
        
    }
}

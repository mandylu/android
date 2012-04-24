package com.quanleimu.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.quanleimu.entity.GoodsDetail;
import com.quanleimu.util.Communication;
import com.quanleimu.util.Helper;

public class BigGallery extends BaseActivity implements OnGestureListener{

	int index = 0;
//	private List<Bitmap> listBigBm = new ArrayList<Bitmap>();
	private TextView tvCount;
	private Button btnClose;
	private GestureDetector myGestureDetector;
	private ViewFlipper vfCoupon;
	private int postIndex = -1;
	public GoodsDetail goodsDetail;
	public List<String> listUrl = new ArrayList<String>();
	
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
				
				vfCoupon = (ViewFlipper)findViewById(R.id.vfCoupon);
				myGestureDetector = new GestureDetector(this);
				
				
				//tvCountNum.setText((vfCoupon.getCurrentView().getId()+1)+"/"+listCup.size());
				Bitmap mb = BitmapFactory.decodeResource(BigGallery.this.getResources(),R.drawable.moren1);
				mb= Helper.toRoundCorner(mb, 20);
				
				for(int i=0;i<listUrl.size();i++)
				{
					View v = LayoutInflater.from(BigGallery.this).inflate(R.layout.biggalleryview, null);
					ImageView ivBig = (ImageView)v.findViewById(R.id.ivBig);
					ivBig.setImageBitmap(mb);
					
					
					ivBig.setTag(listUrl.get(i));
					LoadImage.addTask(listUrl.get(i), ivBig);
//					SimpleImageLoader.showImg(ivBig,listUrl.get(i),BigGallery.this);
					LoadImage.doTask();
					v.setId(i);
					vfCoupon.addView(v);
				}
				if(postIndex >= 0)
				{
					vfCoupon.setDisplayedChild(postIndex);
				}
				
				tvCount.setText((postIndex+1)+"/"+listUrl.size());
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			
		}
	}

//	//ΪGallery��ֵ
//	class MainAdapter extends BaseAdapter
//	{
//		Context context;
//		public MainAdapter(Context context)
//		{
//			this.context = context;
//		}
//	
//		@Override
//		public int getCount() {
//			return listUrl.size();
//		}
//	
//		@Override
//		public Object getItem(int arg0) {
//			return null;
//		}
//	
//		@Override
//		public long getItemId(int position) {
//			return 0;
//		}
//	
//		@Override
//		public View getView(int position, View convertView, ViewGroup parent) {
//			LayoutInflater inflater = LayoutInflater.from(context);
//			View v = null;
//			if(convertView == null)
//			{
//				v = inflater.inflate(R.layout.biggalleryview, null);
//			}
//			else
//				v = (View)convertView;
//			ImageView ivBig = (ImageView)v.findViewById(R.id.ivBig);
//			SimpleImageLoader.showImg(ivBig,listUrl.get(position),BigGallery.this);
////			ivBig.setImageBitmap(listUrl.get(position));
//			return v;
//		}
//	
//	}

	@Override
	public void onClick(View v) {
		if(v.getId() == btnClose.getId())
		{
			BigGallery.this.finish();
		}
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
			if (e1.getX() - e2.getX() > 50) {
			
				this.vfCoupon.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_in));
				this.vfCoupon.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_out));
				if(vfCoupon.getCurrentView().getId() == (listUrl.size()-1))
				{
					this.vfCoupon.focusSearch(listUrl.size()-1);
					tvCount.setText(listUrl.size()+"/"+listUrl.size());
				}
				else
				{
					this.vfCoupon.showNext();
					tvCount.setText((vfCoupon.getCurrentView().getId()+1)+"/"+listUrl.size());
				}
				return true;
			} 
			else if (e1.getX() - e2.getX() < -50) {
			
				this.vfCoupon.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_in));
				this.vfCoupon.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_out));
				if(vfCoupon.getCurrentView().getId() == 0)
				{
					this.vfCoupon.focusSearch(0);
					tvCount.setText(1+"/"+listUrl.size());
				}
				else
				{
					this.vfCoupon.showPrevious();
					tvCount.setText((vfCoupon.getCurrentView().getId()+1)+"/"+listUrl.size());
				}
				return true;
		}

		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		return myGestureDetector.onTouchEvent(event);
	}
	
}

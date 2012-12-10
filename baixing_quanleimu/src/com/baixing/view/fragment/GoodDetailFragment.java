package com.baixing.view.fragment;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.baixing.entity.GoodsDetail;
import com.baixing.entity.GoodsDetail.EDATAKEYS;
import com.baixing.entity.GoodsList;
import com.baixing.entity.UserBean;
import com.baixing.imageCache.SimpleImageLoader;
import com.baixing.jsonutil.JsonUtil;
import com.baixing.util.Communication;
import com.baixing.util.ErrorHandler;
import com.baixing.util.GoodsListLoader;
import com.baixing.util.TextUtil;
import com.baixing.util.Tracker;
import com.baixing.util.TrackConfig.TrackMobile.BxEvent;
import com.baixing.util.TrackConfig.TrackMobile.Key;
import com.baixing.util.TrackConfig.TrackMobile.PV;
import com.baixing.util.Util;
import com.baixing.util.ViewUtil;
import com.baixing.view.AdViewHistory;
import com.baixing.view.AuthController;
import com.baixing.widget.ContextMenuItem;
import com.baixing.widget.HorizontalListView;
import com.quanleimu.activity.BaiduMapActivity;
import com.quanleimu.activity.BaseActivity;
import com.quanleimu.activity.BaseFragment;
import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.activity.R;


public class GoodDetailFragment extends BaseFragment implements AnimationListener, View.OnTouchListener,View.OnClickListener, OnItemSelectedListener/*, PullableScrollView.PullNotifier, View.OnTouchListener*/, GoodsListLoader.HasMoreListener{

	public interface IListHolder{
		public void startFecthingMore();
		public boolean onResult(int msg, GoodsListLoader loader);//return true if getMore succeeded, else otherwise
	};
	
	
//	final private String strCollect = "收藏";
//	final private String strCancelCollect = "取消收藏";
	final private int msgRefresh = 5;
	final private int msgUpdate = 6;
	final private int msgDelete = 7;
	
	public static final int MSG_ADINVERIFY_DELETED = 0x00010000;
	public static final int MSG_MYPOST_DELETED = 0x00010001;

	public GoodsDetail detail = new GoodsDetail();
	private boolean called = false;
//	private View titleControlView = null;
	private AuthController authCtrl;
	private UserBean user = null;
	private String json = "";
	
//	private Bundle mBundle;
	
	private Bitmap mb_loading = null;
	
//	private int type = 240;//width of screen
//	private int paddingLeftMetaPixel = 16;//meta, right part, value
	
	private boolean keepSilent = false;
	
	private GoodsListLoader mListLoader;
//	private int mCurIndex = 0;
	
	private IListHolder mHolder = null;
	
//	private Dialog manageDlg = null;
	
	private WeakReference<View> loadingMorePage;
	
//	private boolean initCalled = false;
	
	private boolean fromChat = false;
	
	List<View> pages = new ArrayList<View>();
	
	enum REQUEST_TYPE{
		REQUEST_TYPE_REFRESH,
		REQUEST_TYPE_UPDATE,
		REQUEST_TYPE_DELETE
	}
	
	@Override
	public void onStackTop(boolean isBack) {
		super.onStackTop(isBack);
	}

	@Override
	public void onDestroy(){
		this.keepSilent = true;
		
//		if(null != listUrl && listUrl.size() > 0)
//			SimpleImageLoader.Cancel(listUrl);
//		this.mListLoader = null;
		
		//View history is disabled from version 3.1
//		Thread t = new Thread(new Runnable(){
//			public void run(){
//				Helper.saveDataToLocate(QuanleimuApplication.getApplication().getApplicationContext(), "listLookHistory", QuanleimuApplication.getApplication().getListLookHistory());
//			}
//		});
//		t.start();
	
		super.onDestroy();
	}
	
	@Override
	public boolean handleBack(){
		this.keepSilent = false;

		return false;
	}
	
	
	
	@Override
	public void onPause() {
		this.keepSilent = true;
		super.onPause();
		pages.clear();
//		Gallery glDetail = (Gallery) getView().findViewById(R.id.glDetail);
//		if(glDetail != null){
////			glDetail.getc
//		}
//		glDetail = null;
		
	}
	
	@Override
	public void onResume(){
		updateButtonStatus();
		if (isMyAd() || !isValidMessage())
		{
			if (user==null)
				user = (UserBean) Util.loadDataFromLocate(this.getActivity(), "user", UserBean.class);
			this.pv = PV.MYVIEWAD;
			Tracker.getInstance()
			.pv(PV.MYVIEWAD)
			.append(Key.SECONDCATENAME, detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_CATEGORYENGLISHNAME))
			.append(Key.ADID, detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID))
			.append(Key.ADSENDERID, user!=null? user.getId() : null)
			.append(Key.ADSTATUS, detail.getValueByKey("status"))
			.end();
		} else {
			this.pv = PV.VIEWAD;
			Tracker.getInstance()
			.pv(this.pv)
			.append(Key.SECONDCATENAME, detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_CATEGORYENGLISHNAME))
			.append(Key.ADID, detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID))
			.end();
		}	
			
		//		QuanleimuApplication.addViewCounter(this.detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID));
		this.keepSilent = false;
		super.onResume();
		
		if (called)
		{
			called = false;
			if (!isInMyStore())
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle(R.string.dialog_title_info)
				.setMessage(R.string.tip_add_fav)
				.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				})
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						handleStoreBtnClicked();
					}
					
				}).create().show();
			}
		}
	}
	
	private void updateButtonStatus()
	{
//		if(isMyAd()){
//			btnStatus = 1;
//		}
//		else{
//			if(isInMyStore()){
//				btnStatus = 0;
//			}
//			else{
//				btnStatus = -1;
//			}
//		}
	}
	
	private boolean isMyAd(){
		if(detail == null) return false;
		return QuanleimuApplication.getApplication().isMyAd(detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID));		
	}
	
	private boolean isInMyStore(){
		if(detail == null) return false;
		List<GoodsDetail> myStore = QuanleimuApplication.getApplication().getListMyStore();
		if(myStore == null) return false;
		for(int i = 0; i < myStore.size(); ++ i){
			if(myStore.get(i).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID)
					.equals(detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID))){
				return true;
			}
		}
		return false;		
	}
//	
//	private int mLastY = 0;
//	private int mLastExceedingY = 0;
	public boolean onTouch (View v, MotionEvent event){
//		if(!keepSilent){
//			switch(event.getAction()){
//			case MotionEvent.ACTION_MOVE:
//				
//				break;
//			case MotionEvent.ACTION_CANCEL:
//			case MotionEvent.ACTION_UP:
//				//mLastExceedingY = 0;
//				break;
//			}
//		}		
	    switch (event.getAction()) {
	    case MotionEvent.ACTION_DOWN:
	    case MotionEvent.ACTION_MOVE: 
	    	if(getView() != null && getView().findViewById(R.id.svDetail) != null){
	    		((ViewPager)getView().findViewById(R.id.svDetail)).requestDisallowInterceptTouchEvent(true);
	    	}
	        break;
	    case MotionEvent.ACTION_OUTSIDE:
	    case MotionEvent.ACTION_UP:
//	    case MotionEvent.ACTION_CANCEL:
	    	if(getView() != null && getView().findViewById(R.id.svDetail) != null){
	    		((ViewPager)getView().findViewById(R.id.svDetail)).requestDisallowInterceptTouchEvent(false);
	    	}
	        break;		
	    }
		return this.keepSilent;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.mListLoader = (GoodsListLoader) getArguments().getSerializable("loader");
		int index = getArguments().getInt("index", 0);
		if(mListLoader == null 
				|| mListLoader.getGoodsList() == null 
				|| mListLoader.getGoodsList().getData() == null
				|| mListLoader.getGoodsList().getData().size() <= index){
			return;
		}
		detail = mListLoader.getGoodsList().getData().get(index);
		if (savedInstanceState != null) //
		{
//			this.mListLoader.setHandler(handler);
			this.mListLoader.setHasMoreListener(this);
		}
		
		this.fromChat = this.getArguments().getBoolean("fromChat");
	}
	
	private View getPage(int index){
		for(int i = 0; i < pages.size(); ++ i){
			if(pages.get(i).getTag() != null && (Integer)pages.get(i).getTag() == index){
				return pages.get(i);
			}
		}
		return null;
	}
	
	private View getNewPage(int index){
		for(int i = 0; i < pages.size(); ++ i){
			if(pages.get(i).getTag() == null){
				pages.get(i).setTag(index);
				ViewParent parent = pages.get(i).getParent();
				if(parent != null){
					if(parent instanceof ViewGroup){
						((ViewGroup)parent).removeView(pages.get(i));
					}else{
						break;
					}
					Log.d("has a parent", "has parent************************************************************************");
				}
				return pages.get(i);
			}
		}
		View detail = LayoutInflater.from(this.getAppContext()).inflate(R.layout.gooddetailcontent, null);
		detail.setTag(index);
		pages.add(detail);
		return detail;
	}
	
	private void removePage(int index){
		for(int i = 0; i < pages.size(); ++ i){
			if(pages.get(i) != null && pages.get(i).getTag() != null && (Integer)pages.get(i).getTag() == index){
				HorizontalListView glDetail = (HorizontalListView) pages.get(i).findViewById(R.id.glDetail);
//				glDetail.setVisibility(View.GONE);
				glDetail.setAdapter(null);
				pages.get(i).setTag(null);
				if(pages.get(i) instanceof ScrollView){
					((ScrollView)pages.get(i)).scrollTo(0, 0);
				}
			}
		}
	}
	
	@Override
	public void onDestroyView(){
		if(mb_loading != null){
			mb_loading.recycle();
			mb_loading = null;
		}
		super.onDestroyView();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if(detail == null || mListLoader == null) return null;
		final int mCurIndex = getArguments().getInt("index", 0);
		this.keepSilent = false;//magic flag to refuse unexpected touch event
		
		final View v = inflater.inflate(R.layout.gooddetailview, null);
		
		BitmapFactory.Options o =  new BitmapFactory.Options();
        o.inPurgeable = true;
        mb_loading = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.icon_vad_loading, o);
        
        final ViewPager vp = (ViewPager) v.findViewById(R.id.svDetail);
        vp.setAdapter(new PagerAdapter() {
			
			public Object instantiateItem(View arg0, int position) 
			{
				Log.d("instantiateItem", "instantiateItem:    " + position);
				View detail = getNewPage(position);//LayoutInflater.from(vp.getContext()).inflate(R.layout.gooddetailcontent, null);
				
				
				detail.setTag(R.id.accountEt, detail);
				((ViewPager) arg0).addView(detail, 0);
				if (position == mListLoader.getGoodsList().getData().size())
				{
					detail.findViewById(R.id.loading_more_progress_parent).setVisibility(View.VISIBLE);
					detail.findViewById(R.id.llDetail).setVisibility(View.GONE);
					loadMore(detail);
				}
				else
				{
					GoodsDetail detaiObj = mListLoader.getGoodsList().getData().get(position);
					initContent(detail, detaiObj, position, ((ViewPager) arg0), false);
				}
				return detail;
			}
			
            public void destroyItem(View arg0, int index, Object arg2)
            {
                ((ViewPager) arg0).removeView((View) arg2);
                
                final Integer pos = (Integer) ((View) arg2).getTag();
                if (pos < mListLoader.getGoodsList().getData().size())
                {
//                	Log.d("imagecount", "imagecount, destroyItem: " + pos + "  " + mListLoader.getGoodsList().getData().get(pos).toString());
                	List<String> listUrl = getImageUrls(mListLoader.getGoodsList().getData().get(pos));
                	if(null != listUrl && listUrl.size() > 0){
                		SimpleImageLoader.Cancel(listUrl);
	            		for(int i = 0; i < listUrl.size(); ++ i){
	            			decreaseImageCount(listUrl.get(i), pos);
	//            			QuanleimuApplication.getImageLoader().forceRecycle(listUrl.get(i));
	            		}
                	}
                }
                removePage(pos);
                
                
            }

			public boolean isViewFromObject(View arg0, Object arg1) {
				return arg0 == arg1;
			}
			
			public int getCount() {
				if(mListLoader == null || mListLoader.getGoodsList() == null || mListLoader.getGoodsList().getData() == null){
					return 0;
				}
				return mListLoader.getGoodsList().getData().size() + (mListLoader.hasMore() ? 1 : 0);
			}
		});
//        if(mCurIndex == 0) return v;
        vp.setCurrentItem(mCurIndex);
        vp.setOnPageChangeListener(new OnPageChangeListener() {
			private int currentPage = 0;
			public void onPageSelected(int pos) {
				currentPage = pos;
				keepSilent = false;//magic flag to refuse unexpected touch event
				//tracker
				if (isMyAd() || !isValidMessage())
				{
					GoodDetailFragment.this.pv = PV.MYVIEWAD;
					Tracker.getInstance()
					.pv(PV.MYVIEWAD)
					.append(Key.SECONDCATENAME, detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_CATEGORYENGLISHNAME))
					.append(Key.ADID, detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID))
					.append(Key.ADSENDERID, user!=null? user.getId() : null)
					.append(Key.ADSTATUS, detail.getValueByKey("status"))
					.end();
				} else {
					GoodDetailFragment.this.pv = PV.VIEWAD;
					Tracker.getInstance()
					.pv(GoodDetailFragment.this.pv)
					.append(Key.SECONDCATENAME, detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_CATEGORYENGLISHNAME))
					.append(Key.ADID, detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID))
					.end();
				}
				
				if (pos != mListLoader.getGoodsList().getData().size())
				{
					detail = mListLoader.getGoodsList().getData().get(pos);
					mListLoader.setSelection(pos);
					updateTitleBar(getTitleDef());
					updateContactBar(v.getRootView(), false);
				}
				else
				{
					updateTitleBar(getTitleDef());
					updateContactBar(v.getRootView(), true);
				}
			}
			
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				currentPage = arg0;
			}
			
			public void onPageScrollStateChanged(int arg0) {
				if(arg0 != ViewPager.SCROLL_STATE_IDLE) return;
				
				List<String>listUrl = getImageUrls(detail);
				if(listUrl == null || listUrl.size() == 0){
					ViewGroup currentVG = (ViewGroup)getPage(currentPage);
					if(currentVG != null){
						View noimage = currentVG.findViewById(R.id.vad_no_img_tip);
						if(noimage != null){
							noimage.setVisibility(View.VISIBLE);
						}
						View detail = currentVG.findViewById(R.id.glDetail);
						if(detail != null){
							detail.setVisibility(View.GONE);
						}
					}
				}
//				if(listUrl != null && listUrl.size() > 0){
				else{
					ViewGroup currentVG = (ViewGroup)getPage(currentPage);
					if(currentVG != null){
						HorizontalListView glDetail = (HorizontalListView) currentVG.findViewById(R.id.glDetail);
						VadImageAdapter adapter = (VadImageAdapter)glDetail.getAdapter();
						if(adapter != null){
							List<String> curLists = adapter.getImages();
							boolean sameList = true;
							if(curLists != null && curLists.size() == listUrl.size()){
								for(int i = 0; i < curLists.size(); ++ i){
									String cstr = curLists.get(i);
									String lstr = listUrl.get(i);
									if(cstr != null && cstr.length() > 0 && lstr != null && lstr.length() > 0){
										if(!cstr.equals(lstr)){
											sameList = false;
											break;
										}
									}
								}
							}else{
								sameList = false;
							}
							if(!sameList){
								adapter.setContent(listUrl);
								adapter.notifyDataSetChanged();
							}
						}else{
							glDetail.setAdapter(new VadImageAdapter(getActivity(), listUrl, currentPage));
						}
	//					
						glDetail.setOnTouchListener(GoodDetailFragment.this);
						
						glDetail.setOnItemClickListener(new AdapterView.OnItemClickListener() {
		
							@Override
							public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
								if(galleryReturned){
									Bundle bundle = createArguments(null, null);
									bundle.putInt("postIndex", arg2);
									bundle.putSerializable("goodsDetail", detail);
									galleryReturned = false;
									pushFragment(new BigGalleryFragment(), bundle);		
								}
							}
						});
					}
				}				
			}
		});

       
        vp.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
//				 View g = vp.findViewById(R.id.glDetail);
//				 Rect rect = new Rect();  
//	                g.getLocalVisibleRect(rect);  
//	                                                                   
//	                if(rect.contains((int)event.getX(), (int)event.getY()))  
//	                {  
//	                	Log.e("POINTER", "dispatch touch event to gallery view");
//	                        g.dispatchTouchEvent(event);  
//	                        return true;
//	                }  
	                return false;  
			}
        	
        } );
        
        mListLoader.setSelection(mCurIndex);
        mListLoader.setHandler(new Handler(){
				@Override
				public void handleMessage(Message msg) {
					if(null != mHolder){
						if(mHolder.onResult(msg.what, mListLoader)){
							onGotMore();
						}else{
							onNoMore();
						}
						
						if(msg.what == ErrorHandler.ERROR_NETWORK_UNAVAILABLE){
							onLoadMoreFailed();
						}
					}else{
						switch (msg.what) {
						case GoodsListLoader.MSG_FINISH_GET_FIRST:				 
							GoodsList goodsList = JsonUtil.getGoodsListFromJson(mListLoader.getLastJson());
							mListLoader.setGoodsList(goodsList);
							if (goodsList == null || goodsList.getData().size() == 0) {
								Message msg1 = Message.obtain();
								msg1.what = ErrorHandler.ERROR_COMMON_FAILURE;
								Bundle bundle = new Bundle();
								bundle.putString("popup_message", "没有符合的结果，请稍后并重试！");
								msg1.setData(bundle);
								QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg1);
							} else {
								//QuanleimuApplication.getApplication().setListGoods(goodsList.getData());
							}
							mListLoader.setHasMore(true);
							notifyPageDataChange(true);
							break;
						case GoodsListLoader.MSG_NO_MORE:					
							onNoMore();
							
							mListLoader.setHasMore(false);
							notifyPageDataChange(false);
							
							break;
						case GoodsListLoader.MSG_FINISH_GET_MORE:	
							GoodsList goodsList1 = JsonUtil.getGoodsListFromJson(mListLoader.getLastJson());
							if (goodsList1 == null || goodsList1.getData().size() == 0) {
								Message msg2 = Message.obtain();
								msg2.what = ErrorHandler.ERROR_COMMON_WARNING;
								Bundle bundle1 = new Bundle();
								bundle1.putString("popup_message", "后面没有啦！");
								msg2.setData(bundle1);
								QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
								
								onNoMore();
								
								mListLoader.setHasMore(false);
								notifyPageDataChange(false);
							} else {
								List<GoodsDetail> listCommonGoods =  goodsList1.getData();
								for(int i=0;i<listCommonGoods.size();i++)
								{
									mListLoader.getGoodsList().getData().add(listCommonGoods.get(i));
								}
								//QuanleimuApplication.getApplication().setListGoods(mListLoader.getGoodsList().getData());	
								
								mListLoader.setHasMore(true);
								notifyPageDataChange(true);
								onGotMore();
							}
							break;
						case ErrorHandler.ERROR_NETWORK_UNAVAILABLE:
							Message msg2 = Message.obtain();
							msg2.what = ErrorHandler.ERROR_NETWORK_UNAVAILABLE;
							QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
							
							onLoadMoreFailed();
							
							break;
						}
					}
					
					super.handleMessage(msg);
				}

				private void onGotMore() {
					final View page = loadingMorePage == null ? null : (View) loadingMorePage.get();
					if (page != null)
					{
						page.postDelayed(new Runnable() {

							@Override
							public void run() {
								page.findViewById(R.id.loading_more_progress_parent).setVisibility(View.GONE);
								page.findViewById(R.id.llDetail).setVisibility(View.VISIBLE);
								final Integer tag = (Integer)page.getTag();
								if(tag != null){
									initContent(page, mListLoader.getGoodsList().getData().get(tag.intValue()), tag.intValue(), null, false);
								}
							}
							
						}, 10);
					}
				}

				private void onNoMore() {
					View root = getView();
					if (root != null)
					{
						ViewUtil.postShortToastMessage(root, "后面没有啦！", 0);
					}
				}
			});        
        
        return v;
	}
	
	private boolean isValidMessage()
	{
		return !detail.getValueByKey("status").equals("4") && !detail.getValueByKey("status").equals("20");
	}
	
	private void notifyPageDataChange(boolean hasMore)
	{
		if(keepSilent) return;
		PagerAdapter adapter = getContentPageAdapter();
		if (adapter != null)
		{
			adapter.notifyDataSetChanged();
		}
		View rootView = getView();
		if (rootView == null)
		{
			return;
		}
		
		View page = loadingMorePage == null ? null : loadingMorePage.get();
		final ViewPager vp = (ViewPager) rootView.findViewById(R.id.svDetail);
		if (!hasMore && page != null && vp != null)
		{
			vp.removeView(page);
		}
	}
	
	private PagerAdapter getContentPageAdapter()
	{
		View root = getView(); 
		if (root == null)
		{
			return null;
		}
		
		final ViewPager vp = (ViewPager) root.findViewById(R.id.svDetail);
		return vp == null ? null : vp.getAdapter();
	}
	
	private void initContent(View contentView, final GoodsDetail detail, final int pageIndex, ViewPager pager, boolean useRoot)
	{
		
		if(this.getView() == null) return;
		if(useRoot)
			contentView = contentView.getRootView();
		
		RelativeLayout llgl = (RelativeLayout) contentView.findViewById(R.id.llgl);
		
//		if(detail.getImageList() != null){
			List<String>listUrl = getImageUrls(detail);
			
			llgl = (RelativeLayout) contentView.findViewById(R.id.llgl);
			int cur = pager != null ? pager.getCurrentItem() : -1;
			if(listUrl == null || listUrl.size() == 0){
//				llgl.setVisibility(View.GONE);
				if(pageIndex == getArguments().getInt("index", 0) || pageIndex == cur){
					llgl.findViewById(R.id.vad_no_img_tip).setVisibility(View.VISIBLE);
				}else{
					llgl.findViewById(R.id.vad_no_img_tip).setVisibility(View.GONE);
				}
				llgl.findViewById(R.id.glDetail).setVisibility(View.GONE);
				
			}else{
				llgl.findViewById(R.id.vad_no_img_tip).setVisibility(View.GONE);
				llgl.findViewById(R.id.glDetail).setVisibility(View.VISIBLE);
//				int cur = pager != null ? pager.getCurrentItem() : -1;
				HorizontalListView glDetail = (HorizontalListView) contentView.findViewById(R.id.glDetail);
				Log.d("instantiateItem", "instantiateItem:    initContent  " + detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_DESCRIPTION) +  glDetail);
				if(pageIndex == getArguments().getInt("index", 0) || pageIndex == cur){
					glDetail.setAdapter(new VadImageAdapter(getActivity(), listUrl, pageIndex));
					glDetail.setOnTouchListener(this);
					glDetail.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	
						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
							if(galleryReturned){
								Bundle bundle = createArguments(null, null);
								bundle.putInt("postIndex", arg2);
								bundle.putSerializable("goodsDetail", detail);
								galleryReturned = false;
	//							Log.d("haha", "hahaha, new big gallery");
								pushFragment(new BigGalleryFragment(), bundle);		
							}else{
	//							Log.d("hhah", "hahaha, it workssssssssssss");
							}
						}
					});
				}
			}

		TextView txt_tittle = (TextView) contentView.findViewById(R.id.goods_tittle);
		TextView txt_message1 = (TextView) contentView.findViewById(R.id.sendmess1);
//		rl_address.setOnTouchListener(this);

		LinearLayout ll_meta = (LinearLayout) contentView.findViewById(R.id.meta);
		

		this.setMetaObject(contentView, detail);
		
		String title = detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_TITLE);
		String description = detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_DESCRIPTION);

		if ((title == null || title.length() == 0) && description != null)
		{
			title = description.length() > 40 ? description.substring(0, 40) : description;
		}
		
		description += "\n打电话给我时，请一定说明在百姓网看到的，谢谢！";
		description = appendPostFromInfo(detail, description);
		description = appendExtralMetaInfo(detail, description);
		
		txt_message1.setText(description);
		txt_tittle.setText(title);

		final ViewPager vp = pager != null ? pager : (ViewPager) getActivity().findViewById(R.id.svDetail);
		if (vp != null && pageIndex == vp.getCurrentItem())
		{
			updateTitleBar(getTitleDef());
			updateContactBar(vp.getRootView(), false);
		}
		
	}
	
	private void updateContactBar(View rootView, boolean forceHide)
	{
		AdViewHistory.getInstance().markRead(detail.getValueByKey(EDATAKEYS.EDATAKEYS_ID));
		
		if (!isValidMessage() && !forceHide)
		{
			String tips = detail.getValueByKey("tips"); 
			if(tips == null || tips.equals("")){
				tips  = "该信息不符合《百姓网公约》";
			}
			
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			AlertDialog dialog = builder.setTitle(R.string.dialog_title_info)
			.setMessage(tips)
			.setNegativeButton(R.string.delete, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					showSimpleProgress();
					new Thread(new RequestThread(REQUEST_TYPE.REQUEST_TYPE_DELETE)).start();
                    trackerLogEvent(BxEvent.MYVIEWAD_DELETE);	
				}
			})
			.setPositiveButton(R.string.appeal, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					trackerLogEvent(BxEvent.MYVIEWAD_APPEAL);
					Bundle bundle = createArguments(null, null);
					bundle.putInt("type", 1);
					bundle.putString("adId", detail.getValueByKey(EDATAKEYS.EDATAKEYS_ID));
					pushAndFinish(new FeedbackFragment(), bundle);
				}
			}).create();
			dialog.setCanceledOnTouchOutside(false);
			dialog.setOnCancelListener(new OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					finishFragment();
				}
			});
			dialog.show();
		}
		
		LinearLayout rl_phone = (LinearLayout)rootView.findViewById(R.id.phonelayout);
		if (forceHide)
		{
			rl_phone.setVisibility(View.GONE);
			return;
		}
		else if (isMyAd() || !isValidMessage())
		{
			rootView.findViewById(R.id.phone_parent).setVisibility(View.GONE);
			rootView.findViewById(R.id.vad_tool_bar).setVisibility(View.VISIBLE);
			
			
			rootView.findViewById(R.id.vad_btn_edit).setOnClickListener(this);
			rootView.findViewById(R.id.vad_btn_refresh).setOnClickListener(this);
			rootView.findViewById(R.id.vad_btn_delete).setOnClickListener(this);
			
			if (!isValidMessage())
			{
				rootView.findViewById(R.id.vad_btn_edit).setVisibility(View.GONE);
				rootView.findViewById(R.id.vad_btn_refresh).setVisibility(View.GONE);
			}
			return;
		}
		
		
		rootView.findViewById(R.id.phone_parent).setVisibility(View.VISIBLE);
		rootView.findViewById(R.id.vad_tool_bar).setVisibility(View.GONE);

		final String contactS = detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_CONTACT);
		final String mobileArea = detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_MOBILE_AREA);
		ViewGroup btnBuzz = (ViewGroup) rootView.findViewById(R.id.vad_buzz_btn);
		ImageView btnImg = (ImageView) btnBuzz.findViewById(R.id.vad_buzz_btn_img);
		TextView btnTxt = (TextView) btnBuzz.findViewById(R.id.vad_buzz_btn_txt);
		btnTxt.setTextColor(getResources().getColor(R.color.vad_sms));
		
		final boolean buzzEnable = TextUtil.isNumberSequence(contactS) && mobileArea != null && !"".equals(mobileArea) ? true : false;
		btnBuzz.setEnabled(buzzEnable);
		if (!buzzEnable)
		{
			btnTxt.setTextColor(getResources().getColor(R.color.common_button_disable));
			btnImg.setImageResource(R.drawable.icon_sms_disable);
		}
		
		rootView.findViewById(R.id.vad_buzz_btn).setOnClickListener(this);
		rl_phone.setVisibility(View.VISIBLE);

		//Enable or disable call button
		final boolean callEnable = TextUtil.isNumberSequence(contactS);
		rootView.findViewById(R.id.vad_call_btn).setEnabled(callEnable);
		rootView.findViewById(R.id.vad_call_btn).setOnClickListener(callEnable ? this : null);
		View callImg = rootView.findViewById(R.id.icon_call);
		callImg.setBackgroundResource(callEnable ? R.drawable.icon_call : R.drawable.icon_call_disable);
		TextView txtCall = (TextView) rootView.findViewById(R.id.txt_call);
		String text = contactS;
		if (mobileArea != null && mobileArea.length() > 0 && !QuanleimuApplication.getApplication().getCityName().equals(mobileArea))
		{
			text = contactS + "(" + mobileArea + ")";
		}
		else if (mobileArea == null || "".equals(mobileArea.trim()))
		{
			text = contactS + "(非手机号)";
			ContextMenuItem opts = (ContextMenuItem) rootView.findViewById(R.id.vad_call_nonmobile);
			opts.updateOptionList("", getResources().getStringArray(R.array.item_call_nonmobile), 
					new int[] {R.id.vad_call_nonmobile + 1, R.id.vad_call_nonmobile + 2});
		}
		
		txtCall.setText(callEnable ? text : "无联系方式");
		txtCall.setTextColor(getResources().getColor(callEnable ? R.color.vad_call_btn_text : R.color.common_button_disable));
		
	}
	
//	private boolean isCurrentAdFromMobile()
//	{
//		if (detail == null)
//		{
//			return false;
//		}
//		
//		String postFrom = detail.getValueByKey("postMethod");
//		Log.d("postMethod", postFrom);
//		return "api_mobile_android".equals(postFrom) || "baixing_ios".equalsIgnoreCase(postFrom) || "api_wap".equalsIgnoreCase(postFrom);
//	}
	
	
	private String appendExtralMetaInfo(GoodsDetail detail, String description)
	{
		if (detail == null)
		{
			return description;
		}
		
		StringBuffer extralInfo = new StringBuffer();
		ArrayList<String> allMeta = detail.getMetaData();
		for (String meta : allMeta)
		{
			if (!meta.startsWith("价格") && !meta.startsWith("地点") &&
					!meta.startsWith("地区") && !meta.startsWith("查看") && !meta.startsWith("来自") && !meta.startsWith("具体地点") && !meta.startsWith("分类"))
			{
				final int splitIndex = meta.indexOf(" ");
				if (splitIndex != -1)
				{
					extralInfo.append(meta.substring(splitIndex).trim()).append("，");
				}
			}
		}
		
		if (extralInfo.length() > 0)
		{
			extralInfo.deleteCharAt(extralInfo.length() -1 );
			return extralInfo.append("\n\n").append(description).toString(); 
		}
		
		return description;
	}
	
	private String appendPostFromInfo(GoodsDetail detail, String description)
	{
		if (detail == null)
		{
			return description;
		}
		
		String postFrom = detail.getValueByKey("postMethod");
		if ("api_mobile_android".equals(postFrom))
		{
			return description + "\n来自android客户端";
		}
		else if ("baixing_ios".equalsIgnoreCase(postFrom))
		{
			return description + "\n来自iPhone客户端";
		}
		
		return description;
	}
	
	private void startChat()
	{
		Log.d("track","VIEWAD_BUZZ");
		//tracker
		Tracker.getInstance()
		.event(BxEvent.VIEWAD_BUZZ)
		.end();
		if (this.fromChat)
		{
			this.finishFragment();
			return;
		}
		
		if (/*m_viewInfoListener != null &&*/ this.detail != null)
		{
			Bundle bundle = new Bundle();
//			bundle.putString("senderId", myUserId);
			bundle.putString("receiverId", detail.getValueByKey("userId"));
			bundle.putString("adId", detail.getValueByKey("id"));
			bundle.putString("adTitle", detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_TITLE));
			bundle.putBoolean("fromAd", true);
			pushFragment(new TalkFragment(), bundle);
		}
	}
	
	
//	private int btnStatus = -1;//-1:strCollect, 0: strCancelCollect, 1:strManager
	
	private boolean handleRightBtnIfInVerify(){
		if(!detail.getValueByKey("status").equals("0")){
			showSimpleProgress();
			new Thread(new RequestThread(REQUEST_TYPE.REQUEST_TYPE_DELETE)).start();

			return true;	
		}
		return false;
	}
	
	private void handleStoreBtnClicked(){
		if(handleRightBtnIfInVerify()) return;
		Log.d("tracker",!isInMyStore()?"VIEWAD_FAV":"VIEWAD_UNFAV");
		//tracker
		Tracker.getInstance()
		.event(!isInMyStore()?BxEvent.VIEWAD_FAV:BxEvent.VIEWAD_UNFAV)
		.append(Key.SECONDCATENAME, detail.getValueByKey(EDATAKEYS.EDATAKEYS_CATEGORYENGLISHNAME))
		.append(Key.ADID, detail.getValueByKey(EDATAKEYS.EDATAKEYS_ID))
		.end();
		
		if(/*-1 == btnStatus*/!isInMyStore()){			
//			btnStatus = 0;
			List<GoodsDetail> myStore = QuanleimuApplication.getApplication().addFav(detail); 
			
			if (myStore != null)
			{
				Util.saveDataToLocate(QuanleimuApplication.getApplication().getApplicationContext(), "listMyStore", myStore);
			}
						
			updateTitleBar(getTitleDef());
			Toast.makeText(QuanleimuApplication.getApplication().getApplicationContext(), "收藏成功", 3).show();
		}
		else /*if (0 == btnStatus)*/ {
//			btnStatus = -1;
			
			List<GoodsDetail> favList = QuanleimuApplication.getApplication().removeFav(detail);
			Util.saveDataToLocate(this.getAppContext(), "listMyStore", favList);
			updateTitleBar(getTitleDef());
			Toast.makeText(this.getActivity(), "取消收藏", 3).show();
		}
	}
	
	class ManagerAlertDialog extends AlertDialog{
		public ManagerAlertDialog(Context context, int theme){
			super(context, theme);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.vad_title_fav_parent:
			handleStoreBtnClicked();
			break;
		case R.id.vad_call_btn:
		{
			Log.d("tracker","VIEWAD_MOBILECALLCLICK");
			//tracker
			Tracker.getInstance()
			.event(BxEvent.VIEWAD_MOBILECALLCLICK)
			.end();
			
			final String mobileArea = detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_MOBILE_AREA);
			if (mobileArea == null || "".equals(mobileArea.trim()))
			{
				getView().findViewById(R.id.vad_call_nonmobile).performLongClick();
			}
			else if (mobileArea != null && mobileArea.length() > 0 && !QuanleimuApplication.getApplication().getCityName().equals(mobileArea)) {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle(R.string.dialog_title_warning)
				.setMessage(R.string.warning_danger_mobile)
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				})
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						startContact(false);
					}
					
				}).create().show();
			}
			else
			{
				startContact(false);
			}
			
			break;
		}
		case R.id.retry_load_more:
			retryLoadMore();
			break;
		case R.id.vad_buzz_btn:
			startContact(true);
			break;
		case R.id.vad_btn_refresh:{
			showSimpleProgress();
			new Thread(new RequestThread(REQUEST_TYPE.REQUEST_TYPE_REFRESH)).start();

            trackerLogEvent(BxEvent.MYVIEWAD_REFRESH);
			break;
		}
		case R.id.vad_btn_edit:{
			
			Bundle args = createArguments(null, null);
			args.putSerializable("goodsDetail", detail);
			args.putString("cateNames", detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_CATEGORYENGLISHNAME));
			pushFragment(new PostGoodsFragment(), args);
            trackerLogEvent(BxEvent.MYVIEWAD_EDIT);
			break;
		}
		case R.id.vad_btn_delete:{
			new AlertDialog.Builder(getActivity()).setTitle("提醒")
			.setMessage("是否确定删除")
			.setPositiveButton("确定", new DialogInterface.OnClickListener() {							
				@Override
				public void onClick(DialogInterface dialog, int which) {
					showSimpleProgress();
					new Thread(new RequestThread(REQUEST_TYPE.REQUEST_TYPE_DELETE)).start();
                    trackerLogEvent(BxEvent.MYVIEWAD_DELETE);
				}
			})
			.setNegativeButton(
		     "取消", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();							
				}
			})
		     .show();
			break;
		}
		}
	}



    /**
     * add track log for MyViewad_Edit MyViewad_Refresh MyViewad_Delete MyViewad_Appeal
     * @param event
     */
    private void trackerLogEvent(BxEvent event) {
        String tmpCateName = detail.data.get("categoryEnglishName");
        String secondCategoryName = tmpCateName != null ? tmpCateName : "empty categoryEnglishName";
        String tmpInsertedTime = detail.data.get("insertedTime");
        long postedSeconds = -1;
        if (tmpInsertedTime != null) {
            long nowTime = new Date().getTime() / 1000;
            postedSeconds = nowTime - Long.valueOf(tmpInsertedTime);
        }

        Tracker.getInstance().event(event)
                .append(Key.SECONDCATENAME, secondCategoryName)
                .append(Key.POSTEDSECONDS, postedSeconds)
                .end();
    }
	
	private boolean galleryReturned = true;
	
	@Override
	protected void onFragmentBackWithData(int requestCode, Object result){
		if(PostGoodsFragment.MSG_POST_SUCCEED == requestCode){
			this.finishFragment(requestCode, result);
		}else if(BigGalleryFragment.MSG_GALLERY_BACK == requestCode){
//			Log.d("haha", "hahaha,   from gallery back");
			galleryReturned = true;
		}
	}

	
	private void setMetaObject(View currentPage, GoodsDetail detail){
		LinearLayout ll_meta = (LinearLayout) currentPage.findViewById(R.id.meta);
		if(ll_meta == null) return;
		ll_meta.removeAllViews();
		
		LayoutInflater inflater = LayoutInflater.from(currentPage.getContext());
		
		String price = detail.getValueByKey(EDATAKEYS.EDATAKEYS_PRICE);
		if (price != null && !"".equals(price))
		{
			View item = createMetaView(inflater, "价格:", price, null);
			ll_meta.addView(item);
			((TextView) item.findViewById(R.id.tvmeta)).setTextColor(getResources().getColor(R.color.vad_meta_price));
		}
		
		
		
		String area = detail.getValueByKey(EDATAKEYS.EDATAKEYS_AREANAME);
		String address = detail.getMetaValueByKey("具体地点");
		if (address != null)
		{
			area += "  " + address;
		}
		
		if (area != null)
		{
			View areaV = createMetaView(inflater, "地区:", area, new View.OnClickListener() {
				public void onClick(View v) {
					showMap();
				}
			});
			ll_meta.addView(areaV);
		}
	}
	
	
	
	private View createMetaView(LayoutInflater inflater, String label, String value, View.OnClickListener clickListener)
	{
		View v = inflater.inflate(R.layout.item_meta, null);
		
		TextView tvmetatxt = (TextView) v.findViewById(R.id.tvmetatxt);
		TextView tvmeta = (TextView) v.findViewById(R.id.tvmeta);
		
		tvmetatxt.setText(label);
		tvmeta.setText(value);
		
		if (clickListener != null)
		{
			v.findViewById(R.id.action_indicator_img).setVisibility(View.VISIBLE);
			v.setOnClickListener(clickListener);
		}
		else
		{
			v.findViewById(R.id.action_indicator_img).setVisibility(View.INVISIBLE);
		}
		
		LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) v.getLayoutParams();
		if (layoutParams== null)  layoutParams =  new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		layoutParams.height = (int) getResources().getDimension(R.dimen.vad_meta_item_height);
		v.setLayoutParams(layoutParams);
		
		return v;
	}
	
	
	@Override
	public void onAnimationEnd(Animation animation) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub
		
	}
	

	@Override
	protected void handleMessage(Message msg, Activity activity, View rootView) {

		switch (msg.what) {
		case msgRefresh:
			if(json == null){
				Toast.makeText(activity, "刷新失败，请稍后重试！", 0).show();
				break;
			}
			try {
				JSONObject jb = new JSONObject(json);
				JSONObject js = jb.getJSONObject("error");
				String message = js.getString("message");
				int code = js.getInt("code");
				if (code == 0) {
					new Thread(new RequestThread(REQUEST_TYPE.REQUEST_TYPE_UPDATE)).start();
					Toast.makeText(getActivity(), message, 0).show();
				}else if(2 == code){
					hideProgress();
					new AlertDialog.Builder(getActivity()).setTitle("提醒")
					.setMessage(message)
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {							
						@Override
						public void onClick(DialogInterface dialog, int which) {
							showSimpleProgress();
							new Thread(new RequestThread(REQUEST_TYPE.REQUEST_TYPE_REFRESH, 1)).start();
							dialog.dismiss();
						}
					})
					.setNegativeButton(
				     "取消", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();							
						}
					})
				     .show();

				}else {
					hideProgress();
					Toast.makeText(getActivity(), message, 0).show();
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;			
		case msgUpdate:
			hideProgress();
			GoodsList goods = JsonUtil.getGoodsListFromJson(json);
			List<GoodsDetail> goodsDetails = goods.getData();
			if(goodsDetails != null && goodsDetails.size() > 0){
				for(int i = 0; i < goodsDetails.size(); ++ i){
					if(goodsDetails.get(i).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID)
							.equals(detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID))){
						detail = goodsDetails.get(i);
						break;
					}
				}
				List<GoodsDetail>listMyPost = QuanleimuApplication.getApplication().getListMyPost();
				if(listMyPost != null){
					for(int i = 0; i < listMyPost.size(); ++ i){
						if(listMyPost.get(i).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID)
								.equals(detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID))){
							listMyPost.set(i, detail);
							break;
						}
					}
				}
				//QuanleimuApplication.getApplication().setListMyPost(listMyPost);
			}

//			setMetaObject(); FIXME: should update current UI.
			break;
		case msgDelete:
			hideProgress();
			try {
				JSONObject jb = new JSONObject(json);
				JSONObject js = jb.getJSONObject("error");
				String message = js.getString("message");
				int code = js.getInt("code");
				if (code == 0) {
					if(detail.getValueByKey("status").equals("0")){
						List<GoodsDetail> listMyPost = QuanleimuApplication.getApplication().getListMyPost();
						if(null != listMyPost){
							for(int i = 0; i < listMyPost.size(); ++ i){
								if(listMyPost.get(i).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID)
										.equals(detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID))){
									listMyPost.remove(i);
									break;
								}
							}
						}
						finishFragment(MSG_MYPOST_DELETED, null);
					}
					else{
						finishFragment(MSG_ADINVERIFY_DELETED, detail.getValueByKey(EDATAKEYS.EDATAKEYS_ID));
					}
//					finish();
					Toast.makeText(activity, message, 0).show();
				} else {
					// 删除失败
					Toast.makeText(activity, "删除失败,请稍后重试！", 0).show();
					finishFragment();
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		default:
			break;
		}
	
	}

	class RequestThread implements Runnable{
		private REQUEST_TYPE type;
		private int pay = 0;
		public RequestThread(REQUEST_TYPE type){
			this.type = type;
		}
		public RequestThread(REQUEST_TYPE type, int pay) {
			this.type = type;
			this.pay = pay;
		}
		@Override
		public void run(){
			synchronized(GoodDetailFragment.this){
				ArrayList<String> requests = null;
				String apiName = null;
				int msgToSend = -1;
				if(REQUEST_TYPE.REQUEST_TYPE_DELETE == type){
					requests = doDelete();
					apiName = "ad_delete";
					msgToSend = msgDelete;
				}
				else if(REQUEST_TYPE.REQUEST_TYPE_REFRESH == type){
					requests = doRefresh(this.pay);
					apiName = "ad_refresh";
					msgToSend = msgRefresh;
				}
				else if(REQUEST_TYPE.REQUEST_TYPE_UPDATE == type){
					requests = doUpdate();
					apiName = "ad_list";
					msgToSend = msgUpdate;
				}
				if(requests != null){
					String url = Communication.getApiUrl(apiName, requests);
					try {
						json = Communication.getDataByUrl(url, true);
					} catch (UnsupportedEncodingException e) {
						QuanleimuApplication.getApplication().getErrorHandler().sendEmptyMessage(ErrorHandler.ERROR_NETWORK_UNAVAILABLE);
						hideProgress();
					} catch (IOException e) {
						QuanleimuApplication.getApplication().getErrorHandler().sendEmptyMessage(ErrorHandler.ERROR_NETWORK_UNAVAILABLE);
						hideProgress();
					} catch (Communication.BXHttpException e){
						
					}
//					myHandler.sendEmptyMessage(msgToSend);
					sendMessage(msgToSend, null);
				}
			}
		}
	}
	
	private ArrayList<String> doRefresh(int pay){
		json = "";
		ArrayList<String> list = new ArrayList<String>();

		UserBean user = (UserBean) Util.loadDataFromLocate(this.getActivity(), "user", UserBean.class);
		if(user != null && user.getPhone() != null && !user.getPhone().equals("")){
			String mobile = user.getPhone();
			String password = user.getPassword();
	
			list.add("mobile=" + mobile);
			String password1 = Communication.getMD5(password);
			password1 += Communication.apiSecret;
			String userToken = Communication.getMD5(password1);
			list.add("userToken=" + userToken);
		}
		list.add("adId=" + detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID));
		list.add("rt=1");
		if(pay != 0){
			list.add("pay=1");
		}

		return list;
	}
	
	private ArrayList<String> doUpdate(){
		json = "";
		ArrayList<String> list = new ArrayList<String>();
		
		UserBean user = (UserBean) Util.loadDataFromLocate(this.getActivity(), "user", UserBean.class);
		if(user != null && user.getPhone() != null && !user.getPhone().equals("")){
			String mobile = user.getPhone();
			String password = user.getPassword();
	
			list.add("mobile=" + mobile);
			String password1 = Communication.getMD5(password);
			password1 += Communication.apiSecret;
			String userToken = Communication.getMD5(password1);
			list.add("userToken=" + userToken);
		}
		list.add("query=id:" + detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID));
		list.add("rt=1");
		return list;		
	}
	
	private ArrayList<String> doDelete(){
		// TODO Auto-generated method stub
		json = "";
		ArrayList<String> list = new ArrayList<String>();

		UserBean user = (UserBean) Util.loadDataFromLocate(this.getAppContext(), "user", UserBean.class);
		if(user != null && user.getPhone() != null && !user.getPhone().equals("")){
			String mobile = user.getPhone();
			String password = user.getPassword();
			list.add("mobile=" + mobile);
			String password1 = Communication.getMD5(password);
			password1 += Communication.apiSecret;
			String userToken = Communication.getMD5(password1);
			list.add("userToken=" + userToken);
		}
		list.add("adId=" + detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID));
		list.add("rt=1");
		
		return list;		
	}
	
	private HashMap<String, List<Integer> > imageMap = new HashMap<String, List<Integer> >();
	private void increaseImageCount(String url, int pos){
		if(url == null) return;
//		Log.d("imagecount", "imagecount, increase:  " + url);
		if(imageMap.containsKey(url)){
			List<Integer> values = imageMap.get(url);
			for(int i = 0; i < values.size(); ++ i){
				if(values.get(i) ==  pos){
					return;
				}
			}
			values.add(pos);
			imageMap.put(url, values);
		}else{
			List<Integer> value = new ArrayList<Integer>();
			value.add(pos);
			imageMap.put(url, value);
		}
	}
	
	private void decreaseImageCount(String url, int pos){
		if(url == null) return;
//		Log.d("imagecount", "imagecount, decrease:  " + url);
		if(imageMap.containsKey(url)){
			List<Integer> values = imageMap.get(url);
			for(int i = 0; i < values.size(); ++ i){
				if(values.get(i) == pos){
					values.remove(i);
					break;
				}
			}
			if(values.size() == 0){
				QuanleimuApplication.getImageLoader().forceRecycle(url);
				imageMap.remove(url);
//				Log.d("remove", "imagecount    do remove");
			}else{
				imageMap.put(url, values);
//				Log.d("0, remove", "imagecount not 0, no remove~~~~:   " + values.size());
			}
		}
	}	

	class VadImageAdapter extends BaseAdapter {


		Context context;
		List<String> listUrl;
		private int position;
		public VadImageAdapter(Context context, List<String> listUrl, int detailPostion) {
			this.context = context;
			this.listUrl = listUrl;
			position = detailPostion;
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
			iv.setImageBitmap(mb_loading);
			
			if (listUrl.size() != 0 && listUrl.get(position) != null && !listUrl.get(position).equals("")) {
				String prevTag = (String)iv.getTag();
				iv.setTag(listUrl.get(position));
				SimpleImageLoader.showImg(iv, listUrl.get(position), prevTag, context);
				increaseImageCount(listUrl.get(position), this.position);
			}
			
			//Log.d("GoodDetailView: ", "getView for position-" + position);
			
			return root;
		}


	}

	
	@Override
	public void initTitle(TitleDef title){
		title.m_leftActionHint = "返回";
		title.m_rightActionHint = "";//detail.getValueByKey("status").equals("0") ? "收藏" : null;
		if(this.mListLoader != null && mListLoader.getGoodsList() != null && mListLoader.getGoodsList().getData() != null){
			title.m_title = ( this.mListLoader.getSelection() + 1 ) + "/" + 
					this.mListLoader.getGoodsList().getData().size();			
		}
		title.m_visible = true;
		
		LayoutInflater inflater = LayoutInflater.from(this.getActivity());
		title.m_titleControls = inflater.inflate(R.layout.vad_title, null); 
		
		updateTitleBar(title);
	}
	
	private void updateTitleBar(TitleDef title)
	{
		
		if(isMyAd() || !isValidMessage()){
			title.m_titleControls.findViewById(R.id.vad_title_fav_parent).setVisibility(View.GONE);
		}
		else{
			title.m_titleControls.findViewById(R.id.vad_title_fav_parent).setVisibility(View.VISIBLE);
		}
		
		title.m_titleControls.findViewById(R.id.vad_title_fav_parent).setOnClickListener(this);
		TextView favBtn = (TextView) title.m_titleControls.findViewById(R.id.btn_fav_unfav);
		if (favBtn != null)
		{
			favBtn.setText(isInMyStore() ? "取消收藏" : "收藏");
//			favBtn.setImageResource(isInMyStore() ? R.drawable.icon_unfav : R.drawable.icon_fav);
		}
		
		TextView createTimeView = (TextView) title.m_titleControls.findViewById(R.id.vad_create_time);
		if(detail != null){
			String dateV = detail.getValueByKey(EDATAKEYS.EDATAKEYS_DATE);
			if (dateV != null)
			{
				try {
					long timeL = Long.parseLong(dateV) * 1000;
					createTimeView.setText(TextUtil.timeTillNow(timeL, getAppContext()) + "发布");
				}
				catch(Throwable t)
				{
					createTimeView.setText("");
				}
			}
			
			TextView viewTimes = (TextView) getTitleDef().m_titleControls.findViewById(R.id.vad_viewed_time);
			viewTimes.setText(detail.getValueByKey("count") + "次查看");
		}
	}
	
	@Override
	public void initTab(TabDef tab){
		tab.m_visible = false;
	}
	
	
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
    	if (parent.getAdapter() instanceof VadImageAdapter)
    	{
    		VadImageAdapter mainAdapter = (VadImageAdapter) parent.getAdapter();
    		
    		List<String> listUrl = mainAdapter.getImages();
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
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0)
    {
        // TODO Auto-generated method stub
    }	


	@Override
	public void onHasMoreStatusChanged() {
	}
	
	private void onLoadMoreFailed()
	{
		final View page = loadingMorePage == null ? null : (View) loadingMorePage.get();
		if (page != null)
		{
			page.findViewById(R.id.retry_load_more).setOnClickListener(GoodDetailFragment.this);
			page.postDelayed(new Runnable() {
				@Override
				public void run() {
					page.findViewById(R.id.loading_more_progress_parent).setVisibility(View.GONE);
					page.findViewById(R.id.retry_more_parent).setVisibility(View.VISIBLE);
					page.findViewById(R.id.llDetail).setVisibility(View.GONE);
				}
				
			}, 10);
		}
	}
	
	private void retryLoadMore()
	{
		//We assume that this action always on UI thread.
		final View page = loadingMorePage == null ? null : (View) loadingMorePage.get();
		if (page != null)
		{
			page.findViewById(R.id.loading_more_progress_parent).setVisibility(View.VISIBLE);
			page.findViewById(R.id.retry_more_parent).setVisibility(View.GONE);
			page.findViewById(R.id.llDetail).setVisibility(View.GONE);
		}
		
		if (null != mHolder) {
			mHolder.startFecthingMore();
		} else {
			mListLoader
					.startFetching(
							false,
							((GoodsListLoader.E_LISTDATA_STATUS.E_LISTDATA_STATUS_ONLINE == mListLoader
									.getDataStatus()) ? Communication.E_DATA_POLICY.E_DATA_POLICY_NETWORK_CACHEABLE
									: Communication.E_DATA_POLICY.E_DATA_POLICY_ONLY_LOCAL));
		}
	}
	
	private void loadMore(View page) {
		loadingMorePage = new WeakReference(page);
		
		retryLoadMore();
	}
	
	public void setListHolder(IListHolder holder)
	{
		this.mHolder = holder;
	}
	
	private static List<String> getImageUrls(GoodsDetail goodDetail)
	{
		List<String> listUrl = null;
		
		if (goodDetail.getImageList() != null)
		{
			listUrl = new ArrayList<String>();
			String b = (goodDetail.getImageList().getResize180());//.substring(1, (goodDetail.getImageList().getResize180()).length()-1);
			if(b == null) return listUrl;
			b = Communication.replace(b);
			String[] c = b.split(",");
			for(int i=0;i<c.length;i++) 
			{
				listUrl.add(c[i]);
			}
		}
		
		return listUrl;
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem menuItem) {
		
		View v = getView();
//		Log.d("goodetail","itemselect:"+menuItem.getItemId());
		switch (menuItem.getItemId())
		{
			case R.id.vad_call_nonmobile + 1: {
				startContact(false);
				return true;
			}
			case R.id.vad_call_nonmobile + 2: {
				ClipboardManager clipboard = (ClipboardManager)
				        getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
				clipboard.setText(detail.getValueByKey(EDATAKEYS.EDATAKEYS_CONTACT));
				ViewUtil.postShortToastMessage(getView(), R.string.tip_clipd_contact, 0);
				return true;
			}
		}
		
		return super.onContextItemSelected(menuItem);
	}
	
	private void startContact(boolean sms)
	{
		if (sms){//右下角发短信
			Log.d("tracker","VIEWAD_SMS");
			Tracker.getInstance()
			.event(BxEvent.VIEWAD_SMS)
			.end();
		}
			
		String contact = detail.getValueByKey(EDATAKEYS.EDATAKEYS_CONTACT);
		if (contact != null)
		{
			Intent intent = new Intent(
					sms ? Intent.ACTION_SENDTO : Intent.ACTION_DIAL,
					Uri.parse((sms ? "smsto:" : "tel:") + contact));
			List<ResolveInfo> ls = getActivity().getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
			if (ls != null && ls.size() > 0)
			{
				startActivity(intent);
				if (!sms)
				{
					called = true;
				}
			}
			else
			{
				ViewUtil.postShortToastMessage(getView(), sms ? R.string.warning_no_sms_app_install : R.string.warning_no_phone_app_install, 0);
			}
		}
	}
	
	private void showMap()
	{
		if (detail == null)
		{
            Toast.makeText(getActivity(), "无信息无法显示地图", 1).show();
			return;
		}
		
		if(keepSilent) {
            Toast.makeText(getActivity(), "当前无法显示地图", 1).show();
            return;
        }
		final BaseActivity baseActivity = (BaseActivity)getActivity();
		if (baseActivity != null){
			if (Build.VERSION.SDK_INT >  16)//Fix baidu map SDK crash on android4.2 device.
			{
				ViewUtil.startMapForAds(baseActivity, detail);
			}
			else
			{
				Bundle bundle = new Bundle();
				bundle.putSerializable("detail", detail);
				baseActivity.getIntent().putExtras(bundle);
				
				baseActivity.getIntent().setClass(baseActivity, BaiduMapActivity.class);
				baseActivity.startActivity(baseActivity.getIntent());
			}
			Tracker.getInstance().pv(PV.VIEWADMAP).append(Key.SECONDCATENAME, detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_CATEGORYENGLISHNAME)).append(Key.ADID, detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID)).end();
		} else {
            Toast.makeText(getActivity(), "显示地图失败", 1).show();
        }
	}

	public boolean hasGlobalTab()
	{
		return false;
	}
	
}

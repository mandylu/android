//liuchong@baixing.com
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
import android.app.AlertDialog.Builder;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.baixing.activity.BaiduMapActivity;
import com.baixing.activity.BaseActivity;
import com.baixing.activity.BaseFragment;
import com.baixing.adapter.VadImageAdapter;
import com.baixing.data.GlobalDataManager;
import com.baixing.entity.Ad;
import com.baixing.entity.Ad.EDATAKEYS;
import com.baixing.entity.AdList;
import com.baixing.entity.UserBean;
import com.baixing.imageCache.SimpleImageLoader;
import com.baixing.jsonutil.JsonUtil;
import com.baixing.tracking.TrackConfig.TrackMobile.BxEvent;
import com.baixing.tracking.TrackConfig.TrackMobile.Key;
import com.baixing.tracking.TrackConfig.TrackMobile.PV;
import com.baixing.tracking.TrackConfig.TrackMobile.Value;
import com.baixing.tracking.Tracker;
import com.baixing.util.Communication;
import com.baixing.util.ErrorHandler;
import com.baixing.util.VadListLoader;
import com.baixing.util.TextUtil;
import com.baixing.util.Util;
import com.baixing.util.ViewUtil;
import com.baixing.view.AdViewHistory;
import com.baixing.widget.ContextMenuItem;
import com.baixing.widget.HorizontalListView;
import com.quanleimu.activity.R;

public class VadFragment extends BaseFragment implements View.OnTouchListener,View.OnClickListener, OnItemSelectedListener, VadListLoader.HasMoreListener, VadImageAdapter.IImageProvider, VadListLoader.Callback {

	public interface IListHolder{
		public void startFecthingMore();
		public boolean onResult(int msg, VadListLoader loader);//return true if getMore succeeded, else otherwise
	};
	
	
	final private int MSG_REFRESH = 5;
	final private int MSG_UPDATE = 6;
	final private int MSG_DELETE = 7;
	public static final int MSG_ADINVERIFY_DELETED = 0x00010000;
	public static final int MSG_MYPOST_DELETED = 0x00010001;

	public Ad detail = new Ad();
	private boolean called = false;
	private String json = "";
	
	private WeakReference<Bitmap> mb_loading = null;
	
	private boolean keepSilent = false;
	
	private VadListLoader mListLoader;
	
	private IListHolder mHolder = null;
	
	private WeakReference<View> loadingMorePage;
	
	List<View> pages = new ArrayList<View>();
	
	enum REQUEST_TYPE{
		REQUEST_TYPE_REFRESH,
		REQUEST_TYPE_UPDATE,
		REQUEST_TYPE_DELETE
	}
	
	@Override
	public void onDestroy(){
		this.keepSilent = true;
		
		Thread t = new Thread(new Runnable(){
			public void run(){
				try{
					Thread.sleep(2000);
					if(mb_loading != null && mb_loading.get() != null){
						mb_loading.get().recycle();
						mb_loading = null;
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
		t.start();
	
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
	}
	
	@Override
	public void onResume(){
		if (isMyAd() || !detail.isValidMessage())
		{
			this.pv = PV.MYVIEWAD;
			Tracker.getInstance()
			.pv(PV.MYVIEWAD)
			.append(Key.SECONDCATENAME, detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_CATEGORYENGLISHNAME))
			.append(Key.ADID, detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID))
			.append(Key.ADSENDERID, GlobalDataManager.getInstance().getAccountManager().getMyId(getAppContext()))
			.append(Key.ADSTATUS, detail.getValueByKey("status"))
			.end();
		} else {
			this.pv = PV.VIEWAD;
			Tracker.getInstance()
			.pv(this.pv)
			.append(Key.SECONDCATENAME, detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_CATEGORYENGLISHNAME))
			.append(Key.ADID, detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID))
			.end();
		}	
			
		this.keepSilent = false;
		super.onResume();
		
		if (called)
		{
			called = false;
			if (!isInMyStore())
			{
				Tracker.getInstance().event(BxEvent.VIEWAD_HINTFAV).end();
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle(R.string.dialog_title_info)
				.setMessage(R.string.tip_add_fav)
				.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Tracker.getInstance().event(BxEvent.VIEWAD_HINTFAVRESULT).append(Key.RESULT, Value.CANCEL).end();
					}
				})
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Tracker.getInstance().event(BxEvent.VIEWAD_HINTFAVRESULT).append(Key.RESULT, Value.FAV).end();
						handleStoreBtnClicked();
					}
					
				}).create().show();
			}
		}
	}
	
	private boolean isMyAd(){
		if(detail == null) return false;
		return GlobalDataManager.getInstance().isMyAd(detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID));		
	}
	
	private boolean isInMyStore(){
		if(detail == null) return false;
		return GlobalDataManager.getInstance().isFav(detail);
	}
//	
	public boolean onTouch (View v, MotionEvent event){
	    switch (event.getAction()) {
	    case MotionEvent.ACTION_DOWN:
	    case MotionEvent.ACTION_MOVE: 
	    	if(getView() != null && getView().findViewById(R.id.svDetail) != null){
	    		((ViewPager)getView().findViewById(R.id.svDetail)).requestDisallowInterceptTouchEvent(true);
	    	}
	        break;
	    case MotionEvent.ACTION_OUTSIDE:
	    case MotionEvent.ACTION_UP:
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
		this.mListLoader = (VadListLoader) getArguments().getSerializable("loader");
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if(detail == null || mListLoader == null) return null;
		final int mCurIndex = getArguments().getInt("index", 0);
		this.keepSilent = false;//magic flag to refuse unexpected touch event
		
		final View v = inflater.inflate(R.layout.gooddetailview, null);
		
		BitmapFactory.Options o =  new BitmapFactory.Options();
        o.inPurgeable = true;
        mb_loading = new WeakReference<Bitmap>(BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.icon_vad_loading, o));
        
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
					Ad detaiObj = mListLoader.getGoodsList().getData().get(position);
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
				if (isMyAd() || !detail.isValidMessage())
				{
					VadFragment.this.pv = PV.MYVIEWAD;
					Tracker.getInstance()
					.pv(PV.MYVIEWAD)
					.append(Key.SECONDCATENAME, detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_CATEGORYENGLISHNAME))
					.append(Key.ADID, detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID))
					.append(Key.ADSENDERID, GlobalDataManager.getInstance().getAccountManager().getMyId(getActivity()))
					.append(Key.ADSTATUS, detail.getValueByKey("status"))
					.end();
				} else {
					VadFragment.this.pv = PV.VIEWAD;
					Tracker.getInstance()
					.pv(VadFragment.this.pv)
					.append(Key.SECONDCATENAME, detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_CATEGORYENGLISHNAME))
					.append(Key.ADID, detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID))
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
							glDetail.setAdapter(new VadImageAdapter(getActivity(), listUrl, currentPage, VadFragment.this));
						}
	//					
						glDetail.setOnTouchListener(VadFragment.this);
						
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
	                return false;  
			}
        	
        } );
        
        mListLoader.setSelection(mCurIndex);
        mListLoader.setCallback(this);       
        
        return v;
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
	
	private void initContent(View contentView, final Ad detail, final int pageIndex, ViewPager pager, boolean useRoot)
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
				Log.d("instantiateItem", "instantiateItem:    initContent  " + detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_DESCRIPTION) +  glDetail);
				if(pageIndex == getArguments().getInt("index", 0) || pageIndex == cur){
					glDetail.setAdapter(new VadImageAdapter(getActivity(), listUrl, pageIndex, VadFragment.this));
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
		
		String title = detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_TITLE);
		String description = detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_DESCRIPTION);

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
		
		if (!detail.isValidMessage() && !forceHide)
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
					postDelete(true, new OnCancelListener() {
						
						@Override
						public void onCancel(DialogInterface dialog) {
							finishFragment();
						}
					});
				}
			})
			.setPositiveButton(R.string.appeal, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					trackerLogEvent(BxEvent.MYVIEWAD_APPEAL);
					Bundle bundle = createArguments("申诉", null);
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
		else if (isMyAd() || !detail.isValidMessage())
		{
			rootView.findViewById(R.id.phone_parent).setVisibility(View.GONE);
			rootView.findViewById(R.id.vad_tool_bar).setVisibility(View.VISIBLE);
			
			
			rootView.findViewById(R.id.vad_btn_edit).setOnClickListener(this);
			rootView.findViewById(R.id.vad_btn_refresh).setOnClickListener(this);
			rootView.findViewById(R.id.vad_btn_delete).setOnClickListener(this);
			
			if (!detail.isValidMessage())
			{
				rootView.findViewById(R.id.vad_btn_edit).setVisibility(View.GONE);
				rootView.findViewById(R.id.vad_btn_refresh).setVisibility(View.GONE);
			}
			return;
		}
		
		
		rootView.findViewById(R.id.phone_parent).setVisibility(View.VISIBLE);
		rootView.findViewById(R.id.vad_tool_bar).setVisibility(View.GONE);

		final String contactS = detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_CONTACT);
		final String mobileArea = detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_MOBILE_AREA);
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
		String text = "立即拨打" + contactS;
		if (mobileArea != null && mobileArea.length() > 0 && !GlobalDataManager.getInstance().getCityName().equals(mobileArea))
		{
//			text = contactS + "(" + mobileArea + ")";
		}
		else if (mobileArea == null || "".equals(mobileArea.trim()))
		{
//			text = contactS + "(非手机号)";
			ContextMenuItem opts = (ContextMenuItem) rootView.findViewById(R.id.vad_call_nonmobile);
			opts.updateOptionList("", getResources().getStringArray(R.array.item_call_nonmobile), 
					new int[] {R.id.vad_call_nonmobile + 1, R.id.vad_call_nonmobile + 2});
		}
		
		txtCall.setText(callEnable ? text : "无联系方式");
		txtCall.setTextColor(getResources().getColor(callEnable ? R.color.vad_call_btn_text : R.color.common_button_disable));
		
	}
	
	private String appendExtralMetaInfo(Ad detail, String description)
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
	
	private String appendPostFromInfo(Ad detail, String description)
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
		
		if(!isInMyStore()){			
			List<Ad> myStore = GlobalDataManager.getInstance().addFav(detail); 
			
			if (myStore != null)
			{
				Util.saveDataToLocate(GlobalDataManager.getInstance().getApplicationContext(), "listMyStore", myStore);
			}
						
			updateTitleBar(getTitleDef());
			Toast.makeText(GlobalDataManager.getInstance().getApplicationContext(), "收藏成功", 3).show();
		}
		else  {
			List<Ad> favList = GlobalDataManager.getInstance().removeFav(detail);
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
			
			final String mobileArea = detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_MOBILE_AREA);
			if (mobileArea == null || "".equals(mobileArea.trim()))
			{
				Tracker.getInstance().event(BxEvent.VIEWAD_NOTCALLABLE).end();
				getView().findViewById(R.id.vad_call_nonmobile).performLongClick();
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
			args.putString("cateNames", detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_CATEGORYENGLISHNAME));
			pushFragment(new PostGoodsFragment(), args);
            trackerLogEvent(BxEvent.MYVIEWAD_EDIT);
			break;
		}
		case R.id.vad_btn_delete:{
			postDelete(true, null);
			break;
		}
		}
	}
	
	private void postDelete(boolean cancelable, OnCancelListener listener)
	{
		Builder builder = new AlertDialog.Builder(getActivity()).setTitle("提醒")
		.setMessage("是否确定删除")
		.setPositiveButton("确定", new DialogInterface.OnClickListener() {							
			@Override
			public void onClick(DialogInterface dialog, int which) {
				showSimpleProgress();
				new Thread(new RequestThread(REQUEST_TYPE.REQUEST_TYPE_DELETE)).start();
                trackerLogEvent(BxEvent.MYVIEWAD_DELETE);
			}
		});
		
		if (cancelable)
		{
			builder = builder.setNegativeButton(
					"取消", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();							
						}
					});
		}
		
		AlertDialog dialog = builder.create();
		dialog.show();
		if (listener != null)
		{
			dialog.setOnCancelListener(listener);
		}
		
		dialog.show();
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

	
	private void setMetaObject(View currentPage, Ad detail){
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
		if (address != null && address.trim().length() > 0)
		{
			area = address;
		}
		
		View areaV = createMetaView(inflater, "地区:", area, new View.OnClickListener() {
			public void onClick(View v) {
				showMap();
			}
		});
		ll_meta.addView(areaV);
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
	protected void handleMessage(Message msg, Activity activity, View rootView) {

		switch (msg.what) {
		case MSG_REFRESH:
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
		case MSG_UPDATE:
			hideProgress();
			AdList goods = JsonUtil.getGoodsListFromJson(json);
			List<Ad> goodsDetails = goods.getData();
			if(goodsDetails != null && goodsDetails.size() > 0){
				for(int i = 0; i < goodsDetails.size(); ++ i){
					if(goodsDetails.get(i).getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID)
							.equals(detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID))){
						detail = goodsDetails.get(i);
						break;
					}
				}
				List<Ad>listMyPost = GlobalDataManager.getInstance().getListMyPost();
				if(listMyPost != null){
					for(int i = 0; i < listMyPost.size(); ++ i){
						if(listMyPost.get(i).getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID)
								.equals(detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID))){
							listMyPost.set(i, detail);
							break;
						}
					}
				}
				//QuanleimuApplication.getApplication().setListMyPost(listMyPost);
			}

//			setMetaObject(); FIXME: should update current UI.
			break;
		case MSG_DELETE:
			hideProgress();
			try {
				JSONObject jb = new JSONObject(json);
				JSONObject js = jb.getJSONObject("error");
				String message = js.getString("message");
				int code = js.getInt("code");
				if (code == 0) {
					if(detail.getValueByKey("status").equals("0")){
						List<Ad> listMyPost = GlobalDataManager.getInstance().getListMyPost();
						if(null != listMyPost){
							for(int i = 0; i < listMyPost.size(); ++ i){
								if(listMyPost.get(i).getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID)
										.equals(detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID))){
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
			synchronized(VadFragment.this){
				ArrayList<String> requests = null;
				String apiName = null;
				int msgToSend = -1;
				if(REQUEST_TYPE.REQUEST_TYPE_DELETE == type){
					requests = doDelete();
					apiName = "ad_delete";
					msgToSend = MSG_DELETE;
				}
				else if(REQUEST_TYPE.REQUEST_TYPE_REFRESH == type){
					requests = doRefresh(this.pay);
					apiName = "ad_refresh";
					msgToSend = MSG_REFRESH;
				}
				else if(REQUEST_TYPE.REQUEST_TYPE_UPDATE == type){
					requests = doUpdate();
					apiName = "ad_list";
					msgToSend = MSG_UPDATE;
				}
				if(requests != null){
					String url = Communication.getApiUrl(apiName, requests);
					try {
						json = Communication.getDataByUrl(url, true);
					} catch (UnsupportedEncodingException e) {
						ErrorHandler.getInstance().handleError(ErrorHandler.ERROR_NETWORK_UNAVAILABLE, null);
						hideProgress();
					} catch (IOException e) {
						ErrorHandler.getInstance().handleError(ErrorHandler.ERROR_NETWORK_UNAVAILABLE, null);
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
		list.add("adId=" + detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID));
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
		list.add("query=id:" + detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID));
		list.add("rt=1");
		return list;		
	}
	
	private ArrayList<String> doDelete(){
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
		list.add("adId=" + detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID));
		list.add("rt=1");
		
		return list;		
	}
	
	private HashMap<String, List<Integer> > imageMap = new HashMap<String, List<Integer> >();
	private void increaseImageCount(String url, int pos){
		if(url == null) return;
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
		if(imageMap.containsKey(url)){
			List<Integer> values = imageMap.get(url);
			for(int i = 0; i < values.size(); ++ i){
				if(values.get(i) == pos){
					values.remove(i);
					break;
				}
			}
			if(values.size() == 0){
				GlobalDataManager.getImageLoader().forceRecycle(url);
				imageMap.remove(url);
			}else{
				imageMap.put(url, values);
			}
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
		
		if(isMyAd() || !detail.isValidMessage()){
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
			page.findViewById(R.id.retry_load_more).setOnClickListener(VadFragment.this);
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
							((VadListLoader.E_LISTDATA_STATUS.E_LISTDATA_STATUS_ONLINE == mListLoader
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
	
	private static List<String> getImageUrls(Ad goodDetail)
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
		
		switch (menuItem.getItemId())
		{
			case R.id.vad_call_nonmobile + 1: {
				Tracker.getInstance().event(BxEvent.VIEWAD_NOTCALLABLERESULT).append(Key.RESULT, Value.CALL).end();
				startContact(false);
				return true;
			}
			case R.id.vad_call_nonmobile + 2: {
				Tracker.getInstance().event(BxEvent.VIEWAD_NOTCALLABLERESULT).append(Key.RESULT, Value.COPY).end();
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
				called = true;
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
			Tracker.getInstance().pv(PV.VIEWADMAP).append(Key.SECONDCATENAME, detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_CATEGORYENGLISHNAME)).append(Key.ADID, detail.getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID)).end();
		} else {
            Toast.makeText(getActivity(), "显示地图失败", 1).show();
        }
	}

	public boolean hasGlobalTab()
	{
		return false;
	}

	@Override
	public void onShowView(ImageView imageView, String url, String previousUrl, final int index) {
		SimpleImageLoader.showImg(imageView, url, previousUrl, getActivity());
		increaseImageCount(url, index);
	}

	@Override
	public void onRequestComplete(int respCode, Object data) {

		if(null != mHolder){
			if(mHolder.onResult(respCode, mListLoader)){
				onGotMore();
			}else{
				onNoMore();
			}
			
			if(respCode == ErrorHandler.ERROR_NETWORK_UNAVAILABLE){
				onLoadMoreFailed();
			}
		}else{
			switch (respCode) {
			case VadListLoader.MSG_FINISH_GET_FIRST:				 
				AdList goodsList = JsonUtil.getGoodsListFromJson(mListLoader.getLastJson());
				mListLoader.setGoodsList(goodsList);
				if (goodsList == null || goodsList.getData().size() == 0) {
					ErrorHandler.getInstance().handleError(ErrorHandler.ERROR_COMMON_FAILURE, "没有符合的结果，请稍后并重试！");
				} else {
					//QuanleimuApplication.getApplication().setListGoods(goodsList.getData());
				}
				mListLoader.setHasMore(true);
				notifyPageDataChange(true);
				break;
			case VadListLoader.MSG_NO_MORE:					
				onNoMore();
				
				mListLoader.setHasMore(false);
				notifyPageDataChange(false);
				
				break;
			case VadListLoader.MSG_FINISH_GET_MORE:	
				AdList goodsList1 = JsonUtil.getGoodsListFromJson(mListLoader.getLastJson());
				if (goodsList1 == null || goodsList1.getData().size() == 0) {
					ErrorHandler.getInstance().handleError(ErrorHandler.ERROR_COMMON_WARNING, "后面没有啦！");
					
					onNoMore();
					
					mListLoader.setHasMore(false);
					notifyPageDataChange(false);
				} else {
					List<Ad> listCommonGoods =  goodsList1.getData();
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
				ErrorHandler.getInstance().handleError(ErrorHandler.ERROR_NETWORK_UNAVAILABLE, null);
				
				onLoadMoreFailed();
				
				break;
			}
		}
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
	
}

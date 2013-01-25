//liuchong@baixing.com
package com.baixing.activity;

import java.lang.ref.WeakReference;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.baixing.android.api.ApiClient;
import com.baixing.android.api.ApiParams;
import com.baixing.broadcast.CommonIntentAction;
import com.baixing.entity.AdList;
import com.baixing.data.GlobalDataManager;
import com.baixing.imageCache.ImageLoaderManager;
import com.baixing.jsonutil.JsonUtil;
import com.baixing.util.VadListLoader;
import com.baixing.view.fragment.MyAdFragment;
import com.baixing.view.fragment.PersonalProfileFragment;
import com.baixing.view.fragment.VadFragment;
import com.quanleimu.activity.R;

public class PersonalActivity extends BaseTabActivity {

	@Override
	protected int getTabIndex() {
		return TAB_INDEX_PERSONAL;
	}
	
	private void jumpToPersonalPost(Intent intent){
		String action = intent.getAction();
		if (action != null && action.equals(CommonIntentAction.ACTION_BROADCAST_POST_FINISH)) {
			Bundle extras = intent.getExtras();
			if(extras != null){
				extras.putBoolean(CommonIntentAction.ACTION_BROADCAST_POST_FINISH, true);
				if(this.getSupportFragmentManager().getBackStackEntryCount() > 1 
						&& !(getCurrentFragment() instanceof MyAdFragment)){
					pushFragment(new PersonalProfileFragment(), bundle, true);
				}
				pushFragment(new MyAdFragment(), extras, false);
			}		
		}
	}
	
	@Override
	public void onCreate(Bundle savedBundle){
		super.onCreate(savedBundle);
		if(GlobalDataManager.context == null || GlobalDataManager.context.get() == null){
			GlobalDataManager.context = new WeakReference<Context>(this);
		}
		ImageLoaderManager.initImageLoader();
		this.setContentView(R.layout.main_post);
		onSetRootView(findViewById(R.id.root));
		
		if (this.getSupportFragmentManager().getBackStackEntryCount() == 0){
			pushFragment(new PersonalProfileFragment(), bundle, true);
		}
//		Intent intent = getIntent();
		
//		jumpToPersonalPost(intent);
//		showDetailViewFromWX();
		globalTabCtrl.attachView(findViewById(R.id.common_tab_layout), 	this);
		initTitleAction();
	}
	
	@Override
	public void onResume(){
		super.onResume();
		jumpToPersonalPost(getIntent());
		showDetailViewFromWX();
		this.sendBroadcast(new Intent(CommonIntentAction.ACTION_BROADCAST_SHARE_BACK_TO_FRONT));
	}
	
	@Override
	protected void onNewIntent(final Intent intent) {		
		super.onNewIntent(intent);
		setIntent(intent);
		
//		Thread t = new Thread(new Runnable(){
//			@Override
//			public void run(){
//				jumpToPersonalPost(intent);
//			}
//		});
//		t.start();
//		showDetailViewFromWX();
	}
	
	private void showDetailViewFromWX(){
		Intent intent = this.getIntent();
		if(intent != null){
			Bundle bundle = intent.getExtras();
			if(bundle != null){
				if(bundle.getBoolean("isFromWX") && bundle.getString("detailFromWX") != null){
					ApiParams param = new ApiParams();
					param.addParam("query", "id:" + (String)bundle.getString("detailFromWX"));
					ProgressDialog pd = ProgressDialog.show(this, "", "请稍候");
					pd.setCancelable(false);
					pd.show();
					try {
						String result = ApiClient.getInstance().invokeApi(ApiClient.Api.createGet("ad_list"), param);
						AdList gl = JsonUtil.getGoodsListFromJson(result);
						if(gl != null){
							VadListLoader glLoader = new VadListLoader(null, null, null, gl);
							glLoader.setGoodsList(gl);
							glLoader.setHasMore(false);		
							BaseFragment bf = this.getCurrentFragment();
							if(bf != null && bf instanceof VadFragment){
								bf.finishFragment();
							}
							Bundle bundle2 = new Bundle();
							bundle2.putSerializable("loader", glLoader);
							bundle2.putInt("index", 0);
	
							this.pushFragment(new VadFragment(), bundle2, false);
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					pd.dismiss();
				}
			}
		}		
	}
}

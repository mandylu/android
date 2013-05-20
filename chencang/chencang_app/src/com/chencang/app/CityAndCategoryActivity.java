package com.chencang.app;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;

import com.baixing.activity.BaseActivity;
import com.baixing.activity.BaseFragment;
import com.baixing.activity.BaseFragment.TitleDef;
import com.baixing.data.CityAndCategorySelector;
import com.baixing.data.GlobalDataManager;
import com.baixing.entity.Category;
import com.baixing.util.Util;
import com.baixing.view.fragment.CityChangeFragment;
import com.baixing.view.fragment.CityAndCategorySelectionFragment;
import com.baixing.view.fragment.FirstCateFragment;
import com.baixing.view.fragment.SecondCateFragment;
import com.chencang.core.R;

public class CityAndCategoryActivity extends BaseActivity{
	public static final int MSG_SELECTION_DONE = 0x12340000;
	public static final String MSG_SELECTION_SUCCEED = "selection_succeed";
//	public static final int MSG_SELECTION_CANCEL = 0x12340001;
	private BroadcastReceiver receiver = null;
	private String originalCityEnName = null;
	private String originalCityName = null;
	private String originalCateEnName = null;
	private String originalCateName = null; 
	private byte[] lastCatBytes = null;
	private byte[] lastCityBytes = null;
	
	private void initReceiver(){
		if(receiver == null){
			receiver = new BroadcastReceiver(){

				@Override
				public void onReceive(Context context, Intent intent) {
					// TODO Auto-generated method stub
					boolean categorySelectable = CityAndCategorySelector.getInstance().isCategorySelectable();
					boolean citySelectable = CityAndCategorySelector.getInstance().isCitySelectable();
					if(categorySelectable && citySelectable && 
							(intent.getAction().equals(SecondCateFragment.BROADCAST_SEL_CATEGORY_SUCCEED)
									|| intent.getAction().equals(CityChangeFragment.BROADCAST_CITY_SEL_SUCCEED))){
						int count = getSupportFragmentManager().getBackStackEntryCount();
						for(int i = 1; i < count; ++ i){
							BaseFragment currentFragmet = getCurrentFragment();
							if(currentFragmet != null){
								popFragment(currentFragmet);
							}
						}
					}else if((categorySelectable && citySelectable 
							&& intent.getAction().equals(CityAndCategorySelectionFragment.BROADCAST_CITY_AND_CAT_SELECTED))
							|| (categorySelectable && !citySelectable && intent.getAction().equals(SecondCateFragment.BROADCAST_SEL_CATEGORY_SUCCEED))
							|| (citySelectable && !categorySelectable && intent.getAction().equals(CityChangeFragment.BROADCAST_CITY_SEL_SUCCEED))){
						Intent intentRet = new Intent();
						intentRet.putExtra(MSG_SELECTION_SUCCEED, true);
						CityAndCategoryActivity.this.setResult(RESULT_OK, intentRet);
						CityAndCategoryActivity.this.finish();
					}					
				}
				
			};
		}
	}
	
	private void registerReceiver(){
		IntentFilter filter = new IntentFilter();
		filter.addAction(SecondCateFragment.BROADCAST_SEL_CATEGORY_SUCCEED);
		filter.addAction(CityChangeFragment.BROADCAST_CITY_SEL_SUCCEED);
		filter.addAction(CityAndCategorySelectionFragment.BROADCAST_CITY_AND_CAT_SELECTED);
		initReceiver();
		registerReceiver(receiver, filter);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(com.chencang.core.R.layout.main_activity);
		
		this.originalCityEnName = GlobalDataManager.getInstance().getCityEnglishName();
		this.originalCityName = GlobalDataManager.getInstance().getCityName();
		this.originalCateEnName = GlobalDataManager.getInstance().getCategoryEnglishName();
		this.originalCateName 	= GlobalDataManager.getInstance().getCategoryName();
		this.lastCatBytes = Util.loadData(this, "lastCategory");
		this.lastCityBytes = Util.loadData(this, "cityName");
		
		boolean categorySelectable = CityAndCategorySelector.getInstance().isCategorySelectable();
		boolean citySelectable = CityAndCategorySelector.getInstance().isCitySelectable();
		if(categorySelectable && citySelectable){
			pushFragment(new CityAndCategorySelectionFragment(), new Bundle(), true);
			registerReceiver();
		}else if(categorySelectable){
			String cateEnglishName = Util.getConfigName("category");
			Category cate = GlobalDataManager.getInstance().findSpecifiedCategory(cateEnglishName);
			if(cate != null){
				Bundle bundle = new Bundle();
				bundle.putSerializable("cates", cate);
				bundle.putBoolean("isPost", false);
				pushFragment(new SecondCateFragment(), bundle, true);
				registerReceiver();
			}else{
				pushFragment(new FirstCateFragment(), new Bundle(), true);
				registerReceiver();
			}
		}else if(citySelectable){
			this.pushFragment(new CityChangeFragment(), new Bundle(), true);
			registerReceiver();
		}
	}

	@Override
	public void handleBack(){
		BaseFragment currentFragmet = getCurrentFragment();
		try {
			if (currentFragmet != null){				
				if(!currentFragmet.handleBack()) {
					String city = GlobalDataManager.getInstance().getCityEnglishName();
					String category = GlobalDataManager.getInstance().getCategoryEnglishName();
					if (getSupportFragmentManager().getBackStackEntryCount() <= 1 
							&& city != null && city.length() > 0 && category != null && category.length() > 0) {
						GlobalDataManager.getInstance().setCategoryEnglishName(originalCateEnName);
						GlobalDataManager.getInstance().setCategoryName(originalCateName);
						GlobalDataManager.getInstance().setCityEnglishName(originalCityEnName);
						GlobalDataManager.getInstance().setCityName(originalCityName);
						Util.clearData(this, "cityName");
						Util.clearData(this, "lastCategory");
						if (this.lastCatBytes != null) {
							Util.saveDataToFile(this, null, "lastCategory", lastCatBytes);
						}
						if (this.lastCityBytes != null) {
							Util.saveDataToFile(this, null, "cityName", lastCityBytes);
						}
						
						Intent intentRet = new Intent();
						intentRet.putExtra(MSG_SELECTION_SUCCEED, false);
						CityAndCategoryActivity.this.setResult(RESULT_CANCELED, intentRet);
						finish();
						return;
					}
				}else{
					return;
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		super.handleBack();
	}
	
	@Override
	public void onResume(){
		super.onResume();
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		if(receiver != null){
			this.unregisterReceiver(receiver);
			receiver = null;
		}
	}
}
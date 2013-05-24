package com.baixing.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.widget.TextView;

import com.baixing.activity.BaseFragment;
import com.baixing.activity.BaseFragment.TitleDef;
import com.baixing.data.GlobalDataManager;
import com.baixing.entity.Category;
import com.baixing.util.Util;
import com.baixing.util.ViewUtil;
import com.chencang.core.R;

public class CityAndCategorySelectionFragment extends BaseFragment implements OnClickListener{
	
	public static final String BROADCAST_CITY_AND_CAT_SELECTED = "broadcast_city_and_cat_selected";

	@Override
	protected View onInitializeView(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		View rootView = inflater.inflate(R.layout.cityandcategoryselection, null);
		rootView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		((TextView)rootView.findViewById(R.id.city_selector).findViewById(R.id.postshow)).setText("城市");
		((TextView)rootView.findViewById(R.id.city_selector).findViewById(R.id.posthint)).setHint("请选择城市");
		
		((TextView)rootView.findViewById(R.id.category_selector).findViewById(R.id.postshow)).setText("类目");
		((TextView)rootView.findViewById(R.id.category_selector).findViewById(R.id.posthint)).setHint("请选择类目");
		

		
		rootView.findViewById(R.id.city_selector).setOnClickListener(this);
		rootView.findViewById(R.id.category_selector).setOnClickListener(this);
		return rootView;
	}
	
	private void showCityAndCategory(){
		String city = GlobalDataManager.getInstance().getCityName();
		String category = GlobalDataManager.getInstance().getCategoryName();
		if(getView() == null) return;
		View rootView = getView().getRootView();
		if(city != null && city.length() > 0){
			((TextView)rootView.findViewById(R.id.city_selector).findViewById(R.id.posthint)).setText(city);
		}
		if(category != null && category.length() > 0){
			((TextView)rootView.findViewById(R.id.category_selector).findViewById(R.id.posthint)).setText(category);
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v.getId() == R.id.city_selector){
			pushFragment(new CityChangeFragment(), new Bundle());
		}else if(v.getId() == R.id.category_selector){
			String cateEnglishName = Util.getConfigName("category");
			if(cateEnglishName == null || cateEnglishName.equals("") || cateEnglishName.equals("all")){
				pushFragment(new FirstCateFragment(), new Bundle());
			}else{
				Category cate = GlobalDataManager.getInstance().findSpecifiedCategory(cateEnglishName);
				
				if(cate != null){
					Bundle bundle = new Bundle();
					bundle.putSerializable("cates", cate);
					bundle.putBoolean("isPost", false);
					pushFragment(new SecondCateFragment(), bundle);
				}
			}
		}
	}
	
	@Override
	public void onResume(){
		this.showCityAndCategory();
		super.onResume();
	}
	
	
	@Override
	protected void initTitle(TitleDef title) {
		title.m_title = "切换城市和分类";
		String cate = GlobalDataManager.getInstance().getCategoryEnglishName();
		String city = GlobalDataManager.getInstance().getCityEnglishName();
		if(cate != null && cate.length() > 0 && city != null && city.length() > 0){
			title.m_leftActionHint = "返回";
		}
		title.m_rightActionHint = "完成";
	}
	
	@Override
	public void handleRightAction(){
		String city = GlobalDataManager.getInstance().getCityEnglishName();
		String category = GlobalDataManager.getInstance().getCategoryEnglishName();
		if(city != null && category != null && city.length() > 0 && category.length() > 0){
			Intent intent = new Intent(BROADCAST_CITY_AND_CAT_SELECTED);
			getActivity().sendBroadcast(intent);
//			finishFragment();
		}else{
			ViewUtil.showToast(getActivity(), "请选择城市和类目", false);
		}
	}

}
package com.baixing.view.fragment;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.baixing.activity.BaseFragment;
import com.baixing.activity.BaseFragment.TitleDef;
import com.baixing.adapter.CommonItemAdapter;
import com.baixing.data.GlobalDataManager;
import com.baixing.entity.Category;
import com.chencang.core.R;

public class FirstCateFragment extends BaseFragment implements OnItemClickListener{

	@Override
	protected View onInitializeView(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		View v =  inflater.inflate(R.layout.firstcategory, null);
		v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		ListView lv = (ListView)v.findViewById(R.id.firstcategory);
		
		
		List<Category> all = GlobalDataManager.getInstance().getFirstLevelCategory();
		List<String> allNames = new ArrayList<String>();
		for(int i = 0; i < all.size(); ++ i){
			allNames.add(all.get(i).getName());
		}
		
		CommonItemAdapter adapter = new CommonItemAdapter(this.getAppContext(), allNames, 100, false);
		
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(this);
		return v;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		List<Category> all = GlobalDataManager.getInstance().getFirstLevelCategory();
		if(arg2 >= 0 && arg2 < all.size()){
			Bundle bundle = new Bundle();
			bundle.putSerializable("cates", all.get(arg2));
			bundle.putBoolean("isPost", false);
			pushFragment(new SecondCateFragment(), bundle);
		}
	}
	
	@Override
	public void onFragmentBackWithData(int msg, Object obj){
		if(msg == SecondCateFragment.MSG_SEL_CATEGORY_SUCCEED){
			this.finishFragment(msg, obj);
		}
	}
	
	@Override
	protected void initTitle(TitleDef title) {
		title.m_title = "选择类目";
		String cate = GlobalDataManager.getInstance().getCategoryEnglishName();
		if(cate != null && cate.length() > 0){
			title.m_leftActionHint = "返回";
		}
	}
	
}
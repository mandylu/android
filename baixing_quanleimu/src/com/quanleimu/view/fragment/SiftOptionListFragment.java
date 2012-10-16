package com.quanleimu.view.fragment;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.quanleimu.activity.BaseFragment;
import com.quanleimu.activity.BaseFragment.TabDef;
import com.quanleimu.activity.BaseFragment.TitleDef;
import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.activity.R;
import com.quanleimu.adapter.CommonItemAdapter;
import com.quanleimu.entity.Filterss;
import com.quanleimu.entity.labels;

public class SiftOptionListFragment extends BaseFragment {
	public int temp = -1;
	public List<labels> filterLabels = new ArrayList<labels>();
	public ListView lv;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Bundle bundle = getArguments();
		List<Filterss> listFilterss = QuanleimuApplication.getApplication().getListFilterss();
		temp = bundle.getInt("temp");
		filterLabels.addAll(listFilterss.get(temp).getLabelsList());
		labels nolimit = new labels();
		nolimit.setLabel("不限");
		filterLabels.add(0, nolimit);
	
		View v = inflater.inflate(R.layout.siftlist, null);
		
		lv = (ListView) v.findViewById(R.id.lv_test);
		if(listFilterss != null && listFilterss.size() != 0)
		{
			CommonItemAdapter adapter = new CommonItemAdapter(this.getActivity(), filterLabels, 10, true);
			adapter.setHasArrow(false);
			lv.setAdapter(adapter);
		}
		
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				
				Bundle bundle = getArguments();
//				if(null != m_viewInfoListener){
					if(arg2 != 0){
						bundle.putString("value", QuanleimuApplication.getApplication().getListFilterss().get(temp)
								.getValuesList().get(arg2-1).getValue());
						// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
						bundle.putString("label", QuanleimuApplication.getApplication().getListFilterss().get(temp)
								.getLabelsList().get(arg2-1).getLabel());
						bundle.remove("all");
					}else{
						bundle.putString("all", "不限");
					}
					
//					m_viewInfoListener.onBack(1234, bundle);
					finishFragment(1234, bundle);
//				}
			}
		});
		
		return v;
	}
	
	@Override
	public void initTitle(TitleDef title){
		title.m_visible = true;
		title.m_title = getArguments().getString(BaseFragment.ARG_COMMON_TITLE);
		title.m_leftActionHint = getArguments().getString(BaseFragment.ARG_COMMON_BACK_HINT); 
	}
	
	@Override
	public void initTab(TabDef tab){
		tab.m_visible = false;
	}
	
}

package com.quanleimu.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.activity.R;
import com.quanleimu.entity.Filterss;
import com.quanleimu.entity.labels;
import com.quanleimu.adapter.CommonItemAdapter;
public class SiftOptionListView extends BaseView {

	public int temp = -1;
	public List<labels> filterLabels = new ArrayList<labels>();
	public ListView lv;
	
	Bundle bundle = null;
	
	protected void Init(){		
		List<Filterss> listFilterss = QuanleimuApplication.getApplication().getListFilterss();
		temp = bundle.getInt("temp");
		filterLabels.addAll(listFilterss.get(temp).getLabelsList());
		labels nolimit = new labels();
		nolimit.setLabel("不限");
		filterLabels.add(0, nolimit);
	
		LayoutInflater inflater = LayoutInflater.from(getContext());
		this.addView(inflater.inflate(R.layout.siftlist, null));
		
		lv = (ListView) findViewById(R.id.lv_test);
		if(listFilterss != null && listFilterss.size() != 0)
		{
			CommonItemAdapter adapter = new CommonItemAdapter(this.getContext(), filterLabels, 10);
			adapter.setHasArrow(false);
			lv.setAdapter(adapter);
		}
		
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				
				if(null != m_viewInfoListener){
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
					
					m_viewInfoListener.onBack(1234, bundle);
				}
			}
		});
	}
	
	public SiftOptionListView(Context context, Bundle bundle_){
		super(context);		
		bundle = bundle_;
		Init();
	}

	@Override
	public TitleDef getTitleDef(){
		TitleDef title = new TitleDef();
		title.m_visible = true;
		title.m_title = bundle.getString("title");
		title.m_leftActionHint = bundle.getString("back"); 
		return title;
	}
	
	@Override
	public TabDef getTabDef(){
		TabDef tab = new TabDef();
		tab.m_visible = false;
		return tab;
	}
}


package com.quanleimu.view;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.quanleimu.entity.Filterss;
import com.quanleimu.entity.PostGoodsBean;

import com.quanleimu.activity.BaseActivity;
import com.quanleimu.activity.R;
import com.quanleimu.adapter.CommonItemAdapter;
public class PostGoodsSelectionView extends BaseView {

	public int temp = -1;
	public List<Filterss> listFilterss = new ArrayList<Filterss>();
	private PostGoodsBean postBean;
	private List<String> list;
	public ListView lv;
	private int message;
	
	public PostGoodsSelectionView(BaseActivity context, Bundle bundle, PostGoodsBean bean, int backMessage){
		super(context, bundle);
		postBean = bean;
		message = backMessage;
		init();
	}
	

	private void init(){
		LayoutInflater inflater = LayoutInflater.from(this.getContext());
		View v = inflater.inflate(R.layout.postgoodsselectionview, null);
		this.addView(v);

		list = postBean.getLabels();
		lv = (ListView) v.findViewById(R.id.lv_test);
		CommonItemAdapter adapter = new CommonItemAdapter(this.getContext(), list);
		adapter.setHasArrow(false);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
 
				if(m_viewInfoListener != null){
					m_viewInfoListener.onBack(message, arg2);
				}

			}
		});
	}
	
	@Override
	public TitleDef getTitleDef(){
		TitleDef title = new TitleDef();
		title.m_visible = true;
		title.m_title = postBean.getDisplayName();
		title.m_leftActionHint = "发布";
		return title;
	}
	
	@Override
	public TabDef getTabDef(){
		TabDef tab = new TabDef();
		tab.m_visible = true;
		tab.m_tabSelected = ETAB_TYPE.ETAB_TYPE_PUBLISH;
		return tab;
	}

}

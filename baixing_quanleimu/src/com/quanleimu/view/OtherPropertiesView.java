package com.quanleimu.view;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.quanleimu.activity.BaseActivity;
import com.quanleimu.activity.R;
import com.quanleimu.adapter.CheckableAdapter;
import com.quanleimu.adapter.CheckableAdapter.CheckableItem;
public class OtherPropertiesView extends BaseView {
	public int temp = -1;
	private int message;
	private List<CheckableAdapter.CheckableItem>others = null;
	
	public OtherPropertiesView(BaseActivity context, Bundle bundle, List<String> others, int backMessage){
		super(context, bundle);
		message = backMessage;
		this.others = new ArrayList<CheckableAdapter.CheckableItem>();
		for(int i = 0; i < others.size(); ++ i){
			CheckableItem item = new CheckableItem();
			item.checked = false;
			item.txt = others.get(i);
			this.others.add(item);
		}
		init();
	}
	

	private void init(){
		LayoutInflater inflater = LayoutInflater.from(this.getContext());
		View v = inflater.inflate(R.layout.post_othersview, null);
		this.addView(v);

		ListView lv = (ListView) v.findViewById(R.id.post_other_list);
		final CheckableAdapter adapter = new CheckableAdapter(this.getContext(), others);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
				CheckableItem item = others.get(position);
				item.checked = !item.checked;
				others.set(position, item);
				adapter.setList(others);
			}
		});
	}
	
	@Override
	public boolean onRightActionPressed(){
		if(m_viewInfoListener != null){
			String lists = "";
			for(int i = 0; i < others.size(); ++ i){
				if(others.get(i).checked){
					lists += "," + i;
				}
			}
			if(lists.length() > 0){
				lists = lists.substring(1);
			}
			m_viewInfoListener.onBack(message, lists);
		}
		return true;
	}
	
	@Override
	public TitleDef getTitleDef(){
		TitleDef title = new TitleDef();
		title.m_visible = true;
		title.m_title = "请选择要填写的项目";
		title.m_leftActionHint = "发布";
		title.m_rightActionHint = "完成";
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

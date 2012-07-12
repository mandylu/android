package com.quanleimu.view;

import java.util.ArrayList;
import java.util.List;

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
	private int message;
	private List<CheckableAdapter.CheckableItem>others = null;
	private boolean singleSelection = false;
	private String title = "请选择要填写的项目"; 
	private String selected = "";
	
	public OtherPropertiesView(BaseActivity context, List<String> others, int backMessage, boolean singleSelect){
		super(context);
		message = backMessage;
		singleSelection = singleSelect;
		this.others = new ArrayList<CheckableAdapter.CheckableItem>();
		for(int i = 0; i < others.size(); ++ i){
			CheckableItem item = new CheckableItem();
			item.checked = false;
			item.txt = others.get(i);
			this.others.add(item);
		}
		init();
	}
	
	public void setTitle(String title){
		this.title = title;
	}
	
	public void setSelectedItems(String items){
		selected = items;
		if(selected == null) return;
		String[] selecteds = selected.split(",");
		for(int i = 0; i < selecteds.length; ++ i){
			for(int j = 0; j < others.size(); ++ j){
				CheckableItem item = others.get(j);
				if(selecteds[i].equals(item.txt)){
					item.checked =true;
					others.set(j, item);
				}
			}
		}
		ListView lv = (ListView)findViewById(R.id.post_other_list);
		if(lv != null){
			CheckableAdapter adapter = (CheckableAdapter)lv.getAdapter();
			if(null != adapter){
				adapter.setList(others);
			}
		}
	}

	private void init(){
		LayoutInflater inflater = LayoutInflater.from(this.getContext());
		View v = inflater.inflate(R.layout.post_othersview, null);
		this.addView(v);

		ListView lv = (ListView) v.findViewById(R.id.post_other_list);
		final CheckableAdapter adapter = new CheckableAdapter(this.getContext(), others, 10);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
				CheckableItem item = others.get(position);
				item.checked = !item.checked;
				others.set(position, item);
				adapter.setList(others);
				if(singleSelection && null != m_viewInfoListener){
					m_viewInfoListener.onBack(message, position);
				}
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
		title.m_title = this.title;
		title.m_leftActionHint = "发布";
		if(!singleSelection){
			title.m_rightActionHint = "完成";
		}
		return title;
	}
	
	@Override
	public TabDef getTabDef(){
		TabDef tab = new TabDef();
		tab.m_visible = false;
		tab.m_tabSelected = ETAB_TYPE.ETAB_TYPE_PUBLISH;
		return tab;
	}

}

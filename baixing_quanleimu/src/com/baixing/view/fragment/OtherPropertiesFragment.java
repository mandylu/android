package com.baixing.view.fragment;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.baixing.adapter.CheckableAdapter;
import com.baixing.adapter.CheckableAdapter.CheckableItem;
import com.quanleimu.activity.BaseFragment;
import com.quanleimu.activity.BaseFragment.TabDef;
import com.quanleimu.activity.BaseFragment.TitleDef;
import com.quanleimu.activity.R;

public class OtherPropertiesFragment extends BaseFragment {
	private List<CheckableAdapter.CheckableItem>others = null;
	private boolean singleSelection = false;
	private String title = "请选择要填写的项目"; 
	private String selected = "";
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getArguments();
		this.singleSelection = bundle.getBoolean("singleSelection", false);
		List<String> properties = (List) bundle.getSerializable("properties");
		this.selected = bundle.getString("selected");
		
		this.others = new ArrayList<CheckableAdapter.CheckableItem>();
		
		for(int i = 0; i < properties.size(); ++ i){
			CheckableItem item = new CheckableItem();
			item.checked = false;
			item.txt = properties.get(i);
			this.others.add(item);
		}
		
		if (selected != null)
		{
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
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.post_othersview, null);

		ListView lv = (ListView) v.findViewById(R.id.post_other_list);
		lv.setDivider(null);
		final CheckableAdapter adapter = new CheckableAdapter(this.getActivity(), others, 0x1FFFFFFF, false);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
				CheckableItem item = others.get(position);
				item.checked = !item.checked;
				others.set(position, item);
				adapter.setList(others);
				if(singleSelection){
//					m_viewInfoListener.onBack(message, position);
					finishFragment(requestCode, position);
				}
			}
		});
		
		return v;
	}
	
	
	@Override
	public void handleRightAction(){
//		if(m_viewInfoListener != null){
			String lists = "";
			for(int i = 0; i < others.size(); ++ i){
				if(others.get(i).checked){
					lists += "," + i;
				}
			}
			if(lists.length() > 0){
				lists = lists.substring(1);
			}
			finishFragment(requestCode, lists);
//			m_viewInfoListener.onBack(message, lists);
//		}
	}
	
	@Override
	public void initTitle(TitleDef title){
		title.m_visible = true;
		title.m_title = this.title;
		title.m_leftActionHint = "发布";
		if(!singleSelection){
			title.m_rightActionHint = "完成";
		}
	}
	
	@Override
	public void initTab(TabDef tab){
		tab.m_visible = false;
		tab.m_tabSelected = ETAB_TYPE.ETAB_TYPE_PUBLISH;
	}
	
	
	
	
}

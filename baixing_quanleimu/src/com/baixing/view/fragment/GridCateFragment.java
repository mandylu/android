package com.baixing.view.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.baixing.entity.FirstStepCate;
import com.baixing.util.Tracker;
import com.baixing.util.TrackConfig.TrackMobile.PV;
import com.quanleimu.activity.BaseFragment;
import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.activity.R;

public class GridCateFragment extends BaseFragment implements OnItemClickListener {
	private static final String []texts 	= {"物品交易", "车辆买卖", "房屋租售", "全职招聘", 
										   "兼职招聘", "求职简历", "交友活动", "宠物", 
										   "生活服务", "教育培训"};
	private static final int []icons 	= {R.drawable.icon_category_wupinjiaoyi, R.drawable.icon_category_car, 		R.drawable.icon_category_house, 	R.drawable.icon_category_quanzhi, 
										   R.drawable.icon_category_jianzhi,     R.drawable.icon_category_vita, 	R.drawable.icon_category_friend, 	R.drawable.icon_category_pet,
										   R.drawable.icon_category_service,     R.drawable.icon_category_education};
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	public boolean hasGlobalTab() {
		return true;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.firstcategory, null);
		v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < icons.length; i++)
		{
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("tvCategoryName", texts[i]);
			map.put("ivCategoryImage", icons[i]);			
			list.add(map);
		}
		
		SimpleAdapter adapter = new SimpleAdapter(getActivity(), list, R.layout.item_category, 
				new String[]{"tvCategoryName", "ivCategoryImage"}, new int[]{R.id.tvCategoryName, R.id.ivCategoryImage});
		ListView gridView = (ListView) v.findViewById(R.id.firstcategory);
		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(this);
		return v;
	}
	
	@Override
	public void onFragmentBackWithData(int message, Object obj){
		finishFragment(message, obj);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		this.pv = PV.POSTCATE1;
		Tracker.getInstance().pv(this.pv).end();
//		getView().requestFocus();
	}

	@Override
	public void initTitle(TitleDef title){
		title.m_visible = true;
		title.m_title = "选择类目";
//		title.m_leftActionHint = "返回";
	}
	
	@Override
	public void initTab(TabDef tab){
		tab.m_visible = true;
		tab.m_tabSelected = ETAB_TYPE.ETAB_TYPE_PUBLISH;
	}


	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int index, long arg3) {
		List<FirstStepCate> allCates = QuanleimuApplication.getApplication()
				.getListFirst();
		if (allCates == null || allCates.size() <= index)
			return;
		
		FirstStepCate selectedCate = null;
		String selText = texts[index];
		for (int i = 0; i < allCates.size(); ++i) {
			if (allCates.get(i).name.equals(selText)){
				selectedCate = allCates.get(i);
				break;
			}
		}
		
		if (this.requestCode != INVALID_REQUEST_CODE)
		{
			this.finishFragment(this.requestCode, selectedCate);
		}
		else
		{
			Bundle bundle = new Bundle();
			bundle.putInt(ARG_COMMON_REQ_CODE, this.requestCode);
			bundle.putSerializable("cates", selectedCate);
			bundle.putBoolean("isPost", true);
			pushFragment(new SecondCateFragment(), bundle);
		}
	}
}

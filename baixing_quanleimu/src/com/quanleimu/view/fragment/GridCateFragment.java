package com.quanleimu.view.fragment;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.quanleimu.activity.BaseFragment;
import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.activity.R;
import com.quanleimu.entity.FirstStepCate;
import com.quanleimu.util.TrackConfig.TrackMobile.PV;
import com.quanleimu.util.Tracker;
import com.quanleimu.widget.CustomizeGridView;
import com.quanleimu.widget.CustomizeGridView.GridInfo;
import com.quanleimu.widget.CustomizeGridView.ItemClickListener;
//import android.widget.AdapterView;
//import android.widget.AdapterView.OnItemClickListener;
//import com.quanleimu.adapter.GridAdapter;
//import com.quanleimu.adapter.GridAdapter.GridInfo;

public class GridCateFragment extends BaseFragment implements ItemClickListener {

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.gridcategory, null);
		int []icons 	= {R.drawable.icon_category_wupinjiaoyi, R.drawable.icon_category_car, 		R.drawable.icon_category_house, 	R.drawable.icon_category_quanzhi, 
						   R.drawable.icon_category_jianzhi,     R.drawable.icon_category_vita, 	R.drawable.icon_category_friend, 	R.drawable.icon_category_pet,
						   R.drawable.icon_category_service,     R.drawable.icon_category_education};
		String []texts 	= {"物品交易", "车辆买卖", "房屋租售", "全职招聘", 
						   "兼职招聘", "求职简历", "交友活动", "宠物", 
						   "生活服务", "教育培训"};
		
		List<GridInfo> gitems = new ArrayList<GridInfo>();
		for (int i = 0; i < icons.length; i++)
		{
			GridInfo gi = new GridInfo();
			gi.imgResourceId = icons[i];
			gi.text = texts[i];
			gitems.add(gi);
		}

//		GridAdapter adapter = new GridAdapter(this.getActivity());
//		adapter.setList(gitems, 3);
//		((GridView) v.findViewById(R.id.gridcategory)).setAdapter(adapter);
//		((GridView) v.findViewById(R.id.gridcategory)).setOnItemClickListener(this);
		CustomizeGridView grid = (CustomizeGridView) v.findViewById(R.id.gridcategory);
		grid.setData(gitems, 3);
		grid.setItemClickListener(this);
		return v;
	}

	@Override
	public void onItemClick(GridInfo info, int index) {
		List<FirstStepCate> allCates = QuanleimuApplication.getApplication()
				.getListFirst();
		if (allCates == null)
			return;
		if (info == null)
			return;
		FirstStepCate selectedCate = null;
		for (int i = 0; i < allCates.size(); ++i) {
			String selText = info.text;
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
	
	@Override
	public void onFragmentBackWithData(int message, Object obj){
		finishFragment(message, obj);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		Tracker.getInstance().pv(PV.POSTCATE1).end();
//		getView().requestFocus();
	}

	@Override
	public void initTitle(TitleDef title){
		title.m_visible = true;
		title.m_title = "选择类目";
		title.m_leftActionHint = "返回";
	}
	
	@Override
	public void initTab(TabDef tab){
		tab.m_visible = true;
		tab.m_tabSelected = ETAB_TYPE.ETAB_TYPE_PUBLISH;
	}
}

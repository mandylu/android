package com.quanleimu.view.fragment;

import java.util.ArrayList;
import java.util.List;

import com.quanleimu.activity.BaseFragment;
import com.quanleimu.activity.BaseFragment.TabDef;
import com.quanleimu.activity.BaseFragment.TitleDef;
import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.activity.R;
import com.quanleimu.adapter.GridAdapter;
import com.quanleimu.adapter.GridAdapter.GridInfo;
import com.quanleimu.entity.FirstStepCate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;

public class GridCateFragment extends BaseFragment implements OnItemClickListener {

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.gridcategory, null);

		List<GridInfo> gitems = new ArrayList<GridInfo>();
		GridInfo gi = new GridInfo();
		gi.imgResourceId = R.drawable.ershou;
		gi.text = "物品交易";
		gitems.add(gi);
		gi = new GridInfo();
		gi.imgResourceId = R.drawable.cheliang;
		gi.text = "车辆买卖";
		gitems.add(gi);
		gi = new GridInfo();
		gi.imgResourceId = R.drawable.fang;
		gi.text = "房屋租售";
		gitems.add(gi);
		gi = new GridInfo();
		gi.imgResourceId = R.drawable.gongzuo;
		gi.text = "全职招聘";
		gitems.add(gi);
		gi = new GridInfo();
		gi.imgResourceId = R.drawable.jianzhi;
		gi.text = "兼职招聘";
		gitems.add(gi);
		gi = new GridInfo();
		gi.imgResourceId = R.drawable.qiuzhi;
		gi.text = "求职简历";
		gitems.add(gi);
		gi = new GridInfo();
		gi.imgResourceId = R.drawable.huodong;
		gi.text = "交友活动";
		gitems.add(gi);
		gi = new GridInfo();
		gi.imgResourceId = R.drawable.chongwuleimu;
		gi.text = "宠物";
		gitems.add(gi);
		gi = new GridInfo();
		gi.imgResourceId = R.drawable.fuwu;
		gi.text = "生活服务";
		gitems.add(gi);
		gi = new GridInfo();
		gi.imgResourceId = R.drawable.jiaoyupeixun;
		gi.text = "教育培训";
		gitems.add(gi);

		GridAdapter adapter = new GridAdapter(this.getActivity());
		adapter.setList(gitems, 3);
		((GridView) v.findViewById(R.id.gridcategory)).setAdapter(adapter);
		((GridView) v.findViewById(R.id.gridcategory)).setOnItemClickListener(this);
		
		return v;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		List<FirstStepCate> allCates = QuanleimuApplication.getApplication()
				.getListFirst();
		if (allCates == null)
			return;
		if (arg1.getTag() == null)
			return;
		for (int i = 0; i < allCates.size(); ++i) {
			String selText = ((GridAdapter.GridHolder) arg1.getTag()).text.getText().toString();
			if (allCates.get(i).name.equals(selText)){
				Bundle bundle = new Bundle();
				bundle.putInt(ARG_COMMON_REQ_CODE, this.requestCode);
				bundle.putSerializable("cates", allCates.get(i));
				bundle.putBoolean("isPost", true);
				pushFragment(new SecondCateFragment(), bundle);
			}
		}
	}
	
	@Override
	public void onFragmentBackWithData(int message, Object obj){
		finishFragment(message, obj);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		getView().requestFocus();
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

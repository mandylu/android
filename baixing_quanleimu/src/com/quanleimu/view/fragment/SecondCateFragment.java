package com.quanleimu.view.fragment;

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
import com.quanleimu.activity.R;
import com.quanleimu.adapter.CommonItemAdapter;
import com.quanleimu.entity.FirstStepCate;
import com.quanleimu.entity.SecondStepCate;

public class SecondCateFragment extends BaseFragment implements OnItemClickListener{
	
	private boolean isPost = false;
	private FirstStepCate cate = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		isPost = getArguments().getBoolean("isPost", false);
		cate = (FirstStepCate) getArguments().getSerializable("cates");
	}
	
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.post_othersview, null);

		CommonItemAdapter adapter = new CommonItemAdapter(this.getActivity(), cate.getChildren(), 0x1FFFFFFF, false);
		ListView lvContent = (ListView)v.findViewById(R.id.post_other_list);
		lvContent.setAdapter(adapter);
		lvContent.setOnItemClickListener(this);
		
		return v;
	}

	@Override
	public void onResume(){
		super.onResume();
		ListView lvContent = (ListView)getView().findViewById(R.id.post_other_list);
		lvContent.requestFocus();
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (cate == null || cate.getChildren() == null
				|| cate.getChildren().size() <= arg2)
			return;
		SecondStepCate secCate = cate.getChildren().get(arg2);
		if (!isPost) {
			Bundle bundle = createArguments(secCate.getName(), "返回");
			bundle.putString("categoryEnglishName", secCate.getEnglishName());
			bundle.putString("siftresult", "");
			if (requestCode != INVALID_REQUEST_CODE) {
				String toRet = secCate.englishName + "," + secCate.name;
				finishFragment(requestCode, toRet);
			} else {
				bundle.putString("categoryName", secCate.getName());
				pushFragment(new GetGoodFragment(), bundle);
			}
		} else {
			String names = secCate.englishName + "," + secCate.name;
			if (requestCode != INVALID_REQUEST_CODE) {
				finishFragment(requestCode, names);
			} else {
				Bundle bundle = createArguments(null, null);
				bundle.putSerializable("cateNames", names);
				pushFragment(new PostGoodsFragment(), bundle);
				// m_viewInfoListener.onNewView(new
				// PostGoodsView((BaseActivity)getContext(), bundle,
				// names));//FIXME:
			}
		}
	}

	@Override
	public void initTitle(TitleDef title){
		title.m_visible = true;
		title.m_title = cate.getName();
		title.m_leftActionHint = "返回";
	}
	
	@Override
	public void initTab(TabDef tab){
		tab.m_visible = true;
		tab.m_tabSelected = ETAB_TYPE.ETAB_TYPE_PUBLISH;
	}

}

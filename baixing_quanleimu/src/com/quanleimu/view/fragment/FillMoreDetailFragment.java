package com.quanleimu.view.fragment;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.quanleimu.activity.BaseFragment;
import com.quanleimu.activity.BaseFragment.TabDef;
import com.quanleimu.activity.BaseFragment.TitleDef;
import com.quanleimu.activity.R;
import com.quanleimu.entity.PostGoodsBean;

public class FillMoreDetailFragment extends BaseFragment {
	private LinkedHashMap<String, PostGoodsBean> beans = null;
	private List<String> details = null;
	PostParamsHolder params;
	private LinearLayout llDetails = null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle bundle = getArguments();
		
		this.beans = new LinkedHashMap<String, PostGoodsBean>();
		if (bundle.containsKey("beans"))
		{
			this.beans.putAll((Map) bundle.getSerializable("beans"));
		}
		this.details = (List) bundle.getSerializable("details");
		
		PostParamsHolder existingValues = (PostParamsHolder) bundle.getSerializable("existing");
		if(existingValues != null) {
			params = existingValues;
		}
		else
		{
			params = new PostParamsHolder();
		}
	}
	
	@Override
	public void initTitle(TitleDef title){
		title.m_visible = true;
		title.m_title = "填写更多细节";
		title.m_leftActionHint = "返回";
//		title.m_rightActionHint = "完成";
		title.m_rightActionImg = -1;//FIXME:
	}
	
	@Override
	public void initTab(TabDef tab){
		tab.m_visible = false;
	}	
	
	@Override
	public void onResume(){
		super.onResume();
		setEditTextValue();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		ViewGroup v = (ViewGroup)inflater.inflate(R.layout.fillmoredetail, null);

		if(beans == null || details == null) return v;
		llDetails = (LinearLayout)v.findViewById(R.id.layoutdetails);
		for(int i = 0; i < details.size(); ++ i){
			PostGoodsBean bean = beans.get(details.get(i));
			if(bean == null) continue;
			ViewGroup layout = PostGoodsFragment.createItemByPostBean(bean, this);
			if(layout != null){
				llDetails.addView(layout);
				TextView border = new TextView(getActivity());
				border.setLayoutParams(new LayoutParams(
						LayoutParams.FILL_PARENT, 1, 1));
				border.setBackgroundResource(R.drawable.list_divider);
				if(bean.getControlType().equals("input")){
					((TextView)layout.findViewById(R.id.postinput)).setText("");
				}
				llDetails.addView(border);
			}
			if(params.containsKey(bean.getDisplayName())){
				String value = params.getData(bean.getDisplayName());
				if(value == null || value.equals(""))continue;
				if(bean.getUnit() != null && !bean.getUnit().equals("")){
					value = value.replace(bean.getUnit(), "");
				}
				View control = (View)layout.getTag(PostGoodsFragment.HASH_CONTROL);
				if(control instanceof CheckBox){
					if(value.contains(((CheckBox)control).getText())){
						((CheckBox)control).setChecked(true);
					}
					else{
						((CheckBox)control).setChecked(false);
					}
				}else if(control instanceof EditText){					
					((EditText)control).setText(value);
				}else if(control instanceof TextView){
					String[] values = value.split(",");
					String displayName = "";
					if(bean.getLabels() != null){
						for(int j = 0; j < bean.getValues().size(); ++ j){
							for(int m = 0; m < values.length; ++ m){
								if(bean.getValues().get(j).equals(values[m])){
									displayName += "," + bean.getLabels().get(j);
								}
							}
						}
					}
					if(displayName.length() > 0 && displayName.charAt(0) == ','){
						displayName = displayName.substring(1);
					}
					((TextView)control).setText(displayName);
				}
			}
		}
		
		return v;
	}
	
	@Override
	public void onPause(){
		PostGoodsFragment.extractInputData(llDetails, params);
		super.onPause();
	}
	
	@Override
	public void onFragmentBackWithData(int message, Object obj){
		PostGoodsFragment.fetchResultFromViewBack(message, obj, llDetails, params);
		setEditTextValue();
	}
	
	///////seems a bullshit bug of android, 
	///////if the last item with inputbox is filled with some text, 
	///////whenever the view is backtotop again, 
	///////all the item with inputbox will be filled with the same content
	///////so force setting the content here, if you find any clue about this, tell me plz. xumengyi@baixing.com
	private void setEditTextValue(){
		for(int i = 0; i < llDetails.getChildCount(); ++ i){
			PostGoodsBean postGoodsBean = (PostGoodsBean)llDetails.getChildAt(i).getTag(PostGoodsFragment.HASH_POST_BEAN);
			if(postGoodsBean == null) continue;
			
			if (postGoodsBean.getControlType().equals("input") 
					|| postGoodsBean.getControlType().equals("textarea")) {
				EditText et = (EditText)llDetails.getChildAt(i).getTag(PostGoodsFragment.HASH_CONTROL);
				if(et != null){
					if(params.containsKey(postGoodsBean.getDisplayName())) 
						et.setText(params.getData(postGoodsBean.getDisplayName()));
					
				}
			}
		}
		
	}
	
	@Override
	public void handleRightAction(){
		PostGoodsFragment.extractInputData(llDetails, params);
		finishFragment(requestCode, params);
	}
	
	
}

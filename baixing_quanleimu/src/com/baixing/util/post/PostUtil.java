//xumengyi@baixing.com
package com.baixing.util.post;

import java.util.List;
import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.baixing.data.GlobalDataManager;
import com.baixing.entity.Ad;
import com.baixing.entity.PostGoodsBean;
import com.baixing.view.fragment.MultiLevelSelectionFragment;
import com.baixing.view.fragment.PostParamsHolder;
import com.quanleimu.activity.R;

public class PostUtil{
	public static String getDisplayValue(PostGoodsBean bean, Ad detail, String detailKey){
		if(bean == null || detail == null || detailKey == null || detailKey.equals(""))return "";
		String value = detail.getValueByKey(detailKey);
		String displayValue = "";
		if(bean.getControlType().equals("input") || bean.getControlType().equals("textarea")){
			displayValue = detail.getValueByKey(detailKey);
			if(displayValue != null && !bean.getUnit().equals("")){
				int pos = displayValue.lastIndexOf(bean.getUnit());
				if(pos != -1){
					displayValue = displayValue.substring(0, pos);
				}
			}
		}else if(bean.getControlType().equals("select") || bean.getControlType().equals("checkbox")){
			List<String> beanVs = bean.getValues();
			if(beanVs != null){
				for(int t = 0; t < beanVs.size(); ++ t){
					if(bean.getControlType().equals("checkbox") && bean.getLabels() != null && bean.getLabels().size() > 1){
						if(value.contains(beanVs.get(t))){
							displayValue += (displayValue.equals("") ? "" : ",") + bean.getLabels().get(t);
							continue;
						}
					}
					if(beanVs.get(t).equals(value)){
						displayValue = bean.getLabels().get(t);
						break;
					}
				}
			}
			if(displayValue.equals("")){
				String _sValue = detail.getValueByKey(detailKey + "_s"); 
				if(_sValue != null && !_sValue.equals("")){
					displayValue = _sValue;
				}
			}
		}
		return displayValue;
	}
	
	static public void extractInputData(ViewGroup vg, PostParamsHolder params){
		if(vg == null) return;
		for(int i = 0; i < vg.getChildCount(); ++ i){
			PostGoodsBean postGoodsBean = (PostGoodsBean)vg.getChildAt(i).getTag(PostCommonValues.HASH_POST_BEAN);
			if(postGoodsBean == null) continue;
			
			if (postGoodsBean.getControlType().equals("input") 
					|| postGoodsBean.getControlType().equals("textarea")) {
				EditText et = (EditText)vg.getChildAt(i).getTag(PostCommonValues.HASH_CONTROL);
				if(et != null){
					params.put(postGoodsBean.getName(),  et.getText().toString(), et.getText().toString());
				}
			}
			else if(postGoodsBean.getControlType().equals("checkbox")){
				if(postGoodsBean.getValues().size() == 1){
					CheckBox box = (CheckBox)vg.getChildAt(i).getTag(PostCommonValues.HASH_CONTROL);
					if(box != null){
						if(box.isChecked()){
							params.put(postGoodsBean.getName(), postGoodsBean.getValues().get(0),postGoodsBean.getValues().get(0));
						}
						else{
							params.remove(postGoodsBean.getName());
						}
					}
				}
			}
		}
	}
	
	public static boolean fetchResultFromViewBack(int message, Object obj, ViewGroup vg, PostParamsHolder params){//??
		if(vg == null) return false;
		
		boolean match = false;
		for(int i = 0; i < vg.getChildCount(); ++ i){
			View v = vg.getChildAt(i);
			PostGoodsBean bean = (PostGoodsBean)v.getTag(PostCommonValues.HASH_POST_BEAN);
			if(bean == null) continue;
			if(bean.getName().hashCode() == message){
				TextView tv = (TextView)v.getTag(PostCommonValues.HASH_CONTROL);
				if(obj instanceof Integer){					
					String txt = bean.getLabels().get((Integer)obj);
					String txtValue = bean.getValues().get((Integer)obj);
//					postMap.put(bean.getDisplayName(), txtValue);
					if(tv != null){
						tv.setText(txt);
					}
					match = true;
					params.put(bean.getName(), txt, txtValue);
				}
				else if(obj instanceof String){
					String check = (String)obj;
					String[] checks = check.split(",");
					String value = "";
					String txt = "";
					for(int t = 0; t < checks.length; ++ t){
						if(checks[t].equals(""))continue;
		 				txt += "," + bean.getLabels().get(Integer.parseInt(checks[t]));
						value += "," + bean.getValues().get(Integer.parseInt(checks[t]));
					}
					if(txt.length() > 0){
						txt = txt.substring(1);
					}
					if(value.length() > 0){
						value = value.substring(1);
					}
					if(tv != null){
//						tv.setWidth(vg.getWidth() * 2 / 3);
						tv.setText(txt);
					}
					match = true;
					params.put(bean.getName(), txt, value);
				}
				else if(obj instanceof MultiLevelSelectionFragment.MultiLevelItem){
					if(tv != null){
						tv.setText(((MultiLevelSelectionFragment.MultiLevelItem)obj).txt);
					}
					match = true;
					params.put(bean.getName(), ((MultiLevelSelectionFragment.MultiLevelItem)obj).txt, ((MultiLevelSelectionFragment.MultiLevelItem)obj).id);
				}
			}
		}
		
		return match;
	}	
	
	static public ViewGroup createItemByPostBean(PostGoodsBean postBean,Context context){
		ViewGroup layout = null;
		if (postBean.getControlType().equals("input")) {
			LayoutInflater inflater = LayoutInflater.from(context);
			View v = postBean.getName().equals(PostCommonValues.STRING_DETAIL_POSITION) ? 
					inflater.inflate(R.layout.item_post_location, null) : 
						inflater.inflate(R.layout.item_post_edit, null);

			((TextView)v.findViewById(R.id.postshow)).setText(postBean.getDisplayName());

			EditText text = (EditText)v.findViewById(R.id.postinput);
			v.setTag(PostCommonValues.HASH_POST_BEAN, postBean);
			v.setTag(PostCommonValues.HASH_CONTROL, text);
			if(postBean.getNumeric() != 0){
				text.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
			}
			
			if (postBean.getName().equals("contact")) {
				text.setInputType(InputType.TYPE_CLASS_PHONE);
			}
			
			if (!postBean.getUnit().equals("")) {
				((TextView)v.findViewById(R.id.postunit)).setText(postBean.getUnit());
			}
			layout = (ViewGroup)v;
		} else if (postBean.getControlType().equals("select")) {//select的设置
			LayoutInflater inflater = LayoutInflater.from(context);
			View v = inflater.inflate(R.layout.item_post_select, null);	
			((TextView)v.findViewById(R.id.postshow)).setText(postBean.getDisplayName());
			v.setTag(PostCommonValues.HASH_POST_BEAN, postBean);
			v.setTag(PostCommonValues.HASH_CONTROL, v.findViewById(R.id.posthint));
			layout = (ViewGroup)v;
		}
		else if (postBean.getControlType().equals("checkbox")) {
			LayoutInflater inflater = LayoutInflater.from(context);

			if(postBean.getLabels().size() > 1){
				View v = inflater.inflate(R.layout.item_post_select, null);
				((TextView)v.findViewById(R.id.postshow)).setText(postBean.getDisplayName());
				v.setTag(PostCommonValues.HASH_POST_BEAN, postBean);
				v.setTag(PostCommonValues.HASH_CONTROL, v.findViewById(R.id.posthint));
				layout = (ViewGroup)v;
			}
			else{
				View v = inflater.inflate(R.layout.item_text_checkbox, null);
				v.findViewById(R.id.divider).setVisibility(View.GONE);
				((TextView)v.findViewById(R.id.checktext)).setText(postBean.getDisplayName());
				v.findViewById(R.id.checkitem).setTag(postBean.getDisplayName());
				v.setTag(PostCommonValues.HASH_POST_BEAN, postBean);
				v.setTag(PostCommonValues.HASH_CONTROL, v.findViewById(R.id.checkitem));	
				layout = (ViewGroup)v;				
			}
		} else if (postBean.getControlType().equals("textarea")) {
			LayoutInflater inflater = LayoutInflater.from(context);
			View v = inflater.inflate(R.layout.item_post_description, null);
			((TextView)v.findViewById(R.id.postdescriptionshow)).setText(postBean.getDisplayName());

			EditText descriptionEt = (EditText)v.findViewById(R.id.postdescriptioninput);

			if(postBean.getName().equals(PostCommonValues.STRING_DESCRIPTION))//description is builtin keyword
			{
				String personalMark = GlobalDataManager.getInstance().getPersonMark();
				if(personalMark != null && personalMark.length() > 0){
					personalMark = "\n\n" + personalMark;
					descriptionEt.setText(personalMark);
				}
			}
			
			v.setTag(PostCommonValues.HASH_POST_BEAN, postBean);
			v.setTag(PostCommonValues.HASH_CONTROL, descriptionEt);
			layout = (ViewGroup)v;
		}//获取到item的layout
		
		return layout;
	}

	static public boolean inArray(String item, String[] array){
		for(int i = 0; i < array.length; i++){
			if(item.equals(array[i])){
				return true;
			}
		}
		return false;
	}
	
	static public void adjustMarginBottomAndHeight(View view){
		LinearLayout.LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
		if (layoutParams == null){
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
		}
		layoutParams.bottomMargin = view.getContext().getResources().getDimensionPixelOffset(R.dimen.post_marginbottom);		
		layoutParams.height = view.getResources().getDimensionPixelOffset(R.dimen.post_item_height);
		view.setLayoutParams(layoutParams);
	}
}
package com.baixing.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baixing.activity.BaseFragment;
import com.baixing.activity.BaseFragment.TitleDef;
import com.baixing.data.GlobalDataManager;
import com.baixing.entity.BXLocation;
import com.baixing.util.LocationService;
import com.baixing.util.ViewUtil;
import com.quanleimu.activity.R;

public class ContactAndAddressDetailFragment extends BaseFragment{
	private boolean isContact = false;
	public static final int MSG_RET_CODE = 0xffff1100;
	
	@Override
	public void initTitle(TitleDef title){
		title.m_visible = true;
		title.m_leftActionHint = "返回";
		
		title.m_rightActionHint = "完成";
		Bundle bundle = getArguments();
		if(bundle != null && bundle.containsKey(ARG_COMMON_TITLE)){
			title.m_title = bundle.getString(ARG_COMMON_TITLE);					
		}		
	}
	
	@Override
	public void onResume(){
		super.onResume();
		final View input = isContact ? getView().findViewById(R.id.contact_edit) : getView().findViewById(R.id.postinput);
		if(input != null){
			input.postDelayed(new Runnable(){
				@Override
				public void run(){
					input.requestFocus();
					InputMethodManager inputMgr = 
							(InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
					inputMgr.showSoftInput(input, InputMethodManager.SHOW_FORCED);
				}			
			}, 100);
		}
	}
	
	static private String getLocationSummary(BXLocation location){
		String address = (location.detailAddress == null || location.detailAddress.equals("")) ? 
        		((location.subCityName == null || location.subCityName.equals("")) ?
						"" 
						: location.subCityName)
				: location.detailAddress;
        if(address == null || address.length() == 0) return "";
        if(location.adminArea != null && location.adminArea.length() > 0){
        	address = address.replaceFirst(location.adminArea, "");
        }
        if(location.cityName != null && location.cityName.length() > 0){
        	address = address.replaceFirst(location.cityName, "");
        }
        return address;
	}

	@Override
	protected View onInitializeView(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		final LinearLayout llEdit = (LinearLayout)inflater.inflate(R.layout.post_contact_addr_edit, null);
		
		final Bundle bundle = this.getArguments();
		if(bundle != null){
			if(bundle.containsKey("edittype")){
				if(bundle.getString("edittype").equals("contact")){
					isContact = true;
					llEdit.findViewById(R.id.edit_post_location).setVisibility(View.GONE);
					String value = GlobalDataManager.getInstance().getPhoneNumber();
					if(value == null || value.length() == 0 && bundle.containsKey("defaultValue")){
						value = bundle.getString("defaultValue");
					}
					((TextView)llEdit.findViewById(R.id.contact_edit)).setText(value);
				}else if(bundle.getString("edittype").equals("address")){
					isContact = false;
					llEdit.findViewById(R.id.ll_contact).setVisibility(View.GONE);
					String adr = GlobalDataManager.getInstance().getAddress();
					if(adr == null || adr.length() == 0){
						if(bundle.containsKey("defaultValue")){							
							adr = bundle.getString("defaultValue");
						}
					}
					((TextView)llEdit.findViewById(R.id.postinput)).setText(adr);
					llEdit.findViewById(R.id.location).setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							if(bundle.containsKey("location")){
								BXLocation location = (BXLocation)bundle.getSerializable("location");
								String address = getLocationSummary(location);
								((TextView)llEdit.findViewById(R.id.postinput)).setText(address);
							}else{
								llEdit.postDelayed(new Runnable(){
									@Override
									public void run(){
										ViewUtil.showToast(getActivity(), "无法获得当前位置", false);
									}
								}, 0);
							}
						}
					});
				}
			}
		}
		return llEdit;
	}
	
	private boolean saveContent(boolean finish){
		if(isContact){
			String text = ((TextView)getView().findViewById(R.id.contact_edit)).getText().toString();
			if(text == null || text.length() == 0 || text.trim().length() == 0){
				if(finish){
					ViewUtil.showToast(getAppContext(), "联系方式不能为空", false);
				}
				return false;
			}
			GlobalDataManager.getInstance().setPhoneNumber(((TextView)getView().findViewById(R.id.contact_edit)).getText().toString());
		}else{
			String text = ((TextView)getView().findViewById(R.id.postinput)).getText().toString();
			if(text == null || text.length() == 0){
				if(finish){
					ViewUtil.showToast(getAppContext(), "地址不能为空", false);
				}
				return false;
			}
			
			GlobalDataManager.getInstance().setAddress(((TextView)getView().findViewById(R.id.postinput)).getText().toString());
		}
		return true;
	}
	
	@Override
	public boolean handleBack(){
		saveContent(false);
		return false;
	}
	
	@Override
	public void handleRightAction(){
		if(!saveContent(true)) return;
		this.finishFragment();
	}
	
}
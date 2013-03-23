package com.baixing.anonymous;

import android.util.Pair;

import com.baixing.anonymous.AnonymousNetworkListener.ResponseData;
import com.baixing.data.GlobalDataManager;
import com.baixing.entity.UserBean;
import com.baixing.network.api.ApiParams;
import com.baixing.util.Util;
import com.baixing.util.post.PostCommonValues;

public class AccountService implements AnonymousNetworkListener{
	public final static String Action_Done = "action_done";
	private AnonymousExecuter anonyExecuter;
	private AnonymousAccountLogic anonyLogic;
	private Pair<String, String> nextActionAndStatus;
	private String currentStatus;
	private AnonymousNetworkListener actionListener;
	
	public void setActionListener(AnonymousNetworkListener listener){
		this.actionListener = listener;
	}
	
	@Override
	public void onActionDone(String action, ResponseData response) {
		// TODO Auto-generated method stub
		if(response.success){
			if(nextActionAndStatus != null && nextActionAndStatus.second != null){
				currentStatus = nextActionAndStatus.second;
				anonyLogic.setCurrentStatus(currentStatus);
				nextActionAndStatus = this.anonyLogic.getActionAndNextStatus();
				if(nextActionAndStatus != null){
					if(nextActionAndStatus.first.equals(BaseAnonymousLogic.Action_Custom)){
						if(actionListener != null){
							actionListener.onActionDone(action, response);
							actionListener.onActionDone(Action_Done, null);
						}
					}else{
						if(actionListener != null){
							actionListener.onActionDone(action, response);
						}						
						this.anonyExecuter.executeAction(nextActionAndStatus.first, mobile);
					}
				}
			}
		}else{
			if(actionListener != null){
				actionListener.onActionDone(action, response);
			}
			anonyLogic.setCurrentStatus(BaseAnonymousLogic.Status_Initialization);
		}		
	}
	
	private void initExecuter(){
		if(anonyExecuter == null){
			anonyExecuter = new AnonymousExecuter();
			anonyExecuter.setCallback(this);
		}	
	}
	
	public String getAccountStatus(String mobile){
		initExecuter();
		return AnonymousExecuter.retreiveAccountStatusSync(mobile);
	}
	
//	private boolean isUserLoginned(){
//		UserBean user = GlobalDataManager.getInstance().getAccountManager().getCurrentUser();
//		if(user != null && user.getPhone() != null && user.getPhone().equals(mobile)){
//			return true;
//		}
//		return false;
//	}
	
	private AccountService(){
		
	}
	
	private static AccountService instance;
	
	public static AccountService getInstance(){
		if(instance == null){
			instance = new AccountService();
		}
		return instance;
	}
	
	private String mobile;
	private String accountStatus;
	private String password;
	
	public void initStatus(String mobile){
		this.mobile = mobile;
	}
	
	public void initPassword(String password){
		this.password = password;
	}
	
	public void setCurrentStatus(String status){
		currentStatus = status;
	}
	
	public void start(String initAccountStatus, String initLogicStatus){
		initExecuter();
		accountStatus = initAccountStatus;
		if(accountStatus == null || accountStatus.length() == 0){
			accountStatus = getAccountStatus(mobile);
		}
		String deviceNumber = Util.getDevicePhoneNumber();
		String phoneStatus = (deviceNumber != null && deviceNumber.contains(mobile)) ? 
				BaseAnonymousLogic.Status_Number_Available : BaseAnonymousLogic.Status_Number_UnAvailable;
		anonyLogic = new AnonymousAccountLogic(accountStatus, phoneStatus);
		if(initLogicStatus != null && initLogicStatus.length() > 0 ){
			anonyLogic.setCurrentStatus(initLogicStatus);
		}else{
			UserBean user = GlobalDataManager.getInstance().getAccountManager().getCurrentUser();
			if(user != null && user.getPhone() != null){
				if(user.getPhone().equals(mobile)){
					anonyLogic.setCurrentStatus(BaseAnonymousLogic.Status_Loginned);
				}
			}
		}
		nextActionAndStatus = anonyLogic.getActionAndNextStatus();
		currentStatus = anonyLogic.getCurrentStatus();

		if(nextActionAndStatus != null){
			if(nextActionAndStatus.first.equals(BaseAnonymousLogic.Action_Custom)){
				if(actionListener != null){
					actionListener.onActionDone(Action_Done, null);
				}
			}else{
				anonyExecuter.executeAction(nextActionAndStatus.first, this.mobile);
			}
		}
	}
	
	public void start(String initStatus){
		start(initStatus, null);
	}
	
	public void start(){
		start(null);
	}

	@Override
	public void beforeActionDone(String action, ApiParams outParams) {
		// TODO Auto-generated method stub
		if(actionListener != null){
			actionListener.beforeActionDone(action, outParams);
		}
//		if(action.equals(BaseAnonymousLogic.Action_AutoVerifiy) || action.equals(BaseAnonymousLogic.Action_Register)){
//			outParams.addParam("password", password);
//		}
	}
}
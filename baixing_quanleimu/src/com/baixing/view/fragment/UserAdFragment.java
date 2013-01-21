package com.baixing.view.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baixing.activity.BaseFragment;
import com.baixing.adapter.VadListAdapter;
import com.baixing.adapter.VadListAdapter.GroupItem;
import com.baixing.android.api.ApiError;
import com.baixing.android.api.ApiParams;
import com.baixing.android.api.cmd.BaseCommand;
import com.baixing.android.api.cmd.BaseCommand.Callback;
import com.baixing.android.api.cmd.HttpGetCommand;
import com.baixing.entity.Ad;
import com.baixing.entity.AdList;
import com.baixing.entity.UserProfile;
import com.baixing.jsonutil.JsonUtil;
import com.baixing.util.Communication;
import com.baixing.util.ErrorHandler;
import com.baixing.util.VadListLoader;
import com.baixing.util.ViewUtil;
import com.baixing.view.AdViewHistory;
import com.baixing.widget.PullToRefreshListView;
import com.quanleimu.activity.R;

public class UserAdFragment extends BaseFragment implements PullToRefreshListView.OnRefreshListener, VadListLoader.Callback {

	private static final int MSG_UPDATE_PROFILE = 1;
	private static final int MSG_LIST_UPDATE = 2;
		
	
	private int userId;
	private PostParamsHolder filterParamHolder = new PostParamsHolder();
	private UserProfile userProfile;
	private List<Ad> userAdList = new ArrayList<Ad>();
	private VadListLoader listLoader;
	private VadListAdapter adapter;
	
	@Override
	protected void handleMessage(Message msg, Activity activity, View rootView) {
		switch (msg.what) {
		case MSG_UPDATE_PROFILE:
			if (userProfile == null) {
				ViewUtil.postShortToastMessage(rootView, R.string.warning_fail_to_get_profile, 30);
			}
			
			reCreateTitle();
			refreshHeader();
			
			break;
		case MSG_LIST_UPDATE:
			rebuildPage(rootView);
			break;
		}
	}

	@Override
	protected void initTitle(TitleDef title) {
		title.m_leftActionHint = "返回";
		title.m_title = userProfile == null ? "" : userProfile.nickName;
	}

	@Override
	public boolean hasGlobalTab() {
		return false;
	}
	
	private PullToRefreshListView findAdListView() {
		if (getView() == null) {
			return null;
		}
		
		return (PullToRefreshListView) getView().findViewById(R.id.lvGoodsList);
	}
	
	public void onCreate(Bundle savedBundle) {
		super.onCreate(savedBundle);
		
		this.userId = getArguments().getInt("userId");
		String idStr = userId + "";
		filterParamHolder.put("userId", idStr, idStr);
	}

	@Override
	protected View onInitializeView(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.user_adlist, null);
		
		try {
			if (!Communication.isNetworkActive()) {
				ErrorHandler.getInstance().handleError(ErrorHandler.ERROR_NETWORK_UNAVAILABLE, null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		PullToRefreshListView lvGoodsList = (PullToRefreshListView) v.findViewById(R.id.lvGoodsList);
		adapter = new VadListAdapter(this.getActivity(), userAdList, null);
        adapter.setHasDelBtn(true);
		lvGoodsList.setAdapter(adapter);

		AdList gl = new AdList();
		gl.setData(userAdList == null ? new ArrayList<Ad>() : userAdList);
	
		listLoader = new VadListLoader(null, this, null, null);
		listLoader.setHasMore(false);
		listLoader.setGoodsList(gl);
		
		lvGoodsList.setOnRefreshListener(this);	
		
		return v;
	}
	
	public void onResume() {
		super.onResume();
	}
	
	private void updateData(VadListAdapter adapter, List<Ad> list)
	{
		GroupItem g = new GroupItem();
		g.resultCount = list.size();
		g.filterHint = "共发布" + list.size() + "条信息";
		
		ArrayList<GroupItem> gList = new ArrayList<VadListAdapter.GroupItem>();
		gList.add(g);
		
		adapter.setList(list, gList);
	}
	
	public void onStackTop(boolean isBack) {
		if (userProfile == null) {
			getProfile();
		}
		
		rebuildPage(getView());
	}
	
	private void rebuildPage(View rootView){		
		if(listLoader != null){
			listLoader.setCallback(this);
		}
		
		PullToRefreshListView listView = findAdListView();
		if (listLoader.getGoodsList().getData() != null && listLoader.getGoodsList().getData().size() > 0)
		{
			adapter = new VadListAdapter(getActivity(), listLoader.getGoodsList().getData(), AdViewHistory.getInstance());
			listView.setAdapter(adapter);
			updateData(adapter, listLoader.getGoodsList().getData());
			listView.setSelectionFromHeader(listLoader.getSelection());
		}
		else
		{
			adapter = new VadListAdapter(getActivity(), new ArrayList<Ad>(), AdViewHistory.getInstance());
			listView.setAdapter(adapter);
			listView.fireRefresh();
		}
		listView.onRefreshComplete();
		

	}
	
	private void getProfile() {
		
		ApiParams params = new ApiParams();
		params.addParam("userId", userId);
		BaseCommand cmd = HttpGetCommand.createCommand(0, "user_profile", params);
		cmd.execute(new Callback() {
			
			@Override
			public void onNetworkFail(int requstCode, ApiError error) {
				userProfile = null;
				handler.sendEmptyMessage(MSG_UPDATE_PROFILE);
			}
			
			@Override
			public void onNetworkDone(int requstCode, String responseData) {
				userProfile = UserProfile.from(responseData);
				handler.sendEmptyMessage(MSG_UPDATE_PROFILE);
			}
		});
	}
	
	@Override
	public void onRequestComplete(int respCode, Object data) {

		AdList adList = JsonUtil.getGoodsListFromJson(listLoader.getLastJson());
		if (adList == null || adList.getData().size() == 0) {
			if(null != userAdList) userAdList.clear();
			listLoader.setGoodsList(new AdList());
		}
		else{
			userAdList = adList.getData();
			if(userAdList != null){
				for(int i = 0; i < userAdList.size() - 1;){
					Ad ad = userAdList.get(i);
					if(!ad.getValueByKey("status").equals("0") 
							&& !ad.getValueByKey("status").equals("4")
							&& !ad.getValueByKey("status").equals("20")){
						userAdList.remove(i);
					}
					else {
						i++;
					}
				}
				AdList gl2 = new AdList();
				gl2.setData(userAdList);
				listLoader.setGoodsList(gl2);
			}
		}
		
		this.sendMessage(MSG_LIST_UPDATE, null);
	}

	@Override
	public void onRefresh() {
		ApiParams params = new ApiParams();
		params.addParam("query", filterParamHolder.toUrlString());
		
		listLoader.setRows(1000);
		listLoader.setParams(params);
		int msg = MSG_LIST_UPDATE;
		listLoader.startFetching(true, msg, msg, msg,Communication.isNetworkActive() ? Communication.E_DATA_POLICY.E_DATA_POLICY_NETWORK_UNCACHEABLE : Communication.E_DATA_POLICY.E_DATA_POLICY_ONLY_LOCAL);
	}

}

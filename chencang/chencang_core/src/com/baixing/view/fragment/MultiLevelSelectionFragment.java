package com.baixing.view.fragment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.baixing.activity.BaseFragment;
import com.baixing.adapter.BXAlphabetSortableAdapter.BXHeader;
import com.baixing.adapter.BXAlphabetSortableAdapter.BXPinyinSortItem;
import com.baixing.adapter.CheckableAdapter;
import com.baixing.adapter.CheckableAdapter.CheckableItem;
import com.baixing.adapter.CommonItemAdapter;
import com.baixing.data.GlobalDataManager;
import com.baixing.entity.PostGoodsBean;
import com.baixing.jsonutil.JsonUtil;
import com.baixing.network.api.ApiParams;
import com.baixing.network.api.BaseApiCommand;
import com.chencang.core.R;

public class MultiLevelSelectionFragment extends BaseFragment {
	private final static String[] munisipalities = {"北京", "上海", "重庆", "天津"};
	
	public static class MultiLevelItem extends Object implements Serializable {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 6707309053604383782L;
		public String txt;
		public String id;
		@Override
		public String toString(){
			return txt;
		}
	}
	private final int MESSAGE_GET_METAOBJ = 1;
	private List<MultiLevelItem>items = null;
	private String title = "请选择"; 
	private String json = null;
	private String id = null;
	ListAdapter adapter = null;
	private int remainLevel = 0;
	private ListView listView = null;
	private String selectedValue = null;
	
	@Override
	public void initTitle(TitleDef title){
		title.m_visible = true;
		title.m_title = this.title;
		title.m_leftActionHint = "返回";
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
//		if(null == adapter){
//			if(items == null || items.size() == 0){
//				pd = ProgressDialog.show(getContext(), "提示", "请稍候...");
//				pd.setCancelable(true);
//				pd.show();
//				(new Thread(new GetMetaDataThread(id))).start();
//			}
//			else{
//				initContent(remainLevel > 0);
//			}
//		}
	}
	
	@Override
	public void onStackTop(boolean isBack) {
		if(items == null || items.size() == 0){
			showProgress(R.string.dialog_title_info, R.string.dialog_message_waiting, true);
			(new Thread(new GetMetaDataThread(id))).start();
		}
		else{
			initContent(remainLevel > 0);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle bundle = getArguments();
		if (bundle.containsKey(ARG_COMMON_TITLE))
		{
			this.title = bundle.getString(ARG_COMMON_TITLE);
		}
		
		if (bundle.containsKey("selectedValue"))
		{
			this.selectedValue = bundle.getString("selectedValue");
		}
		
		if (bundle.containsKey("items"))
		{
			this.items = (List) bundle.getSerializable("items");
		}
		this.remainLevel = bundle.getInt("maxLevel");
		
		if (bundle.containsKey("metaId"))
		{
			this.id = bundle.getString("metaId");
		}
	}

	@Override
	public View onInitializeView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View v = inflater.inflate(R.layout.post_othersview, null);
		
		return v;
	}



	
	@Override
	protected void handleMessage(Message msg, Activity activity, View rootView) {

		switch (msg.what) {
		case MESSAGE_GET_METAOBJ:
			hideProgress();
			
			if(json != null){
				LinkedHashMap<String, PostGoodsBean> beans = JsonUtil.getPostGoodsBean(json);
				if(beans != null){
//					if(msg.obj != null){
//						QuanleimuApplication.putCacheNetworkRequest((String)msg.obj, json);
//					}
					PostGoodsBean bean = beans.get((String)beans.keySet().toArray()[0]);
					if(MultiLevelSelectionFragment.this.items == null || MultiLevelSelectionFragment.this.items.size() == 0){
						MultiLevelSelectionFragment.this.items = new ArrayList<MultiLevelItem>();
						if(bean.getLabels() != null){
							if(bean.getLabels().size() > 1){
								MultiLevelItem tAll = new MultiLevelItem();
								tAll.txt ="全部";
								tAll.id = null;
								MultiLevelSelectionFragment.this.items.add(tAll);
							}
							for(int i = 0; i < bean.getLabels().size(); ++ i){
								MultiLevelItem t = new MultiLevelItem();
								t.txt = bean.getLabels().get(i);
								t.id = bean.getValues().get(i);
								MultiLevelSelectionFragment.this.items.add(t);
							}
						}
						else{
//							if(m_viewInfoListener != null){
								MultiLevelItem nItem = new MultiLevelItem();
								nItem.id = MultiLevelSelectionFragment.this.id;
								nItem.txt = MultiLevelSelectionFragment.this.title;
								finishFragment(fragmentRequestCode, nItem);
//								m_viewInfoListener.onBack(message, nItem);
								return;
//							}
						}
					}
					else{
//						MultiLevelSelectionView.this.init(bean.getSubMeta().equals("1") || bean.getLabels().size() > 0);
					}
					MultiLevelSelectionFragment.this.initContent(MultiLevelSelectionFragment.this.remainLevel > 0); 
				}
			}
			break;
		}
	
	}
	
	private boolean isMunisipality(String name) {
		for (String m : munisipalities) {
			if (m.equals(name))
				return true;
		}
		return false;
	}

	private void initContent(final boolean hasNextLevel){
		final ListView lv = (ListView) getView().findViewById(R.id.post_other_list);
		if(lv!=null) listView = lv;
		lv.setDivider(null);
		
		final ArrayList<CheckableItem> checkList = new ArrayList<CheckableItem>();
		if(!hasNextLevel){
			for(int i = 0; i < items.size(); ++ i){
				CheckableItem t = new CheckableItem();
				t.txt = items.get(i).txt;
				t.id = items.get(i).id;
				String itemId = t.id != null ? t.id : this.id;
				t.checked = itemId != null ? itemId.equals(selectedValue) : false;
				checkList.add(t);
			}
			adapter = new CheckableAdapter(this.getActivity(), checkList, 10, true);
		}
		else{
			adapter = new CommonItemAdapter(this.getActivity(), items, 10, true);
//			adapter = new com.baixing.adapter.BXAlphabetAdapter(this.getContext(), items);
		}
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
				Object shit = adapter.getItem(position);
				if(adapter.getItem(position) instanceof BXHeader) return;
				if(position == 0 && (items.size() > 10 
						&& !(adapter.getItem(0) instanceof BXHeader) && !(adapter.getItem(0) instanceof BXPinyinSortItem))){
					Bundle bundle = createArguments(null, null);
					bundle.putBoolean("hasNextLevel", hasNextLevel);
					bundle.putSerializable("selections", hasNextLevel ? (ArrayList)items : checkList);
					pushFragment(new SelectionSearchFragment(), bundle);
					return;
				}
				if ((position == 1 || position == 0 || position == 2) && adapter.getItem(position).toString().equals("全部")){
					MultiLevelItem nItem = new MultiLevelItem();
					nItem.id = MultiLevelSelectionFragment.this.id;
					nItem.txt = nItem.id == null || nItem.id.equals("") ? 
							"全部" : MultiLevelSelectionFragment.this.title;
					//MultiLevelSelectionFragment.this.title.equals("请选择") ? "全部" : MultiLevelSelectionFragment.this.title;
//					m_viewInfoListener.onBack(message, nItem);
					finishFragment(fragmentRequestCode, nItem);
					return;
				}
				if(hasNextLevel){
					
						MultiLevelItem item = adapter.getItem(position) instanceof BXPinyinSortItem ? 
								(MultiLevelItem)((BXPinyinSortItem)adapter.getItem(position)).obj
								: (MultiLevelItem)adapter.getItem(position);
								
						if (isMunisipality(item.txt)) { // TODO: levels:"sheng,city"时，直辖市没有下一级了。 @zhongjiawu
							finishFragment(fragmentRequestCode, item);
						} else {
							Bundle bundle = createArguments(item.txt, null);
							bundle.putInt(ARG_COMMON_REQ_CODE, fragmentRequestCode);
							bundle.putInt("maxLevel", MultiLevelSelectionFragment.this.remainLevel - 1);
							bundle.putString("metaId", item.id);
							bundle.putString("selectedValue", selectedValue);
							pushFragment(new MultiLevelSelectionFragment(), bundle);
						}
//					}
				}
				else{
					CheckableItem item = adapter.getItem(position) instanceof BXPinyinSortItem ? 
							(CheckableItem)((BXPinyinSortItem)adapter.getItem(position)).obj
							: (CheckableItem)adapter.getItem(position);

					((CheckableAdapter)adapter).setItemCheckStatus(position, !item.checked);
					MultiLevelItem mItem = new MultiLevelItem();
					mItem.id = item.id;
					mItem.txt = item.txt;

//					if(null != m_viewInfoListener){
//						m_viewInfoListener.onBack(message, mItem);
//					}
					finishFragment(fragmentRequestCode, mItem);
				}
			}
		});		
	}
	
	class GetMetaDataThread implements Runnable {
		private String id;
		public GetMetaDataThread(String id) {
			this.id = id;
		}

		@Override
		public void run() {
			String apiName = "metaobject";
//			ArrayList<String> list = new ArrayList<String>();
			ApiParams params = new ApiParams();
			params.addParam("objIds", id);
//			list.add("objIds=" + id);
			try {
//				String url = Communication.getApiUrl(apiName, list);
				json = BaseApiCommand.createCommand(apiName, true, params).executeSync(GlobalDataManager.getInstance().getApplicationContext());//ApiClient.getInstance().invokeApi(Api.createGet(apiName), params);
//				json = Communication.getDataByUrl(url, false);
			} catch (Exception e) {
//				e.printStackTrace();
				Log.d("QLM", "fail to get meta object, caused by " + e.getMessage());
			}
			sendMessage(MESSAGE_GET_METAOBJ, null);
		}
	}
	
	@Override
	public void onFragmentBackWithData(int message, Object obj){
		if(message == SelectionSearchFragment.MSG_SELECTIONVIEW_BACK){
			if(adapter instanceof CommonItemAdapter && obj instanceof MultiLevelItem){
				Bundle bundle = createArguments(((MultiLevelItem)obj).txt, null);
				bundle.putInt(ARG_COMMON_REQ_CODE, fragmentRequestCode);
				bundle.putInt("maxLevel", MultiLevelSelectionFragment.this.remainLevel - 1);
				bundle.putString("metaId", ((MultiLevelItem)obj).id);
				pushFragment(new MultiLevelSelectionFragment(), bundle);
			}
			else if(adapter instanceof CheckableAdapter && obj instanceof CheckableItem){
				MultiLevelItem mItem = new MultiLevelItem();
				mItem.id = ((CheckableItem)obj).id;
				mItem.txt = ((CheckableItem)obj).txt;
	
//				if(null != m_viewInfoListener){
//					m_viewInfoListener.onBack(this.message, mItem);
//				}
				finishFragment(fragmentRequestCode, mItem);
			}
			return;
		}
//		if(this.m_viewInfoListener != null){
//			this.m_viewInfoListener.onBack(message, obj);
//		}
		finishFragment(fragmentRequestCode, obj);
	}
	
	public boolean hasGlobalTab()
	{
		return false;
	}
}

//xuweiyan@baixing.com
package com.baixing.widget;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.baixing.data.GlobalDataManager;
import com.baixing.entity.Category;
import com.baixing.entity.PostGoodsBean;
import com.baixing.jsonutil.JsonUtil;
import com.baixing.network.api.ApiError;
import com.baixing.network.api.ApiParams;
import com.baixing.network.api.BaseApiCommand;
import com.baixing.network.api.BaseApiCommand.Callback;
import com.baixing.util.ViewUtil;
import com.baixing.view.fragment.MultiLevelSelectionFragment.MultiLevelItem;
import com.chencang.core.R;

public class CustomDialogBuilder {

	private final int MESSAGE_GET_METAOBJ = 1;
	public static final int MSG_CATEGORY_SEL_BACK = 11;
	public static final int MSG_DIALOG_BACK_WITH_DATA = 12;
	static final public int HOLDER_TAG = "secondCateAdapter_itemholder".hashCode();

	public static final String ARG_COMMON_REQ_CODE = "reqestCode";
	private Context context = null;
	private int remainLevel = 0;
	private boolean hasNextLevel = false;
	private boolean isCategoryItem = false;
	private List items = null;
//	private List<MultiLevelItem> secondLevelItems = null;
	private String selectedValue = null;
	private String id = null;
	private String json = null;
	private Handler handler = null;
	private Object lastChoise = null;
	private ProgressDialog pd;
	private Handler delegateHandler;
	private int requestCode;
	
	public CustomDialogBuilder(Context context,Handler delegateHandler, Bundle bundle) {
		this.context = context;
		this.delegateHandler = delegateHandler;
		this.remainLevel = bundle.getInt("maxLevel");
		hasNextLevel = remainLevel > 0;
		
		this.items = (List) bundle.getSerializable("items");
		this.requestCode = bundle.getInt(ARG_COMMON_REQ_CODE);
		if (bundle.getInt(ARG_COMMON_REQ_CODE) == MSG_CATEGORY_SEL_BACK)
			isCategoryItem = true;
		if (bundle.containsKey("selectedValue") && bundle.getString("selectedValue")!=null)
		{
			this.selectedValue = bundle.getString("selectedValue");//selectedValue
		}
//		if (bundle.containsKey("metaId"))
//		{
//			this.id = bundle.getString("metaId");
//		}
	}
	
	public void start() {
		CustomDialog cd = getCustomDialog();
		cd.show();
		configCustomDialog(cd);
	}
	
	
	private void configCustomDialog(final CustomDialog cd) {
		
		ListView lv = cd.getListView();
		List<Map<String,Object>> firstLevelList = new ArrayList<Map<String,Object>>();
		if (isCategoryItem) {
			cd.setTitle("请选择分类");
			for (String item : (List<String>)items) {
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("tv", item);
				firstLevelList.add(map);
			}
		} else {
			cd.setTitle("请选择");
			for (MultiLevelItem item : (List<MultiLevelItem>)items) {
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("tv", item.toString());
				map.put("id", item.id);
				firstLevelList.add(map);
			}
		}
		
//		SimpleAdapter simpleAdapter = new SimpleAdapter(context, firstLevelList, R.layout.item_seccategory_simple2,
//				new String[]{"tv"}, new int[]{R.id.tv});
//		lv.setAdapter(simpleAdapter);
		
		configFirstLevel(cd, lv, firstLevelList);
		
		
	}
	
	protected final void showProgress(int titleResid, int messageResid, boolean cancelable) {
		String title = context.getString(titleResid);
		String message = context.getString(messageResid);
		showProgress(title, message, cancelable);
	}
	
	protected final void showProgress(String title, String message, boolean cancelable)
	{
		hideProgress();

        if (context != null)
		{
			pd = ProgressDialog.show(context, title, message);
			pd.setCancelable(cancelable);
            pd.setCanceledOnTouchOutside(cancelable);
		}
	}
	
	protected final void hideProgress()
	{
		if (pd != null && pd.isShowing())
		{
			pd.dismiss();
		}
	}
	
	private void sendMessage(int what, Object data) {
		Message message = null;
		if (handler != null) {
			message = handler.obtainMessage();
			message.what = what;
			if (data != null)
				message.obj = data;
			
			handler.sendMessage(message);
		}
	}
	
	private void configFirstLevel(final CustomDialog cd, final ListView lv, final List list) {
		//adapter
		FirstCateAdapter firstAdapter = new FirstCateAdapter(list);//List<Map<String,Object>> firstLevelList
		lv.setAdapter(firstAdapter);
		//lv set on item click listener
		
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				System.out.println("onItemClick");
				if (hasNextLevel) {
					System.out.println("hasNextLevel true");
					final List<Map<String,Object>> secondLevelList = new ArrayList<Map<String,Object>>();//
					if (isCategoryItem) {//分类模块
						System.out.println("isCategoryItem " + isCategoryItem);
						cd.setTitle("请选择分类");
//						List<FirstStepCate> allCates = QuanleimuApplication.getApplication().getListFirst();
						List<Category> allCates = GlobalDataManager.getInstance().getFirstLevelCategory();
						if (allCates == null || allCates.size() <= pos)
						{
							System.out.println("Reload category");
							GlobalDataManager.getInstance().loadCategorySync();//reload
							allCates = GlobalDataManager.getInstance().getFirstLevelCategory();//.getListFirst();//recheck
							if(allCates == null || allCates.size() <= pos){
								System.out.println("仁至义尽");
								return;
							}
						}
						Category selectedCate = null;
						String selText = (String)((Map<String,Object>)list.get(pos)).get("tv");//
						for (int i=0; i< allCates.size(); i++) {
							if (allCates.get(i).getName().equals(selText)) {
								selectedCate = allCates.get(i);
								break;
							}
						}
						
						Map<String,Object> backMap = new HashMap<String,Object>();
						backMap.put("tvCategoryName", "返回上一级");
						backMap.put("tvCategoryEnglishName", "back");
						secondLevelList.add(backMap);//
						List<Category> children = selectedCate.getChildren();
						for (Category cate : children) {
							Map<String,Object> map = new HashMap<String,Object>();
							map.put("tvCategoryName", cate.getName());
							map.put("tvCategoryEnglishName", cate.getEnglishName());
							secondLevelList.add(map);//
						}
						//configSecondLevel
						configSecondLevel(cd, lv, secondLevelList);//
					}//isCategoryItem over
					else {//not category Item,need Thread & handler
						handler = new Handler() {
							@Override
							public void handleMessage(Message msg) {
								switch(msg.what) {
									case MESSAGE_GET_METAOBJ:
									hideProgress();
									//json->list
									if (json != null) {
										LinkedHashMap<String,PostGoodsBean> beans = JsonUtil.getPostGoodsBean(json);
										if (beans != null) {
											PostGoodsBean bean = beans.get((String)beans.keySet().toArray()[0]);
//											List<MultiLevelItem> secondLevelItems = null;
//											if (CustomDialogBuilder.this.secondLevelItems == null || CustomDialogBuilder.this.secondLevelItems.size() == 0) {
												List<MultiLevelItem> secondLevelItems = new ArrayList<MultiLevelItem>();
												if (bean.getLabels() != null) {
													
													MultiLevelItem tBack = new MultiLevelItem();
													tBack.txt = "返回上一级";
													tBack.id = null;
													secondLevelItems.add(tBack);
													if (bean.getLabels().size() > 1) {
														MultiLevelItem tAll = new MultiLevelItem();
														MultiLevelItem selectedItem = (MultiLevelItem)msg.obj;
														tAll.txt = selectedItem.toString();
														tAll.id = selectedItem.id;
														secondLevelItems.add(tAll);
													}
													for (int i=0; i<bean.getLabels().size(); i++) {
														MultiLevelItem t = new MultiLevelItem();
														t.txt = bean.getLabels().get(i);
														t.id = bean.getValues().get(i);
														secondLevelItems.add(t);
													}
												}
												else {
//													showProgress(R.string.dialog_title_info, R.string.dialog_message_waiting, true);
//													CustomDialogBuilder.this.id = bean.get;
//													String txt = ((MultiLevelItem)(CustomDialogBuilder.this.items.get(pos))).toString();
//													(new Thread(new GetMetaDataThread(id,txt))).start();
													MultiLevelItem item = new MultiLevelItem();
													item.txt = bean.getDisplayName();
													item.id = bean.getName();
													CustomDialogBuilder.this.lastChoise = item;
													handleLastLevelChoice(cd);
													return;
												}
//											}
//											else {
//												
//											}
											//secondLevelItems -> list
											//List<MultiLevelItem> -> List<Map<String,Object>>
											//configSecondLevel
											configSecondLevel(cd, lv, secondLevelItems);										}
									}
									else{
										ViewUtil.showToast(context, "网络连接异常", false);
										return;
									}
									
									break;
								}
								
							}//handleMessage
							
						};
						String selText = (String)((Map<String,Object>)list.get(pos)).get("tv");
						if (selText.equals("全部")) {
							MultiLevelItem item = (MultiLevelItem) items.get(pos);
							CustomDialogBuilder.this.lastChoise = item;
							handleLastLevelChoice(cd);
						} else {
							showProgress(R.string.dialog_title_info, R.string.dialog_message_waiting, true);
							CustomDialogBuilder.this.id = ((MultiLevelItem)(CustomDialogBuilder.this.items.get(pos))).id;
							String txt = ((MultiLevelItem)(CustomDialogBuilder.this.items.get(pos))).toString();
//							(new Thread(new GetMetaDataThread(id,txt))).start();
							sendGetMetaCmd(id, txt);
						}
					}//not category over
					
				} else {
					System.out.println("hasNextLevel false");
					MultiLevelItem item = (MultiLevelItem) items.get(pos);
					CustomDialogBuilder.this.lastChoise = item;
					handleLastLevelChoice(cd);
				}
			}
		});
		
	}
	
	private void configSecondLevel(final CustomDialog cd, ListView lv, final List list) {

		//adapter
		final SecondCateAdapter secondAdapter = new SecondCateAdapter(list);
		//listview change adapter & set onitemclicklistener
		lv.setAdapter(secondAdapter);
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int pos, long arg3) {
				if (pos == 0) {
					//back
					configCustomDialog(cd);
				} else {
					if (isCategoryItem) {
						Map<String,Object> map = (Map<String,Object>) list.get(pos);
						String categoryNames = map.get("tvCategoryEnglishName") + "," + map.get("tvCategoryName");
						CustomDialogBuilder.this.lastChoise = categoryNames;
					} else {
						MultiLevelItem item = (MultiLevelItem) list.get(pos);
						CustomDialogBuilder.this.lastChoise = item;
					}
					handleLastLevelChoice(cd);
				}
			}
		});
	}
	
	private void handleLastLevelChoice(CustomDialog cd) {
		//send a message with data to let caller to handle message.
		//use this.lastChoise
		cd.dismiss();
		
		Message message = null;
		if (delegateHandler != null) {
			message = delegateHandler.obtainMessage();
			message.what = MSG_DIALOG_BACK_WITH_DATA;
			Bundle bundle = new Bundle();
			bundle.putSerializable("lastChoise", (Serializable) this.lastChoise);
//			bundle.putBoolean("isCategoryItem", isCategoryItem);
			bundle.putInt(ARG_COMMON_REQ_CODE, requestCode);
			message.obj = bundle;
			delegateHandler.sendMessage(message);
		}
	}
	private CustomDialog getCustomDialog() {
		return new CustomDialog(context);
	}
	
	private void sendGetMetaCmd(final String id, final String txt) {
		ApiParams params = new ApiParams();
		params.addParam("objIds", id);
		BaseApiCommand.createCommand("metaobject", true, params).execute(context, new Callback() {
			
			@Override
			public void onNetworkFail(String apiName, ApiError error) {
				MultiLevelItem selectedItem = new MultiLevelItem();
				selectedItem.id = id;
				selectedItem.txt = txt;
				sendMessage(MESSAGE_GET_METAOBJ, selectedItem);
			}
			
			@Override
			public void onNetworkDone(String apiName, String responseData) {
				json = responseData;
				
				MultiLevelItem selectedItem = new MultiLevelItem();
				selectedItem.id = id;
				selectedItem.txt = txt;
				sendMessage(MESSAGE_GET_METAOBJ, selectedItem);
			}
		});
	}
	
	
	class FirstCateAdapter extends BaseAdapter {
		private List list = null;
		public FirstCateAdapter(List list) {
			this.list = list;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			TextView tv = null;
			ImageView img = null;
			
			v = LayoutInflater.from(CustomDialogBuilder.this.context).inflate(
					R.layout.item_seccategory_simple2, null);
			
			tv = (TextView) v.findViewById(R.id.tv);
			img = (ImageView) v.findViewById(R.id.img);
			
			if (tv != null) {
				String displayText = (String)((Map<String,Object>)(list.get(position))).get("tv");
				tv.setText(displayText);
			}
			if (!isCategoryItem && img != null) {
				if (((Map<String,Object>)list.get(position)).get("id").equals(CustomDialogBuilder.this.selectedValue)) {
					img.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.pic_radio_selected));
				}
			}
			
			return v;
		}
		class Holder {
			public TextView tv;
			
		}
	}
	class SecondCateAdapter extends BaseAdapter {
		private List list = null;
		public SecondCateAdapter(List list) {
			this.list = list;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			TextView tv = null;
			ImageView img = null;
			if (position == 0) {
				v = LayoutInflater.from(CustomDialogBuilder.this.context)
						.inflate(R.layout.item_seccategory_simple, null);
				tv = (TextView) v.findViewById(R.id.tv);
			} else {
				v = LayoutInflater.from(CustomDialogBuilder.this.context)
						.inflate(R.layout.item_seccategory_simple2, null);
				tv = (TextView) v.findViewById(R.id.tv);
				img = (ImageView) v.findViewById(R.id.img);
			}
			
			if (tv != null) {
				String displayText = isCategoryItem 
						? (String)(((List<Map<String,Object>>)list).get(position).get("tvCategoryName")) 
						: (String)(((List<MultiLevelItem>)list).get(position).toString());
				if (!isCategoryItem && position == 1) displayText = "全部";
				tv.setText(displayText);
			}
			if (img != null) {
				if (!isCategoryItem) {
					if (((List<MultiLevelItem>)list).get(position).id.equals(CustomDialogBuilder.this.selectedValue)) {
						img.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.pic_radio_selected));
					}
				} else if (((List<Map<String,Object>>)list).get(position).get("tvCategoryName").equals(CustomDialogBuilder.this.selectedValue)) {
					img.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.pic_radio_selected));
				}
			}
			return v;
		}
		class Holder {
			public TextView tv;
			
		}
	}
}

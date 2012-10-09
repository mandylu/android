package com.quanleimu.view.fragment;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

import com.quanleimu.activity.BaseActivity;
import com.quanleimu.activity.BaseFragment;
import com.quanleimu.activity.BaseFragment.TabDef;
import com.quanleimu.activity.BaseFragment.TitleDef;
import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.activity.R;
import com.quanleimu.entity.Filterss;
import com.quanleimu.entity.PostMu;
import com.quanleimu.entity.values;
import com.quanleimu.jsonutil.JsonUtil;
import com.quanleimu.util.BXStatsHelper;
import com.quanleimu.util.Communication;
import com.quanleimu.util.Util;

public class SiftFragment extends BaseFragment {
	
	private static final int MSG_UPDATE_KEYWORD = 3;
	
	public List<String> listsize = new ArrayList<String>();

	// 定义变量
	public String backPageName = "";
	private EditText ed_sift;

	public int temp;
	public String res = "";
	public String value_resl = "";
	public int idselected;
	TextView tvmeta = null;

	private Map<Integer, TextView> selector = new HashMap<Integer, TextView>();
	private Map<String, EditText> editors = new HashMap<String, EditText>();

	public List<Filterss> listFilterss = new ArrayList<Filterss>();

	private Map<String, String> labelmap = new HashMap<String, String>();

	public Map<String, String> valuemap = new HashMap<String, String>();

	public String categoryEnglishName = "";
	public String json = "";

	private final int MSG_MULTISEL_BACK = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getArguments();
		categoryEnglishName = bundle.getString("categoryEnglishName");
		backPageName = bundle.getString(ARG_COMMON_BACK_HINT);	
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.sifttest, null);
		
		ed_sift = (EditText) v.findViewById(R.id.edsift);
		ed_sift.clearFocus();
		
		return v;
	}
	
	public void onResume()
	{
		super.onResume();
		
		// AND 地区_s:m7259
		PostMu postMu = (PostMu) Util
				.loadDataFromLocate(
						getContext(),
						"saveFilterss"
								+ categoryEnglishName
								+ QuanleimuApplication.getApplication().cityEnglishName);
		if (postMu == null || postMu.getJson().equals("")) {
			pd = new ProgressDialog(this.getContext());
			pd.setTitle("提示");
			pd.setMessage("请稍候...");
			pd.setCancelable(true);
			pd.show();
			new Thread(new GetGoodsListThread(true)).start();
		} else {
			json = postMu.getJson();
			long time = postMu.getTime();
			if (time + 24 * 3600 * 1000 < System.currentTimeMillis()) {
				sendMessage(1, null);
				pd = new ProgressDialog(this.getContext());
				pd.setTitle("提示");
				pd.setMessage("请稍候...");
				pd.setCancelable(true);
				pd.show();
				new Thread(new GetGoodsListThread(false)).start();
			} else {
				// sendMessage(1, null);
				loadSiftFrame(getView());
			}
		}
	}
	
	public void handleRightAction(){
			
			
			Bundle bundle = createArguments(null, backPageName);
			bundle.putString("categoryEnglishName", categoryEnglishName);
			collectValue(bundle);
//			pushAndFinish(new GetGoodFragment(), bundle);
			BXStatsHelper.getInstance().increase(BXStatsHelper.TYPE_LISTINGFILTER_SEND, null);
			finishFragment(requestCode, bundle);
		
		
	}//called when right button on title bar pressed, return true if handled already, false otherwise
	
	public void initTitle(TitleDef title){
		title.m_visible = true;
		title.m_title = "筛选";
		title.m_leftActionHint = "返回";
		title.m_rightActionHint = "确定";
	}
	public void initTab(TabDef tab){
		tab.m_visible = false;
	}
	
	public void onPause()
	{
		super.onPause();
		
		collectValue(getArguments());
	}
	
	private void collectValue(Bundle bundle)
	{
		String result = "";
		String resultLabel = "";

		String str = ed_sift.getText().toString().trim();
		if( valuemap != null && valuemap.size() != 0)
		{
			for (int i = 0; i < listFilterss.size(); i++) {

				String key = listFilterss.get(i).getName();
				if (valuemap.get(key) != null && !valuemap.get(key).equals("")) {
					result += " AND "
							+ URLEncoder.encode(key) + ":"
							+ URLEncoder.encode(valuemap.get(key));
				}
			}
		}

		if( labelmap != null && labelmap.size() != 0)
		{
			for (int i = 0; i < listFilterss.size(); i++) {

				String key = listFilterss.get(i).getName();
				if(labelmap.get(key) != null && !labelmap.get(key).equals("")){
					resultLabel += " AND " + URLEncoder.encode(key) + ":" + URLEncoder.encode(labelmap.get(key));
				}
			}
		}	
		
		for(int i = 0; i < editors.size(); ++i){
			String key = editors.keySet().toArray()[i].toString();
			
			EditText txtEditor = (EditText)editors.get(key);
			String textInput = txtEditor.getText().toString();
			if(textInput.length() > 0){
				result += " AND "
						+ URLEncoder.encode(key) + ":"
						+ URLEncoder.encode(textInput);
			}
		}

		
		if (!str.equals("")) {
			if(result.length() > 0){
				result += " AND ";
			}
			result += URLEncoder.encode(str);
		}
		
//		if (!str.equals("")) {
//			result += URLEncoder.encode(str);
//		}
		
		
		if(result.length() > 0)
		{
			bundle.putString("siftresult", result);
			bundle.putString("siftlabels", resultLabel);
		}else{
				bundle.putString("siftresult", "");
				bundle.putString("siftlabels", "");
		}
		
	}
	
	@Override
	public void onFragmentBackWithData(int message, Object obj) {
		if (message == 1234) {
			Bundle data = (Bundle)obj;
			
			String s = data.getString("all"); 
			if(s==null || s.equals("")){
				res = data.getString("label");
				value_resl = data.getString("value");
				
				if(temp < listFilterss.size() && listFilterss.get(temp).toString().length() > 0){
					valuemap.put(listFilterss.get(temp).getName(), value_resl);
				}
				selector.get(temp).setText(res);
			}else{
				//res = datas.getString("label");
				//value_resl = datas.getString("value");
				
				if(temp < listFilterss.size() && listFilterss.get(temp).toString().length() > 0){
					valuemap.remove(listFilterss.get(temp).getName());
				}
				selector.get(temp).setText(s);
			}
		}
		else if(MSG_MULTISEL_BACK == message){
			if(obj instanceof MultiLevelSelectionFragment.MultiLevelItem){
				final String txt = ((MultiLevelSelectionFragment.MultiLevelItem)obj).txt;
				selector.get(temp).setText(txt);
				if(((MultiLevelSelectionFragment.MultiLevelItem)obj).id != null 
						&&!((MultiLevelSelectionFragment.MultiLevelItem)obj).id.equals("")){
					labelmap.put(listFilterss.get(temp).getName(), ((MultiLevelSelectionFragment.MultiLevelItem)obj).txt);
					valuemap.put(listFilterss.get(temp).getName(), ((MultiLevelSelectionFragment.MultiLevelItem)obj).id);					
				}
				else{
					if(temp < listFilterss.size() && listFilterss.get(temp).toString().length() > 0){
						valuemap.remove(listFilterss.get(temp).getName());
						labelmap.remove(listFilterss.get(temp).getName());
					}					
				}
			}
		}
	}
	
	
	class GetGoodsListThread implements Runnable {
		private boolean isUpdate;
		public GetGoodsListThread(boolean isUpdate){
			this.isUpdate = isUpdate;
		}
		@Override
		public void run() {
			String apiName = "category_meta_filter";
			ArrayList<String> list = new ArrayList<String>();

			list.add("categoryEnglishName=" + categoryEnglishName);
			list.add("cityEnglishName=" + QuanleimuApplication.getApplication().cityEnglishName);

			String url = Communication.getApiUrl(apiName, list);
			try {
				json = Communication.getDataByUrl(url, false);
				if (json != null) {
					PostMu postMu = new PostMu();
					postMu.setJson(json);
					postMu.setTime(System.currentTimeMillis());
					Util.saveDataToLocate(SiftFragment.this.getContext(), "saveFilterss"+categoryEnglishName+QuanleimuApplication.getApplication().cityEnglishName, postMu);
					if(isUpdate){
						sendMessage(1, null);
					}
				} else {
					sendMessage(2, null);
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Communication.BXHttpException e){
				
			}
			if(pd != null){
				pd.dismiss();
			}
		}
	}
	
	private void loadSiftFrame(View rootView)
	{
		listFilterss = JsonUtil.getFilters(json).getFilterssList();
		QuanleimuApplication.getApplication().setListFilterss(listFilterss);
		LinearLayout ll_meta = (LinearLayout) rootView.findViewById(R.id.meta);
		LayoutInflater inflater = LayoutInflater.from(SiftFragment.this
				.getContext());
		if (listFilterss == null) {
			ll_meta.setVisibility(View.GONE);
		} else {
			ll_meta.removeAllViews();
			
			HashMap<String, String> preValues = null;
			HashMap<String, String> preLabels = null;
			Bundle bundle = getArguments();
			String keyWords = null;
			if (bundle != null) {
				String preEncResult = bundle.getString("siftresult");
				if (null != preEncResult) {
					String decResult = URLDecoder.decode(preEncResult);
					String[] pairs = decResult.split("AND ");
					if (pairs != null) {
						preValues = new HashMap<String, String>();
					}
					for (int x = 0; x < pairs.length; ++x) {
						String[] subPairs = pairs[x].split(":");
						if (subPairs.length <= 0 || subPairs.length > 2)
							continue;
						if (subPairs.length == 1 && subPairs[0] != null
								&& !subPairs[0].equals("")
								&& !subPairs[0].equals(" ")) {
							keyWords = subPairs[0];
						} else if (subPairs.length == 2) {
							subPairs[0] = subPairs[0].trim();
							subPairs[1] = subPairs[1].trim();
							preValues.put(subPairs[0], subPairs[1]);
						}
					}
				}

				String preEncLabels = bundle.getString("siftlabels");
				if (null != preEncLabels) {
					String decResult = URLDecoder.decode(preEncLabels);
					String[] pairs = decResult.split("AND ");
					if (pairs != null) {
						preLabels = new HashMap<String, String>();
					}
					for (int x = 0; x < pairs.length; ++x) {
						String[] subPairs = pairs[x].split(":");
						if (subPairs.length != 2)
							continue;
						subPairs[0] = subPairs[0].trim();
						subPairs[1] = subPairs[1].trim();
						preLabels.put(subPairs[0], subPairs[1]);
					}
				}
			}

			for (int i = 0; i < listFilterss.size(); ++i) {
				View v = null;
				TextView tvmetatxt = null;

				if (listFilterss.get(i).getControlType().equals("select")) {
					v = inflater.inflate(R.layout.item_post_select, null);
					valuemap.put(listFilterss.get(i).getName(), "");
					tvmetatxt = (TextView) v.findViewById(R.id.postshow);
					tvmetatxt.setText(listFilterss.get(i).getDisplayName());

					tvmeta = (TextView) v.findViewById(R.id.posthint);
					if (preValues != null
							&& preValues.containsKey(listFilterss.get(i)
									.getName())) {
						String preValue = preValues.get(listFilterss.get(i)
								.getName());
						boolean valid = false;
						if (preLabels != null
								&& preLabels.containsKey(listFilterss
										.get(i).getName())) {
							tvmeta.setText(preLabels.get(listFilterss
									.get(i).getName()));
							labelmap.put(listFilterss.get(i).getName(),
									preLabels.get(listFilterss.get(i)
											.getName()));
							valid = true;
						}
						valuemap.put(listFilterss.get(i).getName(),
								preValue);
						if (!valid) {
							List<values> values = listFilterss.get(i)
									.getValuesList();
							for (int z = 0; z < values.size(); ++z) {
								if (values.get(z).getValue()
										.equals(preValue)) {
									tvmeta.setText(listFilterss.get(i)
											.getLabelsList().get(z)
											.getLabel());
									valid = true;
									break;
								}
							}

							if (!valid) {
								tvmeta.setText("请选择");
							}
						}
					} else {
						tvmeta.setText("请选择");
					}
					selector.put(i, tvmeta);

					v.setTag(i);

					v.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							temp = Integer.parseInt(v.getTag().toString());
							Bundle bundle = createArguments(null, null);
							bundle.putAll(getArguments());
							bundle.putInt("temp", temp);
							bundle.putString("title", listFilterss
									.get(temp).getDisplayName());
							bundle.putString("back", "筛选");

							// if(null != m_viewInfoListener){
							// m_viewInfoListener.onNewView(new
							// SiftOptionListView(getContext(), bundle));
							// }
							Filterss fss = listFilterss.get(temp);
							if (fss.getLevelCount() > 0) {
								ArrayList<MultiLevelSelectionFragment.MultiLevelItem> items = new ArrayList<MultiLevelSelectionFragment.MultiLevelItem>();
								MultiLevelSelectionFragment.MultiLevelItem head = new MultiLevelSelectionFragment.MultiLevelItem();
								head.txt = "全部";
								head.id = "";
								items.add(head);
								for (int i = 0; i < fss.getLabelsList()
										.size(); ++i) {
									MultiLevelSelectionFragment.MultiLevelItem t = new MultiLevelSelectionFragment.MultiLevelItem();
									t.txt = fss.getLabelsList().get(i)
											.getLabel();
									t.id = fss.getValuesList().get(i)
											.getValue();
									items.add(t);
								}
								
								bundle.putInt(ARG_COMMON_REQ_CODE,
										MSG_MULTISEL_BACK);
								bundle.putSerializable("items", items);
								bundle.putInt("maxLevel",
										fss.getLevelCount() - 1);
								((BaseActivity) getActivity())
										.pushFragment(
												new MultiLevelSelectionFragment(),
												bundle, false);
							}
						}
					});

				}
				// else
				// if(listFilterss.get(i).getControlType().equals(""))
				else {
					v = inflater.inflate(R.layout.item_post_edit, null);
					tvmetatxt = (TextView) v.findViewById(R.id.postshow);
					tvmetatxt.setText(listFilterss.get(i).getDisplayName());
					tvmeta = (EditText) v.findViewById(R.id.postinput);
					final String key = listFilterss.get(i).getName();
					editors.put(key,
							(EditText) tvmeta);
					if (null != preValues
							&& preValues.containsKey(listFilterss.get(i)
									.getName())) {
						String preValue = preValues.get(listFilterss.get(i)
								.getName());
						tvmeta.setText(preValue);
//						valuemap.put(listFilterss.get(i).getName(),
//								preValue);
					}
				}
				TextView border = new TextView(
						SiftFragment.this.getContext());
				border.setLayoutParams(new LayoutParams(
						LayoutParams.FILL_PARENT, 1, 1));
				border.setBackgroundResource(R.drawable.list_divider);

				ll_meta.addView(v);
				ll_meta.addView(border);
			}

			if (keyWords != null) {
//				((TextView) SiftFragment.this.findViewById(R.id.edsift))
//						.setText(keyWords);
				sendMessage(MSG_UPDATE_KEYWORD, keyWords);
			}

		}
	}
	

	@Override
	protected void handleMessage(Message msg, Activity activity, View rootView) {

		switch (msg.what) {
		case 1:
			if (pd != null) {
				pd.dismiss();
			}

			if (rootView != null)
			{
				loadSiftFrame(rootView);
			}

			break;
		case 2:
			if (pd != null) {
				pd.dismiss();
			}
			Toast.makeText(activity, "服务当前不可用，请稍后重试！", 3).show();
			break;
		case MSG_UPDATE_KEYWORD:
			((TextView) rootView.findViewById(R.id.edsift))
			.setText((String) msg.obj);
			break;
		}

	}

}

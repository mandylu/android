package com.quanleimu.activity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.quanleimu.entity.Filterss;
import com.quanleimu.entity.PostMu;
import com.quanleimu.jsonutil.JsonUtil;
import com.quanleimu.util.Communication;
import com.quanleimu.util.Util;

public class SiftTest extends BaseActivity {
	public List<String> listsize = new ArrayList<String>();

	// 定义变量
	public String backPageName = "";
	public TextView tvTitle;
	public Button btnBack, btnStore;
	private EditText ed_sift;

	public int temp;
	public String res = "";
	public String value_resl = "";
	public int idselected;
	TextView tvmeta = null;

	private Vector<TextView> selector = new Vector<TextView>();

	public List<Filterss> listFilterss = new ArrayList<Filterss>();

	public Map<Integer, String> savemap = new HashMap<Integer, String>();

	public Vector<String> name = new Vector<String>();
	public Map<Integer, String> valuemap = new HashMap<Integer, String>();

	// ------------------------------------
	// ListView lv_test;
	// MyAdapter myAdapter;

	public String categoryEnglishName = "";
	public String json = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.sift2);
		super.onCreate(savedInstanceState);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		categoryEnglishName = intent.getExtras().getString(
				"categoryEnglishName");
		backPageName = intent.getExtras().getString("backPageName");

		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText("筛选");

		ed_sift = (EditText) findViewById(R.id.edsift);
		ed_sift.clearFocus();

		btnStore = (Button) findViewById(R.id.btnStore);

		btnBack = (Button) findViewById(R.id.btnBack);
		btnBack.setText(backPageName);
		btnBack.setOnClickListener(this);
		

		btnStore.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String result = "";
				// "query="
				// + "cityEnglishName:shanghai AND categoryEnglishName:"
				// + categoryEnglishName;
				String str = ed_sift.getText().toString().trim();
				if(name != null && name.size() != 0)
				{
					for (int i = 0; i < name.size(); i++) {
						// if (!valuemap.get(i).equals("") && !str.equals("")) {
						// System.out.println("看打印------------->"
						// + name.elementAt(i) + "=" + valuemap.get(i)
						// + "key=" + str);
						//
						// } else if (!valuemap.get(i).equals("")) {
						// System.out.println("看打印------------->"
						// + name.elementAt(i) + "=" + valuemap.get(i));
						// }
						if (valuemap != null && valuemap.size() != 0 && valuemap.get(i) != null && !valuemap.get(i).equals("")) {
							result += " AND "
									+ URLEncoder.encode(name.elementAt(i)) + ":"
									+ URLEncoder.encode(valuemap.get(i));
						}
					}
					if (!str.equals("")) {
						result += URLEncoder.encode(str);
					}
					intent.setClass(SiftTest.this, GetGoods.class);
					bundle.putString("siftresult", result);
					intent.putExtras(bundle);
					startActivity(intent);
				}
				

			}
		});
		// AND 地区_s:m7259
		PostMu postMu = (PostMu) Util.loadDataFromLocate(
				this, "saveFilterss"+categoryEnglishName+myApp.cityEnglishName);
		if (postMu == null || postMu.getJson().equals("")) {
			System.out.println("下载");
			new Thread(new GetGoodsListThread(true)).start();
		} else {
			System.out.println("缓存");
			json = postMu.getJson();
			long time = postMu.getTime();
			if(time + 24*3600*1000 < System.currentTimeMillis()){
				myHandler.sendEmptyMessage(1);
				new Thread(new GetGoodsListThread(false)).start();
			}else{
				myHandler.sendEmptyMessage(1);
			}
		}

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//

	}

	@Override
	public void onClick(View v) {
		if (v.getId() == btnBack.getId()) {
			this.finish();
		}
		super.onClick(v);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == 1234) {
			Bundle datas = data.getExtras();
			String s = datas.getString("all"); 
			if(s==null || s.equals("")){
				res = datas.getString("label");
				value_resl = datas.getString("value");
				valuemap.put(temp, value_resl);
				selector.elementAt(temp).setText(res);
				savemap.put(temp, res);
			}else{
				
				selector.elementAt(temp).setText(s);
			}
			

		}
		super.onActivityResult(requestCode, resultCode, data);
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
			list.add("cityEnglishName=" + myApp.cityEnglishName);

			String url = Communication.getApiUrl(apiName, list);
			System.out.println("url ------ >" + url);
			try {
				json = Communication.getDataByUrl(url);
				if (json != null) {
					PostMu postMu = new PostMu();
					postMu.setJson(json);
					postMu.setTime(System.currentTimeMillis());
					Util.saveDataToLocate(SiftTest.this, "saveFilterss"+categoryEnglishName+myApp.cityEnglishName, postMu);
					if(isUpdate){
						myHandler.sendEmptyMessage(1);
					}
				} else {
					myHandler.sendEmptyMessage(2);
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	// 管理线程的Handler
	Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				if (pd != null) { 
					pd.dismiss();
				}

				listFilterss = JsonUtil.getFilters(json).getFilterssList();
				myApp.setListFilterss(listFilterss);
				LinearLayout ll_meta = (LinearLayout) findViewById(R.id.meta);
				LayoutInflater inflater = LayoutInflater.from(SiftTest.this);
				if (listFilterss == null) {
					ll_meta.setVisibility(View.GONE);
				} else {
					
					for (int i = 0; i < listFilterss.size(); i++) {
						View v = null;
						ImageView tvim = null;
						EditText tved = null;
						TextView tvmetatxt = null;
						v = inflater.inflate(R.layout.item_sift, null);
						v.setLayoutParams(new LinearLayout.LayoutParams(
								LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

						if (i == 0) {
							v.setBackgroundResource(R.drawable.btn_top_bg);
							if(listFilterss.size() == 1){
								v.setBackgroundResource(R.drawable.btn_s_bg);
							}
						} else if (i == listFilterss.size() - 1) {
							// v.setBackgroundResource(R.drawable.btn_m_bg);
							v.setBackgroundResource(R.drawable.btn_down_bg);
						} else {
							v.setBackgroundResource(R.drawable.btn_m_bg);
						}

						v.setPadding(10, 10, 10, 10);

						if (listFilterss.get(i).getControlType().equals("select")) {
							// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
							name.add(listFilterss.get(i).getName());
							valuemap.put(i, "");
							// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
							savemap.put(i, "");
							tvmetatxt = (TextView) v.findViewById(R.id.tvmetatxt);
							tvmetatxt.setLayoutParams(new LinearLayout.LayoutParams(
									LayoutParams.FILL_PARENT,
									LayoutParams.WRAP_CONTENT, 1));
							tvmetatxt.setText(listFilterss.get(i).getDisplayName());

							tvmeta = (TextView) v.findViewById(R.id.tvmeta);
							tvmeta.setGravity(Gravity.RIGHT);
							tvmeta.setLayoutParams(new LinearLayout.LayoutParams(
									LayoutParams.FILL_PARENT,
									LayoutParams.WRAP_CONTENT, 1));
							tvmeta.setText("请选择");

							tvim = (ImageView) v.findViewById(R.id.tvimage);
							tvim.setLayoutParams(new LinearLayout.LayoutParams(
									LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT));
							tvim.setPadding(10, 10, 10, 10);
							tvim.setImageResource(R.drawable.arrow);
							tvim.setBackgroundDrawable(null);

							tved = (EditText) v.findViewById(R.id.tved);
							tved.setVisibility(View.GONE);
							selector.add(tvmeta);
						}
						// else
						// if(listFilterss.get(i).getControlType().equals(""))
						else {
							tvmetatxt = (TextView) v.findViewById(R.id.tvmetatxt);
							tvmetatxt.setText(listFilterss.get(i).getDisplayName());
							tvmeta = (TextView) v.findViewById(R.id.tvmeta);
							tvim = (ImageView) v.findViewById(R.id.tvimage);
							tved = (EditText) v.findViewById(R.id.tved);
							tved.clearFocus();
							tvmeta.setVisibility(View.GONE);
							tvim.setVisibility(View.GONE);
							// selector.add(tvmeta);
						}

						v.setTag(i);

						v.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								temp = Integer.parseInt(v.getTag().toString());
								System.out.println("Result------>" + v.getTag());
								bundle.putInt("temp", temp);
								bundle.putString("title", listFilterss.get(temp).getDisplayName());
								bundle.putString("back", "筛选");
								intent.putExtras(bundle);
								intent.setClass(SiftTest.this, Test001.class);
								
								startActivityForResult(intent, 1234);

							}
						});
						ll_meta.addView(v);
					}

				}

				break;
			case 2:
				if (pd != null) {
					pd.dismiss();
				}
				Toast.makeText(SiftTest.this, "未获取到数据", 3).show();
				break;

			}
			super.handleMessage(msg);
		}
	};

	private void updateUI() {
		myApp.setListFilterss(listFilterss);
		LinearLayout ll_meta = (LinearLayout) findViewById(R.id.meta);
		LayoutInflater inflater = LayoutInflater.from(SiftTest.this);
		if (listFilterss == null) {
			ll_meta.setVisibility(View.GONE);
		} else {
			for (int i = 0; i < listFilterss.size(); i++) {
				View v = null;
				ImageView tvim = null;
				EditText tved = null;
				TextView tvmetatxt = null;
				v = inflater.inflate(R.layout.item_sift, null);
				v.setLayoutParams(new LinearLayout.LayoutParams(
						LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

				if (i == 0) {
					v.setBackgroundResource(R.drawable.btn_top_bg);
				} else if (i == listFilterss.size() - 1) {
					// v.setBackgroundResource(R.drawable.btn_m_bg);
					v.setBackgroundResource(R.drawable.btn_down_bg);
				} else {
					v.setBackgroundResource(R.drawable.btn_m_bg);
				}

				v.setPadding(10, 10, 10, 10);

				if (listFilterss.get(i).getControlType().equals("select")) {
					// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
					name.add(listFilterss.get(i).getName());
					valuemap.put(i, "");
					// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
					savemap.put(i, "");
					tvmetatxt = (TextView) v.findViewById(R.id.tvmetatxt);
					tvmetatxt.setLayoutParams(new LinearLayout.LayoutParams(
							LayoutParams.FILL_PARENT,
							LayoutParams.WRAP_CONTENT, 1));
					tvmetatxt.setText(listFilterss.get(i).getDisplayName());

					tvmeta = (TextView) v.findViewById(R.id.tvmeta);
					tvmeta.setGravity(Gravity.RIGHT);
					tvmeta.setLayoutParams(new LinearLayout.LayoutParams(
							LayoutParams.FILL_PARENT,
							LayoutParams.WRAP_CONTENT, 1));
					tvmeta.setText("请选择");

					tvim = (ImageView) v.findViewById(R.id.tvimage);
					tvim.setLayoutParams(new LinearLayout.LayoutParams(
							LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT));
					tvim.setPadding(10, 10, 10, 10);
					tvim.setImageResource(R.drawable.arrow);
					tvim.setBackgroundDrawable(null);

					tved = (EditText) v.findViewById(R.id.tved);
					tved.setVisibility(View.GONE);
					selector.add(tvmeta);
				}
				// else
				// if(listFilterss.get(i).getControlType().equals(""))
				else {
					tvmetatxt = (TextView) v.findViewById(R.id.tvmetatxt);
					tvmetatxt.setText(listFilterss.get(i).getDisplayName());
					tvmeta = (TextView) v.findViewById(R.id.tvmeta);
					tvim = (ImageView) v.findViewById(R.id.tvimage);
					tved = (EditText) v.findViewById(R.id.tved);
					tved.clearFocus();
					tvmeta.setVisibility(View.GONE);
					tvim.setVisibility(View.GONE);
					// selector.add(tvmeta);
				}

				v.setTag(i);

				v.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						temp = Integer.parseInt(v.getTag().toString());
						System.out.println("Result------>" + v.getTag());
						bundle.putInt("temp", temp);
						intent.setClass(SiftTest.this, Test001.class);
						intent.putExtras(bundle);
						startActivityForResult(intent, 1234);

					}
				});
				ll_meta.addView(v);
			}

		}
	}
}

package com.quanleimu.view;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.activity.R;
import com.quanleimu.entity.GoodsDetail;
import com.quanleimu.entity.GoodsList;
import com.quanleimu.imageCache.SimpleImageLoader;
import com.quanleimu.jsonutil.JsonUtil;
import com.quanleimu.util.Communication;
import com.quanleimu.util.Helper;
import com.quanleimu.util.Util;

public class SearchGoods extends BaseView implements OnScrollListener {

	// 定义控件
	public Button btnSearch, btnBack;
	public ListView lvSearchResult;
	public Button btnMore;
	public LinearLayout loadingLayout;

	public ProgressDialog pd;
	public String title = "";
	/**
	 * 设置布局显示目标最大化
	 */
	public LayoutParams FFlayoutParams = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.FILL_PARENT,
			LinearLayout.LayoutParams.FILL_PARENT);

	// 定义变量
	public String searchContent = "";
	public String act_type = "";

	public CommonAdapter commonAdapter = null;
	public List<GoodsDetail> listSearchGoods = new ArrayList<GoodsDetail>();
	public List<GoodsDetail> listCommonSearchGoods = new ArrayList<GoodsDetail>();
	public GoodsList goodsList = new GoodsList();

	public String json = "";
	public String fields = "";
	public int startRow = 0;

	public int isFirst = 0;
	public CommonAdapter adapter;
	public int totalCount = -1;
	
	private String backPageName = "";
	private ProgressBar progressBar;
	private TextView tvAddMore;
	
	protected void Init(){
		LayoutInflater inflater = LayoutInflater.from(getContext());
		this.addView(inflater.inflate(R.layout.searchgoods, null));
		
		// 参数 用来过滤
		fields = "mobile,id,link,title,description,date,areaNames,categoryEnglishName,lat,lng,images_big,images_resize180,metaData";
		
		LayoutParams WClayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		// findViewById
		lvSearchResult = (ListView) findViewById(R.id.lvSearchResult);

		//线性布局  
        LinearLayout layout = new LinearLayout(getContext());  
        //设置布局 水平方向  
        layout.setOrientation(LinearLayout.HORIZONTAL);  
         //进度条  
        progressBar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleSmall);
         //进度条显示位置  
        progressBar.setVisibility(View.GONE);
        
        layout.addView(progressBar, WClayoutParams);  
        
        tvAddMore = new TextView(getContext());  
        tvAddMore.setTextSize(18);
        tvAddMore.setText("更多...");  
        tvAddMore.setGravity(Gravity.CENTER_VERTICAL);  
        layout.addView(tvAddMore, WClayoutParams);  
        layout.setGravity(Gravity.CENTER);  
        loadingLayout = new LinearLayout(getContext());  
        loadingLayout.setBackgroundResource(R.drawable.alpha_bg);
//        loadingLayout.setBackgroundColor(R.color.white);
        loadingLayout.addView(layout, WClayoutParams);  
        loadingLayout.setGravity(Gravity.CENTER); 
        
        tvAddMore.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				progressBar.setVisibility(View.VISIBLE);
				tvAddMore.setText("加载中...");
				
				//点击获取更多 按钮布局消失
				isFirst = -1;
				startRow = listSearchGoods.size();
				new Thread(new GetGoodsListThread()).start();
			}
		});

        lvSearchResult.setDivider(null);
		lvSearchResult.addFooterView(loadingLayout);

		listSearchGoods = QuanleimuApplication.getApplication().getListSearchGoods();
		totalCount = QuanleimuApplication.getApplication().getSearchCount();

		lvSearchResult.setOnScrollListener(this);

		lvSearchResult
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						if(arg2 == listSearchGoods.size())
						{
							progressBar.setVisibility(View.VISIBLE);
							tvAddMore.setText("加载中...");
							
							//点击获取更多 按钮布局消失
							isFirst = -1;
							startRow = listSearchGoods.size();
							new Thread(new GetGoodsListThread()).start();
						}
						else
						{
							if(null != m_viewInfoListener){
								Bundle bundle = new Bundle();
//								bundle.putString("backPageName", title);
//								bundle.putString("detail_type", "searchgoods");
//								bundle.putInt("detail_pos", arg2);
								m_viewInfoListener.onNewView(new GoodDetailView(listSearchGoods.get(arg2), getContext(), bundle));
							}
						}
					}
				});

		pd = ProgressDialog.show(getContext(), "提示", "请稍后...");
		pd.setCancelable(true);
		new Thread(new GetGoodsListThread()).start();

	}
	
	public SearchGoods(Context context, Bundle bundle){
		super(context, bundle);
		
		backPageName = bundle.getString("backPageName");
		searchContent = bundle.getString("searchContent");
		act_type = bundle.getString("actType");
		if(act_type.equals("search")){
			title = searchContent;
		}
		else{
			title = bundle.getString("name");
		}
		
		Init();
	}

	public boolean onRightActionPressed(){
		m_viewInfoListener.onNewView(new SearchView(getContext(), new Bundle()));
		return true;
	}//called when right button on title bar pressed, return true if handled already, false otherwise
	
	@Override
	public TitleDef getTitleDef(){
		TitleDef title = new TitleDef();
		title.m_visible = true;
		title.m_leftActionHint = backPageName;
		title.m_title = this.title;
		title.m_rightActionHint = "搜索";
		return title;
	}
	
	@Override
	public TabDef getTabDef(){
		TabDef tab = new TabDef();
		tab.m_visible = true;
		return tab;
	}

	// 管理线程的Handler
	Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				goodsList = JsonUtil.getGoodsListFromJson(json);
				totalCount = goodsList.getCount();

				if (goodsList == null || goodsList.getCount() == 0) {
					if (pd != null) {
						pd.dismiss();
					}
					Toast.makeText(getContext(), "没有符合条件的结果，请重新输入！", 3).show();
				} else {
					listSearchGoods = goodsList.getData();

					QuanleimuApplication.getApplication().setSearchCount(totalCount);

					QuanleimuApplication.getApplication().setListSearchGoods(listSearchGoods);

					if (totalCount > listSearchGoods.size()) {
						loadingLayout.setVisibility(View.VISIBLE);
					} else {
						loadingLayout.setVisibility(View.GONE);
					}
					adapter = new CommonAdapter(getContext(), listSearchGoods);
					lvSearchResult.setAdapter(adapter);
					if (pd != null) {
						pd.dismiss();
					}
				}

				break;
			case 2:
				if (pd != null) {
					pd.dismiss();
				}
				progressBar.setVisibility(View.GONE);
				tvAddMore.setText("更多...");
				loadingLayout.setVisibility(View.GONE);
				Toast.makeText(getContext(), "没有符合条件的结果，请重新输入！", 3).show();
				// 判断总数是不是已经超出当前集合长度
				if (goodsList.getCount() > listSearchGoods.size()) {
					loadingLayout.setVisibility(View.VISIBLE);
				} else {
					loadingLayout.setVisibility(View.GONE);
				}
				break;
			case 3:
				if (pd != null) {
					pd.dismiss();
				}
				progressBar.setVisibility(View.GONE);
				tvAddMore.setText("更多...");
				loadingLayout.setVisibility(View.GONE);
				goodsList = JsonUtil.getGoodsListFromJson(json);

				if (goodsList == null || goodsList.getCount() == 0) {
					Toast.makeText(getContext(), "没有符合条件的结果，请重新输入！", 3).show();
				} else {
					listCommonSearchGoods = goodsList.getData();
					for (int i = 0; i < listCommonSearchGoods.size(); i++) {
						listSearchGoods.add(listCommonSearchGoods.get(i));
					}
					QuanleimuApplication.getApplication().setListSearchGoods(listSearchGoods);

					adapter.setList(listSearchGoods);
					adapter.notifyDataSetChanged();
					
				}
				if (totalCount > listSearchGoods.size()) {
					loadingLayout.setVisibility(View.VISIBLE);
				} else {
					loadingLayout.setVisibility(View.GONE);
				}
				break;
			case 10:
				if (pd != null) {
					pd.dismiss();
				}
				progressBar.setVisibility(View.GONE);
				tvAddMore.setText("更多...");
				loadingLayout.setVisibility(View.GONE);
				Toast.makeText(getContext(), "网络连接失败，请检查设置！", 3).show();
				// 判断总数是不是已经超出当前集合长度
				if (goodsList.getCount() > listSearchGoods.size()) {
					loadingLayout.setVisibility(View.VISIBLE);
				} else {
					loadingLayout.setVisibility(View.GONE);
				}
				break;
			}

			super.handleMessage(msg);
		}
	};

	class GetGoodsListThread implements Runnable {
		@Override
		public void run() {
			String apiName = "ad_list";
			ArrayList<String> list = new ArrayList<String>();

			list.add("fields=" + URLEncoder.encode(fields));
			list.add("query="
					+ Communication.urlEncode(URLEncoder
							.encode("cityEnglishName:"
									+ QuanleimuApplication.getApplication().getCityEnglishName() + " AND "
									+ searchContent)));
			list.add("start=" + startRow);
			list.add("rows=" + 30);

			String url = Communication.getApiUrl(apiName, list);
			try {
				json = Communication.getDataByUrl(url);

				if (json != null) {
					if (isFirst == -1) {
						isFirst = 0;
						myHandler.sendEmptyMessage(3);
					} else {
						myHandler.sendEmptyMessage(1);
					}

				} else {
					myHandler.sendEmptyMessage(2);
				}
			} catch (UnsupportedEncodingException e) {
			} catch (IOException e) {
				myHandler.sendEmptyMessage(10);
			}

		}
	}

	public class CommonAdapter extends BaseAdapter {

		public Context context;
		public TextView tvDes, tvPrice, tvDateAndAddress;
		public ImageView ivInfo;
		public List<GoodsDetail> list = new ArrayList<GoodsDetail>();

		public List<GoodsDetail> getList() {
			return list;
		}

		public void setList(List<GoodsDetail> list) {
			this.list = list;
		}

		public CommonAdapter() {
			super();
			// TODO Auto-generated constructor stub
		}

		public CommonAdapter(Context context, List<GoodsDetail> list) {
			super();
			this.context = context;
			this.list = list;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return list.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = LayoutInflater.from(context);
			View v = null;
//			if (convertView == null) {
				v = inflater.inflate(R.layout.item_common, null);
//			} else {
//				v = (View) convertView;
//			}

			if (list.size() == 1) {
				v.setBackgroundResource(R.drawable.btn_s_bg);
			} else {
				if (position == 0) {
					v.setBackgroundResource(R.drawable.btn_top_bg);
				} else if (position == list.size() - 1) {
					v.setBackgroundResource(R.drawable.btn_down_bg);
				} else {
					v.setBackgroundResource(R.drawable.btn_m_bg);
				}
			}
			tvDes = (TextView) v.findViewById(R.id.tvDes);
			tvPrice = (TextView) v.findViewById(R.id.tvPrice);
			tvPrice.setTextColor(Color.RED);
			tvDateAndAddress = (TextView) v.findViewById(R.id.tvDateAndAddress);
			tvDateAndAddress.setTextColor(R.color.hui);

			ivInfo = (ImageView) v.findViewById(R.id.ivInfo);
			BitmapFactory.Options o =  new BitmapFactory.Options();
            o.inPurgeable = true;
			Bitmap tmb = BitmapFactory.decodeResource(context.getResources(),R.drawable.moren1, o);
			Bitmap mb= Helper.toRoundCorner(tmb, 20);
			tmb.recycle();
			
			Bitmap tmb1 = BitmapFactory.decodeResource(context.getResources(),R.drawable.moren, o);
			Bitmap mb1= Helper.toRoundCorner(tmb1, 20);
			tmb1.recycle();
			
			ivInfo.setImageBitmap(mb);

			

			int type = Util.getWidthByContext(context);
			RelativeLayout.LayoutParams lp = null;
			switch (type) {
			case 240:
				lp = new RelativeLayout.LayoutParams(45, 45);
				break;
			case 320:
				lp = new RelativeLayout.LayoutParams(60, 60);
				break;
			case 480:
				lp = new RelativeLayout.LayoutParams(90, 90);
				break;
			case 540:
				lp = new RelativeLayout.LayoutParams(100, 100);
				break;
			case 640:
				lp = new RelativeLayout.LayoutParams(120, 120);
				break;
			default:
				lp = new RelativeLayout.LayoutParams(140, 140);
				break;
			}

			lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			ivInfo.setLayoutParams(lp);

			if (list.get(position).getImageList() == null
					|| list.get(position).getImageList().equals("")
					|| list.get(position).getImageList().getResize180() == null
					|| list.get(position).getImageList().getResize180()
							.equals("")) {
				ivInfo.setImageBitmap(mb1);
			} else {
				String b = (list.get(position).getImageList().getResize180())
						.substring(1, (list.get(position).getImageList()
								.getResize180()).length() - 1);
				b = Communication.replace(b);

				if (b.contains(",")) {
					String[] c = b.split(",");
					if (c[0] == null || c[0].equals("")) {
						ivInfo.setImageBitmap(mb1);
					} else {
						ivInfo.setTag(c[0]);
						SimpleImageLoader.showImg( ivInfo, c[0],getContext());

						//TODO::check whether this comments cause problem!!!!
//						if (position <= 3) {
//							LoadImage.doTask();
//						}

						// SimpleImageLoader.showImg(ivInfo, c[0],context);
					}
				} else {
					if (b == null || b.equals("")) {
						ivInfo.setImageBitmap(mb1);
					} else {
						ivInfo.setTag(b);
						SimpleImageLoader.showImg( ivInfo, b, getContext());

//						if (position <= 3) {
//							LoadImage.doTask();
//						}
						// SimpleImageLoader.showImg(ivInfo, b,context);
					}
				}
			}

			// SimpleImageLoader.showImg(ivHead,
			// list.get(position).UserAvatar,0);

			String price = "";
			try {
				price = list.get(position).getMetaValueByKey("价格") + "";
			} catch (Exception e) {
				price = "";
			}
			if (price.equals("null") || price.equals("")) {
				tvPrice.setVisibility(View.GONE);
			} else {
				tvPrice.setText(price);
			}
			tvDes.setText(list.get(position).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_TITLE));

			String dateV = list.get(position).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_DATE);
			if (dateV != null && !dateV.equals("")) {
				Date date = new Date(Long.parseLong(dateV) * 1000);
				SimpleDateFormat df = new SimpleDateFormat("MM月dd日",
						Locale.SIMPLIFIED_CHINESE);

				String areaV = list.get(position).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_AREANAME);
				if (areaV != null
						&& !areaV.equals("")) {
					tvDateAndAddress.setText(df.format(date) + " "
							+ areaV);
				} else {
					tvDateAndAddress.setText(df.format(date));
				}
			} else {
				String areaV = list.get(position).getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_AREANAME);
				if (areaV != null
						&& !areaV.equals("")) {
					tvDateAndAddress.setText(areaV);
				} else {
					tvDateAndAddress.setVisibility(View.GONE);
				}
			}

			return v;
		}

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		//TODO:: check this
//		if (scrollState == SCROLL_STATE_IDLE) {
//			LoadImage.doTask();
//		}

	}
}

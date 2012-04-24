package com.quanleimu.activity;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.quanleimu.entity.Filterss;
import com.quanleimu.entity.PostGoodsBean;

public class PostGoodsSelection extends BaseActivity {

	public int temp = -1;
	public List<Filterss> listFilterss = new ArrayList<Filterss>();
	private Button backBtn;
	private TextView title;
	private PostGoodsBean postBean;
	private List<String> list;
	public ListView lv;

	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.goodsselectionlist);
		super.onCreate(savedInstanceState);
		intent = getIntent();
		bundle = intent.getExtras();
		
		String ti = bundle.getString("title");
		String back = bundle.getString("back"); 
		postBean = (PostGoodsBean) bundle.getSerializable("postBean");
		list = postBean.getLabels();
		lv = (ListView) findViewById(R.id.lv_test);
		lv.setAdapter(new ItemList());
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
 
				
				bundle.putInt("id", arg2);
				// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
				intent.putExtras(bundle);
				setResult(PostGoods.POST_LIST, intent);
				finish();

			}
		});

		backBtn = (Button) findViewById(R.id.btnBack);
		title = (TextView) findViewById(R.id.tvTitle);
		backBtn.setText(back);
		title.setText(ti);
		backBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
		
	}

	class ItemList extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return list.size();
		} 

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = getLayoutInflater().inflate(R.layout.item_goodsselectionlist,
					null);
			
			if(position==0){ 
				convertView.setBackgroundResource(R.drawable.btn_top_bg);
			}else if(position==list.size()-1){
				convertView.setBackgroundResource(R.drawable.btn_down_bg);
			}else{
				convertView.setBackgroundResource(R.drawable.btn_m_bg);
			}
			convertView.setPadding(10, 10, 10, 10);
			TextView txts = (TextView) convertView
					.findViewById(R.id.goodsselectionlisttxt);
			txts.setText(list.get(position));
			return convertView;
		}

	}
}

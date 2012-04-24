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

public class Test001 extends BaseActivity {

	public int temp = -1;
	public List<Filterss> listFilterss = new ArrayList<Filterss>();
	private Button backBtn;
	private TextView title;
	public ListView lv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.siftlist);
		super.onCreate(savedInstanceState);
		
		intent = getIntent();
		bundle = intent.getExtras();
		
		temp = bundle.getInt("temp");
		String ti = bundle.getString("title");
		String back = bundle.getString("back"); 
		
		System.out.println(temp+" "+ti+" "+back);
		
		listFilterss = myApp.getListFilterss();
		// listFilterss.get(temp).getLabelsList();
		backBtn = (Button) findViewById(R.id.btnBack);
		title = (TextView) findViewById(R.id.tvTitle);
		backBtn.setText(back);
		title.setText(ti);
		backBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
		lv = (ListView) findViewById(R.id.lv_test);
		if(listFilterss != null && listFilterss.size() != 0)
		{
			lv.setAdapter(new ItemList());
		}
		
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// myApp.savemap.put(temp,
				// listFilterss.get(temp).getLabelsList()
				// .get(arg2).getLabel());

				intent = getIntent();
				bundle = new Bundle();
				// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~`
				if(arg2 != 0){
					bundle.putString("value", listFilterss.get(temp)
							.getValuesList().get(arg2-1).getValue());
					// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
					bundle.putString("label", listFilterss.get(temp)
							.getLabelsList().get(arg2-1).getLabel());
				}else{
					bundle.putString("all", "不限");
				}
				
				intent.putExtras(bundle);
				setResult(1234, intent);
				finish();

			}
		});

	}

	class ItemList extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return listFilterss.get(temp).getLabelsList().size()+1;
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
			convertView = getLayoutInflater().inflate(R.layout.item_siftlist,
					null);
			
			if(position==0){ 
				convertView.setBackgroundResource(R.drawable.btn_top_bg);
			}else if(position>listFilterss.get(temp).getLabelsList().size()-1){
//				convertView.setBackgroundResource(R.drawable.btn_m_bg);
				convertView.setBackgroundResource(R.drawable.btn_down_bg);
			}else{
				convertView.setBackgroundResource(R.drawable.btn_m_bg);
			}
			convertView.setPadding(10, 10, 10, 10);
			
			TextView txts = (TextView) convertView
			.findViewById(R.id.siftlisttxt);
			if(position == 0){
				txts.setText("不限");
			}else{
				txts.setText(listFilterss.get(temp).getLabelsList().get(position-1)
						.getLabel());
			}
			
			
			return convertView;
		}

	}

	
}


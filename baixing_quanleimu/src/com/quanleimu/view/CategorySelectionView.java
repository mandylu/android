package com.quanleimu.view;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.quanleimu.activity.BaseActivity;
import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.adapter.CommonItemAdapter;
import com.quanleimu.adapter.MainCateAdapter;
//import com.quanleimu.adapter.SecondCatesAdapter;
import com.quanleimu.entity.AllCates;
import com.quanleimu.entity.FirstStepCate;
import com.quanleimu.entity.PostMu;
import com.quanleimu.entity.SecondStepCate;
import com.quanleimu.jsonutil.JsonUtil;
import com.quanleimu.util.Communication;
import com.quanleimu.util.ErrorHandler;
import com.quanleimu.util.Util;
import com.quanleimu.activity.R;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
public class CategorySelectionView extends ListView {
	
	public interface ICateSelectionListener{
		
		public void OnMainCategorySelected(FirstStepCate selectedMainCate);
		
		public void OnSubCategorySelected(SecondStepCate selectedSubCate);
	};
	
	
	protected ICateSelectionListener selectionListener = null;
	protected ProgressDialog progressDialog = null;
	
	protected static String mainCateCacheTag = "saveFirstStepCate";
	protected static String mainCateAPI = "category_list";
	protected AllCates mainCate = null;
	//protected ListView lvAllCates = null;
	protected MainCateAdapter allCateAdapter = null;

//	protected List<SecondStepCate> subCate = null;
	//protected ListView lvSubCate = null;
	protected CommonItemAdapter secondCateAdapter = null;
	
	public enum ECATE_LEVEL{
		ECATE_LEVEL_MAIN,
		ECATE_LEVEL_SUB
	};
	protected ECATE_LEVEL curLevel = ECATE_LEVEL.ECATE_LEVEL_MAIN;
	public ECATE_LEVEL getLevel(){
		return curLevel;
	}
	
	protected class MainCateItemClickListener implements AdapterView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1,
				int arg2, long arg3) {
			
			FirstStepCate selectedMainCate = (FirstStepCate)CategorySelectionView.this.allCateAdapter.getList().get(arg2);
			String cateName = selectedMainCate.getName();
			

			
			if(null == secondCateAdapter){
				secondCateAdapter = new CommonItemAdapter(CategorySelectionView.this.getContext(), selectedMainCate.getChildren());
			}
			
			CategorySelectionView.this.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					SecondStepCate selectedSubCate =  (SecondStepCate)CategorySelectionView.this.secondCateAdapter.getList().get(arg2);							
					
					if(null != CategorySelectionView.this.selectionListener){
						if(QuanleimuApplication.listUsualCates != null){
							int size = QuanleimuApplication.listUsualCates.size();
							for(int i = 0; i < size; ++ i){
								if(QuanleimuApplication.listUsualCates.get(i).getName().equals(selectedSubCate.getName())){
									QuanleimuApplication.listUsualCates.remove(i);
									break;
								}
							}
							QuanleimuApplication.listUsualCates.add(0, selectedSubCate);
							size = QuanleimuApplication.listUsualCates.size();
							while(size > 5){
								QuanleimuApplication.listUsualCates.remove(5);
								size = QuanleimuApplication.listUsualCates.size();
							}
							Util.saveDataToLocate(getContext(), "listUsualCates", QuanleimuApplication.listUsualCates);
						}
						CategorySelectionView.this.selectionListener.OnSubCategorySelected(selectedSubCate);
					}
				}
			});
			
			if(null != secondCateAdapter && !cateName.equals((String)secondCateAdapter.getTag())){
				secondCateAdapter.setTag(cateName);
				secondCateAdapter.setList(selectedMainCate.getChildren());
			}
			
			CategorySelectionView.this.setAdapter(secondCateAdapter);
			CategorySelectionView.this.curLevel = ECATE_LEVEL.ECATE_LEVEL_SUB;
			
			if(null != CategorySelectionView.this.selectionListener){
				CategorySelectionView.this.selectionListener.OnMainCategorySelected(selectedMainCate);
				}
			}
		};
	
	public CategorySelectionView(Context context, View headerView, View footerView) {
		super(context);
		
		init(context);
		
		setHeaderFooterView(headerView, footerView);
	}

	public void init(Context context) {
		this.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
//		this.setSelector(R.drawable.list_selector);
		this.setCacheColorHint(0);
//		this.setFocusable(true);
		
		try{
			Drawable divider = this.getResources().getDrawable(R.drawable.list_divider);
			this.setDivider(divider);
			this.setDividerHeight(1);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		this.setBackgroundResource(R.drawable.home_bg);
			
		this.setOnItemClickListener(new MainCateItemClickListener());
	}
	
	public void setHeaderFooterView(View headerView, View footerView){
		if(null != headerView)
			this.addHeaderView(headerView);
		if(null != footerView)
		this.addFooterView(footerView);
		
		//main cate list
		List<FirstStepCate> allCates = QuanleimuApplication.getApplication().getListFirst();
		
		if(null != allCates && allCates.size() > 0)
		{	
			this.ApplyAllCates(allCates);
			
			PostMu postMu = (PostMu) Util.loadDataFromLocate(getContext(), mainCateCacheTag);
	
			if (postMu != null && !postMu.getJson().equals("")) {
				
				long time = postMu.getTime();
				if (time + (7 * 24 * 3600 * 1000) < System.currentTimeMillis()) {
					(new AllCateTask()).execute(true);
				} 
			} 
		}
		else {
			progressDialog = ProgressDialog.show(getContext(), "提示", "正在更新分类列表,请稍候...");			
			progressDialog.setCancelable(true);
			
			(new AllCateTask()).execute(true);
		}
	}
	
	public CategorySelectionView(Context context, AttributeSet attrs){
		super(context, attrs);

		init(context);		
	}

	protected void parseCategory(String json) {
		
		mainCate = JsonUtil.getAllCatesFromJson(Communication.decodeUnicode(json));
		
		((QuanleimuApplication)((BaseActivity)getContext()).getApplication()).setListFirst(mainCate.getChildren());
		
		ApplyAllCates(mainCate.getChildren());
	}

	protected void ApplyAllCates(List<FirstStepCate> allCateList) {
		if (allCateList == null || allCateList.size() == 0) {
			QuanleimuApplication.getApplication().getErrorHandler().sendEmptyMessage(ErrorHandler.ERROR_SERVICE_UNAVAILABLE);
		} else {			
			if(allCateAdapter == null){
				allCateAdapter = new MainCateAdapter(getContext(), allCateList);
				this.setAdapter(allCateAdapter);	
			}
			else{
				allCateAdapter.setList(allCateList);
			}
		}
	}
	
	public void setSelectionListener(ICateSelectionListener listener){
		this.selectionListener = listener;
	}	
	
	public boolean OnBack(){
		if(curLevel == ECATE_LEVEL.ECATE_LEVEL_SUB){
			this.setOnItemClickListener(new MainCateItemClickListener());
			this.setAdapter(allCateAdapter);
			curLevel = ECATE_LEVEL.ECATE_LEVEL_MAIN; 			
			return true;
		}
		
		return false;
	}
	
	
	class AllCateTask extends AsyncTask<Boolean, Void, String> {	
		
		public AllCateTask() {
		}
		
		protected String doInBackground(Boolean... bs) {   
			
			String apiName = CategorySelectionView.mainCateAPI;
			ArrayList<String> list = new ArrayList<String>();
			String url = Communication.getApiUrl(apiName, list);
			try {
				String json = Communication.getDataByUrl(url);

				if (json != null) {
					
					PostMu postMu = new PostMu();
					postMu.setJson(json);
					postMu.setTime(System.currentTimeMillis());
					Util.saveDataToLocate(CategorySelectionView.this.getContext(), mainCateCacheTag, postMu);
					
					
//					File file = new File("/sdcard/cateJson.txt");
//					ObjectOutputStream out = null;   
//					try {     
//						out = new ObjectOutputStream(new FileOutputStream(file));
//						
//						out.writeObject(postMu);
//					}catch(IOException e){
//						
//					}catch(Exception e){
//						
//					}
//					finally { 
//						if (out != null) {    
//							try{
//								out.close();     
//							}catch(Exception e){}
//						}   
//					} 
					
				} else {
					QuanleimuApplication.getApplication().getErrorHandler().sendEmptyMessage(ErrorHandler.ERROR_SERVICE_UNAVAILABLE);
				}
				
				return json;
			} catch (UnsupportedEncodingException e) {
				QuanleimuApplication.getApplication().getErrorHandler().sendEmptyMessage(ErrorHandler.ERROR_NETWORK_UNAVAILABLE);
				e.printStackTrace();
			} catch (IOException e) {
				QuanleimuApplication.getApplication().getErrorHandler().sendEmptyMessage(ErrorHandler.ERROR_NETWORK_UNAVAILABLE);
				e.printStackTrace();
			}
			
			return null;
		}
		
		protected void onPostExecute(String json) { 
			if(null != json && json.length() > 0){
				parseCategory(json);
			}
			
			if(progressDialog != null){
				progressDialog.dismiss();
			}
		}
	};
}

package com.baixing.broadcast.push;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import com.baixing.activity.BaseActivity;
import com.baixing.android.api.ApiClient;
import com.baixing.android.api.ApiParams;
import com.baixing.data.GlobalDataManager;
import com.baixing.entity.AdList;
import com.baixing.entity.Category;
import com.baixing.jsonutil.JsonUtil;
import com.baixing.util.VadListLoader;
import com.baixing.view.fragment.HomeFragment;
import com.baixing.view.fragment.ListingFragment;
import com.baixing.view.fragment.MyAdFragment;
import com.baixing.view.fragment.SecondCateFragment;
import com.baixing.view.fragment.VadFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.Toast;

public class PageJumper{
	private static final String PAGE_HOME = "home";
	private static final String PAGE_LISTING = "listing";
	private static final String PAGE_CATEGORY = "category";
	private static final String PAGE_VIEWAD = "viewad";
	private static final String PAGE_MY = "my";
	
	static public boolean isValidPage(String page){
		return page.equals(PAGE_CATEGORY) 
				|| page.equals(PAGE_LISTING)
				|| page.equals(PAGE_HOME)
				|| page.equals(PAGE_MY)
				|| page.equals(PAGE_VIEWAD);
	}
	
	static public void jumpToPage(BaseActivity currentActivity, String pageName, String data){
		Fragment currentFrag = currentActivity.getCurrentFragment();
		if(pageName.equals(PAGE_HOME)){
			if(currentFrag == null || !(currentFrag instanceof HomeFragment)){
				currentActivity.pushFragment(new HomeFragment(), new Bundle(), true);
			}
		}else if(pageName.equals(PAGE_LISTING)){
			try {
				JSONObject obj = new JSONObject(data);
				String categoryEnglishName = obj.getString("englishname");
				if(categoryEnglishName == null || categoryEnglishName.length() == 0){
					return;
				}
				List<Category> allCates = GlobalDataManager.getInstance().getFirstLevelCategory();
				if(allCates != null){
					for(int i = 0; i < allCates.size(); ++ i){
						Category cat = allCates.get(i);
						List<Category> subCates = cat.getChildren();
						for(int j = 0; j < subCates.size(); ++ j){
							if(subCates.get(j).getEnglishName().equals(categoryEnglishName)){
								String name = subCates.get(j).getName();
								Bundle arg = new Bundle();
								arg.putString("name", name);
								arg.putString("backPageName", "返回");
								arg.putString("categoryEnglishName", categoryEnglishName);
								currentActivity.pushFragment(new ListingFragment(), arg, false);
								return;
							}
						}
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Toast.makeText(currentActivity, "数据错误", 0).show();
				e.printStackTrace();
			}
		}else if(pageName.equals(PAGE_CATEGORY)){
			
			try {
				JSONObject obj = new JSONObject(data);
				String categoryEnglishName = obj.getString("englishname");
				if(categoryEnglishName == null || categoryEnglishName.length() == 0){
					return;
				}

				List<Category> cates = GlobalDataManager.getInstance().getFirstLevelCategory();
				if(cates != null){
					for(int i = 0; i < cates.size(); ++ i){
						if(cates.get(i).getEnglishName().equals(categoryEnglishName)){
							Category cate = cates.get(i);
							Bundle bundle = new Bundle();
//							bundle.putInt(BaseFragment.ARG_COMMON_REQ_CODE, );
							bundle.putSerializable("cates", cate);
							bundle.putBoolean("isPost", false);
							currentActivity.pushFragment(new SecondCateFragment(), bundle, true);
							return;
						}
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if(pageName.equals(PAGE_VIEWAD)){
			try {
				JSONObject obj = new JSONObject(data);
				ApiParams param = new ApiParams();
				param.addParam("query", "id:" + obj.getString("id"));
				String result = ApiClient.getInstance().invokeApi(ApiClient.Api.createGet("ad_list"), param);
				AdList gl = JsonUtil.getGoodsListFromJson(result);
				if(gl != null && gl.getData() != null && gl.getData().size() > 0){
					VadListLoader glLoader = new VadListLoader(null, null, null, gl);
					glLoader.setGoodsList(gl);
					glLoader.setHasMore(false);		
					Bundle bundle2 = new Bundle();
					bundle2.putSerializable("loader", glLoader);
					bundle2.putInt("index", 0);	
					currentActivity.pushFragment(new VadFragment(), bundle2, false);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if(pageName.equals(PAGE_MY)){
			Bundle bundle = new Bundle();
			bundle.putInt(MyAdFragment.TYPE_KEY, 0);
			currentActivity.pushFragment(new MyAdFragment(), bundle, false);
		}
	}
}
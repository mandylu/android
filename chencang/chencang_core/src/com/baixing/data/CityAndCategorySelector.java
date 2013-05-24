package com.baixing.data;

import java.util.List;

import android.util.Pair;

import com.baixing.entity.Category;
import com.baixing.util.Util;

public class CityAndCategorySelector{
	public boolean isCitySelectable(){
		String city = Util.getConfigName("city");
		return (city == null || city.equals("all")) ? true : false;
	}
	
	static private CityAndCategorySelector instance;
	
	static public CityAndCategorySelector getInstance(){
		if(instance == null){
			instance =  new CityAndCategorySelector();
		}
		return instance;
	}
	
	private CityAndCategorySelector(){
		
	}
	
	public Pair<String, String> parseCategory(){
		String category = Util.getConfigName("category");
		if(category == null || category.equals("all") || category.equals("")){
			return null;
		}
		String name = "";
		List<Category> all = GlobalDataManager.getInstance().getFirstLevelCategory();
		for(int i = 0; i < all.size(); ++ i){
			if(all.get(i).getEnglishName().equals(category)){
				categorySelectable = true;
				break;
			}
			List<Category> sec = all.get(i).getChildren();
			for(int j = 0; j < sec.size(); ++ j){
				if(sec.get(j).getEnglishName().equals(category)){
					categorySelectable = false;
					name = sec.get(j).getName();
					break;
				}
			}
		}
		return name.equals("") ? null : new Pair<String, String>(category, name);
	}
	
	private boolean categorySelectable = true;
	public boolean isCategorySelectable(){
		return categorySelectable;
	}
}
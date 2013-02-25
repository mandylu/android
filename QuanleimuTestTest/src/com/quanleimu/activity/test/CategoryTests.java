package com.quanleimu.activity.test;

import com.baixing.entity.Category;
import com.baixing.jsonutil.JsonUtil;
import com.baixing.util.TextUtil;
import com.baixing.util.Util;

import android.test.AndroidTestCase;
import android.util.Pair;

/**
 * 
 * @author liuchong
 *
 */
public class CategoryTests extends AndroidTestCase {
	
	public void testLoadCate() {
		Pair<Long, String> pair = Util.loadDataAndTimestampFromAssets(this.getContext(), "cateJson.txt");
		
		String json = pair.second;
		Category root = JsonUtil.loadCategoryTree(TextUtil.decodeUnicode(json));
		assertEquals("root", root.getEnglishName());
		assertEquals("所有类目", root.getName());
		assertNull(root.getParent());
		assertNotNull(root.getChildren());
		assertTrue(root.getChildren().size() > 0);
		
	}
	
	public void testFindCatByEnglishName() {
		Pair<Long, String> pair = Util.loadDataAndTimestampFromAssets(this.getContext(), "cateJson.txt");
		
		String json = pair.second;
		Category root = JsonUtil.loadCategoryTree(TextUtil.decodeUnicode(json));

		//First level check
		Category ershou = root.findCategoryByEnglishName("ershou");
		assertEquals("物品交易", ershou.getName());
		assertEquals("root", ershou.getParentEnglishName());
		assertTrue(ershou.getChildren().size() > 0);
		
		
		//Second level check
		Category fushi = root.findCategoryByEnglishName("fushi");
		assertEquals("服装/配饰", fushi.getName());
		assertEquals("ershou", fushi.getParentEnglishName());
		assertTrue(fushi.getChildren().size() == 0);
	}
	
	public void testFindChildByName() {
		Pair<Long, String> pair = Util.loadDataAndTimestampFromAssets(this.getContext(), "cateJson.txt");
		
		String json = pair.second;
		Category root = JsonUtil.loadCategoryTree(TextUtil.decodeUnicode(json));
		
		//First level find.
		Category ershou = root.findChildByName("物品交易");
		assertEquals("物品交易", ershou.getName());
		assertEquals("root", ershou.getParentEnglishName());
		assertTrue(ershou.getChildren().size() > 0);
		
		//Second level find
		Category fushi = ershou.findChildByName("服装/配饰");
		assertEquals("服装/配饰", fushi.getName());
		assertEquals("ershou", fushi.getParentEnglishName());
		assertTrue(fushi.getChildren().size() == 0);
	}
}

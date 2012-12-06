package com.quanleimu.activity.test;

import java.io.IOException;

import android.test.AndroidTestCase;

import com.baixing.entity.GoodsDetail;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import junit.framework.TestCase;

public class JsonParser extends AndroidTestCase {

	String s = "{\"data\":{\"价格\":\"220元\",\"faburen\":\"m33660\",\"categoryNames\":\"物品交易,台式电脑\",\"count\":\"59\",\"areaNames\":\"上海,闵行,航华\",\"link\":\"http://shanghai.baixing.com/diannao/a251166214.html\",\"lng\":\"121.4912392\",\"地区\":\"m2216\",\"cityEnglishName\":\"shanghai\",\"categoryEnglishName\":\"diannao\",\"contact\":\"15026870495\",\"wanted\":\"0\",\"id\":\"251166214\",\"title\":\"大硬盘的富士康主板加航嘉电源加飞利浦显示器\",\"description\":\"以前公司当监控服务器用的电脑,装在机房铁柜里的,故机箱是带提手的,基本配置,无光驱,富士康主板,intel865北桥,256M内存,自己可加大内存，西部数据250G大硬盘,飞利浦17纯平显示器,航嘉300W电源,一切可正常使用,还有一块视频采集卡120块,以前装在此服务器里面的,都要可以一起300拿走\",\"userId\":\"61244266\",\"mobileArea\":\"上海\",\"imageFlag\":\"1\",\"lat\":\"31.2470894\",\"categoryFirstLevelEnglishName\":\"ershou\",\"status\":\"0\",\"createdTime\":\"1354614737\",\"分类_s\":\"影音体验,组装机\",\"地区_s\":\"航华,闵行,上海\",\"insertedTime\":\"1354183331\",\"areaFirstLevelId\":\"null\",\"areaCityLevelId\":\"21\",\"postMethod\":\"Normal\",\"lastOperation\":\"false\",\"分类\":\"m18992\",\"具体地点\":\"776公交站附近\",\"areaSecondLevelId\":\"null\",\"mobile\":\"15026870495\"},\"metaData\":[\"类型 组装机 影音体验\",\"价格 220元\",\"地点 闵行 航华\",\"具体地点 776公交站附近\",\"发布人 个人\",\"分类 物品交易|台式电脑\",\"查看 59次\"],\"imageList\":{\"big\":\"http://tu.baixing.net/bc728d72133740f6fb563ee0496f4435.jpg_bi,http://tu.baixing.net/e35a139282080f8899c1e08836417d93.jpg_bi,http://tu.baixing.net/83548a4230f3546377d285b5526399e8.jpg_bi,http://tu.baixing.net/9c1ac60130ad0f8dff32618b126676d2.jpg_bi,http://tu.baixing.net/bdf3d7d5fa7afc3ac8b9a23dc1dce7d3.jpg_bi,http://tu.baixing.net/3cafec498b8cd06e44603ae3e1e13bce.jpg_bi,http://tu.baixing.net/007447da2952c043de9843a4705c4ade.jpg_bi,http://tu.baixing.net/d26d57295646eede1124f2995f764b15.jpg_bi\",\"resize180\":\"http://tu.baixing.net/bc728d72133740f6fb563ee0496f4435.jpg_180x180,http://tu.baixing.net/e35a139282080f8899c1e08836417d93.jpg_180x180,http://tu.baixing.net/83548a4230f3546377d285b5526399e8.jpg_180x180,http://tu.baixing.net/9c1ac60130ad0f8dff32618b126676d2.jpg_180x180,http://tu.baixing.net/bdf3d7d5fa7afc3ac8b9a23dc1dce7d3.jpg_180x180,http://tu.baixing.net/3cafec498b8cd06e44603ae3e1e13bce.jpg_180x180,http://tu.baixing.net/007447da2952c043de9843a4705c4ade.jpg_180x180,http://tu.baixing.net/d26d57295646eede1124f2995f764b15.jpg_180x180\",\"small\":null,\"square\":null},\"distance\":0.0,\"keys\":[\"价格\",\"faburen\",\"categoryNames\",\"count\",\"areaNames\",\"link\",\"lng\",\"地区\",\"cityEnglishName\",\"categoryEnglishName\",\"contact\",\"wanted\",\"id\",\"title\",\"description\",\"userId\",\"mobileArea\",\"imageFlag\",\"lat\",\"categoryFirstLevelEnglishName\",\"status\",\"createdTime\",\"分类_s\",\"地区_s\",\"insertedTime\",\"areaFirstLevelId\",\"areaCityLevelId\",\"postMethod\",\"lastOperation\",\"分类\",\"具体地点\",\"areaSecondLevelId\",\"mobile\"]}";
	
	public void testParseGoodDetail()
	{
		ObjectMapper mapper = new ObjectMapper();
		ObjectReader reader = mapper.reader(GoodsDetail.class);
		try {
			Object obj = reader.readValue(s);
			assertTrue(true);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}
	}
	
}

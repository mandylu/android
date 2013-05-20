package com.baixing.jsonutil;

import java.util.ArrayList;
import java.util.List;

import com.baixing.entity.Category;
import com.baixing.entity.CityDetail;

public class LocateJsonData {

	public static List<Category> getUsualCatesJson()
	{
		List<Category> listSecondCate = new ArrayList<Category>();
		
		Category secondStepCate1 = new Category();
		secondStepCate1.setEnglishName("shouji");
		secondStepCate1.setName("二手手机");
		secondStepCate1.setParentEnglishName("ershou");
		
		Category secondStepCate2 = new Category();
		secondStepCate2.setEnglishName("gongren");
		secondStepCate2.setName("本地工人/技工");
		secondStepCate2.setParentEnglishName("gongzuo");
		
		Category secondStepCate3 = new Category();
		secondStepCate3.setEnglishName("nvzhaonan");
		secondStepCate3.setName("女找男");
		secondStepCate3.setParentEnglishName("huodong");
		
		Category secondStepCate4 = new Category();
		secondStepCate4.setEnglishName("ershouqiche");
		secondStepCate4.setName("二手轿车");
		secondStepCate4.setParentEnglishName("cheliang");
		
		Category secondStepCate5 = new Category();
		secondStepCate5.setEnglishName("zhengzu");
		secondStepCate5.setName("租房");
		secondStepCate5.setParentEnglishName("fang");
		
		listSecondCate.add(secondStepCate1);
		listSecondCate.add(secondStepCate2);
		listSecondCate.add(secondStepCate3);
		listSecondCate.add(secondStepCate4);
		listSecondCate.add(secondStepCate5);
		return listSecondCate;
	}
	
	public static List<CityDetail> hotCityList()
	{
		List<CityDetail> listHotCity = new ArrayList<CityDetail>();
		
		CityDetail cityDetail1 = new CityDetail();
		cityDetail1.setEnglishName("shanghai");
		cityDetail1.setName("上海");
		listHotCity.add(cityDetail1);
		
		CityDetail cityDetail2 = new CityDetail();
		cityDetail2.setEnglishName("guangzhou");
		cityDetail2.setName("广州");
		listHotCity.add(cityDetail2);
		
		CityDetail cityDetail3 = new CityDetail();
		cityDetail3.setEnglishName("beijing");
		cityDetail3.setName("北京");
		listHotCity.add(cityDetail3);
		
		CityDetail cityDetail4 = new CityDetail();
		cityDetail4.setEnglishName("shenzhen");
		cityDetail4.setName("深圳");
		listHotCity.add(cityDetail4);
		
		CityDetail cityDetail12 = new CityDetail();
		cityDetail12.setEnglishName("chongqing");
		cityDetail12.setName("重庆");
		listHotCity.add(cityDetail12);
		
		CityDetail cityDetail13 = new CityDetail();
		cityDetail13.setEnglishName("tianjin");
		cityDetail13.setName("天津");
		listHotCity.add(cityDetail13);
		
		CityDetail cityDetail5 = new CityDetail();
		cityDetail5.setEnglishName("suzhou");
		cityDetail5.setName("苏州");
		listHotCity.add(cityDetail5);
		
		CityDetail cityDetail6 = new CityDetail();
		cityDetail6.setEnglishName("chengdu");
		cityDetail6.setName("成都");
		listHotCity.add(cityDetail6);
		
		CityDetail cityDetail7 = new CityDetail();
		cityDetail7.setEnglishName("xian");
		cityDetail7.setName("西安");
		listHotCity.add(cityDetail7);
		
		CityDetail cityDetail8 = new CityDetail();
		cityDetail8.setEnglishName("shenyang");
		cityDetail8.setName("沈阳");
		listHotCity.add(cityDetail8);
		
		
		CityDetail cityDetail10 = new CityDetail();
		cityDetail10.setEnglishName("wuxi");
		cityDetail10.setName("无锡");
		listHotCity.add(cityDetail10);
		
		CityDetail cityDetail11 = new CityDetail();
		cityDetail11.setEnglishName("hangzhou");
		cityDetail11.setName("杭州");
		listHotCity.add(cityDetail11);
		//chongqing
		
		return listHotCity;
		
	}
	
}

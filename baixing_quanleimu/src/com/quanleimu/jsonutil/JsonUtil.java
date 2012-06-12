package com.quanleimu.jsonutil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.quanleimu.entity.AllCates;
import com.quanleimu.entity.CityDetail;
import com.quanleimu.entity.CityList;
import com.quanleimu.entity.Filters;
import com.quanleimu.entity.Filterss;
import com.quanleimu.entity.FirstStepCate;
import com.quanleimu.entity.GoodsDetail;
import com.quanleimu.entity.GoodsList;
import com.quanleimu.entity.HotData;
import com.quanleimu.entity.HotList;
import com.quanleimu.entity.ImageList;
import com.quanleimu.entity.PostGoodsBean;
import com.quanleimu.entity.SecondStepCate;
import com.quanleimu.entity.labels;
import com.quanleimu.entity.values;
public class JsonUtil {

	// 获取所有城市列表
	public static CityList parseCityListFromJson(String jsonData) {
		CityList cityList = new CityList();
		try {
			List<CityDetail> list = new ArrayList<CityDetail>();
			// JSONArray jsonA = new JSONArray(jsonData);

			JSONObject jsonObj = new JSONObject(jsonData);

			JSONArray jsonArray = jsonObj.names();
			for (int i = 0; i < jsonArray.length(); i++) {
				CityDetail cityDetail = new CityDetail();
				cityDetail.setId(jsonArray.getString(i));
				JSONObject jsonCity = jsonObj.getJSONObject(jsonArray
						.getString(i));
				cityDetail.setEnglishName(jsonCity.getString("englishName"));
				cityDetail.setName(jsonCity.getString("name"));
				cityDetail.setSheng(jsonCity.getString("sheng"));
				list.add(cityDetail);
			}
			cityList.setListDetails(list);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		return cityList;
	}
	
	// 获取热点集合
	public static List<HotList> parseCityHotFromJson(String jsonData) {
		List<HotList> listHot = new ArrayList<HotList>();

		try {
			JSONArray jsonA = new JSONArray(jsonData);
			for (int i = 0; i < jsonA.length(); i++) {
				HotList hotList = new HotList();
				JSONObject jsonObj = jsonA.getJSONObject(i);
				try {
					hotList.setImgUrl(jsonObj.getString("imgUrl"));
				} catch (Exception e1) {
					hotList.setImgUrl("");
					e1.printStackTrace();
				}
				try {
					hotList.setType(Integer.valueOf(jsonObj.getInt("type")));
				} catch (Exception e1) {
					hotList.setType(-1);
					e1.printStackTrace();
				}

				HotData hotData = new HotData();
				JSONObject jsonHotData = jsonObj.getJSONObject("data");
				try {
					if(jsonHotData.has("keyword"))
					{
						hotData.setKeyword(jsonHotData.getString("keyword"));
					}
				} catch (Exception e) {
					hotData.setKeyword("");
					e.printStackTrace();
				}
				try {
					if(jsonHotData.has("title"))
					{
						hotData.setTitle(jsonHotData.getString("title"));
					}
				} catch (Exception e) {
					hotData.setTitle("");
					e.printStackTrace();
				}
				try {
//					jsonHotData.has("weburl");
					if(jsonHotData.has("weburl") == true)
					{
						hotData.setWeburl(jsonHotData.getString("weburl"));
					}
					
				} catch (Exception e) {
					hotData.setWeburl("");
					e.printStackTrace();
				}

				hotList.setHotData(hotData);
				listHot.add(hotList);
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return listHot;
	}

	static AllCates allCates = null;
	// 获取所有类目列表
	public static AllCates getAllCatesFromJson(String jsonData) {
//		static final AllCates allCates = null;
		if(allCates != null) return allCates;
		try {
			allCates = new AllCates();
			JSONObject jsonObj = new JSONObject(jsonData);
			allCates.setName(jsonObj.getString("name"));
			allCates.setEnglishName(jsonObj.getString("englishName"));

			JSONArray jsonA = new JSONArray(jsonObj.getString("children"));
			List<FirstStepCate> listFirst = new ArrayList<FirstStepCate>();
			for (int i = 0; i < jsonA.length(); i++) {
				FirstStepCate firstStepCate = new FirstStepCate();
				JSONObject jsonFirstStepCate = jsonA.getJSONObject(i);
				firstStepCate.setName(jsonFirstStepCate.getString("name"));
				firstStepCate.setEnglishName(jsonFirstStepCate
						.getString("englishName"));
				firstStepCate.setParentEnglishName(jsonFirstStepCate
						.getString("parentEnglishName"));

				JSONArray jsonB = new JSONArray(
						jsonFirstStepCate.getString("children"));
				List<SecondStepCate> listSecond = new ArrayList<SecondStepCate>();
				for (int j = 0; j < jsonB.length(); j++) {
					SecondStepCate secondStepCate = new SecondStepCate();
					JSONObject jsonSecondStepCate = jsonB.getJSONObject(j);

					secondStepCate
							.setName(jsonSecondStepCate.getString("name"));
					secondStepCate.setEnglishName(jsonSecondStepCate
							.getString("englishName"));
					secondStepCate.setParentEnglishName(jsonSecondStepCate
							.getString("parentEnglishName"));

					listSecond.add(secondStepCate);
				}
				firstStepCate.setChildren(listSecond);

				listFirst.add(firstStepCate);
			}
			allCates.setChildren(listFirst);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			allCates = null;
			e1.printStackTrace();
		}
		return allCates;
	}

	// 获取附近的Goods信息
	public static GoodsList getGoodsListFromJson(String jsonData) {
		GoodsList goodsList = new GoodsList();
		JSONObject jsonObj;
		try {
			System.out.println("jsonData------------------->"+jsonData);
			jsonObj = new JSONObject(jsonData);
			System.out.println("jsonObj------------------->"+jsonObj);
			goodsList.setCount(jsonObj.getInt("count"));
			List<GoodsDetail> list = new ArrayList<GoodsDetail>();
			JSONArray jsonArray = new JSONArray();
			try {
				jsonArray = jsonObj.getJSONArray("data");
				for (int i = 0; i < jsonArray.length(); i++) {
					
					JSONObject jsonGoods = jsonArray.optJSONObject(i);
					GoodsDetail goodsDetail = new GoodsDetail();

					JSONArray names = jsonGoods.names();
					for(int j = 0; j < names.length(); ++ j){
						Object subObj = jsonGoods.get(names.getString(j));
						if(subObj != null){
							String value = null;
							if(subObj.getClass().equals(String.class) && ((String)subObj).length() > 0){
								value = (String)subObj;								
							}
							else if(subObj.getClass().equals(Integer.class)){
								value = ((Integer)subObj).toString();
							}else if(subObj.getClass().equals(Double.class)){
								value = ((Double)subObj).toString();
							}else if(subObj.getClass().equals(Float.class)){
								value = ((Float)subObj).toString();
							}else{
								Log.println(0, "in JsonUtil ", "unknown jason value type!!!!!");
							}
								
							goodsDetail.setValueByKey(names.getString(j), value);
						}						
					}
/*					
					try {
						goodsDetail.setId(jsonGoods.getString("id"));
					} catch (Exception e1) {
						goodsDetail.setId("");
					}

					try {
						goodsDetail.setLink(jsonGoods.getString("link"));
					} catch (Exception e1) {
						goodsDetail.setLink("");
					}
					
					try {
						goodsDetail.setMobile(jsonGoods.getString("mobile"));
					} catch (JSONException e1) {
						goodsDetail.setMobile("无");
					}

					try {
						goodsDetail.setDate(Long.parseLong(jsonGoods.getString("createdTime")));
					} catch (Exception e1) {
						goodsDetail.setDate(Long.parseLong(""));
					}
					try {
						goodsDetail.setLat(jsonGoods.getString("lat"));
					} catch (Exception e1) {
						goodsDetail.setLat("");
					}
					try {
						goodsDetail.setLng(jsonGoods.getString("lng"));
					} catch (Exception e1) {
						goodsDetail.setLng("");
					}
					try {
						goodsDetail.setCategoryEnglishName(jsonGoods
								.getString("categoryEnglishName"));
					} catch (Exception e1) {
						goodsDetail.setCategoryEnglishName("");
					}
					try {
						goodsDetail.setAreaNames(jsonGoods
								.getString("areaNames"));
					} catch (Exception e1) {
						goodsDetail.setAreaNames("无");
					}
					*/
					// 为ImageList赋值
					JSONObject jsonImages = null;
					ImageList imageList = new ImageList();
					try {
						jsonImages = jsonGoods.getJSONObject("images");
					} catch (Exception e2) {
						jsonImages = null;
					}
					if (jsonImages == null) {
						imageList = null;
					} else {
						try {
							imageList.setBig(jsonImages.getString("big"));
						} catch (Exception e1) {
							imageList.setBig("");
						}
						try {
							imageList.setResize180(jsonImages
									.getString("resize180"));
						} catch (Exception e1) {
							imageList.setResize180("");
						}
					}

					goodsDetail.setImageList(imageList);

					ArrayList<String> metas = new ArrayList<String>();
					JSONArray jsonMeta;
					try {
						jsonMeta = jsonGoods.getJSONArray("metaData");
					} catch (Exception e1) {
						jsonMeta = null;
					}
					if (jsonMeta == null || jsonMeta.length() == 0) {
						metas = null;
					} else {
						for (int j = 0; j < jsonMeta.length(); j++) {
							String meta = jsonMeta.get(j).toString();
							//String a[] = meta.split(" ");
							metas.add(meta);
							//map.put(a[0], a[1]);
						}
					}
					goodsDetail.setMetaData(metas);
					list.add(goodsDetail);

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			goodsList.setData(list);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return goodsList;
	}

	public static Filters getFilters(String jsonData) {
		Filters filters = new Filters();
		try {
			JSONArray jsonArray = new JSONArray(jsonData);
			List<Filterss> list = new ArrayList<Filterss>();
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				Filterss mfil = new Filterss();
				try {
					mfil.setName(jsonObject.getString("name"));
				} catch (Exception e1) {
					mfil.setName("");
				}
				try {
					mfil.setDisplayName(jsonObject.getString("displayName"));
				} catch (Exception e1) {
					mfil.setDisplayName("");
				}
				try {
					mfil.setUnit(jsonObject.getString("unit"));
				} catch (Exception e1) {
					mfil.setUnit("");
				}
				try {
					mfil.setControlType(jsonObject.getString("controlType"));
				} catch (Exception e1) {
					mfil.setControlType("");
				}
				// values数组
				JSONArray valueArray;
				List<values> vlist = new ArrayList<values>();
				try {
					valueArray = jsonObject.getJSONArray("values");
				} catch (Exception e) {
					valueArray = null;
				}
				if (valueArray == null) {
					vlist = null;
				} else {
					for (int j = 0; j < valueArray.length(); j++) {
						values mvalues = new values();
						try {
							mvalues.setValue(valueArray.get(j).toString());
						} catch (Exception e) {
							mvalues.setValue("");
						}
						vlist.add(mvalues);
					}
					mfil.setValuesList(vlist);
				}

				// labels
				List<labels> llist = new ArrayList<labels>();
				JSONArray labelArray;
				try {
					labelArray = jsonObject.getJSONArray("labels");
				} catch (Exception e) {
					labelArray = null;

				}
				if (labelArray == null) {
					llist = null;
				} else {
					for (int k = 0; k < labelArray.length(); k++) {
						labels mlables = new labels();
						try {
							mlables.setLabel(labelArray.get(k).toString());
						} catch (Exception e) {
							mlables.setLabel("");
						}
						llist.add(mlables);
					}
					mfil.setLabelsList(llist);
				}

				try {
					mfil.setNumeric(jsonObject.getString("numeric"));
				} catch (Exception e) {
					mfil.setNumeric("");
				}
				try {
					mfil.setRequired(jsonObject.getString("required"));
				} catch (Exception e) {
					mfil.setRequired("");
					e.printStackTrace();
				}

				list.add(mfil);
				filters.setFilterssList(list);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return filters;

	}

	public static LinkedHashMap<String,PostGoodsBean> getPostGoodsBean(String jsonData) {
		LinkedHashMap<String,PostGoodsBean> postList = new LinkedHashMap<String,PostGoodsBean>();
		try {
			JSONArray jsonArray = new JSONArray(jsonData);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				PostGoodsBean postGoods = new PostGoodsBean();

				try {
					postGoods.setUnit(jsonObject.getString("unit"));
				} catch (Exception e1) {
					postGoods.setUnit("");
				}
				try {
					postGoods.setControlType(jsonObject
							.getString("controlType"));
				} catch (Exception e1) {
					postGoods.setControlType("");
				}
				try {
					postGoods.setNumeric(jsonObject.getInt("numeric"));
				} catch (Exception e1) {
					postGoods.setNumeric(0);
				}
				try {
					postGoods.setRequired(jsonObject.getString("required"));
				} catch (Exception e1) {
					postGoods.setRequired("");
				}
				try {
					postGoods.setDisplayName(jsonObject
							.getString("displayName"));
				} catch (Exception e1) {
					postGoods.setDisplayName("");
				}
				try {
					postGoods.setName(jsonObject.getString("name"));
				} catch (Exception e1) {
					postGoods.setName("");
				}
				try {
					postGoods.setSubMeta(jsonObject.getString("subMeta"));
				} catch (Exception e1) {
					postGoods.setSubMeta("");
				}
				

				// values数组
				JSONArray valueArray;
				List<String> vlist = new ArrayList<String>();
				try {
					valueArray = jsonObject.getJSONArray("values");
				} catch (Exception e) {
					valueArray = null;
				}
				if (valueArray == null) {
					vlist = null;
				} else {
					for (int j = 0; j < valueArray.length(); j++) {
						String values = "";
						try {
							values = valueArray.get(j).toString();
						} catch (Exception e) {
							values = "";
						}
						vlist.add(values);
					}
					postGoods.setValues(vlist);
				}

				// labels
				List<String> llist = new ArrayList<String>();
				JSONArray labelArray;
				try {
					labelArray = jsonObject.getJSONArray("labels");
				} catch (Exception e) {
					labelArray = null;
				}
				if (labelArray == null) {
					llist = null;
				} else {
					for (int k = 0; k < labelArray.length(); k++) {
						String labels = "";
						try {
							labels = labelArray.get(k).toString();
						} catch (Exception e) {
							labels = "";
						}
						llist.add(labels);
					}
					postGoods.setLabels(llist);
				}

				postList.put(postGoods.getDisplayName(), postGoods);
				// filters.setFilterssList(list);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return postList;

	}
}

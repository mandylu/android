package com.quanleimu.jsonutil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.quanleimu.database.ChatMessageDatabase;
import com.quanleimu.entity.AllCates;
import com.quanleimu.entity.ChatMessage;
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
	
	public static List<ChatMessage> parseChatMessages(JSONArray msgs)
	{
		final List<ChatMessage> list = new ArrayList<ChatMessage>();
		final int count = msgs.length();
		
		for (int i=0; i<count; i++)
		{
			try
			{
				ChatMessage tmp = ChatMessage.fromJson(msgs.getJSONObject(i));
				list.add(tmp);
			} 
			catch(Throwable t)
			{
				//Ignor.
			}
		}
		
		return list;
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
	
	private static AllCates getAllCatesFromJsonByJackson(String jsonData){
		if(allCates != null) return allCates;
		allCates = new AllCates();
		try {
			JsonFactory factory = new JsonFactory();
			JsonParser parser = factory.createJsonParser(jsonData);
			while (parser.nextToken() != JsonToken.END_OBJECT){
				String currentName = parser.getCurrentName();
				if(currentName == null){
					continue;
				}
				if(currentName.equals("name")){
					parser.nextToken();
					allCates.setName(parser.getText());
				}else if(currentName.equals("englishName")){
					parser.nextToken();
					allCates.setEnglishName(parser.getText());
				}else if(currentName.equals("children")){
					JsonToken jt = parser.nextToken();///start_array
					List<FirstStepCate> firsts = new ArrayList<FirstStepCate>();
					while(jt != JsonToken.END_ARRAY){
						if(jt != JsonToken.START_OBJECT){
							jt = parser.nextToken();
							continue;
						}
						jt = parser.nextToken();///start_object
						FirstStepCate firstStepCate = new FirstStepCate();
						while(jt != JsonToken.END_OBJECT){
							
							String flName = parser.getCurrentName();
							if(flName.equals("name")){
								jt = parser.nextToken();
								firstStepCate.setName(parser.getText());
							}else if(flName.equals("englishName")){
								jt = parser.nextToken();
								firstStepCate.setEnglishName(parser.getText());
							}else if(flName.equals("parentEnglishName")){
								jt = parser.nextToken();
								firstStepCate.setParentEnglishName(parser.getText());
							}else if(flName.equals("children")){
								jt = parser.nextToken();//start_array
								List<SecondStepCate> listSecond = new ArrayList<SecondStepCate>();
									
								while(jt != JsonToken.END_ARRAY){
									if(jt != JsonToken.START_OBJECT){
										jt = parser.nextToken();
										continue;
									}
									SecondStepCate secStepCate = new SecondStepCate();
									jt = parser.nextToken();//start_object
									while(jt != JsonToken.END_OBJECT){
										String secName = parser.getCurrentName();
										jt = parser.nextToken();
										String text = parser.getText();
										if(secName.equals("name")){
											secStepCate.setName(text);
										}else if(secName.equals("englishName")){
											secStepCate.setEnglishName(text);
										}else if(secName.equals("parentEnglishName")){
											secStepCate.setParentEnglishName(text);
										}
										jt = parser.nextToken();
									}
									jt = parser.nextToken();
									listSecond.add(secStepCate);
								}
								firstStepCate.setChildren(listSecond);
							}
							jt = parser.nextToken();
						}
						firsts.add(firstStepCate);
					}
					allCates.setChildren(firsts);
				}
			}
	
		}catch(JsonParseException e){
			allCates = null;
			e.printStackTrace();
		}catch(IOException e){
			// TODO Auto-generated catch block
			allCates = null;
			e.printStackTrace();
		}
		return allCates; 
	}
	
	// 获取所有类目列表
	public static AllCates getAllCatesFromJson(String jsonData) {
//		long t1 = System.currentTimeMillis();
		return getAllCatesFromJsonByJackson(jsonData);
//		static final AllCates allCates = null;
//		if(allCates != null) return allCates;
//		try {
//			allCates = new AllCates();
//			JSONObject jsonObj = new JSONObject(jsonData);
//			allCates.setName(jsonObj.getString("name"));
//			allCates.setEnglishName(jsonObj.getString("englishName"));
//
//			JSONArray jsonA = new JSONArray(jsonObj.getString("children"));
//			List<FirstStepCate> listFirst = new ArrayList<FirstStepCate>();
//			for (int i = 0; i < jsonA.length(); i++) {
//				FirstStepCate firstStepCate = new FirstStepCate();
//				JSONObject jsonFirstStepCate = jsonA.getJSONObject(i);
//				firstStepCate.setName(jsonFirstStepCate.getString("name"));
//				firstStepCate.setEnglishName(jsonFirstStepCate
//						.getString("englishName"));
//				firstStepCate.setParentEnglishName(jsonFirstStepCate
//						.getString("parentEnglishName"));
//
//				JSONArray jsonB = new JSONArray(
//						jsonFirstStepCate.getString("children"));
//				List<SecondStepCate> listSecond = new ArrayList<SecondStepCate>();
//				for (int j = 0; j < jsonB.length(); j++) {
//					SecondStepCate secondStepCate = new SecondStepCate();
//					JSONObject jsonSecondStepCate = jsonB.getJSONObject(j);
//
//					secondStepCate
//							.setName(jsonSecondStepCate.getString("name"));
//					secondStepCate.setEnglishName(jsonSecondStepCate
//							.getString("englishName"));
//					secondStepCate.setParentEnglishName(jsonSecondStepCate
//							.getString("parentEnglishName"));
//
//					listSecond.add(secondStepCate);
//				}
//				firstStepCate.setChildren(listSecond);
//
//				listFirst.add(firstStepCate);
//			}
//			allCates.setChildren(listFirst);
//		} catch (JSONException e1) {
//			// TODO Auto-generated catch block
//			allCates = null;
//			e1.printStackTrace();
//		}
//		return allCates;
	}
	
	
	public static GoodsList getGoodsListFromJsonByJackson(String jsonData){
		JsonFactory factory = new JsonFactory();
		GoodsList goodsList = new GoodsList();
		List<GoodsDetail> list = new ArrayList<GoodsDetail>();
		try{
			JsonParser parser = factory.createJsonParser(jsonData);
			while (parser.nextToken() != JsonToken.END_OBJECT) {
				String fieldname = parser.getCurrentName();
				if(fieldname == null) continue;
				if(fieldname.equals("data")){
					JsonToken jt = parser.nextToken();///start_array
					jt = parser.nextToken();
					while(jt != JsonToken.END_ARRAY){
						jt = parser.nextToken();///start_object
						
						GoodsDetail detail = new GoodsDetail();
						while(jt != JsonToken.END_OBJECT){
							String fname = parser.getCurrentName();
							if(fname == null){
								parser.nextToken();
								continue;
							}
							if(fname.equals("images")){
								jt = parser.nextToken();//{
								if(jt == JsonToken.START_OBJECT){
									jt = parser.nextToken();	
									ImageList il = new ImageList();
									while(jt != JsonToken.END_OBJECT){
										String imgType = parser.getCurrentName();
										if(jt != JsonToken.START_ARRAY){
											jt = parser.nextToken();
											continue;
										}
										parser.nextToken();
										
										String imgStr = "";
										while(jt != JsonToken.END_ARRAY){
											String text = parser.getText();
											imgStr += text + ",";
											jt = parser.nextToken();
										}
										if(imgStr.length() > 0){
											if(imgType.equals("big")){
												il.setBig(imgStr.substring(0, imgStr.length() - 1));
											}else if(imgType.equals("resize180")){
												il.setResize180(imgStr.substring(0, imgStr.length() - 1));
											}
										}
										jt = parser.nextToken();
									}
									detail.setImageList(il);
									jt = parser.nextToken();////end of image object
								}
							}else if(fname.equals("metaData")){
								jt = parser.nextToken();//START_OBJECT
								jt = parser.nextToken();//START_ARRAY
								ArrayList<String> metas = new ArrayList<String>();
								while(jt != JsonToken.END_ARRAY){
									String text = parser.getText();
									metas.add(text);
									jt = parser.nextToken();
								}		
								detail.setMetaData(metas);
								jt = parser.nextToken();
							}else if(fname.endsWith("_s")){
								jt = parser.nextToken();//START_ARRAY

								String value = "";
								while(jt != JsonToken.END_ARRAY){
									if(jt != JsonToken.START_OBJECT){
										jt = parser.nextToken();
										continue;
									}
									jt = parser.nextToken();///start_object							
									while(jt != JsonToken.END_OBJECT){
										String text = parser.getCurrentName();
										jt = parser.nextToken();///value
										String txtVal = parser.getText();
										if(txtVal != null && txtVal.length() > 0){
											value += txtVal + ",";
										}
										jt = parser.nextToken();
									}
									jt = parser.nextToken();
								}
								if(value.length() > 0){
									value = value.substring(0, value.length() - 1);
									detail.setValueByKey(fname, value);
								}
								jt = parser.nextToken();
							}else{
								jt = parser.nextToken();
								if(jt == JsonToken.START_ARRAY){
									while(jt != JsonToken.END_ARRAY){
										jt = parser.nextToken();
									}
									jt = parser.nextToken();
								}else if(jt == JsonToken.START_OBJECT){
									while(jt != JsonToken.END_OBJECT){
										jt = parser.nextToken();
									}
									jt = parser.nextToken();
								}else{
									String text = parser.getText();
									detail.setValueByKey(fname, text);
									jt = parser.nextToken();
								}
							}
						}
						jt = parser.nextToken();
						list.add(detail);
					}
				}
			}
			parser.close();
		}catch(JsonParseException e){
			
		}catch(IOException e){
			
		}
		goodsList.setData(list);
		return goodsList;
	}

	// 获取附近的Goods信息
	public static GoodsList getGoodsListFromJson(String jsonData) {
		long t1 = System.currentTimeMillis();
		return getGoodsListFromJsonByJackson(jsonData);
//		long t2 = System.currentTimeMillis();
//		Log.d("times: ", "hahahaha:  " + (t2 - t1));
//		GoodsList goodsList = new GoodsList();
//		if(jsonData == null) return goodsList;
//		JSONObject jsonObj;
//		try {
//			jsonObj = new JSONObject(jsonData);
//			goodsList.setCount(jsonObj.getInt("count"));
//			List<GoodsDetail> list = new ArrayList<GoodsDetail>();
//			JSONArray jsonArray = new JSONArray();
//			try {
//				jsonArray = jsonObj.getJSONArray("data");
//				for (int i = 0; i < jsonArray.length(); i++) {
//					
//					JSONObject jsonGoods = jsonArray.optJSONObject(i);
//					GoodsDetail goodsDetail = new GoodsDetail();
//
//					JSONArray names = jsonGoods.names();
//					for(int j = 0; j < names.length(); ++ j){
//						Object subObj = jsonGoods.get(names.getString(j));
//						if(subObj != null){
//							String value = null;
//							if(subObj.getClass().equals(String.class) && ((String)subObj).length() > 0){
//								value = (String)subObj;								
//							}
//							else if(subObj.getClass().equals(Integer.class)){
//								value = ((Integer)subObj).toString();
//							}else if(subObj.getClass().equals(Double.class)){
//								value = ((Double)subObj).toString();
//							}else if(subObj.getClass().equals(Float.class)){
//								value = ((Float)subObj).toString();
//							}else{
//								if(names.getString(j).endsWith("_s")){
//									if(subObj.getClass().equals(JSONArray.class)){
//										value = "";
//										JSONArray _sAry = (JSONArray)subObj;
//										if(_sAry != null){
//											for(int t = 0; t < _sAry.length(); ++ t){
//												String[] subStrings = _sAry.get(t).toString().split(":");
//												if(subStrings.length == 2){
//													int firstIndex = -1;
//													int lastIndex = subStrings[1].length() - 1;
//													for(int s = 0; s < subStrings[1].length(); ++ s){
//														if(firstIndex == -1){
//															if(subStrings[1].charAt(s) != '{'
//																	&& subStrings[1].charAt(s) != '}'
//																	&& subStrings[1].charAt(s) != '"'){
//																firstIndex = s;
//															}
//														}
//														else{
//															if(subStrings[1].charAt(s) == '{'
//																	|| subStrings[1].charAt(s) == '}'
//																	|| subStrings[1].charAt(s) == '"'){
//																lastIndex = s;
//																break;
//															}
//														}
//													}
//													if(t > 0){
//														value += ",";
//													}
//													value += subStrings[1].substring(firstIndex, lastIndex);
//												}
//											}
//										}
//									}
//								}
//								Log.println(0, "in JsonUtil ", "unknown jason value type!!!!!");
//							}
//								
//							goodsDetail.setValueByKey(names.getString(j), value);
//						}						
//					}
//
//					// 为ImageList赋值
//					JSONObject jsonImages = null;
//					ImageList imageList = new ImageList();
//					try {
//						jsonImages = jsonGoods.getJSONObject("images");
//					} catch (Exception e2) {
//						jsonImages = null;
//					}
//					if (jsonImages == null) {
//						imageList = null;
//					} else {
//						try {
//							JSONArray bigAry = jsonImages.getJSONArray("big");
//							if(bigAry != null){
//								String bigStr = "";
//								for(int s = 0; s < bigAry.length(); ++ s){
//									bigStr += "," + bigAry.getString(s);
//								}
//								if(!bigStr.equals("") && bigStr.charAt(0) == ','){
//									bigStr = bigStr.substring(1);
//								}
//								imageList.setBig(bigStr);
//							}
////							imageList.setBig(jsonImages.getJSONArray("big").toString());
////							imageList.setBig(jsonImages.get("big").toString());
//						} catch (Exception e1) {
//							imageList.setBig("");
//						}
//						try {
//							JSONArray resize180Ary = jsonImages.getJSONArray("resize180");
//							if(resize180Ary != null){
//								String r180Str = "";
//								for(int s = 0; s < resize180Ary.length(); ++ s){
//									r180Str += "," + resize180Ary.getString(s);
//								}
//								if(!r180Str.equals("") && r180Str.charAt(0) == ','){
//									r180Str = r180Str.substring(1);
//								}
//								imageList.setResize180(r180Str);
//							}
//							
////							imageList.setResize180(jsonImages.getString("resize180"));
//						} catch (Exception e1) {
//							imageList.setResize180("");
//						}
//					}
//
//					goodsDetail.setImageList(imageList);
//
//					ArrayList<String> metas = new ArrayList<String>();
//					JSONArray jsonMeta;
//					try {
//						jsonMeta = jsonGoods.getJSONArray("metaData");
//					} catch (Exception e1) {
//						jsonMeta = null;
//					}
//					if (jsonMeta == null || jsonMeta.length() == 0) {
//						metas = null;
//					} else {
//						for (int j = 0; j < jsonMeta.length(); j++) {
//							String meta = jsonMeta.get(j).toString();
//							//String a[] = meta.split(" ");
//							metas.add(meta);
//							//map.put(a[0], a[1]);
//						}
//					}
//					goodsDetail.setMetaData(metas);
//					list.add(goodsDetail);
//
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			goodsList.setData(list);
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		long t3 = System.currentTimeMillis();
//		Log.d("previous time: ", "hahahaha prev:  " + (t3 - t2));
//		return goodsList;
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
				try{
					String levels = jsonObject.getString("level");
					if(levels != null){
						mfil.setLevelCount(levels.split(",").length);
					}
				}catch(Exception e){
					mfil.setLevelCount(0);
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
				try{
					String levels = jsonObject.getString("level");
					if(levels != null){
						postGoods.setLevelCount(levels.split(",").length);
					}
				}catch(Exception e){
					postGoods.setLevelCount(0);
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

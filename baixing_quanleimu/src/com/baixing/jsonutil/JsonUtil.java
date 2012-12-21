package com.baixing.jsonutil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.util.Pair;

import com.baixing.database.ChatMessageDatabase;
import com.baixing.entity.AllCates;
import com.baixing.entity.ChatMessage;
import com.baixing.entity.CityDetail;
import com.baixing.entity.CityList;
import com.baixing.entity.Filters;
import com.baixing.entity.Filterss;
import com.baixing.entity.FirstStepCate;
import com.baixing.entity.GoodsDetail;
import com.baixing.entity.GoodsList;
import com.baixing.entity.HotData;
import com.baixing.entity.HotList;
import com.baixing.entity.ImageList;
import com.baixing.entity.PostGoodsBean;
import com.baixing.entity.SecondStepCate;
import com.baixing.entity.labels;
import com.baixing.entity.values;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
public class JsonUtil {
	
	public static CityList parseCityListFromJackson(String jsonData){
		if (jsonData == null || jsonData.length() == 0)
			return null;
		
		CityList cityList = new CityList();
		List<CityDetail> lists = new ArrayList<CityDetail>();
		JsonFactory factory = new JsonFactory();
		try{
			JsonParser parser = factory.createJsonParser(jsonData);
			while (parser.nextToken() != JsonToken.END_OBJECT) {
				String fieldname = parser.getCurrentName();
				if(fieldname == null) continue;
				parser.nextToken();
				CityDetail cityDetail = new CityDetail();
				cityDetail.setId(fieldname);				
				JsonToken jt = parser.nextToken();///start_object
				while(jt != JsonToken.END_OBJECT){
					String key = parser.getCurrentName();
					jt = parser.nextToken();
					String value = parser.getText();
					if(key.equals("englishName")){
						cityDetail.setEnglishName(value);
					}else if(key.equals("name")){
						cityDetail.setName(value);
					}else if(key.equals("sheng")){
						cityDetail.setSheng(value);
					}
					jt = parser.nextToken();					
				}
				lists.add(cityDetail);
			}
		}catch(JsonParseException e){
			
		}catch(IOException e){
			
		}
		catch (Throwable t)
		{
			Log.d("JSON", "unexpected json parse issue " + t);
		}
		cityList.setListDetails(lists);
		return cityList;
	}

	// 获取所有城市列表
	public static CityList parseCityListFromJson(String jsonData) {
		return parseCityListFromJackson(jsonData);
//		CityList cityList = new CityList();
//		try {
//			List<CityDetail> list = new ArrayList<CityDetail>();
//			// JSONArray jsonA = new JSONArray(jsonData);
//
//			JSONObject jsonObj = new JSONObject(jsonData);
//
//			JSONArray jsonArray = jsonObj.names();
//			for (int i = 0; i < jsonArray.length(); i++) {
//				CityDetail cityDetail = new CityDetail();
//				cityDetail.setId(jsonArray.getString(i));
//				JSONObject jsonCity = jsonObj.getJSONObject(jsonArray
//						.getString(i));
//				cityDetail.setEnglishName(jsonCity.getString("englishName"));
//				cityDetail.setName(jsonCity.getString("name"));
//				cityDetail.setSheng(jsonCity.getString("sheng"));
//				list.add(cityDetail);
//			}
//			cityList.setListDetails(list);
//		} catch (JSONException e1) {
//			e1.printStackTrace();
//		}
//		return cityList;
	}
	
	public static List<ChatMessage> parseChatMessagesByJackson(String msg){
		JsonFactory factory = new JsonFactory();
		List<ChatMessage> list = new ArrayList<ChatMessage>();
		try{
			JsonParser parser = factory.createJsonParser(msg);
			while (parser.nextToken() != JsonToken.END_OBJECT) {
				String fieldname = parser.getCurrentName();
				if(fieldname == null) continue;
				if(fieldname.equals("data")){
					JsonToken jt = parser.nextToken();///start_array
					jt = parser.nextToken();
					while(jt != JsonToken.END_ARRAY){
						jt = parser.nextToken();///start_object
						
						ChatMessage chat = new ChatMessage();
						while(jt != JsonToken.END_OBJECT){
							String fname = parser.getCurrentName();
							if(fname == null){
								parser.nextToken();
								continue;
							}
							
							jt = parser.nextToken();
							String text = parser.getText();
							if(fname.equals("u_id_from")){																
								chat.setFrom(text);
								jt = parser.nextToken();
							}else if(fname.equals("u_id_to")){
								chat.setTo(text);
								jt = parser.nextToken();
							}else if(fname.equals("message")){
								chat.setMessage(text);
								jt = parser.nextToken();
							}else if(fname.equals("timestamp")){
								chat.setTimestamp(Long.valueOf(text));
								jt = parser.nextToken();
							}else if(fname.equals("ad_id")){
								chat.setAdId(text);
								jt = parser.nextToken();
							}else if(fname.equals("session_id")){
								chat.setSession(text);
								jt = parser.nextToken();
							}else if(fname.equals("id")){
								chat.setId(text);
								jt = parser.nextToken();
							}							
						}
						jt = parser.nextToken();
						list.add(chat);
					}
				}
			}
		}catch(JsonParseException e){
			
		}catch(IOException e){
			
		}
		catch (Throwable t)
		{
			Log.d("JSON", "unexpected json parse issue " + t);
		}
		return list;				
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
											}else if(imgType.equals("square")){
												il.setSquare(imgStr.substring(0, imgStr.length() - 1));
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
		catch (Throwable t)
		{
			Log.d("JSON", "unexpected json parse issue " + t);
		}
		goodsList.setData(list);
		return goodsList;
	}

	// 获取附近的Goods信息
	public static GoodsList getGoodsListFromJson(String jsonData) {
		return getGoodsListFromJsonByJackson(jsonData);
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
				
				try {
					postGoods.setDefaultValue(jsonObject.getString("default"));
				} catch (Exception e1) {
					postGoods.setDefaultValue("");
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

				postList.put(postGoods.getName(), postGoods);
				// filters.setFilterssList(list);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return postList;

	}
	
	/*
	 * Sample Json:
	 * <pre>
	 * {count: 10, data: [	["chongwujiaoyi","狗狗",	180284]	] }
	 * </pre>
	 */
	public static List<Pair<SecondStepCate, Integer>> parseAdSearchCategoryCountResult(String jsonData)
	{
		List<Pair<SecondStepCate, Integer>> categoryCountList = new ArrayList<Pair<SecondStepCate, Integer>>(8);
		try {
			JSONObject json = new JSONObject(jsonData);
			JSONArray data = json.getJSONArray("data");
			for (int i = 0; i < data.length(); i++)
			{
				JSONArray categoryResult = data.getJSONArray(i);
				String englishName = categoryResult.getString(0);
				String name = categoryResult.getString(1);
				int count = categoryResult.getInt(2);
				
				SecondStepCate secondCate = new SecondStepCate();
				secondCate.setEnglishName(englishName);
				secondCate.setName(name);
				categoryCountList.add(new Pair<SecondStepCate, Integer>(secondCate, count));
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return categoryCountList;
		
	}
}

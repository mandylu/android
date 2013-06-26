package com.quanleimu.activity.test;

import java.util.ArrayList;
import java.util.HashMap;

import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baixing.activity.MainActivity;
import com.baixing.tracking.LogData;
import com.quanleimu.activity.R;

public class PostTest extends BaseTest<MainActivity> {
	public PostTest() {
		super(MainActivity.class);
	}
	
//	public void testCameraPV(){		
//		View v = solo.getText("免费发布");
//		solo.waitForView(v);
//		solo.clickOnView(v);
//
//		View tv = solo.getText("跳过拍照");
//		solo.waitForView(tv);
//		ArrayList<LogData> logs = TrackerLogSaver.getInstance().getLog("pageview", "/post/camera");
//		assertTrue(logs != null && logs.size() == 1);
//		assertTrue(logs.get(0).getMap().get("from").equals("others"));
//		assertTrue(logs.get(0).getMap().get("isEdit").equals("0"));
//		solo.goBack();
//	}
//	
//	public void testPostPV(){
//		View postV = solo.getText("免费发布");
//		solo.waitForView(postV);
//		solo.clickOnView(postV);
//		
//		
//		solo.waitForText("跳过拍照");
//		View tv = solo.getText("跳过拍照");
//		solo.waitForView(tv);
//		solo.clickOnView(tv);
//		
//		View v = solo.getView(R.id.postgoodslayout);
//		solo.waitForView(v);
//		
//		ArrayList<LogData> homePVlogs = TrackerLogSaver.getInstance().getLog("pageview", "/post");
//		assertTrue(homePVlogs != null && homePVlogs.size() == 1);
//		
//		solo.goBack();
//		solo.goBack();
//		solo.clickOnButton(0);
//	}
	
	private void gotoPostPage(){
		View postV = solo.getText("免费发布");
		solo.waitForView(postV);
		solo.clickOnView(postV);
		
		
		View tv = solo.getText("跳过拍照");
		solo.waitForView(tv);
		solo.clickOnView(solo.getView(R.id.cap));
		solo.waitForText("完成");
		
		solo.clickOnText("完成");
		solo.waitForText("分类");
		
		ArrayList<LogData> logs = TrackerLogSaver.getInstance().getLog("event", "Post_ImgUpload");
		assertTrue(logs != null && logs.size() == 1);
		HashMap<String, String> log = logs.get(0).getMap();
		assertTrue(!TextUtils.isEmpty(log.get("result")));
//		assertTrue(log.containsKey("failReason"));
		assertTrue(Integer.valueOf(log.get("size")) > 0);
		assertTrue(Float.valueOf(log.get("uploadTime")) > 0);
	}
	
//	public void testPostEvent(){
//		gotoPostPage();		
//		
//		solo.clickOnText("分类");
//		solo.clickInList(1);
//		solo.clickInList(2);
//		
//		LinearLayout parent = (LinearLayout)solo.getView(R.id.layout_txt);
//		for(int i = 0; i < parent.getChildCount(); ++ i){
//			View child = parent.getChildAt(i);
//			View inputArea = child.findViewById(R.id.postinput);
//			if(inputArea == null){
//				inputArea = child.findViewById(R.id.description_input);
//				if(inputArea == null){
//					View show = child.findViewById(R.id.postshow);
//					if(show != null){
//						String txtShow = ((TextView)show).getText().toString();
//						if(!txtShow.equals("分类")){
//							inputArea = child.findViewById(R.id.posthint);
//						}
//					}
//				}
//			}else{
//				View show = child.findViewById(R.id.postshow);
//				if(show != null){
//					String txtShow = ((TextView)show).getText().toString();
//					if(txtShow.equals("联系电话")){
//						continue;
//					}					
//				}
//			}
//			if(inputArea != null){
//				solo.clickOnView(inputArea);
//				if(inputArea instanceof EditText){
//					solo.enterText((EditText)inputArea, "123");
//				}else{
//					solo.clickInList(1);
//					if(solo.waitForText("返回上一级", 1, 1000)){
//						solo.clickInList(1);
//					}
//				}
//			}
//		}
//				
//		ArrayList<LogData> logs = TrackerLogSaver.getInstance().getLog("event", "Post_Inputing");
//		assertTrue(logs != null && logs.size() >= 1);
//		String lastAction = "";
//		for(LogData data : logs){
//			assertTrue(!TextUtils.isEmpty(data.getMap().get("action")));
//			assertTrue(!lastAction.equals(data.getMap().get("action")));
//		}
//		
//		solo.clickOnButton("立即免费发布");
//		logs = TrackerLogSaver.getInstance().getLog("event", "Post_PostBtnContentClicked");
//		assertTrue(logs != null && logs.size() == 1);
//		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("secondCateName")));
//		
//		assertTrue(solo.waitForText("发布成功"));
//		logs = TrackerLogSaver.getInstance().getLog("event", "Post_PostResult");
//		HashMap<String, String> map = logs.get(0).getMap();
//		assertTrue(!TextUtils.isEmpty(map.get("secondCateName")));
//		assertTrue(!TextUtils.isEmpty(map.get("postStatus")));
//		assertTrue(!TextUtils.isEmpty(map.get("postPicsCount")));
//		assertTrue(!TextUtils.isEmpty(map.get("postDescriptionLineCount")));
//		assertTrue(!TextUtils.isEmpty(map.get("postDescriptionTextCount")));
//		assertTrue(!TextUtils.isEmpty(map.get("postContactTextCount")));
//		assertTrue(!TextUtils.isEmpty(map.get("postDetailPositionAuto")));
//	}
	
	public void testPostCityQuota(){
		solo.clickOnText("百姓网");
		solo.clickOnText("广州");
		gotoPostPage();
		boolean cityQuota = solo.waitForText("百姓网是本地的");
		if(!cityQuota){
			solo.clickOnText("分类");
			solo.clickInList(1);
			solo.clickInList(2);
			assertTrue(solo.waitForText("百姓网是本地的"));
		}
		solo.clickOnButton(0);
		
		ArrayList<LogData> logs = TrackerLogSaver.getInstance().getLog("event", "Post_RuleAlert_Show");
		assertTrue(logs != null && logs.size() == 1);
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("ruleName")));
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("secondCateName")));
		
		logs = TrackerLogSaver.getInstance().getLog("event", "Post_RuleAlert_Action");
		assertTrue(logs != null && logs.size() == 1);
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("ruleName")));
		assertTrue(!TextUtils.isEmpty(logs.get(0).getMap().get("secondCateName")));
		assertTrue(logs.get(0).getMap().get("menuActionType").startsWith("发到"));

		solo.goBack();
		assertTrue(solo.waitForDialogToOpen(1000));
		solo.clickOnButton(0);
		solo.clickOnText("百姓网");
		solo.clickOnText("上海");
		
	}
}
//xumengyi@baixing.com
package com.baixing.view.fragment;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;
import com.baixing.adapter.GoodsListAdapter;
import com.baixing.entity.GoodsDetail;
import com.baixing.entity.GoodsDetail.EDATAKEYS;
import com.baixing.entity.GoodsList;
import com.baixing.entity.UserBean;
import com.baixing.imageCache.SimpleImageLoader;
import com.baixing.jsonutil.JsonUtil;
import com.baixing.message.BxMessageCenter;
import com.baixing.message.BxMessageCenter.IBxNotification;
import com.baixing.message.IBxNotificationNames;
import com.baixing.util.Communication;
import com.baixing.util.ErrorHandler;
import com.baixing.util.GoodsListLoader;
import com.baixing.util.LogData;
import com.baixing.util.TrackConfig.TrackMobile.BxEvent;
import com.baixing.util.TrackConfig.TrackMobile.Key;
import com.baixing.util.TrackConfig.TrackMobile.PV;
import com.baixing.util.TrackConfig.TrackMobile.Value;
import com.baixing.util.Tracker;
import com.baixing.util.Util;
import com.baixing.widget.PullToRefreshListView;
import com.quanleimu.activity.BaseActivity;
import com.quanleimu.activity.BaseFragment;
import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.activity.R;

public class PersonalPostFragment extends BaseFragment  implements PullToRefreshListView.OnRefreshListener, Observer{
	private final int MSG_MYPOST = 1;
//	private final int MSG_INVERIFY = 2;
//	private final int MSG_DELETED = 3;
	private final int MCMESSAGE_DELETE = 5;
	private final int MSG_DELETE_POST_SUCCESS = 6;
	private final int MSG_DELETE_POST_FAIL = 7;
//	private final int MSG_RESTORE_POST_SUCCESS = 8;
	private final int MSG_RESTORE_POST_FAIL = 9;
    private final int MSG_ITEM_OPERATE = 10;
    private final int MSG_SHOW_BIND_DIALOG = 11;

	private PullToRefreshListView lvGoodsList;
//	public ImageView ivMyads, ivMyfav, ivMyhistory;

	private List<GoodsDetail> listMyPost = null;
	private GoodsListAdapter adapter = null;
	private UserBean user;
	private boolean needReloadData = false;

    final static String TYPE_KEY = "PersonalPostFragment_type_key";
    final static int TYPE_MYPOST = 0;   //0:mypost, 2:inverify, 2:deleted
    private int currentType = TYPE_MYPOST;
	private GoodsListLoader glLoader = null;	
	private String json = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        final Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(PersonalPostFragment.TYPE_KEY)) {
            this.currentType = arguments.getInt(PersonalPostFragment.TYPE_KEY,
                    PersonalPostFragment.TYPE_MYPOST);
        }

		user = (UserBean) Util.loadDataFromLocate(this.getActivity(), "user", UserBean.class);
		listMyPost = QuanleimuApplication.getApplication().getListMyPost();
		filterOutAd(listMyPost, user);
		
		BxMessageCenter.defaultMessageCenter().registerObserver(this, IBxNotificationNames.NOTIFICATION_LOGIN);
		BxMessageCenter.defaultMessageCenter().registerObserver(this, IBxNotificationNames.NOTIFICATION_LOGOUT);
	}
	
	
	public void onDestroy() {
		super.onDestroy();
		BxMessageCenter.defaultMessageCenter().removeObserver(this);
	}

	private  void filterOutAd(List<GoodsDetail> list, UserBean user)
	{
		if (list != null && user != null)
		{
			int i=0;
			while (i<listMyPost.size())
			{
				GoodsDetail detail = list.get(i);
				if (!detail.getValueByKey("userId").equals(user.getId()))
				{
					list.remove(i);
				}
				else
				{
					i++;
				}
			}
		}
	}

	@Override
	public void onStackTop(boolean isBack) {
		if(!isBack || needReloadData){
			lvGoodsList.fireRefresh();
			needReloadData = false;
		}
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.personalcenterview, null);
        v.findViewById(R.id.linearType).setVisibility(View.GONE);  // 禁用掉 已发布、审核中、已删除 tabView，后续删除
		
		try {
			if (!Communication.isNetworkActive()) {
				QuanleimuApplication.getApplication().getErrorHandler().sendEmptyMessage(ErrorHandler.ERROR_NETWORK_UNAVAILABLE);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		lvGoodsList = (PullToRefreshListView) v.findViewById(R.id.lvGoodsList);
		adapter = new GoodsListAdapter(this.getActivity(), this.listMyPost, null);
        adapter.setHasDelBtn(true);
		adapter.setOperateMessage(handler, MSG_ITEM_OPERATE);
		lvGoodsList.setAdapter(adapter);

		GoodsList gl = new GoodsList();
		gl.setData(listMyPost == null ? new ArrayList<GoodsDetail>() : listMyPost);
	
		glLoader = new GoodsListLoader(null, handler, null, null);
		glLoader.setHasMore(false);
		glLoader.setGoodsList(gl);
		glLoader.setSearchUserList(true);
		
		lvGoodsList.setOnRefreshListener(this);	
		
		Bundle bundle = this.getArguments();
		if(bundle != null){
			if(bundle.containsKey(PostGoodsFragment.KEY_LAST_POST_CONTACT_USER)){
				if(bundle.getBoolean(PostGoodsFragment.KEY_LAST_POST_CONTACT_USER, false)){
					this.handler.sendEmptyMessageDelayed(MSG_SHOW_BIND_DIALOG, 1000);
				}
				bundle.remove(PostGoodsFragment.KEY_LAST_POST_CONTACT_USER);
			}
			if(bundle.containsKey("forceUpdate")){
				if(bundle.getBoolean("forceUpdate")){
					this.needReloadData = true;
				}
				bundle.remove("forceUpdate");
			}
		}
		
		return v;
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		if(adapter != null){
//			adapter.setHasDelBtn(false);
			adapter.notifyDataSetChanged();
			lvGoodsList.invalidateViews();
		}
		
		for(int i = 0; i < lvGoodsList.getChildCount(); ++i){
			ImageView imageView = (ImageView)lvGoodsList.getChildAt(i).findViewById(R.id.ivInfo);
			
			if(	null != imageView	
					&& null != imageView.getTag() && imageView.getTag().toString().length() > 0
					/*&& null != imageView.getDrawable()
					&& imageView.getDrawable() instanceof AnimationDrawable*/){
				SimpleImageLoader.Cancel(imageView.getTag().toString(), imageView);
			}
		}	
	}



	@Override
	public void onResume() {
		super.onResume();
		this.rebuildPage(getView(), false);
		
		for(int i = 0; i < lvGoodsList.getChildCount(); ++i){
			ImageView imageView = (ImageView)lvGoodsList.getChildAt(i).findViewById(R.id.ivInfo);
			
			if(	null != imageView	
					&& null != imageView.getTag() && imageView.getTag().toString().length() > 0
					/*&& null != imageView.getDrawable()
					&& imageView.getDrawable() instanceof AnimationDrawable*/){
				SimpleImageLoader.showImg(imageView, imageView.getTag().toString(), null, getActivity());
			}
		}
		
		glLoader.setHasMoreListener(null);
		glLoader.setHandler(handler);
		adapter.setList(glLoader.getGoodsList().getData());
		lvGoodsList.setSelectionFromHeader(glLoader.getSelection());
	}

	private void rebuildPage(View rootView, boolean onResult){		
		if(glLoader != null){
			glLoader.setHandler(handler);
		}
		rootView.findViewById(R.id.linearListView);

        BxEvent bxEvent = BxEvent.SENT_RESULT;
        int adsCountValue = 0;

		if(TYPE_MYPOST == currentType){
            bxEvent = BxEvent.SENT_RESULT;

			GoodsList gl = new GoodsList();
			gl.setData(listMyPost);
			glLoader.setGoodsList(gl);
			adapter.setList(listMyPost);
			adapter.notifyDataSetChanged();
			lvGoodsList.invalidateViews();
            if (listMyPost != null) {
                adsCountValue = listMyPost.size();
            }

		}

        Tracker.getInstance().event(bxEvent).append(Key.ADSCOUNT, adsCountValue).end();

		lvGoodsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {			
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				final int index = arg2 - lvGoodsList.getHeaderViewsCount();
				if(index < 0)
					return;
				
				if(TYPE_MYPOST == currentType && null != listMyPost && index < listMyPost.size() ){
					Bundle bundle = createArguments(null, null);
					bundle.putSerializable("loader", glLoader);
					bundle.putInt("index", index);
					pushFragment(new GoodDetailFragment(), bundle);
					
				}
			}
		});
		lvGoodsList.invalidateViews();
		lvGoodsList.setOnRefreshListener(this);	
		lvGoodsList.setSelectionFromHeader(glLoader.getSelection());
	}
	
	
	
	
	@Override
	protected void handleMessage(final Message msg, Activity activity, View rootView) {
		switch (msg.what) {
		case MSG_MYPOST:
//		case MSG_INVERIFY:
//		case MSG_DELETED:
			hideProgress();
			GoodsList gl = JsonUtil.getGoodsListFromJson(glLoader.getLastJson());
//			this.pv = (currentType==TYPE_MYPOST?PV.MYADS_SENT:(PV.MYADS_APPROVING)); //delete MYADS_APPROVING
			this.pv = PV.MYADS_SENT;
			//tracker
			if (gl == null || gl.getData() == null) {//no ads count
				Tracker.getInstance()
				.pv(this.pv)
				.end();
			} else {//ads count
				Tracker.getInstance()
				.pv(this.pv)
				.append(Key.ADSCOUNT, gl.getData().size() + "")
				.end();
			}
			
			if (gl == null || gl.getData().size() == 0) {
				if(msg.what == MSG_MYPOST) {

					if(null != listMyPost) listMyPost.clear();
				}
				glLoader.setGoodsList(new GoodsList());
			}
			else{
				if(msg.what == MSG_MYPOST){
					listMyPost = gl.getData();
					if(listMyPost != null){
						for(int i = listMyPost.size() - 1; i >= 0; -- i){
							if(!listMyPost.get(i).getValueByKey("status").equals("0") 
									&& !listMyPost.get(i).getValueByKey("status").equals("4")
									&& !listMyPost.get(i).getValueByKey("status").equals("20")){
								listMyPost.remove(i);
							}
						}
					}
					GoodsList gl2 = new GoodsList();
					gl2.setData(listMyPost);
					glLoader.setGoodsList(gl2);
				}
			}
			if(msg.what == MSG_MYPOST){
				QuanleimuApplication.getApplication().setListMyPost(listMyPost);
			}
			rebuildPage(rootView, true);
			lvGoodsList.onRefreshComplete();
			break;
		case GoodsListLoader.MSG_FIRST_FAIL:
		case GoodsListLoader.MSG_EXCEPTION:{
			hideProgress();
			lvGoodsList.onRefreshComplete();
			break;
		}

		case MCMESSAGE_DELETE:
			int pos = msg.arg2;
//			pos = pos - lvGoodsList.getHeaderViewsCount();
			String id = glLoader.getGoodsList().getData().get(pos).getValueByKey(EDATAKEYS.EDATAKEYS_ID);
			showSimpleProgress();
			new Thread(new MyMessageDeleteThread(id)).start();
			break;
		case MSG_DELETE_POST_FAIL:
			hideProgress();
			Toast.makeText(activity, "删除失败,请稍后重试！", 0).show();
			break;
		case MSG_DELETE_POST_SUCCESS:
			hideProgress();
			
			Object deletedId = msg.obj;
			try {
				JSONObject jb = new JSONObject(json);
				JSONObject js = jb.getJSONObject("error");
				String message = js.getString("message");
				int code = js.getInt("code");
				List<GoodsDetail> refList = null;
				if(msg.arg1 == TYPE_MYPOST){
					refList = listMyPost;
				}
//				else if(msg.arg1 == TYPE_INVERIFY){
//					refList = listInVerify;
//				}
//				else if(msg.arg1 == TYPE_DELETED){
//					refList = listDeleted;
//				}
				if(refList == null) break;
				if (code == 0) {
					for(int i = 0; i < refList.size(); ++ i){
						if(refList.get(i).getValueByKey(EDATAKEYS.EDATAKEYS_ID).equals((String)deletedId)){
							refList.remove(i);
							break;
						}
					}
					if(msg.arg1 == -1){
						QuanleimuApplication.getApplication().setListMyPost(listMyPost);
					}
					adapter.setList(refList);						
					adapter.notifyDataSetChanged();
					lvGoodsList.invalidateViews();
					Toast.makeText(activity, message, 0).show();
				} else {
					Toast.makeText(activity, "删除失败,请稍后重试！", 0).show();
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			adapter.setUiHold(false);
			break;	
		case MSG_RESTORE_POST_FAIL:
			hideProgress();
			Toast.makeText(activity, "恢复失败,请稍后重试！", 0).show();
			break;
		case ErrorHandler.ERROR_NETWORK_UNAVAILABLE:
			hideProgress();
			//tracker
			Tracker.getInstance()
//			.pv((currentType==MSG_MYPOST?PV.MYADS_SENT:(PV.MYADS_APPROVING)) ) //delete MYADS_APPROVING
			.pv(PV.MYADS_SENT)
			.end();
			
			Message msg2 = Message.obtain();
			msg2.what = ErrorHandler.ERROR_NETWORK_UNAVAILABLE;
			QuanleimuApplication.getApplication().getErrorHandler().sendMessage(msg2);
			lvGoodsList.onRefreshComplete();
			lvGoodsList.onFail();
			
			break;
        case MSG_ITEM_OPERATE:
            showItemOperateMenu(msg);
            break;
        case MSG_SHOW_BIND_DIALOG:
        	showBindDialog();
        	break;
		}
	}
	
	@Override
	public boolean handleBack(){
		Bundle bundle = createArguments(null, null);
//		bundle.putInt("defaultPageIndex", 1);
		((BaseActivity)this.getActivity()).pushFragment(new PersonalInfoFragment(), bundle, true);
		return true;
	}
	
	private String getPostCateEnglishName() {
		Bundle bundle = this.getArguments();
		if(bundle != null && bundle.containsKey(PostGoodsFragment.KEY_IS_EDITPOST)){
			return bundle.getString(PostGoodsFragment.KEY_CATE_ENGLISHNAME);
		}
		return "";
	}
	
	private void showBindDialog(){
		new AlertDialog.Builder(this.getActivity())
		.setMessage(R.string.personalpost_bind_baixing_account)
		.setPositiveButton("是", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Tracker.getInstance()
				.event(BxEvent.POST_POSTWITHLOGIN)
				.append(Key.SECONDCATENAME, getPostCateEnglishName())
				.end();
				
				dialog.dismiss();
				BaseActivity activity = (BaseActivity)getActivity();
				if(activity != null){
					activity.pushFragment(new LoginFragment(), PersonalPostFragment.createArguments(null, null), false);
				}				
			}
		})
		.setNegativeButton("否", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {	
				//tracker
				Tracker.getInstance()
				.event(BxEvent.POST_POSTWITHOUTLOGIN)
				.append(Key.SECONDCATENAME, getPostCateEnglishName())
				.end();
				dialog.dismiss();
			}
		}).show();
		
	}
	
	private boolean isValidMessage(GoodsDetail detail)
	{
		return !detail.getValueByKey("status").equals("4") && !detail.getValueByKey("status").equals("20");
	}
	
	private void postDelete(final LogData event, final String adId, final long postedSeconds)
	{
		new AlertDialog.Builder(getActivity()).setTitle("提醒")
		.setMessage("是否确定删除")
		.setPositiveButton("确定", new DialogInterface.OnClickListener() {							
			@Override
			public void onClick(DialogInterface dialog, int which) {
                showSimpleProgress();
                new Thread(new MyMessageDeleteThread(adId)).start();
                        event.end();
			}
		})
		.setNegativeButton(
	     "取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();							
			}
		})
	     .show();
	}

    private void showItemOperateMenu(final Message msg) {
        final int pos = msg.arg2;
        final GoodsDetail detail = glLoader.getGoodsList().getData().get(pos);        
        final String adId = detail.getValueByKey(EDATAKEYS.EDATAKEYS_ID);

        String tmpInsertedTime = detail.data.get("insertedTime");
        long tmpPostedSeconds = -1;
        if (tmpInsertedTime != null) {
            long nowTime = new Date().getTime() / 1000;
            tmpPostedSeconds = nowTime - Long.valueOf(tmpInsertedTime);
        }
        final long postedSeconds = tmpPostedSeconds;
        
        // 弹出 menu 确认操作
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("操作");

        int r_array_item_operate = R.array.item_operate_mypost;
        
        if (isValidMessage(detail))
        {
        	r_array_item_operate = R.array.item_operate_mypost;
        	Tracker.getInstance().event(BxEvent.SENT_MANAGE)
        	.append(Key.STATUS, Value.VALID)
            .append(Key.SECONDCATENAME, detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_CATEGORYENGLISHNAME))
            .append(Key.ADID, detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID))
            .append(Key.POSTEDSECONDS, postedSeconds)
        	.end();
        }
        else
        {
            r_array_item_operate = R.array.item_operate_inverify;
//            Tracker.getInstance().event(BxEvent.APPROVING_MANAGE).end();
            Tracker.getInstance().event(BxEvent.SENT_MANAGE)
        	.append(Key.STATUS, Value.APPROVING)
            .append(Key.SECONDCATENAME, detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_CATEGORYENGLISHNAME))
            .append(Key.ADID, detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID))
            .append(Key.POSTEDSECONDS, postedSeconds)
        	.end();
        }        

        builder.setItems(r_array_item_operate, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int clickedIndex) {
                if (isValidMessage(detail)) {
                    switch (clickedIndex) {
                        case 0://刷新
                            doRefresh(0, adId);
                            Tracker.getInstance().event(BxEvent.SENT_REFRESH)
                                    .append(Key.STATUS, Value.VALID)
                                    .append(Key.SECONDCATENAME, detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_CATEGORYENGLISHNAME))
                                    .append(Key.ADID, detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID))
                                    .append(Key.POSTEDSECONDS, postedSeconds)
                                    .end();
                            break;
                        case 1://修改
                            Bundle args = createArguments(null, null);
                            args.putSerializable("goodsDetail", detail);
                            args.putString("cateNames", detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_CATEGORYENGLISHNAME));
                            pushFragment(new PostGoodsFragment(), args);
                            Tracker.getInstance().event(BxEvent.SENT_EDIT)
                                    .append(Key.STATUS, Value.VALID)
                                    .append(Key.SECONDCATENAME, detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_CATEGORYENGLISHNAME))
                                    .append(Key.ADID, detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID))
                                    .append(Key.POSTEDSECONDS, postedSeconds)
                                    .end();
                            break;
                        case 2://删除
                        	postDelete(Tracker.getInstance().event(BxEvent.SENT_DELETE)
                        			.append(Key.STATUS, Value.VALID)
                        			.append(Key.SECONDCATENAME, detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_CATEGORYENGLISHNAME))
                                    .append(Key.ADID, detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID))
                                    .append(Key.POSTEDSECONDS, postedSeconds), adId, postedSeconds);
                            break;
                    }
                } 
                else {
                    switch (clickedIndex) {
                        case 0://申诉
                        	Tracker.getInstance().event(BxEvent.SENT_APPEAL)
                            .append(Key.STATUS, Value.APPROVING)
                            .append(Key.SECONDCATENAME, detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_CATEGORYENGLISHNAME))
                            .append(Key.ADID, detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID))
                            .append(Key.POSTEDSECONDS, postedSeconds)
                            .end();
                        	
                            Bundle bundle = createArguments("申诉", null);
                            bundle.putInt("type", 1);
                            bundle.putString("adId", adId);
                            pushFragment(new FeedbackFragment(), bundle);
                            break;
                        case 1://删除
                        	postDelete(Tracker.getInstance().event(BxEvent.SENT_DELETE)
                        			.append(Key.STATUS, Value.APPROVING)
                        			.append(Key.SECONDCATENAME, detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_CATEGORYENGLISHNAME))
                        			.append(Key.ADID, detail.getValueByKey(GoodsDetail.EDATAKEYS.EDATAKEYS_ID))
                                    .append(Key.POSTEDSECONDS, postedSeconds), adId, postedSeconds);
                            break;
                    }
                }
            }
        }).setNegativeButton(
                "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void doRefresh(int pay, final String adId){
        ArrayList<String> requests = new ArrayList<String>();

        UserBean user = (UserBean) Util.loadDataFromLocate(this.getActivity(), "user", UserBean.class);
        if(user != null && user.getPhone() != null && !user.getPhone().equals("")){
        	Util.makeupUserInfoParams(user, requests);
        }
        requests.add("adId=" + adId);
        requests.add("rt=1");
        if(pay != 0){
            requests.add("pay=1");
        }
        String url = Communication.getApiUrl("ad_refresh", requests);
        json = null;
        try {
            json = Communication.getDataByUrl(url, true);
        } catch (UnsupportedEncodingException e) {
            QuanleimuApplication.getApplication().getErrorHandler().sendEmptyMessage(ErrorHandler.ERROR_NETWORK_UNAVAILABLE);
        } catch (IOException e) {
            QuanleimuApplication.getApplication().getErrorHandler().sendEmptyMessage(ErrorHandler.ERROR_NETWORK_UNAVAILABLE);
        } catch (Communication.BXHttpException e){

        }finally {
        	hideProgress();
        }

        if(json == null){
            Toast.makeText(getActivity(), "刷新失败，请稍后重试！", 1).show();
            return;
        }
        try {
            JSONObject jb = new JSONObject(json);
            JSONObject js = jb.getJSONObject("error");
            String message = js.getString("message");
            int code = js.getInt("code");
            if (code == 0) {
                Toast.makeText(getActivity(), message, 1).show();
            }else if(2 == code){
                new AlertDialog.Builder(getActivity()).setTitle("提醒")
                        .setMessage(message)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                showSimpleProgress();
                                doRefresh(1, adId);
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(
                                "取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .show();

            }else {
                Toast.makeText(getActivity(), message, 1).show();
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

	private class MyMessageDeleteThread implements Runnable {
		private String id;
		private int currentType = TYPE_MYPOST;

		private MyMessageDeleteThread(String id){
			this.id = id;
			this.currentType = PersonalPostFragment.this.currentType;
		}

		@Override
		public void run() {
			json = "";
			String apiName = "ad_delete";
			ArrayList<String> list = new ArrayList<String>();
			if(user != null && user.getPhone() != null && !user.getPhone().equals("")){
				Util.makeupUserInfoParams(user, list);
			}
			list.add("adId=" + id);
			list.add("rt=1");

			String url = Communication.getApiUrl(apiName, list);
			try {
				json = Communication.getDataByUrl(url, true);
				if (json != null) {
					Message msg = Message.obtain();//handler.obtainMessage();
					msg.obj = id;
					msg.arg1 = currentType;
					msg.what = MSG_DELETE_POST_SUCCESS;
					handler.sendMessage(msg);
				} else {
					sendMessage(MSG_DELETE_POST_FAIL, null);
				} 
				return;

			} catch (UnsupportedEncodingException e) {
				QuanleimuApplication.getApplication().getErrorHandler().sendEmptyMessage(ErrorHandler.ERROR_NETWORK_UNAVAILABLE);
			} catch (IOException e) {
				QuanleimuApplication.getApplication().getErrorHandler().sendEmptyMessage(ErrorHandler.ERROR_NETWORK_UNAVAILABLE);
			} catch (Communication.BXHttpException e){
				
			}
			hideProgress();
		}
	}


	@Override
	public void initTitle(TitleDef title){
		title.m_leftActionHint = "返回";
		title.m_leftActionStyle = EBUTT_STYLE.EBUTT_STYLE_BACK;
//		title.m_rightActionHint = "编辑";
		if(currentType == TYPE_MYPOST){
			title.m_title = "我的信息";
		}
		title.m_visible = true;
	}
	
	@Override
	public void initTab(TabDef tab){
		tab.m_visible = true;
		tab.m_tabSelected = ETAB_TYPE.ETAB_TYPE_MINE;
	}

	@Override
	public void onRefresh() {
		List<String> params = new ArrayList<String>();
		if(user != null){
			params.add("userId=" + user.getId());
		}		
		if(currentType == TYPE_MYPOST){
			Bundle bundle = this.getArguments();
			if(bundle != null && bundle.getString("lastPost") != null){
				params.add("newAdIds=" + bundle.getString("lastPost"));
			}
			params.add("status=3");
		}
		glLoader.setRows(1000);
		glLoader.setParams(params);
		int msg = MSG_MYPOST;//(currentType == TYPE_MYPOST) ? MSG_MYPOST : (this.currentType == TYPE_INVERIFY ? MSG_INVERIFY : MSG_DELETED);
		glLoader.startFetching(true, msg, msg, msg,Communication.isNetworkActive() ? Communication.E_DATA_POLICY.E_DATA_POLICY_NETWORK_UNCACHEABLE : Communication.E_DATA_POLICY.E_DATA_POLICY_ONLY_LOCAL);
	}
	
	@Override
	public void onFragmentBackWithData(int message, Object obj){
		if(GoodDetailFragment.MSG_ADINVERIFY_DELETED == message){
			if(obj != null){
				if(this.listMyPost != null){
					boolean updateUi = false;
					for(int i = 0; i < listMyPost.size(); ++ i){
						if(listMyPost.get(i).getValueByKey(EDATAKEYS.EDATAKEYS_ID).equals((String)obj)){
							listMyPost.remove(i);
							updateUi = true;
							break;
						}
					}
					if(updateUi){
						adapter.setList(listMyPost);
						adapter.notifyDataSetChanged();
						lvGoodsList.invalidateViews();
					}
				}
			}
		}else if(GoodDetailFragment.MSG_MYPOST_DELETED == message){
			if(glLoader.getGoodsList() != null 
					&& glLoader.getGoodsList().getData() != null 
					&& glLoader.getGoodsList().getData().size() > 0){
				if(QuanleimuApplication.getApplication().getListMyPost() == null ||
						QuanleimuApplication.getApplication().getListMyPost().size() != glLoader.getGoodsList().getData().size()){
					GoodsList gl = new GoodsList();
					gl.setData(QuanleimuApplication.getApplication().getListMyPost());
					glLoader.setGoodsList(gl);
					adapter.setList(QuanleimuApplication.getApplication().getListMyPost());
					adapter.notifyDataSetChanged();
					lvGoodsList.invalidateViews();
				}
			}
		}else if(PostGoodsFragment.MSG_POST_SUCCEED == message){
				if(this.lvGoodsList != null){
					lvGoodsList.fireRefresh();
			}
		}

	}

	@Override
	public void update(Observable observable, Object data) {
		if (data instanceof IBxNotification)
		{
			IBxNotification note = (IBxNotification) data;
			if (IBxNotificationNames.NOTIFICATION_LOGIN.equals(note.getName())
					|| IBxNotificationNames.NOTIFICATION_LOGOUT.equals(note.getName())) {
				user = (UserBean) note.getObject();
				filterOutAd(listMyPost, user);
				needReloadData = true;
				if (adapter != null)
				{
					adapter.notifyDataSetChanged();
				}
			}
		}		
	}
}

package com.baixing.view.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;

import com.baixing.activity.BaseFragment;
import com.baixing.adapter.VadListAdapter;
import com.baixing.broadcast.BXNotificationService;
import com.baixing.data.GlobalDataManager;
import com.baixing.entity.Ad;
import com.baixing.entity.AdList;
import com.baixing.imageCache.ImageLoaderManager;
import com.baixing.jsonutil.JsonUtil;
import com.baixing.message.BxMessageCenter;
import com.baixing.message.BxMessageCenter.IBxNotification;
import com.baixing.message.IBxNotificationNames;
import com.baixing.tracking.Tracker;
import com.baixing.tracking.TrackConfig.TrackMobile.BxEvent;
import com.baixing.tracking.TrackConfig.TrackMobile.Key;
import com.baixing.tracking.TrackConfig.TrackMobile.PV;
import com.baixing.util.*;
import com.baixing.widget.PullToRefreshListView;
import com.baixing.widget.PullToRefreshListView.E_GETMORE;
import com.quanleimu.activity.R;
import com.baixing.android.api.ApiParams;

public class FavoriteAndHistoryFragment extends BaseFragment implements PullToRefreshListView.OnRefreshListener, PullToRefreshListView.OnGetmoreListener, VadFragment.IListHolder, VadListLoader.Callback, Observer {
//    private boolean isFav = false;
    static final int MSG_UPDATEFAV = 1;
//    static final int MSG_UPDATEHISTORY = 2;
    static final int MSG_DELETEAD = 3;
    static final int MSG_DELETEALL = 4;
    static final int MSG_GOTMOREFAV = 5;
//    static final int MSG_GOTMOREHISTORY = 6;
    static final int MSG_NOMOREFAV = 7;
//    static final int MSG_NOMOREHISTORY = 8;
    private final int MSG_ITEM_OPERATE = 9;

    private VadListAdapter adapter = null;
    private PullToRefreshListView pullListView = null;
    private int buttonStatus = -1;//-1:edit 0:finish
    private VadListLoader glLoader = null;
    private AdList tempGoodsList = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        if (getArguments() != null) {
//            isFav = getArguments().getBoolean("isFav", false);
//        }

        glLoader = new VadListLoader(null, this, null, tempGoodsList);
        
        BxMessageCenter.defaultMessageCenter().registerObserver(this, IBxNotificationNames.NOTIFICATION_FAV_ADDED);
        BxMessageCenter.defaultMessageCenter().registerObserver(this, IBxNotificationNames.NOTIFICATION_FAV_REMOVE);
    }
    
    private VadListLoader createGoodsListLoader()
    {
    	List<Ad> data = new ArrayList<Ad>();
    	if (tempGoodsList != null && tempGoodsList.getData() != null)
    	{
    		data.addAll(tempGoodsList.getData());
    	}
    	AdList list = new AdList(data);
    	
    	VadListLoader loader = new VadListLoader(null, null, null, list);
    	loader.setHasMore(false);
    	
    	return loader;
    }

    @Override
	public void onDestroy() {
		super.onDestroy();
		BxMessageCenter.defaultMessageCenter().removeObserver(this);
	}


	@Override
    public View onInitializeView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.personallistview, null);
        pullListView = (PullToRefreshListView) v.findViewById(R.id.plvlist);
        pullListView.setOnRefreshListener(this);
        pullListView.setOnGetMoreListener(this);
        pullListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
                position = position - pullListView.getHeaderViewsCount();
                if (position < 0 || tempGoodsList == null || tempGoodsList.getData() == null || position >= tempGoodsList.getData().size()) return;

                VadFragment f = new VadFragment();
                f.setListHolder(FavoriteAndHistoryFragment.this);
                Bundle bundle = createArguments(null, null);
                bundle.putSerializable("loader", createGoodsListLoader());
                bundle.putInt("index", position);

                buttonStatus = -1; //Reset button status when go to other screen.
                reCreateTitle();

                pushFragment(f, bundle);
            }

        });

        
        tempGoodsList = new AdList(GlobalDataManager.getInstance().getListMyStore());
		AdList list = (AdList)(tempGoodsList.clone()); 
		glLoader.setGoodsList(list);
		tempGoodsList = list;//dirty fix
        glLoader.setHasMore(false);

        adapter = new VadListAdapter(this.getActivity(), tempGoodsList.getData(), null);
        adapter.setHasDelBtn(true);
        adapter.setOperateMessage(handler, MSG_ITEM_OPERATE);
//		adapter.setList(tempGoodsList.getData());		
        pullListView.setAdapter(adapter);


        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
//        this.pv = isFav?PV.FAVADS:PV.HISTORYADS ;
        this.pv = PV.FAVADS;
        int adsCount = 0; //恶心的判断，能否有办法去除？
        if (glLoader != null && glLoader.getGoodsList() != null && glLoader.getGoodsList().getData() != null) {
            adsCount = glLoader.getGoodsList().getData().size();
        }
        Tracker.getInstance().pv(this.pv).append(Key.ADSCOUNT, adsCount).end();
//      Log.d("fav","isFav:"+isFav+",size:"+glLoader.getGoodsList().getData().size());
        for (int i = 0; i < pullListView.getChildCount(); ++i) {
            ImageView imageView = (ImageView) pullListView.getChildAt(i).findViewById(R.id.ivInfo);

            if (null != imageView
                    && null != imageView.getTag() && imageView.getTag().toString().length() > 0                    /*&& null != imageView.getDrawable()
                    && imageView.getDrawable() instanceof AnimationDrawable*/) {
            	ImageLoaderManager.getInstance().showImg(imageView, imageView.getTag().toString(), null, getActivity());
            }
        }

        glLoader.setHasMoreListener(null);
        glLoader.setCallback(this);
        adapter.setList(glLoader.getGoodsList().getData());
        pullListView.setSelectionFromHeader(glLoader.getSelection());
    }

    @Override
    public void onPause() {
        super.onPause();

        for (int i = 0; i < pullListView.getChildCount(); ++i) {
            ImageView imageView = (ImageView) pullListView.getChildAt(i).findViewById(R.id.ivInfo);

            if (null != imageView
                    && null != imageView.getTag() && imageView.getTag().toString().length() > 0
					/*&& null != imageView.getDrawable()
					&& imageView.getDrawable() instanceof AnimationDrawable*/) {
            	ImageLoaderManager.getInstance().Cancel(imageView.getTag().toString(), imageView);
            }
        }
    }

//	@Override
//	public void handleRightAction(){
//		if(-1 == buttonStatus){
////			if(this.m_viewInfoListener != null){
//				TitleDef title = getTitleDef();
//				title.m_rightActionHint = "完成";
//				title.m_leftActionHint = "清空";
//				title.m_leftActionStyle = EBUTT_STYLE.EBUTT_STYLE_NORMAL;
////				m_viewInfoListener.onTitleChanged(title);
//				this.refreshHeader();
////			}
//			if(adapter != null){
//				adapter.setHasDelBtn(true);
//			}
//			buttonStatus = 0;
//		}
//		else{
////			if(this.m_viewInfoListener != null){
//				TitleDef title = getTitleDef();
//				title.m_rightActionHint = "编辑";
//				title.m_leftActionHint = "返回";
//				this.refreshHeader();
////				m_viewInfoListener.onTitleChanged(title);
////			}
//			adapter.setHasDelBtn(false);
//			buttonStatus = -1;
//		}
//		adapter.notifyDataSetChanged();
//		pullListView.invalidateViews();
//	}

    @Override
    public boolean handleBack() {
        if (0 == buttonStatus) {
//			myHandler.sendEmptyMessage(MSG_DELETEALL);
            sendMessage(MSG_DELETEALL, null);
            return true;
        }

        return false;
    }

    @Override
    public void initTitle(TitleDef title) {
        title.m_visible = true;
        title.m_leftActionHint = "返回";
        title.m_title = "收藏的信息";
    }

    @Override
    protected void handleMessage(Message msg, Activity activity, View rootView) {
    	Log.d("fav","handleMessage");
        switch (msg.what) {
            case MSG_UPDATEFAV:
                hideProgress();
                String lastJson = glLoader.getLastJson();
                final boolean noResult = lastJson == null || lastJson.trim().length() == 0; 
                tempGoodsList = JsonUtil.getGoodsListFromJson(lastJson);
                Log.d("fav","updatefav.size:"+tempGoodsList.getData().size());
                if (null == tempGoodsList || (0 == tempGoodsList.getData().size() && noResult)) {
                    ErrorHandler.getInstance().handleError(ErrorHandler.ERROR_SERVICE_UNAVAILABLE, null);

                    pullListView.onFail();
                } else {
                	
                	/* //该问题已经到了不用中文说不清楚的地步！(至少我说不清楚)
                	 *  原则： 服务端返回的数据为准，但是顺序以本地为准。
                	 *  
                	 *  鉴于目前“收藏”这块的设计，下面的逻辑是处理服务器端有数据返回的情况；
                	 *  1. 本地数据顺序不变
                	 *  2. 如果服务端返回结果不包括本地已经存在的某条记录， 那么删除该记录
                	 *  3. 如果本地没有任何数据，以服务端返回为准
                	 */
                    List<Ad> filterResult = new ArrayList<Ad>();
                    List<Ad> oldList = new ArrayList<Ad>();
                    List<Ad> favList = GlobalDataManager.getInstance().getListMyStore();
                    List<Ad> newList = tempGoodsList.getData();
                    if (newList == null)
                    {
                    	newList = new ArrayList<Ad>();
                    }
                    if (favList != null) {
                    	oldList.addAll(favList);
                    }
                    
                    
                    if (oldList.size() == 0) //If we have no local favorites, use server return list.
                    {
                    	filterResult.addAll(newList);
                    }
                    else
                    {
                    	for (Ad d : oldList)
                    	{
                    		final int index1 = newList.indexOf(d);
                    		if (index1 != -1) //If server did not return this detail, means it's deleted by the owner of the ads.
                    		{
                    			Ad dd = newList.remove(index1);
                    			filterResult.add(dd);
                    		}
                    	}
                    	
                    	filterResult.addAll(newList);
                    }
                    
                    tempGoodsList.setData(filterResult);
                    favList = filterResult;//tempGoodsList.getData();
                    
                    

                	GlobalDataManager.getInstance().updateFav(favList);
                	Util.saveDataToLocate(GlobalDataManager.getInstance().getApplicationContext(), "listMyStore", favList);
                	
                    adapter.setList(tempGoodsList.getData());
                    glLoader.setGoodsList(tempGoodsList);
                    glLoader.setHasMore(tempGoodsList.getData().size() >= 30);
                }

                pullListView.onRefreshComplete();

                break;
            case MSG_DELETEAD:
            	Log.d("fav","deleteAd");
                int pos = (Integer) msg.obj;
                List<Ad> goodsList = GlobalDataManager.getInstance().getListMyStore();
                Ad detail = goodsList.remove(pos);
                if (goodsList != tempGoodsList.getData())
                    tempGoodsList.getData().remove(detail);
                //QuanleimuApplication.getApplication().setListMyStore(goodsList);
                GlobalDataManager.getInstance().removeFav(detail);
                Util.saveDataToLocate(GlobalDataManager.getInstance().getApplicationContext(), "listMyStore", goodsList);

                adapter.setList(tempGoodsList.getData());
                adapter.notifyDataSetChanged();
                pullListView.invalidateViews();
                adapter.setUiHold(false);
                break;
            case MSG_DELETEALL:
            	Log.d("fav","deleteAll");
                List<Ad> adList = new ArrayList<Ad>();
                GlobalDataManager.getInstance().clearMyStore();//setListMyStore(new ArrayList<GoodsDetail>(goodsList));
                Util.saveDataToLocate(GlobalDataManager.getInstance().getApplicationContext(), "listMyStore", new ArrayList<Ad>(adList));

                glLoader.getGoodsList().setData(adList);
                glLoader.setHasMore(false);
                adapter.setList(tempGoodsList.getData());
                adapter.notifyDataSetChanged();
                pullListView.invalidateViews();

                if (/*FavoriteAndHistoryView.this.m_viewInfoListener != null*/getActivity() != null) {
                    TitleDef title = getTitleDef();
                    title.m_rightActionHint = "编辑";
                    title.m_leftActionHint = "返回";
                    refreshHeader();
//				m_viewInfoListener.onTitleChanged(title);
                }
                adapter.setHasDelBtn(false);
                buttonStatus = -1;
                break;

            case MSG_GOTMOREFAV:
            case MSG_NOMOREFAV:
            case ErrorHandler.ERROR_NETWORK_UNAVAILABLE:
            	Log.d("fav","gotmorefav & gotmorhistory &...&network unavailable");
                hideProgress();
                onResult(msg.what, glLoader);
                break;

            case MSG_ITEM_OPERATE:
                Tracker.getInstance().event(BxEvent.FAV_MANAGE).end();
                
                // 弹出 menu 确认删除
                final Integer position = new Integer(msg.arg2);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("操作")
                        .setItems(R.array.item_operate_favorite_history,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which == 0) {
                                            sendMessage(MSG_DELETEAD, position);
                                            Tracker.getInstance().event(BxEvent.FAV_DELETE).end();
                                        }
                                    }
                                })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();

                break;
        }

    }


    private static int ITEMS_PER_REQUEST = 30;

    public void updateAdsThread(boolean isFav, boolean isGetMore) {

        ApiParams list = new ApiParams();
        List<Ad> details = GlobalDataManager.getInstance().getListMyStore();

        int startIndex = 0;
        if (isGetMore) {//Notice: should ensure that tempGoodsList is shorter than whole list, Or unexpected results may occur
            startIndex = tempGoodsList.getData().size();
        }
        //list.add("start=0");//this param is controled by param0 of startFetching()
        if (details != null && details.size() > startIndex) {
            String ids = "id:" + details.get(startIndex).getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID);
            for (int i = startIndex + 1; i < details.size() && i < startIndex + ITEMS_PER_REQUEST; ++i) {
                ids += " OR " + "id:" + details.get(i).getValueByKey(Ad.EDATAKEYS.EDATAKEYS_ID);
            }
            list.addParam("query", "(" + ids + ")");
        }

        list.addParam("rt", "1");

        int msgGotFirst = MSG_UPDATEFAV;
        int msgGotMore = MSG_GOTMOREFAV;
        int msgNoMore = MSG_NOMOREFAV;

        glLoader.setParams(list);
        glLoader.setRows(ITEMS_PER_REQUEST);

        if (isGetMore)
            glLoader.startFetching(true, msgGotMore, msgGotMore, msgNoMore, Communication.E_DATA_POLICY.E_DATA_POLICY_NETWORK_UNCACHEABLE);//trick:: param0 is set to true to avoid setting of "start=n>0"
        else
            glLoader.startFetching(true, msgGotFirst, msgGotMore, msgNoMore, Communication.E_DATA_POLICY.E_DATA_POLICY_NETWORK_UNCACHEABLE);
    }

    @Override
    public void onRefresh() {
        if ((GlobalDataManager.getInstance().getListMyStore() != null
                && GlobalDataManager.getInstance().getListMyStore().size() > 0)
                ) {
            updateAdsThread(true, false);
        } else {
            this.pullListView.onRefreshComplete();
        }
    }

    @Override
    public void onGetMore() {
        if ((GlobalDataManager.getInstance().getListMyStore() != null
                && tempGoodsList != null
                && tempGoodsList.getData().size() < GlobalDataManager.getInstance().getListMyStore().size())) {
            updateAdsThread(true, true);
        } else {
            this.pullListView.onGetMoreCompleted(E_GETMORE.E_GETMORE_NO_MORE);
        }

    }

    @Override
    public void startFecthingMore() {
        updateAdsThread(true, true);
    }

    @Override
    public boolean onResult(int msg, VadListLoader loader) {
        if (msg == MSG_GOTMOREFAV) {
            AdList moreGoodsList = JsonUtil.getGoodsListFromJson(loader.getLastJson());
            if (null == moreGoodsList || 0 == moreGoodsList.getData().size()) {

                pullListView.onGetMoreCompleted(E_GETMORE.E_GETMORE_NO_MORE);
                glLoader.setHasMore(false);
                return false;
            } else {
                List<Ad> favList = GlobalDataManager.getInstance().getListMyStore();
                if (tempGoodsList.getData().size() < favList.size()) {
                    List<Ad> tmp = new ArrayList<Ad>();

                    for (int i = moreGoodsList.getData().size() + tempGoodsList.getData().size() - 1; i >= tempGoodsList.getData().size(); --i) {
                        boolean exist = false;
                        for (int j = 0; j < moreGoodsList.getData().size(); ++j) {
                            if (favList.get(i).equals(moreGoodsList.getData().get(j))) {
                                tmp.add(0, moreGoodsList.getData().get(j));
                                favList.set(i, moreGoodsList.getData().get(j));
                                exist = true;
                                break;
                            }
                        }
                        if (!exist) {
                            favList.remove(i);
                        }
                    }
                    List<Ad> prev = tempGoodsList.getData();
                    prev.addAll(tmp);
                    tempGoodsList.setData(prev);
                }

            	GlobalDataManager.getInstance().updateFav(favList);
            	Util.saveDataToLocate(GlobalDataManager.getInstance().getApplicationContext(), "listMyStore", favList);

                adapter.setList(tempGoodsList.getData());
                adapter.notifyDataSetChanged();
                loader.setHasMore(tempGoodsList.getData().size() < favList.size());

                pullListView.onGetMoreCompleted(E_GETMORE.E_GETMORE_OK);
                return true;
            }
        } else if (msg == MSG_NOMOREFAV) {
            glLoader.setHasMore(false);
            pullListView.onGetMoreCompleted(E_GETMORE.E_GETMORE_NO_MORE);
            return false;
        } else if (msg == ErrorHandler.ERROR_NETWORK_UNAVAILABLE) {
            ErrorHandler.getInstance().handleError(ErrorHandler.ERROR_NETWORK_UNAVAILABLE, null);

            pullListView.onFail();
            return false;
        } else if (msg == MSG_UPDATEFAV) {
            pullListView.onRefreshComplete();
            return false;
        }

        return false;
    }


	@Override
	public void update(Observable observable, Object data) {
		if (data instanceof IBxNotification)
		{
			IBxNotification notification = (IBxNotification) data;
			Ad detail = (Ad) notification.getObject();
			boolean needUpdateAdapter = false;
			if (IBxNotificationNames.NOTIFICATION_FAV_ADDED.equals(notification.getName()) && detail != null)
			{
				if (tempGoodsList != null && tempGoodsList.getData() != null && !tempGoodsList.getData().contains(detail))
				{
					tempGoodsList.getData().add(0, detail);
					needUpdateAdapter = true;
				}
			}
			else if (IBxNotificationNames.NOTIFICATION_FAV_REMOVE.equals(notification.getName()))
			{
				if (tempGoodsList != null && tempGoodsList.getData() != null && tempGoodsList.getData().contains(detail))
				{
					tempGoodsList.getData().remove(detail);
					needUpdateAdapter = true;
				}
			}
			
			if (needUpdateAdapter && adapter != null)
			{
				adapter.setList(tempGoodsList.getData());
				adapter.notifyDataSetChanged();
				
				if (pullListView != null)
				{
					pullListView.onRefreshComplete();
				}
			}
		}
	}

	@Override
	public void onRequestComplete(int respCode, Object data) {
		this.sendMessage(respCode, data); //FIXME: should move code from "handleMessage()" here.
	}


}

package com.quanleimu.view.fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.quanleimu.activity.BaseFragment;
import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.activity.R;
import com.quanleimu.entity.ChatSession;
import com.quanleimu.entity.FirstStepCate;
import com.quanleimu.entity.UserBean;
import com.quanleimu.entity.UserProfile;
import com.quanleimu.util.TrackConfig;
import com.quanleimu.util.TrackConfig.TrackMobile.Key;
import com.quanleimu.util.TrackConfig.TrackMobile.PV;
import com.quanleimu.util.Tracker;
import com.quanleimu.util.Util;
import com.quanleimu.view.CustomizePagerManager.PageProvider;
import com.quanleimu.view.CustomizePagerManager.PageSelectListener;
import com.quanleimu.widget.CustomizeGridView;
import com.quanleimu.widget.CustomizeGridView.GridInfo;
import com.quanleimu.widget.CustomizeGridView.ItemClickListener;
import com.quanleimu.widget.EditUsernameDialogFragment;

public class HomeFragment extends BaseFragment implements PageProvider, PageSelectListener, ItemClickListener , View.OnClickListener{

	public static final String NAME = "HomeFragment";
	
	public static final int INDEX_POSTED = 0;
	public static final int INDEX_LIMITED = 1;
	public static final int INDEX_DELETED = 2;
	public static final int INDEX_FAVORITE = 3;
	public static final int INDEX_MESSAGE = 4;
	public static final int INDEX_HISTORY = 5;
	public static final int INDEX_SETTING = 6;	
	
	public int postNum = 0;
	public int limitedNum = 0;
	public int deletedNum = 0;
	public int favoriteNum = 0;
	public int unreadMessageNum = 0;
	public int historyNum = 0;
	private int selectedIndex = 0;

    private UserBean user;
    private UserProfile userProfile;
    private String userProfileJson;
    public static final int MSG_GETPERSONALPROFILE = 99;
    public static final int MSG_EDIT_USERNAME_SUCCESS = 100;
    public static final int MSG_SHOW_TOAST = 101;
    public static final int MSG_SHOW_PROGRESS = 102;
    private EditUsernameDialogFragment editUserDlg;

	protected void initTitle(TitleDef title) {
		LayoutInflater inflator = LayoutInflater.from(getActivity());
		title.m_titleControls = inflator.inflate(R.layout.title_home, null);

		title.hasGlobalSearch = true;
		
		View logoRoot = title.m_titleControls.findViewById(R.id.logo_root);
		
		logoRoot.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				pushFragment(new CityChangeFragment(), createArguments("切换城市", "首页"));
			}
		});
	}
	
	public void onAddTitleControl(View titleControl)
	{
		View logoRoot = titleControl.findViewById(R.id.logo_root);
		if (logoRoot != null)
		{
			logoRoot.setPadding(logoRoot.getPaddingLeft(), 0, logoRoot.getPaddingRight(), 0); //Fix padding issue for nine-patch.
		}
	}

	public void initTab(TabDef tab){
		tab.m_visible = true;
		tab.m_tabSelected = ETAB_TYPE.ETAB_TYPE_MAINPAGE;
	}

	@Override
	public int[] includedOptionMenus() {
		return new int[]{OPTION_CHANGE_CITY};
	}
	
	@Override
	public void handleRightAction(){
		this.pushFragment(new GridCateFragment(), this.getArguments());
	}
	
	@Override
	public void handleSearch() {
		this.pushFragment(new SearchFragment(), this.getArguments());
	};
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = this.getArguments();
		if(bundle != null && bundle.containsKey("defaultPageIndex")){
			selectedIndex = bundle.getInt("defaultPageIndex");
		}
	}
	
	public boolean hasGlobalTab()
	{
		return true;
	}
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		logCreateView(savedInstanceState);

		View v = inflater.inflate(R.layout.homepageview, null);

//		pageMgr.attachView(v, this, this);
		
		int []icons 	= {R.drawable.icon_category_wupinjiaoyi, R.drawable.icon_category_car, 		R.drawable.icon_category_house, 	R.drawable.icon_category_quanzhi, 
						   R.drawable.icon_category_jianzhi,     R.drawable.icon_category_vita, 	R.drawable.icon_category_friend, 	R.drawable.icon_category_pet,
						   R.drawable.icon_category_service,     R.drawable.icon_category_education};
		String []texts 	= {"物品交易", "车辆买卖", "房屋租售", "全职招聘", 
						   "兼职招聘", "求职简历", "交友活动", "宠物", 
						   "生活服务", "教育培训"};
		
		List<GridInfo> gitems = new ArrayList<GridInfo>();
		for (int i = 0; i < icons.length; i++)
		{
			GridInfo gi = new GridInfo();
			gi.imgResourceId = icons[i];
			gi.text = texts[i];
			gitems.add(gi);
		}

		CustomizeGridView gv = (CustomizeGridView) v.findViewById(R.id.gridcategory);
		gv.setData(gitems, 3);
		gv.setItemClickListener(this);
		
		return v;
	}
	@Override
	public void onStackTop(boolean isBack) {
//		//Mobile Track Config入口
//		TrackConfig.getInstance().getConfig();//获取config
		
		String cityName = QuanleimuApplication.getApplication().getCityName();
		if (null == cityName || "".equals(cityName)) {
			this.pushFragment(new CityChangeFragment(), createArguments("切换城市", "首页"));
		}else
		{
			TextView titleLabel = (TextView) getTitleDef().m_titleControls.findViewById(R.id.title_label_city);
			titleLabel.setText(QuanleimuApplication.getApplication().getCityName());			
		}
	}
	@Override
	protected void handleMessage(Message msg, Activity activity, View rootView) {
		switch (msg.what) {
			case 1:
				break;
			case 2:
				hideProgress();
				break;
			case 4:
				hideProgress();
				Toast.makeText(QuanleimuApplication.getApplication().getApplicationContext(), "网络连接失败，请检查设置！", 3).show();
				break;
	        case MSG_GETPERSONALPROFILE:
	            if(userProfileJson != null){
	                userProfile = UserProfile.from(userProfileJson);
	                if (getActivity() != null)
	                {
	                    Util.saveDataToLocate(getActivity(), "userProfile", userProfile);
	                    if(userProfile != null){
	                        fillProfile(userProfile, getView());
	                    }
	                }
	            }
	            break;
	        case MSG_USER_LOGIN:
	        	getView().findViewById(R.id.userInfoLayout).setVisibility(View.VISIBLE);
	            break;
	        case MSG_EDIT_USERNAME_SUCCESS:
	            hideProgress();
	            editUserDlg.dismiss();
	            reloadUser(getView());
				break;
	        case MSG_SHOW_TOAST:
	            hideProgress();
	            Toast.makeText(QuanleimuApplication.getApplication().getApplicationContext(), msg.obj.toString(), 1).show();
	            break;
	        case MSG_SHOW_PROGRESS:
	            showProgress(R.string.dialog_title_info, R.string.dialog_message_updating, true);
	            break;
		}
	}
	
	@Override
	public void onDestroy(){
//		LocationService.getInstance().removeLocationListener(this);
		super.onDestroy();
	}
	
	@Override
	public void onResume(){
		super.onResume();
		this.onPageSelect(selectedIndex);
//		LogPage(selectedIndex);
//		if(!QuanleimuApplication.getApplication().getCurrentLocation(this)){
//			LocationService.getInstance().addLocationListener(getContext(), this);
//		}
	}
	
	@Override
	public void onPause(){
//		LocationService.getInstance().removeLocationListener(this);
		super.onPause();
//		if(glDetail != null){
//			glDetail.setAdapter(null);
//		}
	}


	@Override
	public void onPageSelect(int index) {
		selectedIndex = index;
		if (selectedIndex == 0) {
			this.pv = PV.HOME;
			Tracker.getInstance().pv(this.pv).end();
		}
//		else if (selectedIndex == 1){
//			this.pv = PV.MY;
//			Tracker.getInstance().pv(PV.MY).append(Key.ISLOGIN, Util.isUserLogin()).append(Key.USERID, user!=null ? user.getId() : null).end();
//		}
	}

	@Override
	public View onCreateView(Context context, int index) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View v;		
		if (index == 0)
		{
			v =  inflater.inflate(R.layout.gridcategory, null);
			int []icons 	= {R.drawable.icon_category_wupinjiaoyi, R.drawable.icon_category_car, 		R.drawable.icon_category_house, 	R.drawable.icon_category_quanzhi, 
							   R.drawable.icon_category_jianzhi,     R.drawable.icon_category_vita, 	R.drawable.icon_category_friend, 	R.drawable.icon_category_pet,
							   R.drawable.icon_category_service,     R.drawable.icon_category_education};
			String []texts 	= {"物品交易", "车辆买卖", "房屋租售", "全职招聘", 
							   "兼职招聘", "求职简历", "交友活动", "宠物", 
							   "生活服务", "教育培训"};
			
			List<GridInfo> gitems = new ArrayList<GridInfo>();
			for (int i = 0; i < icons.length; i++)
			{
				GridInfo gi = new GridInfo();
				gi.imgResourceId = icons[i];
				gi.text = texts[i];
				gitems.add(gi);
			}

//			GridAdapter adapter = new GridAdapter(this.getActivity());
//			adapter.setList(gitems, 3);
			CustomizeGridView gv = (CustomizeGridView) v.findViewById(R.id.gridcategory);
			gv.setData(gitems, 3);
			gv.setItemClickListener(this);
//			gv.setAdapter(adapter);
//			gv.setOnItemClickListener(this);
		}
		else
		{
			v = inflater.inflate(R.layout.persongridfactory, null);
			int []icons 	= {R.drawable.icon_my_posted, R.drawable.icon_my_limited, 		R.drawable.icon_my_deleted, 	R.drawable.icon_my_fav, 
							   R.drawable.icon_my_mail,     R.drawable.icon_my_history, 	R.drawable.icon_my_setting};
							   
			String []texts 	= {"已发布", "审核未通过", "已删除", "收藏", 
							   "私信", "最近浏览", "设置"};
			
			int []numbers = {postNum, limitedNum, deletedNum, favoriteNum, 
							unreadMessageNum, historyNum, 0};
			
			boolean []stars = {false, false, false, false,
								(unreadMessageNum > 0), false, false};
			
			List<GridInfo> gitems = new ArrayList<GridInfo>();
			for (int i = 0; i < icons.length; i++)
			{
				GridInfo gi = new GridInfo();
				gi.imgResourceId = icons[i];
				gi.text = texts[i];
//				gi.number = numbers[i]; //数字不用加
				gi.starred = stars[i];
				gitems.add(gi);
			}
		
//			GridAdapter adapter = new GridAdapter(this.getActivity());
//			adapter.setList(gitems, 3);
			CustomizeGridView gv = (CustomizeGridView) v.findViewById(R.id.gridcategory);
			gv.setData(gitems, 3);
			gv.setItemClickListener(this);
//			gv.setAdapter(adapter);
//			gv.setOnItemClickListener(this);

            reloadUser(v);

		}

		return v;
	}

    private void reloadUser(View v) {
        //set user profile info view
        user = Util.getCurrentUser();
        if (user != null && user.getPhone() != null && !user.getPhone().equals("")) {
            userProfile = (UserProfile) Util.loadDataFromLocate(getActivity(), "userProfile");
            if (userProfile != null) {
                fillProfile(userProfile, v);
            } else {
                new Thread(new GetPersonalProfileThread()).start();
            }
        }
    }

	@Override
	public void onItemClick(GridInfo info, int index) {	
		if (selectedIndex == 0) // 浏览信息页面
		{
			List<FirstStepCate> allCates = QuanleimuApplication.getApplication()
					.getListFirst();
			if (allCates == null || allCates.size() == 0)
				return;
			if (info == null)
				return;
			
			FirstStepCate cate = allCates.get(index);
			Bundle bundle = new Bundle();
			bundle.putInt(ARG_COMMON_REQ_CODE, this.requestCode);
			bundle.putSerializable("cates", cate);
			bundle.putBoolean("isPost", false);
			pushFragment(new SecondCateFragment(), bundle);
			
		}
		else // 我的百姓网页面
		{
			//TODO 登录判断，talk session 获取
			switch (index)
			{
			case INDEX_POSTED:
                {
                	pushPersonalPostFragment(PersonalPostFragment.TYPE_MYPOST);				
                }
				break;
			case INDEX_LIMITED:
				{
					pushPersonalPostFragment(PersonalPostFragment.TYPE_INVERIFY);
				}
				break;
			case INDEX_DELETED:
                {
                	pushPersonalPostFragment(PersonalPostFragment.TYPE_DELETED);
                }
                break;
			case INDEX_FAVORITE:
				{
					Bundle bundle = createArguments(null, null);
					bundle.putBoolean("isFav", true);
					pushFragment(new FavoriteAndHistoryFragment(), bundle);					
				}
				break;
			case INDEX_MESSAGE:
				{
					Bundle bundle = createArguments(null, null);
					ArrayList<ChatSession> tmpList = new ArrayList<ChatSession>();
//					tmpList.addAll(this.sessions); 需要获取 sessions 数据
					bundle.putSerializable("sessions", tmpList);
					pushFragment(new SessionListFragment(), bundle);
				}
				break;
			case INDEX_HISTORY:
				{
					Bundle bundle = createArguments(null, null);
					bundle.putBoolean("isFav", false);
					pushFragment(new FavoriteAndHistoryFragment(), bundle);
				}
				break;
			case INDEX_SETTING:
				{
					pushFragment(new SetMainFragment(), null);
				}
				break;
			}

		}
	}

    private void pushPersonalPostFragment(int type) {
//        if(user == null){
//            Bundle bundle = createArguments(null, "用户中心");
//            pushFragment(new LoginFragment(), bundle);
//        }else{
            Bundle bundle = createArguments(null, null);
            bundle.putInt(PersonalPostFragment.TYPE_KEY, type);
            pushFragment(new PersonalPostFragment(), bundle);
//        }
    }
    
    private void fillProfile(UserProfile up, View userInfoView){
        View activity = userInfoView;

        if(up.nickName != null){
            ((TextView)activity.findViewById(R.id.userInfoNickname)).setText(up.nickName);
        }else{
            ((TextView)activity.findViewById(R.id.userInfoNickname)).setText("");
        }
        boolean showBoy = true;
        // 新版本只保留 nickname
//        if(up.gender != null && !up.equals("")){
//            if(up.gender.equals("男")){
//                ((ImageView)activity.findViewById(R.id.personalGenderImage)).setImageResource(R.drawable.pic_wo_male);
////				((ImageView)this.findViewById(R.id.personalImage)).setImageResource(R.drawable.pic_my_avator_boy);
//            }else if(up.gender.equals("女")){
//                ((ImageView)activity.findViewById(R.id.personalGenderImage)).setImageResource(R.drawable.pic_wo_female);
//                showBoy = false;
////				((ImageView)this.findViewById(R.id.personalImage)).setImageResource(R.drawable.pic_my_avator_girl);
//            }
//        }else{
//            ((ImageView)activity.findViewById(R.id.personalImage)).setImageResource(R.drawable.pic_my_avator_boy);
//        }

//        if(up.location != null && !up.location.equals("")){
////            (new Thread(new GetLocationThread(up.location))).start();
//            //TODO fixme 开新线程拿什么定位？
//        }else{
////            ((TextView)activity.findViewById(R.id.userInfoLocation)).setText("");
//        }
//
        if(up.createTime != null && !up.equals("")){
            try{
                Date date = new Date(Long.parseLong(up.createTime) * 1000);
                SimpleDateFormat df = new SimpleDateFormat("yyyy年MM月", Locale.SIMPLIFIED_CHINESE);
                ((TextView)activity.findViewById(R.id.userInfoJoinDays)).setText(df.format(date) + "");
            }catch(Exception e){
                e.printStackTrace();
            }
        }
//        else{
//            ((TextView)activity.findViewById(R.id.personalRegisterTime)).setText("");
//        }
//        String image = null;
//        if(up.resize180Image != null && !up.resize180Image.equals("")){
//            image = up.resize180Image;
//        }
//        if(image != null && !image.equals("") && !image.equals("null")){
////            int height = activity.findViewById(R.id.userInfoAvatar).getMeasuredHeight();
////            int width = activity.findViewById(R.id.userInfoAvatar).getMeasuredWidth();
////            if(height <= 0 || width <= 0){
////                Drawable img = ((ImageView)activity.findViewById(R.id.userInfoAvatar)).getDrawable();
////                if(img != null){
////                    height = img.getIntrinsicHeight();
////                    width = img.getIntrinsicWidth();
////                }
////            }
////            if(height > 0 && width > 0){
////                ViewGroup.LayoutParams lp = activity.findViewById(R.id.userInfoAvatar).getLayoutParams();
////                lp.height = height;
////                lp.width = width;
////                activity.findViewById(R.id.userInfoAvatar).setLayoutParams(lp);
////            }
//
//            SimpleImageLoader.showImg((ImageView) activity.findViewById(R.id.userInfoAvatar),
//                    image, null, activity.getContext(), showBoy ? R.drawable.pic_my_avator_boy : R.drawable.pic_my_avator_girl);
//        }else{
//            ((ImageView)activity.findViewById(R.id.userInfoAvatar)).setImageResource(showBoy ? R.drawable.pic_my_avator_boy : R.drawable.pic_my_avator_girl);
//        }
        View userInfoLayout = activity.findViewById(R.id.userInfoLayout);
        userInfoLayout.setVisibility(View.VISIBLE);
        View editBtn = activity.findViewById(R.id.userInfo_editUsername_btn);
        editBtn.setOnClickListener(this);
    }

    class GetPersonalProfileThread implements Runnable {
        @Override
        public void run() {
            if (user == null)
            {
                return;
            }
            userProfileJson = Util.requestUserProfile(user.getId());
            sendMessage(MSG_GETPERSONALPROFILE, null);

            hideProgress();
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.userInfo_editUsername_btn:
                editUserDlg = new EditUsernameDialogFragment();
                editUserDlg.handler = this.handler;
                editUserDlg.show(getFragmentManager(), null);
                break;

            default:
                break;
        }
    }

    //fixme ming 小手机下载更新失败
    
	public int getEnterAnimation()
	{
		return 0;
	}
	
	public int getExitAnimation()
	{
		return 0;
	}

}



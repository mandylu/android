package com.quanleimu.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;

import com.baixing.broadcast.CommonIntentAction;
import com.baixing.broadcast.PushMessageService;
import com.baixing.database.ChatMessageDatabase;
import com.baixing.util.LocationService;
import com.baixing.util.Sender;
import com.baixing.util.ShortcutUtil;
import com.baixing.util.Tracker;
import com.baixing.view.AdViewHistory;
import com.baixing.view.CustomizeTabHost;
import com.baixing.view.CustomizeTabHost.TabIconRes;
import com.baixing.view.CustomizeTabHost.TabSelectListener;

/**
 * 
 * @author liuchong
 *
 */
public class BaseTabActivity extends BaseActivity implements TabSelectListener {
	public static final String LIFE_TAG = "mainActivity";
	public static final int TAB_INDEX_CAT = 0;
	public static final int TAB_INDEX_POST = 1;
	public static final int TAB_INDEX_PERSONAL = 2;
	
	private final static String SHARE_PREFS_NAME = "baixing_shortcut_app";
	
	protected CustomizeTabHost globalTabCtrl;
	
	protected static int currentMainIndex = TAB_INDEX_CAT;
	
	protected static int ACTIVE_INSTANCE_COUNT = 0;
	protected static boolean isExitingApp = false;
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(savedInstanceState);
		
		if (globalTabCtrl != null)
		{
			globalTabCtrl.setCurrentFocusIndex(getTabIndex()); //Always save current index to the fixed one.
			savedInstanceState.putSerializable("tabCtrl", globalTabCtrl);
		}
	}
	
	protected void onStart()
	{
		super.onStart();
		if (isExitingApp)
		{
			if (ACTIVE_INSTANCE_COUNT > 0)
			{
				ACTIVE_INSTANCE_COUNT--;
				this.finish();
			}
			
			if (ACTIVE_INSTANCE_COUNT == 0)
			{
				isExitingApp = false;
			}
		}
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.w(LIFE_TAG, "activity is created for class " + this.getClass().getName());
		super.onCreate(savedInstanceState);
		ACTIVE_INSTANCE_COUNT++;
		
		if (savedInstanceState != null)
		{
			globalTabCtrl = (CustomizeTabHost)savedInstanceState.get("tabCtrl");
		}
		
		if (globalTabCtrl == null) {
	        String tabBrowse = getString(R.string.tab_browse);
	        String tabUserCenter = getString(R.string.tab_user_center);
	        String tabPost = getString(R.string.tab_post);
			globalTabCtrl = CustomizeTabHost.createTabHost(getTabIndex(), new String[] {tabBrowse, tabPost, tabUserCenter}, 
					new TabIconRes[] {new TabIconRes(R.drawable.icon_footer_category_on, R.drawable.icon_footer_category),
					new TabIconRes(R.drawable.icon_footer_post_on, R.drawable.icon_footer_post),
					new TabIconRes(R.drawable.icon_footer_profile_on, R.drawable.icon_footer_profile)});
		}
		globalTabCtrl.setCurrentFocusIndex(getTabIndex()); //Always focus the right tab.
	}
	
	protected final void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);
		
		/**
		 * Let sub class to handle the new intent, if sub class do not handle it, let current top fragment handle it.
		 */
		if (!handleNewIntent(intent))
		{
			BaseFragment f = getCurrentFragment();
			if (f != null && intent.getBooleanExtra(CommonIntentAction.EXTRA_COMMON_IS_THIRD_PARTY, false))
			{
				f.onActivityResult(intent.getIntExtra(CommonIntentAction.EXTRA_COMMON_REQUST_CODE, -1), 
						intent.getIntExtra(CommonIntentAction.EXTRA_COMMON_RESULT_CODE, -1), 
						(Intent) intent.getExtras().get(CommonIntentAction.EXTRA_COMMON_DATA));
			}
		}
	}
	
	protected boolean handleNewIntent(Intent intent)
	{
		//TODO:
		return false;
	}
	
	protected void onDestroy() {
		if (!isExitingApp)
		{
			ACTIVE_INSTANCE_COUNT--;
		}
		Log.w(LIFE_TAG, "activity is destroy " + this.getClass().getName());
		super.onDestroy();
	}
	
	protected void onResume()
	{
		super.onResume();
		currentMainIndex = getTabIndex();
		globalTabCtrl.showTab(getTabIndex());
	}
	
	protected int getTabIndex()
	{
		return TAB_INDEX_CAT;
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
        	handleBack();
        }
        
        else{
        	return super.onKeyDown(keyCode, event);
        }
        
        return true;
    }
	
	protected void initTitleAction()
	{
		View left = findViewById(R.id.left_action);
		left.setOnClickListener(this);
		View right = findViewById(R.id.right_action);
		right.setOnClickListener(this);
		View search = findViewById(R.id.search_action);
		search.setOnClickListener(this);
	}
	
	public void handleBack(){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); 
        imm.hideSoftInputFromWindow(this.findViewById(R.id.contentLayout).getWindowToken(), 0); 

        BaseFragment currentFragmet = getCurrentFragment();
        
		try{
	    	if( currentFragmet != null && !currentFragmet.handleBack()){
    			if (getSupportFragmentManager().getBackStackEntryCount()>1)
    			{
    				popFragment(currentFragmet);
    			}
    			else
    			{
    				exitMainActivity();
    			}
	    	}	
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.right_action:
			getCurrentFragment().handleRightAction();

			break;
		case R.id.left_action:
			this.handleBack();
			break;
		case R.id.search_action: {
			if (getCurrentFragment() != null) {
				getCurrentFragment().handleSearch();
			}
			break;
		}
		}
		super.onClick(v);
	}

	/**
	 * 
	 */
	public void exitMainActivity() {
		SharedPreferences settings = getSharedPreferences(SHARE_PREFS_NAME, 0);
		String hasShowShortcutMessage = settings.getString("hasShowShortcut", "no");

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		LayoutInflater adbInflater = LayoutInflater.from(this);
		View shortcutLayout = adbInflater.inflate(R.layout.shortcutshow, null);

		final CheckBox shortcutCheckBox = (CheckBox) shortcutLayout.findViewById(R.id.shortcut);
		final boolean needShowShortcut = "no".equals(hasShowShortcutMessage) && !ShortcutUtil.hasShortcut(this);
		if (needShowShortcut)
		{
		    builder.setView(shortcutLayout);
		}

		builder.setTitle(R.string.dialog_title_info)
			.setMessage(R.string.dialog_message_confirm_exit)
			.setNegativeButton(R.string.no, null)
			.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
		{

		    @Override
		    public void onClick(DialogInterface dialog, int which)
		    {
		    	
		    	LocationService.getInstance().stop();
		        if (needShowShortcut && shortcutCheckBox.isChecked())
		        {
		            ShortcutUtil.addShortcut(BaseTabActivity.this);
		        }
		        SharedPreferences settings = getSharedPreferences(SHARE_PREFS_NAME, 0);
		        SharedPreferences.Editor editor = settings.edit();
		        editor.putString("hasShowShortcut", "yes");
		        // Commit the edits!
		        editor.commit();
				Intent pushIntent = new Intent(BaseTabActivity.this, com.baixing.broadcast.BXNotificationService.class);
				BaseTabActivity.this.startService(pushIntent);

				Intent startPush = new Intent(PushMessageService.ACTION_CONNECT);
				startPush.putExtra("updateToken", true);
				BaseTabActivity.this.startService(startPush);
				
				QuanleimuApplication.deleteOldRecorders(3600 * 24 * 3);
//		            		Debug.stopMethodTracing();
				QuanleimuApplication.mDemoApp = null;
//				isInActiveStack = false;
				
				ChatMessageDatabase.prepareDB(BaseTabActivity.this);
				ChatMessageDatabase.clearOldMessage(1000);
				
				// BxSender & BxTracker save data into file.
		    	try {
					Sender.getInstance().save();
					Tracker.getInstance().save();
				} catch (Exception e) {}
		    	Log.d("quanleimu", "exit");
		    	dialog.dismiss();
		    	AdViewHistory.getInstance().clearHistory();
		    	
		    	isExitingApp = true;
		    	ACTIVE_INSTANCE_COUNT--;
				BaseTabActivity.this.finish();
		    }
		});
		builder.create().show();
	}
	

	@Override
	public void beforeChange(int currentIndex, int nextIndex) {
	}

	@Override
	public void afterChange(int newSelectIndex) {
		
		Intent intent = new Intent();
		switch(newSelectIndex)
		{
		case TAB_INDEX_CAT:
			intent.setClass(this, QuanleimuMainActivity.class);
			break;
		case TAB_INDEX_POST:
			intent.setClass(this, PostActivity.class);
			break;
		case TAB_INDEX_PERSONAL:
			intent.setClass(this, PersonalActivity.class);
			break;
		}
		
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//		intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		this.startActivity(intent);
//		BaseTabActivity.this.finish();
		
//		Intent gg = new Intent("switch");
//		gg.setClass(this, ManagerActivity.class);
//		gg.putExtra("intent", intent);
//		this.startActivity(gg);
	}

}

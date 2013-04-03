//liuchong@baixing.com
package com.baixing.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baixing.data.GlobalDataManager;
import com.baixing.entity.CityDetail;
import com.baixing.util.LocationService;
import com.baixing.view.fragment.FirstRunFragment;
import com.quanleimu.activity.R;
import com.umeng.analytics.MobclickAgent;
//import com.tencent.mm.sdk.platformtools.Log;
/**
 * change author cause logic changed a lot.
 * @author liuchong 
 */
public class BaseActivity extends FragmentActivity implements OnClickListener{

	public static final String TAG = "QLM";// "BaseActivity";
	
	public static final String PREF_FIRSTRUN  = "firstRunFlag";
	
	protected static interface BUNDLE_KEYS {
		public static final String KEY_FIRST_FRAGMENT_ID = "firstFragmentId";
	}
	
	//定义Intent和Bundle
	protected Intent intent = null;
	protected Bundle bundle = null;
	protected View v = null; 
	protected ProgressDialog pd;
	
	private int stackSize;
	
	public static final int INVALID_FIRSTFRAGMENT_ID = -1;
	private boolean savedInstance = false;
	protected int firstFragmentId = INVALID_FIRSTFRAGMENT_ID;
	
	@Override
	protected void onNewIntent(Intent intent) {
		savedInstance = false;
		this.setIntent(intent);
		super.onNewIntent(intent);
	}
	
	@Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
		Log.d("baseactivity", "save");
		LocationService.getInstance().stop();
		savedInstanceState.putString("cityEnglishName", GlobalDataManager.getInstance().getCityEnglishName());
		savedInstanceState.putString("cityName", GlobalDataManager.getInstance().getCityName());
		
		ArrayList<String>strDetails = new ArrayList<String>();
		for(int i = 0; i < GlobalDataManager.getInstance().getListCityDetails().size(); ++ i){
			CityDetail detail = GlobalDataManager.getInstance().getListCityDetails().get(i);
			String tstrDetail = "englishName=" + detail.getEnglishName()
					+ ",id=" + detail.getId()
					+ ",name=" + detail.getName()
					+ ",sheng=" + detail.getSheng(); 
			strDetails.add(tstrDetail);
		}
		
		savedInstanceState.putStringArrayList("cityDetails", strDetails);
		savedInstanceState.putInt(BUNDLE_KEYS.KEY_FIRST_FRAGMENT_ID, firstFragmentId);
		savedInstance = true;
		
		
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
    	savedInstance = false;
    	super.onRestoreInstanceState(savedInstanceState);
    	GlobalDataManager.getInstance().setCityEnglishName(savedInstanceState.getString("cityEnglishName"));
    	GlobalDataManager.getInstance().setCityName(savedInstanceState.getString("cityName"));
		
		ArrayList<String>listDetails = savedInstanceState.getStringArrayList("cityDetails");
		
		List<CityDetail> cityDetails = new ArrayList<CityDetail>();
		for(int i = 0; i < listDetails.size(); ++ i){
			String strDetail = listDetails.get(i);
			String[] strDetails = strDetail.split(",");
			CityDetail detail = new CityDetail();
			for(int j = 0; j < strDetails.length; ++ j){
				String[] subItems = strDetails[j].split("=");
				if(subItems[0].equals("englishName")){
					detail.setEnglishName(subItems[1]);
				}
				else if(subItems[0].equals("id")){
					detail.setId(subItems[1]);
				}
				else if(subItems[0].equals("name")){
					detail.setName(subItems[1]);
				}
				else if(subItems[0].equals("sheng")){
					detail.setSheng(subItems[1]);
				}				
			}
			cityDetails.add(detail);
		}
		GlobalDataManager.getInstance().setListCityDetails(cityDetails);
		firstFragmentId = savedInstanceState.getInt(BUNDLE_KEYS.KEY_FIRST_FRAGMENT_ID, INVALID_FIRSTFRAGMENT_ID);
    }
	
	protected TextView tvAddMore ;
	protected LinearLayout loadingLayout;
	
	protected ProgressBar progressBar;  
	
	//防止滑盖手机滑盖刷新
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	} 
	 
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		GlobalDataManager.getInstance().setLastActiveActivity(this.getClass());
		MobclickAgent.onResume(this);
		this.savedInstance = false;
	}



	@Override
	public void onAttachFragment(Fragment fragment) {
		super.onAttachFragment(fragment);
	}
	
	protected final void notifyStackTop()
	{
		if(savedInstance) return;
		BaseFragment f = getCurrentFragment();
		if (f != null)
		{
			int newStackSize = getSupportFragmentManager().getBackStackEntryCount();
			
			Log.e(TAG, "notify stack top " + f.getClass().getName() + "#" + f.hashCode());
			try
			{
				f.notifyOnStackTop(newStackSize < stackSize);
			} catch( Throwable t) {
				
			}
			finally
			{
				stackSize = newStackSize;
			}
			View cover =findViewById(R.id.splash_cover);
			if (cover != null)
			{
				cover.setVisibility(View.GONE);
			}
			
			onStatckTop(f);
		}
	}
	
	/**
	 * Sub class may need to do something when specified fragment is pushed to stack.
	 */
	protected void onStatckTop(BaseFragment f) {
		
	}
	
	public final void showFirstRun(BaseFragment f)
	{
		if(savedInstance) return;
		if (f.getFirstRunId() == -1)
		{
			return; //No need first run.
		}
		String key = f.getClass().getName() + GlobalDataManager.getInstance().getVersion();
		SharedPreferences share = this.getSharedPreferences(PREF_FIRSTRUN, MODE_PRIVATE);
		boolean shown = share.getBoolean(key, false);
		if (!shown)
		{
			Editor edit = share.edit();
			edit.putBoolean(key, true);
			edit.commit();
			
		
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
	        if (prev != null) {
	            ft.remove(prev);
	        }
	        ft.addToBackStack(null);

	        // Create and show the dialog.
	        DialogFragment newFragment = FirstRunFragment.create(key, f.getFirstRunId());
	        newFragment.show(ft, "dialog");
		}
	}
	
	public final void onHideFirstRun(String key)
	{
		SharedPreferences share = this.getSharedPreferences(PREF_FIRSTRUN, MODE_PRIVATE);
		Editor edit = share.edit();
		edit.putBoolean(key, true);
		edit.commit();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		getSupportFragmentManager().addOnBackStackChangedListener(new OnBackStackChangedListener() {
			@Override
			public void onBackStackChanged() {
				notifyStackTop();
			}});
		
		MobclickAgent.onError(this);
		//判断Intent和Bundle
		intent = getIntent();
		if(intent == null)
		{
			intent = new Intent();
		}
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		bundle = intent.getExtras();
		if(bundle == null)
		{
			bundle = new Bundle();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.e(TAG, "activity on activity result.");
		BaseFragment fragment = getCurrentFragment();
		if(fragment != null){
			fragment.onActivityResult(requestCode, resultCode, data);
		}
		super.onActivityResult(requestCode, resultCode, data);
	
	}


	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
		}
	}
	
	//判断网络是否连接成功
	public boolean checkConnection ()throws Exception
	{
		
		boolean a = false;
		ConnectivityManager connManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connManager == null)
		{
			return false;
		}
		
		NetworkInfo mobileInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		State mobile = mobileInfo == null ? null : mobileInfo.getState();
		State wifi = wifiInfo== null ? null : wifiInfo.getState();

		if((mobile != null && mobile.toString().equals("CONNECTED")) || (wifi != null && wifi.toString().equals("CONNECTED")))
		{
			a = true;
		}
		else 
		{
			a = false;
		}
		return a;
	}
	
	//弹出等待框
	public void pdShow(Context context)
	{
		pd = ProgressDialog.show(context, getString(R.string.dialog_title_info),
				getString(R.string.dialog_message_waiting));
		pd.setCancelable(true);
	}
	
	//取消等待框
	public void pdDismiss(Context context)
	{
		pd.dismiss();
	}
	
	public final void pushFragment(BaseFragment fragment, Bundle bundle, String popTo)
	{
		if(savedInstance) return;

		if (bundle != null)
		{
			fragment.setArguments(bundle);
		}
		FragmentManager fm = getSupportFragmentManager();
		final boolean isFirstFragment = fm.getBackStackEntryCount() == 1; 
		FragmentTransaction ft = fm.beginTransaction();
		ft.setCustomAnimations(fragment.getEnterAnimation(), /*R.anim.right_to_left_exit*/0, /*R.anim.left_to_right_enter*/0, fragment.getExitAnimation());
		if (!"".equals(popTo))
		{
			fm.popBackStack(popTo == null ? null : popTo , FragmentManager.POP_BACK_STACK_INCLUSIVE);
		}
		
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		
		ft.replace(R.id.contentLayout, fragment);
		ft.addToBackStack(fragment.getName());
		int id = ft.commit();
		if (isFirstFragment)
		{
			firstFragmentId = id;
//			getIntent().putExtra("firstIndicator", id);
			Log.e(TAG, "first fragment added to stack id is " + id);
		}
		else
		{
			Log.w(TAG, "fragment added to stack id is " + id);
		}
	}
	
	public final void pushFragment(BaseFragment f, Bundle bundle, boolean clearStack)
	{
		pushFragment(f, bundle, clearStack ? null : "");
		
	}
	
	
	public final boolean popFragment(BaseFragment f)
	{
		if(savedInstance) return false;
		FragmentManager fm = getSupportFragmentManager();
		final int entryCount = fm.getBackStackEntryCount();
		FragmentTransaction ft = fm.beginTransaction();
		//Pop current
		boolean popSucceed = fm.popBackStackImmediate();
		
		ft.commit();
		
		if (popSucceed && entryCount <= 1) {
			onFragmentEmpty();
		}
		return popSucceed;
	}
	
	/**
	 * Notify when all the fragment within the stack are pop out.
	 */
	protected void onFragmentEmpty() {
		finish();
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return super.onContextItemSelected(item);
	}

	public BaseFragment getCurrentFragment()
	{
		
		FragmentManager fm = this.getSupportFragmentManager();
//		
		return (BaseFragment) fm.findFragmentById(R.id.contentLayout);
	}
}

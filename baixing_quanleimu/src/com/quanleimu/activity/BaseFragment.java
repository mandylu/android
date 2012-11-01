package com.quanleimu.activity;

import java.util.Set;
import java.util.TreeSet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.quanleimu.util.TrackConfig.TrackMobile.BxEvent;
import com.quanleimu.util.TrackConfig.TrackMobile.Key;
import com.quanleimu.util.Tracker;
import com.quanleimu.util.Util;
import com.quanleimu.view.fragment.CityChangeFragment;
import com.quanleimu.view.fragment.FeedbackFragment;
import com.quanleimu.view.fragment.LoginFragment;
import com.quanleimu.view.fragment.SetMainFragment;
//import com.tencent.mm.sdk.platformtools.Log;
import com.quanleimu.util.TrackConfig.TrackMobile.PV;
/**
 * 
 * @author liuchong
 *
 */
public abstract class BaseFragment extends Fragment {

	public static final String TAG = "QLM";//"BaseFragment";
	public PV pv = PV.BASE; 

	
	protected static int INVALID_REQUEST_CODE = 0xFFFFFFFF;
	protected int requestCode = INVALID_REQUEST_CODE;
	
	public final int MSG_USER_LOGIN 		= 10001;
	public final int MSG_USER_LOGOUT 	 	= 10002;

	/**
	 * Argument keys.
	 */
	public static final String ARG_COMMON_TITLE = "name";
	public static final String ARG_COMMON_BACK_HINT = "backPageName";
	public static final String ARG_COMMON_REQ_CODE = "reqestCode";
	
	private ProgressDialog pd;
	
	private TitleDef titleDef;
	private TabDef tabDef;
	
	protected Handler handler;
	
	public enum EBUTT_STYLE{
		EBUTT_STYLE_BACK,
		EBUTT_STYLE_NORMAL,
		EBUTT_STYLE_CUSTOM
		//EBUTT_STYLE_FORWARD
	};
	
	public final class TitleDef{	
		private TitleDef() {}
		public boolean m_visible = true;
		public String m_leftActionHint = null;
		public EBUTT_STYLE m_leftActionStyle = EBUTT_STYLE.EBUTT_STYLE_BACK;
		public int leftCustomResourceId = -1;
		
		public String m_title = null;
		public View m_titleControls = null;
		
		public String m_rightActionHint = null;
		public int m_rightActionBg = R.drawable.title_bg_selector;//Default right action bg
		public EBUTT_STYLE m_rightActionStyle = EBUTT_STYLE.EBUTT_STYLE_NORMAL;
		public int rightCustomResourceId = -1;
		
		public boolean hasGlobalSearch = false; //Disable search by default
	};
	
	public enum ETAB_TYPE{
		ETAB_TYPE_PREV,
		ETAB_TYPE_MAINPAGE,
		ETAB_TYPE_CATEGORY,
		ETAB_TYPE_PUBLISH,
		ETAB_TYPE_MINE,
		ETAB_TYPE_SETTING
	};
	
	public class TabDef{
		private TabDef() {}
		public boolean m_visible = true;
		public ETAB_TYPE m_tabSelected = ETAB_TYPE.ETAB_TYPE_PREV;
	};
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	protected void handleMessage(Message msg, Activity activity, View rootView)
	{
		//Override me to process you message.
	}
	
	protected final void sendMessage(int what, Object data)
	{
		Message message = null;
		if (handler != null)
		{
			message = handler.obtainMessage();
			message.what = what;
			if (data != null)
			{
				message.obj = data;
			}
			
			handler.sendMessage(message);
		}

	}
	
	protected final void sendMessageDelay(int what, Object data, long delayMillis)
	{
		Message message = null;
		if (handler != null)
		{
			message = handler.obtainMessage();
			message.what = what;
			if (data != null)
			{
				message.obj = data;
			}
			
			handler.sendMessageDelayed(message, delayMillis);
		}
		
	}
	
	public int getEnterAnimation()
	{
		return R.anim.right_to_left_enter;
	}
	
	public int getExitAnimation()
	{
		return R.anim.left_to_right_exit;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setHasOptionsMenu(true);
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				Activity activity = getActivity();
				View rootView = getView();
				if (rootView != null && activity != null) {
					BaseFragment.this.handleMessage(msg, activity, rootView);
				}
				else
				{
					Log.e(TAG, getName() + " cannot dispatch handle message because activity is null ? " + Boolean.valueOf(activity == null));
				}
			}
		};
		
		if (getArguments() != null)
		{
			requestCode = getArguments().getInt(ARG_COMMON_REQ_CODE, INVALID_REQUEST_CODE);
		}
		
		if (savedInstanceState != null)
		{
//			Log.d(TAG, "restore from saved state, check arguments auto save ? " + this.getArguments());
		}
	}
	
	public String getName()
	{
		return this.getClass().getName()  + this.hashCode();
	}
	
	protected final TitleDef getTitleDef()
	{
		if (titleDef == null)
		{
			titleDef = new TitleDef();
			initTitle(titleDef);
		}
		
		return titleDef;
	}
	
	protected final void reCreateTitle()
	{
		titleDef = null;
		getTitleDef();
	}
	
	protected void initTitle(TitleDef title) {
		//Do nothing
	}
	
	public final TabDef getTabDef() {
		if (tabDef == null)
		{
			tabDef = new TabDef();
			initTab(tabDef);
		}
		
		return tabDef;
	}
	
	protected void initTab(TabDef tab) {
	}
	
	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	public static final String[] OPTION_TITLES = {"切换城市", "设置", "反馈",  "登录", "注销", "退出程序"};
	public static final int OPTION_CHANGE_CITY = 0;
	public static final int OPTION_SETTING     = 1;
	public static final int OPTION_FEEDBACK    = 2;
	public static final int OPTION_LOGIN       = 3;
	public static final int OPTION_LOGOUT      = 4;
	public static final int OPTION_EXIT        = 5;	
	private static final int[] baseOptions     = {OPTION_SETTING, OPTION_FEEDBACK, OPTION_EXIT, OPTION_LOGIN};
	private static Set<Integer>    	   options = new TreeSet<Integer>();
	
	public int[] includedOptionMenus ()
	{
		return new int[0];
	}
	
	public int[] excludedOptionMenus ()
	{
		return new int[0];
	}
	
	private void initOptionMenu()
	{
		options.clear();
		for (int i = 0; i < baseOptions.length; i++)
		{
			options.add(baseOptions[i]);
		}
		int[] includeMenus = this.includedOptionMenus();
		for (int i = 0; i < includeMenus.length; i++)
		{
			options.add(includeMenus[i]);
		}
		
		int[] excludeMenus = this.excludedOptionMenus();		
		for (int i = 0; i< excludeMenus.length; i++)
		{
			options.remove(excludeMenus[i]);
		}
				
		if (Util.isUserLogin())
		{
			if (options.remove(OPTION_LOGIN))
			{
				options.add(OPTION_LOGOUT);
			}
		}
		else
		{
			if (options.remove(OPTION_LOGOUT))
			{
				options.add(OPTION_LOGIN);
			}			
		}
	}
	
//	@Override
//	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//		this.initOptionMenu();
//		for (int option : options)
//		{
//			menu.add(0, option, option, OPTION_TITLES[option]);
//		}
//		super.onCreateOptionsMenu(menu, inflater);
//	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		this.initOptionMenu();
		menu.clear();		
		for (int option : options)
		{
			menu.add(0, option, option, OPTION_TITLES[option]);
		}
		Tracker.getInstance().event(BxEvent.MENU_SHOW).append(Key.MENU_SHOW_PAGEURL, this.pv.name()).end();
		super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		String action = "";
		switch (item.getItemId())
		{
		case OPTION_CHANGE_CITY:
			this.pushFragment(new CityChangeFragment(), createArguments("切换城市", ""));
			action = "changecity";
			break;
		case OPTION_SETTING:
			this.pushFragment(new SetMainFragment(), createArguments("设置", ""));
			action = "setting";
			break;
		case OPTION_FEEDBACK:
			this.pushFragment(new FeedbackFragment(), createArguments("反馈", ""));
			action = "feedback";
			break;
		case OPTION_EXIT:
			QuanleimuMainActivity mainActivity = (QuanleimuMainActivity) this.getActivity();
			mainActivity.exitMainActivity();
			action = "exit";
			break;
		case OPTION_LOGIN:
			this.pushFragment(new LoginFragment(), createArguments("登录", ""));		
			BaseFragment.this.sendMessage(MSG_USER_LOGIN, null);
			action = "login";
			break;
		case OPTION_LOGOUT:
			action = "logout";
	        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	        builder.setTitle(R.string.dialog_confirm_logout)
	                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
	                    @Override
	                    public void onClick(DialogInterface dialogInterface, int i) {
	                        Util.logout();
	                        BaseFragment.this.sendMessage(MSG_USER_LOGOUT, null);
	                        Toast.makeText(getAppContext(), "已退出", Toast.LENGTH_SHORT).show();
	                    }
	                })
	                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	                    @Override
	                    public void onClick(DialogInterface dialog, int id) {
	                        dialog.dismiss();
	                    }
	                }).create().show();
			break;
		}
		Tracker.getInstance().event(BxEvent.MENU_ACTION).append(Key.MENU_ACTION_TYPE, action).end(); 		
		return super.onOptionsItemSelected(item);
	}
	
	
	/**
	 * this called before <code>onStart</code> and after <code>onCreateView</code>
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		refreshHeader();
		refreshFooter();
	}
	
	public final void notifyOnStackTop(boolean isBack)
	{
		this.onStackTop(isBack);
	}
	
	protected int getFirstRunId()
	{
		return -1;
	}
	
	public void onStackTop(boolean isBack)
	{

	}
	
	

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.e(TAG, "fragment onActivityResult()");
	}

	/**
	 * View will be destroyed before you leave this fragment.
	 */
	@Override
	public void onDestroyView() {
//		Log.w(TAG, "#" + this.getName() + " going to destory view.");
		if (pd != null && pd.isShowing())
		{
			pd.dismiss();
		}
		super.onDestroyView();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * not in use include put into back stack and view not visible to user.
	 */
	@Override
	public void onDestroy() {
//		Log.w(TAG, "#" + this.getName() + " is going to be destory!");
		this.handler = null;
		super.onDestroy();
	}

	/**
	 * detach will happen when there is no way to return this fragment. eg: pop fragment from stack.
	 */
	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public void onPause() {
		super.onPause();
		
		hideSoftKeyboard();

	}
	
	protected final void hideSoftKeyboard()
	{
		View currentRoot = getView();
		if (currentRoot != null)
		{
			InputMethodManager mgr = (InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			mgr.hideSoftInputFromWindow(currentRoot.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		getActivity().findViewById(R.id.contentLayout).setVisibility(View.VISIBLE);
		
		((BaseActivity) getActivity()).showFirstRun(this);
		Log.d("BaseFragment", ""+this.getClass().getName());

	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
	
	protected final void pushAndFinish(BaseFragment f, Bundle bundle)
	{
		BaseActivity activity = (BaseActivity) this.getActivity();
		activity.pushFragment(f, bundle, this.getName());
	}
	
	protected void pushFragment(BaseFragment f, Bundle bundle)
	{
		
		BaseActivity activity = (BaseActivity) this.getActivity();
		activity.pushFragment(f, bundle, false);
	}
	
	protected final String getFragmentName()
	{
		return this.hashCode() + "";
	}
	
	protected final void finishFragment()
	{
		BaseActivity activity = (BaseActivity) getActivity();
		if(activity != null){
			activity.popFragment(this);
		}
	}
	
	protected final void finishFragment(int resultCode, Object result)
	{
		BaseActivity activity = (BaseActivity) getActivity();
		finishFragment();
		BaseFragment current = activity.getCurrentFragment();
		if (current != null)
		{
			current.onFragmentBackWithData(resultCode, result);
		}
	}
	
	protected void onFragmentBackWithData(int requestCode, Object result)
	{
		Log.w(TAG, "fragment finish with data." + requestCode + "_" + result);
		//TODO:
	}
	
	public boolean handleBack()
	{
		return false;
	}
	
	public void handleRightAction()
	{
		
	}
	
	protected void refreshFooter()
	{

	}
	
	public void handleSearch()
	{
		
	}
	
	/**
	 * Give subclass a chance to do something when we add customize title components to header.
	 * Eg: maybe sub class is using an nine-patch background for title components, and want to set padding after the view is add to window.
	 * 
	 * @param titleControl
	 */
	protected void onAddTitleControl(View titleControl)
	{
		
	}
		
	protected void refreshHeader()
	{
		if (!this.isUiActive())
		{
			Log.e(TAG, "cannot refresh header because ui is not active now");
			return;
		}
		
		TitleDef title = getTitleDef();
		Activity activity = getActivity();
		
		RelativeLayout top = (RelativeLayout)activity.findViewById(R.id.linearTop);
		top.setPadding(0, 0, 0, 0);//For nine patch.
		
		if( title != null && title.m_visible){
			top.setVisibility(View.VISIBLE);
			
			LinearLayout llTitleControls = (LinearLayout) top
					.findViewById(R.id.linearTitleControls);
			TextView tTitle = (TextView) activity.findViewById(R.id.tvTitle);
			
			if (null != title.m_titleControls) {
				llTitleControls.setVisibility(View.VISIBLE);
				tTitle.setVisibility(View.GONE);
				
				View titleControl = llTitleControls.getChildCount() == 0 ? null : llTitleControls.getChildAt(0);
				if (titleControl != title.m_titleControls) {
					llTitleControls.removeAllViews();
					llTitleControls.addView(title.m_titleControls);		
					onAddTitleControl(title.m_titleControls);
				}
			} else {
				llTitleControls.setVisibility(View.GONE);
				tTitle.setVisibility(View.VISIBLE);
				tTitle.setText(title.m_title);
			}
			
			//left action bar settings
			View left = activity.findViewById(R.id.left_action);
			left.setPadding(0, 0, 0, 0);//Fix 9-ppatch issue.
			if(null != title.m_leftActionHint && !title.m_leftActionHint.equals("")){
				left.setVisibility(View.VISIBLE);
				activity.findViewById(R.id.left_line).setVisibility(View.VISIBLE);
			}else if(title.m_leftActionStyle == EBUTT_STYLE.EBUTT_STYLE_CUSTOM && title.leftCustomResourceId > 0){
				left.setVisibility(View.VISIBLE);
				activity.findViewById(R.id.left_line).setVisibility(View.VISIBLE);
			}else{
				left.setVisibility(View.GONE);
				activity.findViewById(R.id.left_line).setVisibility(View.GONE);
			}
			
			View search = activity.findViewById(R.id.search_action);
			search.setPadding(0, 0, 0, 0);//Fix 9-patch issue.
			if (title.hasGlobalSearch)
			{
				search.setVisibility(View.VISIBLE);
			}
			else
			{
				search.setVisibility(View.GONE);
			}
			
			
			//right action bar settings
			View right = activity.findViewById(R.id.right_action);
			if(right != null && title.m_rightActionHint != null && !"".equals(title.m_rightActionHint)){
//				right.setText(title.m_rightActionHint);
				right.setVisibility(View.VISIBLE);
				right.setBackgroundResource(title.m_rightActionBg);
				
				TextView text = (TextView) activity.findViewById(R.id.right_btn_txt);
				text.setText(title.m_rightActionHint);
			}else if(right != null && title.m_rightActionStyle == EBUTT_STYLE.EBUTT_STYLE_CUSTOM && title.rightCustomResourceId > 0){
				right.setVisibility(View.VISIBLE);
				right.setBackgroundResource(title.m_rightActionBg);
			}else if (right != null){
				right.setVisibility(View.GONE);
			}
			right.setPadding(0, 0, 0, 0); //fix 9-patch issue.
		}
		else{
			top.setVisibility(View.GONE);
		}
	}
	
	protected static Bundle createArguments(String title, String backhint)
	{
		Bundle bundle = new Bundle();
		if (title != null) bundle.putString(ARG_COMMON_TITLE, title);
		if (backhint != null) bundle.putString(ARG_COMMON_BACK_HINT, backhint);
		return bundle;
	}
	
	protected final void showSimpleProgress()
	{
		showProgress(R.string.dialog_title_info, R.string.dialog_message_waiting, true);
	}
	
	protected final void showProgress(int titleResid, int messageResid, boolean cancelable) {
		String title = getString(titleResid);
		String message = getString(messageResid);
		showProgress(title, message, cancelable);
	}
	
	protected final void showProgress(String title, String message, boolean cancelable)
	{
		hideProgress();

        if (getActivity() != null)
		{
			pd = ProgressDialog.show(getActivity(), title, message);
			pd.setCancelable(cancelable);
            pd.setCanceledOnTouchOutside(cancelable);
		}
	}
	
	protected final void hideProgress()
	{
		if (pd != null && pd.isShowing())
		{
			pd.dismiss();
		}
	}
	
	protected final Context getAppContext()
	{
//		Activity activity = this.getActivity();
//		if (activity != null )
//		{
//			return activity;
//		}
		return QuanleimuApplication.context;
	}
	
	protected void logCreateView(Bundle bundle)
	{
//		Log.w(TAG,"before create view, do we have one? " + this.getView() + "::create view for " + this.getClass().getName());
	}
	
	
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return super.onContextItemSelected(item);
	}

	private boolean isUiActive()
	{
		return this.getActivity() != null;
	}
	
	
}

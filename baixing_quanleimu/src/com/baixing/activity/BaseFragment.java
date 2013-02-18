//liuchong@baixing.com
package com.baixing.activity;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baixing.data.GlobalDataManager;
import com.baixing.tracking.Tracker;
import com.baixing.tracking.TrackConfig.TrackMobile.BxEvent;
import com.baixing.tracking.TrackConfig.TrackMobile.Key;
import com.baixing.tracking.TrackConfig.TrackMobile.PV;
import com.baixing.util.Util;
import com.baixing.view.fragment.CityChangeFragment;
import com.baixing.view.fragment.FeedbackFragment;
import com.baixing.view.fragment.LoginFragment;
import com.baixing.view.fragment.SettingFragment;
import com.quanleimu.activity.R;
/**
 * 
 * @author liuchong
 *
 */
public abstract class BaseFragment extends Fragment  {

	public static final String TAG = "QLM";//"BaseFragment";
	protected PV pv = PV.BASE; 

	
	protected static int INVALID_REQUEST_CODE = 0xFFFFFFFF;
	protected int fragmentRequestCode = INVALID_REQUEST_CODE;
	
	public final int MSG_USER_LOGIN 		= 10001;
	public final int MSG_USER_LOGOUT 	 	= 10002;
	
	/**
	 * Argument keys.
	 */
	public static final String ARG_COMMON_TITLE = "name";
	public static final String ARG_COMMON_BACK_HINT = "backPageName";
	public static final String ARG_COMMON_REQ_CODE = "reqestCode";
	public static final String ARG_COMMON_ANIMATION_IN = "inAnimation";
	public static final String ARG_COMMON_ANIMATION_EXIT = "exitAnimation";
	public static final String ARG_COMMON_HAS_GLOBAL_TAB = "hasGlobalTabbar";
	
	private ProgressDialog pd;
	
	private TitleDef titleDef;
	
	protected Handler handler;
	private View.OnClickListener titleActionListener;
	
	public Handler getHandler() {
		return handler;
	}

	public static final class TitleDef{
		private TitleDef() {}
		public boolean m_visible = true;
		public String m_leftActionHint = null;
		public int m_leftActionImage = -1;
		
		public String m_title = null;
		public View m_titleControls = null;
		
		public String m_rightActionHint = null;
		public int m_rightActionBg = R.drawable.title_bg_selector;//Default right action bg
		public int rightCustomResourceId = -1;
		
		public boolean hasGlobalSearch = false; //Disable search by default
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
		return getArguments() == null ? R.anim.right_to_left_enter : getArguments().getInt(ARG_COMMON_ANIMATION_IN, R.anim.right_to_left_enter);
	}
	
	public int getExitAnimation()
	{
		return getArguments() == null ? R.anim.left_to_right_exit : getArguments().getInt(ARG_COMMON_ANIMATION_EXIT, R.anim.left_to_right_exit);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		titleActionListener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.right_action:
					handleRightAction();

					break;
				case R.id.left_action:
					if (getActivity() instanceof IExit) {
						((IExit) getActivity()).handleFragmentAction();
					}
					break;
				case R.id.search_action: {
					handleSearch();
					break;
				}
				}
			}
		};
		
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
			fragmentRequestCode = getArguments().getInt(ARG_COMMON_REQ_CODE, INVALID_REQUEST_CODE);
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
				
		if (GlobalDataManager.getInstance().getAccountManager().isUserLogin())
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
		super.onPrepareOptionsMenu(menu);
		Tracker.getInstance().event(BxEvent.MENU_SHOW)
		.append(Key.FRAGMENT, this.getClass().toString())
		.end();
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
			this.pushFragment(new SettingFragment(), createArguments("设置", ""));
			action = "setting";
			break;
		case OPTION_FEEDBACK:
			this.pushFragment(new FeedbackFragment(), createArguments("反馈信息", ""));
			action = "feedback";
			break;
		case OPTION_EXIT:
			BaseTabActivity mainActivity = (BaseTabActivity) this.getActivity();
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
		Tracker.getInstance().event(BxEvent.MENU_ACTION)
		.append(Key.FRAGMENT, this.getClass().toString())
		.append(Key.MENU_ACTION_TYPE, action).end(); 		
		return super.onOptionsItemSelected(item);
	}
	
	
	/**
	 * this called before <code>onStart</code> and after <code>onCreateView</code>
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		refreshHeader(getView());
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
	public final void onDestroyView() {
		onViewDestory(getView());
//		Log.w(TAG, "#" + this.getName() + " going to destory view.");
		if (pd != null && pd.isShowing())
		{
			pd.dismiss();
		}
		TitleDef title = getTitleDef();
		if (title != null && title.m_titleControls != null) {
			((ViewGroup)getView().findViewById(R.id.linearTop)).removeView(title.m_titleControls);
		}
		super.onDestroyView();
	}
	
	protected void onViewDestory(View rootView) {
		//Give sub class a chance to release some resource.
	}

	@Override
	public final View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = onInitializeView(inflater, container, savedInstanceState);
		refreshHeader(rootView);
		return rootView;
	}
	
	protected abstract View onInitializeView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState);

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
		if(activity != null){
			activity.pushFragment(f, bundle, this.getName());
		}
	}
	
	protected void pushFragment(BaseFragment f, Bundle bundle)
	{		
		BaseActivity activity = (BaseActivity) this.getActivity();
		if(activity != null){
			activity.pushFragment(f, bundle, false);
		}
	}
	
	protected final String getFragmentName()
	{
		return this.hashCode() + "";
	}
	
	protected final boolean finishFragment()
	{
		BaseActivity activity = (BaseActivity) getActivity();
		if(activity != null){
			return activity.popFragment(this);
		}
		return false;
	}
	
	protected final void finishFragment(int resultCode, Object result)
	{
		if(!finishFragment()) return;
		
		BaseActivity activity = (BaseActivity) getActivity();
		if(activity == null) return;
		
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
		
	protected void refreshHeader() {
		refreshHeader(getView());
	}
	
	private void refreshHeader(View rootView)
	{
		if (rootView == null)
		{
			Log.e(TAG, "cannot refresh header because ui is not active now");
			return;
		}
		
		View left = rootView.findViewById(R.id.left_action);
		if (left != null) {
			left.setOnClickListener(titleActionListener);
		}
		View right = rootView.findViewById(R.id.right_action);
		if (right != null) {
			right.setOnClickListener(titleActionListener);
		}
		View search = rootView.findViewById(R.id.search_action);
		if (search != null) {
			search.setOnClickListener(titleActionListener);
		}
		
		TitleDef title = getTitleDef();
		
		RelativeLayout top = (RelativeLayout)rootView.findViewById(R.id.linearTop);
		top.setPadding(0, 0, 0, 0);//For nine patch.
		
		if( title != null && title.m_visible){
			top.setVisibility(View.VISIBLE);
			
			LinearLayout llTitleControls = (LinearLayout) top
					.findViewById(R.id.linearTitleControls);
			TextView tTitle = (TextView) rootView.findViewById(R.id.tvTitle);
			
			if (null != title.m_titleControls) {
				llTitleControls.setVisibility(View.VISIBLE);
				tTitle.setVisibility(View.GONE);
				
				View titleControl = llTitleControls.getChildCount() == 0 ? null : llTitleControls.getChildAt(0);
				if (titleControl != title.m_titleControls) {
					ViewParent p = title.m_titleControls.getParent();
					if (p != null) {
						((ViewGroup) p).removeView(title.m_titleControls);
					}
					llTitleControls.addView(title.m_titleControls);	
					onAddTitleControl(title.m_titleControls);
				}
			} else {
				llTitleControls.setVisibility(View.GONE);
				tTitle.setVisibility(View.VISIBLE);
				tTitle.setText(title.m_title);
			}
			
			//left action bar settings
			left.setPadding(0, 0, 0, 0);//Fix 9-ppatch issue.
			if(null != title.m_leftActionHint && !title.m_leftActionHint.equals("")){
				left.setVisibility(View.VISIBLE);
				rootView.findViewById(R.id.left_line).setVisibility(View.VISIBLE);
				if (title.m_leftActionImage != -1) {
					ImageView img = (ImageView) rootView.findViewById(R.id.back_icon);
					img.setImageResource(title.m_leftActionImage);
				}
				
			}else{
				left.setVisibility(View.GONE);
				rootView.findViewById(R.id.left_line).setVisibility(View.GONE);
			}
			
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
			if(right != null && title.m_rightActionHint != null && !"".equals(title.m_rightActionHint)){
//				right.setText(title.m_rightActionHint);
				right.setVisibility(View.VISIBLE);
				right.setBackgroundResource(title.m_rightActionBg);
				
				TextView text = (TextView) rootView.findViewById(R.id.right_btn_txt);
				text.setText(title.m_rightActionHint);
			}else if (right != null){
				right.setVisibility(View.GONE);
			}
			right.setPadding(0, 0, 0, 0); //fix 9-patch issue.
		}
		else{
			top.setVisibility(View.GONE);
		}
	}
	
	protected final Bundle createArguments(String title, String backhint)
	{
		Bundle bundle = new Bundle();
		if (title != null) bundle.putString(ARG_COMMON_TITLE, title);
		if (backhint != null) bundle.putString(ARG_COMMON_BACK_HINT, backhint);
		if (getArguments() != null) {
			bundle.putBoolean(ARG_COMMON_HAS_GLOBAL_TAB, hasGlobalTab());
		}
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
	
	protected final void showProgress(String title, String message, DialogInterface.OnCancelListener cancelListener) {
		hideProgress();

        if (getActivity() != null)
		{
			pd = ProgressDialog.show(getActivity(), title, message);
			pd.setCancelable(cancelListener != null);
			if (cancelListener != null) {
				pd.setOnCancelListener(cancelListener);
				pd.setCanceledOnTouchOutside(false);
			}
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
		return GlobalDataManager.getInstance().getApplicationContext();
	}
	
	protected void logCreateView(Bundle bundle)
	{
//		Log.w(TAG,"before create view, do we have one? " + this.getView() + "::create view for " + this.getClass().getName());
	}
	
	
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		return super.onContextItemSelected(item);
	}

	private boolean isUiActive()
	{
		return this.getActivity() != null;
	}
	
	public boolean hasGlobalTab()
	{
		return getArguments() != null && getArguments().containsKey(ARG_COMMON_HAS_GLOBAL_TAB) ? getArguments().getBoolean(ARG_COMMON_HAS_GLOBAL_TAB) : true;
	}
	
}

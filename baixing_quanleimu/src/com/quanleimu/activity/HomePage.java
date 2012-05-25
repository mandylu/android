package com.quanleimu.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.mobclick.android.MobclickAgent;
import com.quanleimu.util.LocationService;
import com.quanleimu.util.ShortcutUtil;
import com.quanleimu.view.BaseView;
import com.quanleimu.view.SetMain;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Button;
import com.quanleimu.view.PersonalCenterView;

import com.quanleimu.view.HomePageView;
public class HomePage extends BaseActivity implements BaseView.ViewInfoListener{
	private BaseView currentView;

	@Override
	public void onBack(){
    	if(!currentView.onBack() && MyApplication.getApplication().getViewStack().size() > 0){
    		LinearLayout scroll = (LinearLayout)this.findViewById(R.id.contentLayout);
    		scroll.removeAllViews();
    		
    		currentView.onDestroy();
    		currentView = MyApplication.getApplication().getViewStack().pop();
    		setBaseLayout(currentView);            		

    		scroll.addView(currentView);
    		
    	}else{

            SharedPreferences settings = getSharedPreferences(SHARE_PREFS_NAME, 0);
            String hasShowShortcutMessage = settings.getString("hasShowShortcut", "no");

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            LayoutInflater adbInflater = LayoutInflater.from(HomePage.this);
            View shortcutLayout = adbInflater.inflate(R.layout.shortcutshow, null);

            final CheckBox shortcutCheckBox = (CheckBox) shortcutLayout.findViewById(R.id.shortcut);
            final boolean needShowShortcut = "no".equals(hasShowShortcutMessage) && !ShortcutUtil.hasShortcut(this);
            if (needShowShortcut)
            {
                builder.setView(shortcutLayout);
            }

            builder.setTitle("提示:").setMessage("是否退出?").setNegativeButton("否", null).setPositiveButton("是", new DialogInterface.OnClickListener()
            {

                @Override
                public void onClick(DialogInterface dialog, int which)
                {

                    if (needShowShortcut && shortcutCheckBox.isChecked())
                    {
                        ShortcutUtil.addShortcut(HomePage.this);
                    }

                    if (MyApplication.list != null && MyApplication.list.size() != 0)
                    {
                        for (String s : MyApplication.list)
                        {
                            deleteFile(s);
                        }
                        for (int i = 0; i < fileList().length; i++)
                        {
                            System.out.println("fileList()[i]----------->" + fileList()[i]);
                        }
                    }

                    SharedPreferences settings = getSharedPreferences(SHARE_PREFS_NAME, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("hasShowShortcut", "yes");
                    // Commit the edits!
                    editor.commit();

                    System.exit(0);
                }
            });
            builder.create().show();
    	}		
	}
	
	@Override
	public void onNewView(BaseView newView){
		currentView.onPause();
		MyApplication.getApplication().getViewStack().push(currentView);
		currentView = newView;
		LinearLayout scroll = (LinearLayout)this.findViewById(R.id.contentLayout);
		scroll.removeAllViews();
		scroll.addView(currentView);
		
		setBaseLayout(newView);
	}
	
	@Override
	public void onRightBtnTextChanged(String newText){
		Button right = (Button)this.findViewById(R.id.btnRight);
		right.setText(newText);
	}

	@Override
	public void onTitleChanged(String newTitle){
		TextView title = (TextView)this.findViewById(R.id.tvTitle);
		title.setText(newTitle);
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		LocationService.getInstance().stop();
		super.onPause();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onResume() {
		bundle.putString("backPageName", "");
		super.onResume();
	} 

	private void changeTabView(BaseView view){
		view.setInfoChangeListener(this);
		if(currentView instanceof HomePageView){
			ivHomePage.setImageResource(R.drawable.iv_homepage);
		}
		else if(currentView instanceof PersonalCenterView){
			ivMyCenter.setImageResource(R.drawable.iv_mycenter);
		}
		
		
		if(view instanceof HomePageView){
			ivHomePage.setImageResource(R.drawable.iv_homepage_press);
		}
		else if(view instanceof PersonalCenterView){
			ivMyCenter.setImageResource(R.drawable.iv_mycenter_press);
		}
		
		LinearLayout scroll = (LinearLayout)this.findViewById(R.id.contentLayout);
		scroll.removeAllViews();
		scroll.addView(view);
		currentView = view;
		setBaseLayout(view);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) { 
		setContentView(R.layout.homepage);
		LinearLayout scroll = (LinearLayout)this.findViewById(R.id.contentLayout);
		
		ImageView vHomePage = (ImageView)findViewById(R.id.ivHomePage);
		vHomePage.setImageResource(R.drawable.iv_homepage_press);
		
		Button left = (Button)findViewById(R.id.btnLeft);
		left.setOnClickListener(this);
		Button right = (Button)findViewById(R.id.btnRight);
		right.setOnClickListener(this);
		
		ivHomePage = (ImageView)findViewById(R.id.ivHomePage);
		ivHomePage.setOnClickListener(this);
		ivCateMain = (ImageView)findViewById(R.id.ivCateMain);
		ivCateMain.setOnClickListener(this);
		ivPostGoods = (ImageView)findViewById(R.id.ivPostGoods);
		ivPostGoods.setOnClickListener(this);
		ivMyCenter = (ImageView)findViewById(R.id.ivMyCenter);
		ivMyCenter.setOnClickListener(this);
		ivSetMain = (ImageView)findViewById(R.id.ivSetMain);
		ivSetMain.setOnClickListener(this);
		
		if(!MyApplication.update){
			MyApplication.update = true;
			MobclickAgent.setUpdateOnlyWifi(false);
			MobclickAgent.update(this);
		}
				
		BaseView childView = new HomePageView(this, bundle);		
		currentView = childView;
		childView.setInfoChangeListener(this);		
		setBaseLayout(childView);
		scroll.addView(childView);
		
		super.onCreate(savedInstanceState);
	}
	
	private void setBaseLayout(BaseView view){
		
		if(view == null) return;
		
		
		BaseView.TabDef tab = view.getTabDef();
		if(null == tab) return;
		LinearLayout bottom = (LinearLayout)findViewById(R.id.linearBottom);
		if(tab.m_visible){
			bottom.setVisibility(View.VISIBLE);
		}
		else{
			bottom.setVisibility(View.GONE);
		}
		
		ivHomePage.setImageResource((tab.m_tabSelected == BaseView.ETAB_TYPE.ETAB_TYPE_MAINPAGE) ? R.drawable.iv_homepage_press : R.drawable.iv_homepage);
		ivCateMain.setImageResource(tab.m_tabSelected == BaseView.ETAB_TYPE.ETAB_TYPE_CATEGORY ? R.drawable.iv_cate_press : R.drawable.iv_cate);
		ivPostGoods.setImageResource(tab.m_tabSelected == BaseView.ETAB_TYPE.ETAB_TYPE_PUBLISH ? R.drawable.iv_postgoods_press : R.drawable.iv_postgoods);
		ivMyCenter.setImageResource(tab.m_tabSelected == BaseView.ETAB_TYPE.ETAB_TYPE_MINE ? R.drawable.iv_mycenter_press : R.drawable.iv_mycenter);
		ivSetMain.setImageResource(tab.m_tabSelected == BaseView.ETAB_TYPE.ETAB_TYPE_SETTING ? R.drawable.iv_setmain_press : R.drawable.iv_setmain);		
		
		
		BaseView.TitleDef title = view.getTitleDef();
		if(null == title) return;
		LinearLayout top = (LinearLayout)findViewById(R.id.linearTop);
		if(title.m_visible){
			top.setVisibility(View.VISIBLE);
			TextView tTitle = (TextView)findViewById(R.id.tvTitle);
			tTitle.setText(title.m_title);
			if(null != title.m_leftActionHint && !title.m_leftActionHint.equals("")){
				Button left = (Button)findViewById(R.id.btnLeft);
				left.setText(title.m_leftActionHint);
				left.setVisibility(View.VISIBLE);
			}else{
				Button left = (Button)findViewById(R.id.btnLeft);
				left.setVisibility(View.GONE);
			}
			
			if(null != title.m_rightActionHint && !title.m_rightActionHint.equals("")){
				Button right = (Button)findViewById(R.id.btnRight);
				right.setText(title.m_rightActionHint);
				right.setVisibility(View.VISIBLE);
			}else{
				Button right = (Button)findViewById(R.id.btnRight);
				right.setVisibility(View.GONE);
			}
		}
		else{
			top.setVisibility(View.GONE);
		}
	
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnRight:
			currentView.onRightActionPressed();
//			intent.setClass(HomePage.this, Search.class);
//			bundle.putString("searchType", "homePage");
//			intent.putExtras(bundle);
//			startActivity(intent);
			break;
		case R.id.btnLeft:
			if(!currentView.onLeftActionPressed()){
				this.onBack();
			}
//			intent.setClass(HomePage.this, CityChange.class);
//			bundle.putString("backPageName", "首页");
////			bundle.putString("cityName", cityName);
//			intent.putExtras(bundle);
//			startActivity(intent);
			break;
		case R.id.ivHomePage:{
			if(currentView instanceof HomePageView)break;
			changeTabView(new HomePageView(this, bundle));
			break;
		}
		case R.id.ivCateMain:
			///////////set currentview here
			///currentView = ???
			intent.setClass(this, CateMain.class);
			intent.putExtras(bundle);
			startActivity(intent);
			overridePendingTransition(0, 0);
			break;
		case R.id.ivPostGoods:
			///////////set currentview here
			///currentView = ???
			
			intent.setClass(this, PostGoodsCateMain.class);
			intent.putExtras(bundle);
			startActivity(intent);
			overridePendingTransition(0, 0);
			break;
		case R.id.ivMyCenter:
			if(currentView instanceof PersonalCenterView)break;
			changeTabView(new PersonalCenterView(this, bundle));
			break;
		case R.id.ivSetMain:
			///////////set currentview here
			///currentView = ???
	
//			intent.setClass(this, SetMain.class);
//			intent.putExtras(bundle);
//			startActivity(intent);
//			overridePendingTransition(0, 0);
			
			BaseView view = new SetMain(this);
			view.setInfoChangeListener(this);
			onNewView(view);
			
			
			break;
		}
		super.onClick(v);
	}

	private final static String SHARE_PREFS_NAME = "baixing_shortcut_app";
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
        	onBack();
        }
        
        else{
        	return super.onKeyDown(keyCode, event);
        }
        
        return true;
    }
}

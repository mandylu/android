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
import android.widget.ScrollView;
import com.quanleimu.view.BaseView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Button;
import com.quanleimu.view.PersonalCenterView;

import com.quanleimu.view.HomePageView;
public class HomePage extends BaseActivity implements BaseView.ViewInfoListener{
	private BaseView currentView;

	@Override
	public void onBack(){
		
	}
	
	@Override
	public void onNewView(BaseView newView){
		currentView.onPause();
		MyApplication.getApplication().getViewStack().push(currentView);
		currentView = newView;
		ScrollView scroll = (ScrollView)this.findViewById(R.id.scrollView1);
		scroll.removeAllViews();
		scroll.addView(currentView);
		
		setBaseLayout(newView);
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

	@Override
	protected void onCreate(Bundle savedInstanceState) { 
		setContentView(R.layout.homepage);
		ScrollView scroll = (ScrollView)this.findViewById(R.id.scrollView1);
		BaseView childView = new HomePageView(this, bundle);
		setBaseLayout(childView);
		currentView = childView;
		ImageView vHomePage = (ImageView)findViewById(R.id.ivHomePage);
		vHomePage.setImageResource(R.drawable.iv_homepage_press);
		childView.setInfoChangeListener(this);
		scroll.addView(childView);
		
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

		super.onCreate(savedInstanceState);
		if(!MyApplication.update){
			MyApplication.update = true;
			MobclickAgent.setUpdateOnlyWifi(false);
			MobclickAgent.update(this);
		}
		
		
		ImageView ivSetMain = (ImageView)findViewById(R.id.ivSetMain);
		ivSetMain.setOnClickListener(this);
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
			}else{
				Button left = (Button)findViewById(R.id.btnLeft);
				left.setVisibility(View.GONE);
			}
			
			if(null != title.m_rightActionHint && !title.m_rightActionHint.equals("")){
				Button right = (Button)findViewById(R.id.btnRight);
				right.setText(title.m_rightActionHint);
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
			BaseView home = new HomePageView(this, bundle);
			ScrollView scroll = (ScrollView)this.findViewById(R.id.scrollView1);
			scroll.removeAllViews();
			scroll.addView(home);
			currentView = home;
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
			///////////set currentview here
			///currentView = ???ivMyCenter.setImageResource(R.drawable.iv_mycenter_press);
			if(currentView instanceof PersonalCenterView)break;
			BaseView personal = new PersonalCenterView(this, bundle);
			ScrollView scroll = (ScrollView)this.findViewById(R.id.scrollView1);
			scroll.removeAllViews();
			scroll.addView(personal);
			currentView = personal;
			break;
		case R.id.ivSetMain:
			///////////set currentview here
			///currentView = ???
	
//			intent.setClass(this, SetMain.class);
//			intent.putExtras(bundle);
//			startActivity(intent);
//			overridePendingTransition(0, 0);
			
			onNewView(new AboutUs(this));
			
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
        return super.onKeyDown(keyCode, event);
    }
}

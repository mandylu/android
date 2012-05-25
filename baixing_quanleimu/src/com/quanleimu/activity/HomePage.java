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

import com.quanleimu.view.HomePageView;
public class HomePage extends BaseActivity implements BaseView.ViewInfoListener{
	
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
		childView.setInfoChangeListener(this);
		scroll.addView(childView);
		super.onCreate(savedInstanceState);
		if(!MyApplication.update){
			MyApplication.update = true;
			MobclickAgent.setUpdateOnlyWifi(false);
			MobclickAgent.update(this);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnRight:
			intent.setClass(HomePage.this, Search.class);
			bundle.putString("searchType", "homePage");
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		case R.id.btnLeft:
			intent.setClass(HomePage.this, CityChange.class);
			bundle.putString("backPageName", "首页");
//			bundle.putString("cityName", cityName);
			intent.putExtras(bundle);
			startActivity(intent);
			break;
		case R.id.ivHomePage:
			break;
		case R.id.ivCateMain:
			intent.setClass(this, CateMain.class);
			intent.putExtras(bundle);
			startActivity(intent);
			overridePendingTransition(0, 0);
			break;
		case R.id.ivPostGoods:
			intent.setClass(this, PostGoodsCateMain.class);
			intent.putExtras(bundle);
			startActivity(intent);
			overridePendingTransition(0, 0);
			break;
		case R.id.ivMyCenter:
			intent.setClass(this, MyCenter.class);
			intent.putExtras(bundle);
			startActivity(intent);
			overridePendingTransition(0, 0);
			break;
		case R.id.ivSetMain:
			intent.setClass(this, SetMain.class);
			intent.putExtras(bundle);
			startActivity(intent);
			overridePendingTransition(0, 0);
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

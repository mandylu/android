package com.baixing.widget;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baixing.activity.BaseActivity;
import com.quanleimu.activity.R;

@SuppressLint("NewApi")
public class CommentsDialog extends Dialog implements OnItemClickListener {
	
	private class AppInfo{ 
		public Drawable icon;
		public String packageName;
		public String showName;
		public Uri uri;
		public String activityName;
		public AppInfo(String sm, Drawable d, String p, Uri u){
			icon = d;
			packageName = p;
			uri = u;
			showName = sm;
		}
	}
	List<AppInfo> appResources = new ArrayList<AppInfo>();
	
	private class ImageAdapter extends BaseAdapter{  
        private Context mContext;  
  
        public ImageAdapter(Context context) {  
            this.mContext=context;  
        }  
  
        @Override  
        public int getCount() {  
            return appResources.size();  
        }  
  
        @Override  
        public Object getItem(int position) {  
            return appResources.get(position);
        }  
  
        @Override  
        public long getItemId(int position) {  
            // TODO Auto-generated method stub  
            return 0;  
        }  
  
        @Override  
        public View getView(int position, View convertView, ViewGroup parent) {  
            View appView;  
            
            if(convertView == null){  
            	appView = LayoutInflater.from(mContext).inflate(R.layout.categorygriditem, null);
            	appView.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.list_selector));            	
                int width = mContext.getResources().getDimensionPixelSize(android.R.dimen.app_icon_size);
                appView.findViewById(R.id.itemicon).setLayoutParams(new LinearLayout.LayoutParams(width, width));                
                ((ImageView)appView.findViewById(R.id.itemicon)).setScaleType(ImageView.ScaleType.FIT_CENTER);  
            }else{  
            	appView = convertView;  
            }  
            Drawable drawable = appResources.get(position).icon;
            ((TextView)appView.findViewById(R.id.itemtext)).setText(appResources.get(position).showName);
            ((ImageView)appView.findViewById(R.id.itemicon)).setImageDrawable(drawable);
            return appView;  
        }
    }	

	public CommentsDialog(BaseActivity activity) {
		super(activity);
		// TODO Auto-generated constructor stub
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		getWindow().setBackgroundDrawable(new ColorDrawable(0));
		this.setTitle("选择评论渠道");
		View v = LayoutInflater.from(getContext()).inflate(R.layout.comments, null);
		this.setContentView(v);

		Intent intent = new Intent(Intent.ACTION_VIEW);
		Uri[] data = {Uri.parse("market://details?id=" + activity.getPackageName()),
				Uri.parse("http://play.google.com/store/apps/details?id=" + activity.getPackageName())};
		
		
		for(int i = 0; i < data.length; ++ i){
			intent.setData(data[i]);
			List<ResolveInfo> mainLauncherList = activity.getPackageManager().queryIntentActivities(intent, 0);
			for(ResolveInfo info : mainLauncherList){				
				String packageName = info.activityInfo.applicationInfo.packageName;				
				boolean exist = false;
				for(AppInfo p : appResources){
					if(p.packageName.equals(packageName)){
						exist = true;
						break;
					}
				}
				if(!exist){
					Drawable icon = info.activityInfo.applicationInfo.loadIcon(activity.getPackageManager());
					String showName = info.activityInfo.applicationInfo.loadLabel(activity.getPackageManager()).toString();
					AppInfo ai = new AppInfo(showName, icon, packageName, data[i]);					
					if(i == 0){
						ai.activityName = info.activityInfo.name;
					}
					this.appResources.add(ai);
				}
			}
		}
		
		((GridView)v.findViewById(R.id.gridComments)).setAdapter(new ImageAdapter(activity));
		((GridView)v.findViewById(R.id.gridComments)).setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		AppInfo ai = appResources.get(arg2);
		if(TextUtils.isEmpty(ai.activityName)){
			Intent launchIntent = this.getContext().getPackageManager().getLaunchIntentForPackage(ai.packageName);
			launchIntent.setData(ai.uri);
			launchIntent.setAction(Intent.ACTION_VIEW);
			this.getContext().startActivity(launchIntent);
		}else{
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(appResources.get(arg2).uri);
			intent.setComponent(new ComponentName(ai.packageName, ai.activityName));
			getContext().startActivity(intent);
		}
		this.dismiss();
	}
}
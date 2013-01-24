package com.quanleimu.activity.wxapi;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Toast;

import com.baixing.activity.MainActivity;
import com.baixing.activity.PersonalActivity;
import com.baixing.broadcast.CommonIntentAction;
import com.baixing.data.GlobalDataManager;
import com.tencent.mm.sdk.openapi.BaseReq;
import com.tencent.mm.sdk.openapi.BaseResp;
import com.tencent.mm.sdk.openapi.ConstantsAPI;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.ShowMessageFromWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXAppExtendObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.platformtools.Log;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler{
	private IWXAPI mApi;
	static private final String WX_APP_ID = "wx862b30c868401dbc";
	@Override 
	protected void onCreate(Bundle savedInstanceState) {
		this.getWindow().setBackgroundDrawable(null);
		mApi = WXAPIFactory.createWXAPI(this, WX_APP_ID, false);
		mApi.registerApp(WX_APP_ID);
		mApi.handleIntent(this.getIntent(), this);
		super.onCreate(savedInstanceState);
		this.setVisible(false);
	}
	
	public boolean isRunning() {
	    ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    List<RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);
	    
	    for (RunningTaskInfo task : tasks) {
	    	String tas = task.topActivity.toString();
	    	Log.d("packagename", tas);
	        if (tas.equals("ComponentInfo{com.quanleimu.activity/com.baixing.activity.MainActivity}")
	        		|| tas.equals("ComponentInfo{com.quanleimu.activity/com.baixing.activity.PersonalActivity}")
	        		|| tas.equals("ComponentInfo{com.quanleimu.activity/com.baixing.activity.PostActivity}"))
	            return true;                                  
	    }
	    return false;

//	    return false;
	}
	
	@Override
	public void onReq(BaseReq req) {
		switch (req.getType()) {
		case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
//			goToGetMsg();		
			break;
		case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
//			Intent intent = new Intent(Intent.ACTION_MAIN);
//			intent.setComponent(new ComponentName("com.quanleimu.activity","com.quanleimu.activity.QuanleimuApplication"));
//			startActivity(intent);
			if(req instanceof ShowMessageFromWX.Req){
				Log.d("onReq", "is instanceof ShowMessageFromWX.Req");
			}else{
				Log.d("on Req", "not instance of ShowMessageFromWX.Req!!!!");
			}
			WXMediaMessage wxMsg = ((ShowMessageFromWX.Req)req).message;	
			WXAppExtendObject obj = (WXAppExtendObject) wxMsg.mediaObject;
			String result = "";
			if(obj == null){
				Log.d("onReq", "extend obj is null!!!!!");
			}else{
				Log.d("onReq", "extend obj is not ~~~~~ null!!!!!");
				if(obj.fileData != null){
					Log.d("onReq", "extend obj's fileData is not ~~~~~ null!!!!!");
					result = obj.fileData.toString();
				}
				else if(obj.extInfo != null){
					Log.d("onReq", "extend obj's extInfo is not null!!!!!");					
				}
				if(obj.filePath != null){
					Log.d("onReq", obj.filePath);
					try {
						FileInputStream instream = new FileInputStream(obj.filePath);
						if(instream != null){
							InputStreamReader inputreader = new InputStreamReader(instream);
							BufferedReader buffreader = new BufferedReader(inputreader);
							String line = null;
							while((line = buffreader.readLine()) != null){
								result += line;
							}
						}
						instream.close();
					}catch(Exception e){
						e.printStackTrace();
					}
					Log.d("onReq", result);
					Log.d("onReq", "extend obj's filepath is not null!!!!!");
				}
			}
			
			Log.d("on Req, mediaObject", obj.toString());

			Bundle bundle = new Bundle();
			bundle.putBoolean("isFromWX", true);
			bundle.putString("detailFromWX", result);

			if(!isRunning()){
				Log.d("", "not running!!!!");
				Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.quanleimu.activity");
				launchIntent.putExtras(bundle);
				startActivity(launchIntent);
			}else{
				Log.d("", "running~~~!!~!~!~!~");
				Intent intent = new Intent(this, PersonalActivity.class);
				intent.putExtras(bundle);
				startActivity(intent);
			}
			finish();
			break;
		default:
			break;
		}
	}

	@Override
	public void onResp(BaseResp resp) {
		String result = "";
		
		switch (resp.errCode) {
		case BaseResp.ErrCode.ERR_OK:
			result = "发送成功";
			Context ctx = GlobalDataManager.getInstance().getApplicationContext();
			if(ctx != null){
				ctx.sendBroadcast(new Intent(CommonIntentAction.ACTION_BROADCAST_SHARE_SUCCEED));
			}
			break;
		case BaseResp.ErrCode.ERR_USER_CANCEL:
			result = "发送取消";
			break;
		case BaseResp.ErrCode.ERR_AUTH_DENIED:
			result = "发送被拒绝";
			break;
		default:
			result = "发送返回";
			break;
		}
		Toast.makeText(this, result, 3).show();
		finish();
	}
}
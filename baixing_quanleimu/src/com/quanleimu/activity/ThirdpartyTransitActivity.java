package com.quanleimu.activity;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import com.tencent.mm.sdk.platformtools.Log;

public class ThirdpartyTransitActivity extends Activity{
	static public final String ThirdpartyKey = "thirdparty";
	static public final String ThirdpartyType_Albam = "albam";
	static public final String ThirdpartyType_Photo = "photo";
	static public final String Name_PhotoNumber = "currentview";
	public static final String IMAGEUNSPECIFIED = "image/*";
	public static final String isFromPhotoOrAlbam = "isFromPhotoOrAlbam";
	public static final String Key_RequestCode = "requestCode";
	public static final String Key_RequestResult = "requestResult";
	public static final String Key_Data = "data";
	private static final int PHOTOZOOM = 2;
	private static final int PHOTOHRAPH = 1;
	private static final int NONE = 0;
	private int photoNameNumber = -1; 
	private boolean isCreate = false;
	enum ETHIRDPARTYTYPE{
		ETHIRDPARTYTYPE_ALBAM,
		ETHIRDPARTYTYPE_PHOTO
	}
	private ETHIRDPARTYTYPE tptype = null;
	@Override
	public void onCreate(Bundle bundle){
		Log.e("QLM", "third party create");
		this.getWindow().setBackgroundDrawable(null);
		this.isCreate = bundle == null;
		super.onCreate(bundle);
	}
	
	
	
	@Override
	protected void onNewIntent(Intent intent) {
		Log.e("QLM", "third party onNewIntent()");
		super.onNewIntent(intent);
	}



	@Override
	protected void onResume() {
		Log.e("QLM", "third party onResume()");
		super.onResume();
		if (isCreate)
		{
			Intent intent = this.getIntent();
			Bundle extBundle = intent.getExtras();
			
			if(extBundle.containsKey(ThirdpartyKey)){
				String key = extBundle.getString(ThirdpartyKey);
				if(key != null){
					if(key.equals(ThirdpartyType_Albam)){
						tptype = ETHIRDPARTYTYPE.ETHIRDPARTYTYPE_ALBAM;
						startThirdparty(0);
					}
					else if(key.equals(ThirdpartyType_Photo)){
						tptype = ETHIRDPARTYTYPE.ETHIRDPARTYTYPE_PHOTO;
						photoNameNumber = extBundle.getInt(Name_PhotoNumber);
						startThirdparty(photoNameNumber);
					}
				}
			}
			isCreate = false;
		}

	}



	private void startThirdparty(int tmpName){
		if (tptype == ETHIRDPARTYTYPE.ETHIRDPARTYTYPE_ALBAM) {
			Intent intent3 = new Intent(Intent.ACTION_GET_CONTENT);
			intent3.addCategory(Intent.CATEGORY_OPENABLE);
			intent3.setType(IMAGEUNSPECIFIED);
			startActivityForResult(Intent.createChooser(intent3, "选择图片"), PHOTOZOOM);

		} else if (ETHIRDPARTYTYPE.ETHIRDPARTYTYPE_PHOTO == tptype) {
			Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intent2.putExtra(MediaStore.EXTRA_OUTPUT,
					Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "temp" + tmpName + ".jpg")));
			startActivityForResult(intent2, PHOTOHRAPH);
		} 
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("QLM", "third party active result " + data);
		
		Intent backIntent = new Intent(this, QuanleimuMainActivity.class);
		Bundle bundle = new Bundle();
		if(requestCode == PHOTOHRAPH || requestCode == PHOTOZOOM){
			bundle.putBoolean(isFromPhotoOrAlbam, true);
		}
		bundle.putInt(Key_RequestCode, requestCode);
		bundle.putInt(Key_RequestResult, resultCode);
		bundle.putParcelable(Key_Data, data);
		backIntent.putExtras(bundle);
		backIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		this.startActivity(backIntent);
		this.finish();
	}
}
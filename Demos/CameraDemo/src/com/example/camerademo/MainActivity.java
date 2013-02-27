//liuchong@baixing.com
package com.example.camerademo;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.camerademo.util.BitmapUtil;
/**
 * 
 * @author liuchong
 *
 */
public class MainActivity extends Activity implements OnClickListener, SensorEventListener{
	public static final String TAG = "CAMPREV";
	private SensorManager sensorMgr;
	private Sensor sensor;
	
	private CameraPreview mPreview;
    Camera mCamera;
    static boolean isFrontCam; //If current camera is facing or front camera.
//    int cameraCurrentlyLocked;
    
    Orien currentOrien = Orien.DEFAULT;
    SensorEvent lastSensorEvent;
    
    private OnDeleteListener deleteListener;
    
    public static final int MSG_SAVE_DONE = 1;
    public static final int MSG_ORIENTATION_CHANGE = 2;
    
    
    public static enum Orien {
    	DEFAULT("DEFAULT",0),
    	TOP_UP("TOP_UP", 90),
    	RIGHT_UP("RIGHT_UP", 0),
    	BOTTOM_UP("BOTTOM_UP", 270),
    	LEFT_UP("LEFT_UP", 180);
    	String des = "";
    	int orientationDegree;
    	private Orien(String des, int degree) {
    		this.des = des;
    		this.orientationDegree = degree;
    	}
    }
    
    Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case MSG_SAVE_DONE:
				
				appendResultImage((Bitmap) msg.obj);
				
				mCamera.startPreview();
				break;
			case MSG_ORIENTATION_CHANGE:
				TextView text = (TextView) MainActivity.this.findViewById(R.id.orientation);
				if (currentOrien != null) {
					text.setText(currentOrien.des);
				}
				
				autoFocusWhenOrienChange();
				rotateView(R.id.cap, getRotateDegree(currentOrien));
				
				Toast t = Toast.makeText(MainActivity.this, "Screen rotated to " + currentOrien.des, Toast.LENGTH_SHORT);
				t.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
				t.show();
			}
		}
    	
    };
    
    private float getRotateDegree(Orien ori) {
    	switch(ori) {
    	case TOP_UP:
    		return 0;
    	case RIGHT_UP:
    		return 90;
    	case BOTTOM_UP:
    		return 180;
    	case LEFT_UP:
    		return -90;
    	}
    	
    	return 0;
    }
    
    private void rotateView(int id, float degree) {
    	
    		View targetView = findViewById(id);
    		
    		float startDegree = 0.0f;
    		Float lastDegree = (Float) targetView.getTag();
    		if (lastDegree != null) {
    			startDegree = lastDegree.floatValue();
    		}
    	
    	 	Animation an = new RotateAnimation(0.0f, degree, targetView.getWidth()/2, targetView.getHeight()/2);

    	    // Set the animation's parameters
    	    an.setDuration(3000);               // duration in ms
    	    an.setRepeatCount(0);                // -1 = infinite repeated
    	    an.setRepeatMode(Animation.REVERSE); // reverses each repeat
    	    an.setFillAfter(true);               // keep rotation after animation

    	    // Aply animation to image view
    	    targetView.startAnimation(an);//.setAnimation(an);
    	    targetView.setTag(new Float(degree));
    	    
    }
    
    private void appendResultImage(Bitmap bp) {
    	
    	if (bp == null) {
    		return;
    	}
    	
    	ViewGroup vp = (ViewGroup) this.findViewById(R.id.result_parent);
    	LayoutInflater layoutInf = LayoutInflater.from(MainActivity.this);
		View imageRoot = layoutInf.inflate(R.layout.single_image_layout, null);
		ImageView img = (ImageView) imageRoot.findViewById(R.id.result_image);
		img.setOnClickListener(deleteListener);
		
		TextView orienTxt = (TextView) imageRoot.findViewById(R.id.orientation_txt);
		
		img.setImageBitmap(/*cropBitmap(bp, currentOrien)*/bp);
		
		SensorEvent event = lastSensorEvent; //For debug, update the orientation indicator.
		if (event != null) {
			orienTxt.setText(currentOrien.des + this.valueOf(event.values));
		}
		
		try
		{
			vp.addView(imageRoot);
		}
		catch (Throwable t) {
			Log.d(TAG, "error when add image view " + imageRoot);
		}
    }
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//Take picture action.
		findViewById(R.id.cap).setOnClickListener(this);
		
		//Sensor to update current orientation.
		sensorMgr = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
		sensor = sensorMgr.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		
		deleteListener = new OnDeleteListener();
	}
	
	public static Camera getCameraInstance(){
	    Camera c = null;
	    try {
    		c = Camera.open(); // attempt to get a Camera instance, this will open the default facing camera if there is any.
    		isFrontCam = false;
	    }
	    catch (Exception e){
	        // Camera is not available (in use or does not exist)
	    	Log.d("CAM", "fail to open cam " + e.getMessage());
	    }
	    
	    if (c == null) { //For devices which only have front camera.
	    	try {
	    		c = Camera.open(0);
	    		CameraInfo info = new CameraInfo();
	    		Camera.getCameraInfo(0, info);
	    		isFrontCam = info.facing == CameraInfo.CAMERA_FACING_FRONT;
	    	}
	    	catch (Throwable t) {
	    		
	    	}
	    }
	    
	    return c; // returns null if camera is unavailable
	}

	@Override
	protected void onPause() {
		super.onPause();
		sensorMgr.unregisterListener(this);
		if (mCamera != null) {
            mPreview.setCamera(null);
            mCamera.release();
            mCamera = null;
        }
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		ViewGroup cameraP = (ViewGroup) this.findViewById(R.id.camera_parent);
		if (mPreview != null) {
			cameraP.removeView(mPreview);
		}
		mPreview = new CameraPreview(this);
		cameraP.addView(mPreview, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		
		mCamera = getCameraInstance();//Camera.open();
        mPreview.setCamera(mCamera);
        
        if (sensor != null) {
        	sensorMgr.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        
	}
	
	//An indicator to indicate if user.
	private void autoFocusWhenOrienChange() {
		findViewById(R.id.focus_done).setVisibility(View.GONE);
		mCamera.autoFocus(new Camera.AutoFocusCallback() {
			public void onAutoFocus(boolean success, Camera camera) {
				findViewById(R.id.focus_done).setVisibility(success ? View.VISIBLE : View.GONE);
			}
		});
	}
	
	
	private PictureCallback mPicture = new PictureCallback() {

	    @Override
	    public void onPictureTaken(byte[] data, Camera camera) {
	    	findViewById(R.id.cap).setEnabled(true);
	    	
	        File pictureFile = getOutputMediaFile();
	        if (pictureFile == null){
	            Log.d(TAG, "Error creating media file, check storage permissions: ");
	            return;
	        }

	        Bitmap bp = BitmapUtil.saveAndCreateBitmap(data, currentOrien.orientationDegree, MainActivity.this, pictureFile.getAbsolutePath(), isFrontCam);
	        
	        Message msg = handler.obtainMessage(MSG_SAVE_DONE, bp);
	        handler.sendMessage(msg);
	    }
	};
	
	private File getOutputMediaFile() {
//		String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/bx/";
//		File dirF = new File(dir);
//		dirF.mkdirs();
		
		return new File(Environment.getExternalStorageDirectory(), "bx_" + System.currentTimeMillis() + ".jpg");
	}

	@Override
	public void onClick(View v) {
		takePic();
		Log.d(TAG, "capture clicked.");
	}
	
	public void takePic() {
		
		mCamera.cancelAutoFocus();//Cancel last auto focus because we will do auto focus.
		mCamera.autoFocus(new AutoFocusCallback() {
			@Override
			public void onAutoFocus(boolean arg0, Camera arg1) {
//				if (arg0) {//Some device will never return "True"
					findViewById(R.id.cap).setEnabled(false);
					arg1.takePicture(null, null, mPicture);
//				}
			}
		});
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
			lastSensorEvent = sensorEvent;
			float pitch = sensorEvent.values[1];
			float roll = sensorEvent.values[2];
			
			Orien newOrien = updateOrien(pitch, roll);
			if (newOrien != null && currentOrien != newOrien) {
				Log.w(TAG, "orientation changed from " + currentOrien.des + " to " + newOrien.des);
				currentOrien = newOrien;
				handler.sendEmptyMessage(MSG_ORIENTATION_CHANGE);
			}
				
		}
	}
	
	private Orien updateOrien(float pitch, float roll) {
		if ((pitch < -45 && pitch > -135) || 
				(roll >= -15 && roll <= 15 && pitch >= -45 && pitch <= 0)) { //When roll is very small, most likely phone is top up.Do not consider bottom up case because no body will do that.
			return Orien.TOP_UP;
		}
		
		if (pitch > 45 && pitch < 135) {
			return Orien.BOTTOM_UP;
		}
		
		if (roll > 45) {
			return Orien.RIGHT_UP;
		}
		
		if (roll < -45 || 
				(roll > -45 && roll < 0 && (pitch > -15 && pitch < 15))) { //When pitch is very small and roll is negative.
			return Orien.LEFT_UP;
		}
		
		return Orien.DEFAULT;
	}
	
//	private Bitmap cropBitmap(Bitmap bp, Orien orientation) {
//		Matrix m = new Matrix();
//		m.setRotate(orientation.orientationDegree);
//		
//		int size = Math.min(bp.getWidth(), bp.getHeight())/2;
//		
//		return Bitmap.createBitmap(bp, size/2, size/2, size, size, m, true);
//	}
	
	private String valueOf(float[] values) {
		StringBuffer buf = new StringBuffer();
		buf.append("[");
		for (float f : values) {
			buf.append(((int)f)).append(",");
		}
		buf.append("]");
		
		return buf.toString();
	}
	
	class OnDeleteListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			ViewGroup vp = (ViewGroup) findViewById(R.id.result_parent);
			vp.removeView((View) v.getParent());
		}
		
	}
}

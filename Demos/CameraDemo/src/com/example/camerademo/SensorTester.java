package com.example.camerademo;

import java.util.List;

import com.example.camerademo.util.OrientationSensorDetector;
import com.example.camerademo.util.RotationDetector;
import com.example.camerademo.util.RotationDetector.Orien;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class SensorTester extends Activity implements OnClickListener, SensorEventListener {
	
	public static final String TAG	= "SENSOR_TEST";
	Orien currentOrien = Orien.DEFAULT;
	
	
	private SensorManager sensorMgr;
	private Sensor sensor;
	private Sensor sensorA;
	private Sensor sensorM;
	
    SensorEvent gravityEvent;
    SensorEvent magEvent;
    
    RotationDetector detector = new RotationDetector() {
		public Orien updateSensorEvent(SensorEvent sensorEvent) {
			return Orien.DEFAULT;
		}
	};
    
    public static final int MSG_UPDATE_ORIENTATION = 1;
    public static final int MSG_UPDATE_GRIVITY = 2;
    
    
    private Handler handler = new Handler() {
    	public void handleMessage(Message msg) {
    		switch (msg.what) {
    		case MSG_UPDATE_ORIENTATION:
    			TextView orienMsg = (TextView) findViewById(R.id.orientaion_label);
    			orienMsg.setText(currentOrien.des);
    			break;
    			
    		case MSG_UPDATE_GRIVITY:
    			String label  = (String) msg.obj;
    			TextView grivityV = (TextView) findViewById(R.id.grivity_msg);
    			grivityV.setText(label);
    			break;
    		}
    	}
    };
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.sensor_main);
		
		sensorMgr = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
		sensor = sensorMgr.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		if (sensor != null) {
			detector = new OrientationSensorDetector();
		}
		sensorA = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorM = sensorMgr.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		
		findViewById(R.id.refresh).setOnClickListener(this);
	}
	@Override
	protected void onPause() {
		super.onPause();
	}
	@Override
	protected void onResume() {
		super.onResume();
		
        if (sensor != null) {
        	sensorMgr.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        
        if (sensorA != null) {
        	sensorMgr.registerListener(this, sensorA, SensorManager.SENSOR_DELAY_NORMAL);
        }
        
        if (sensorM != null) {
        	sensorMgr.registerListener(this, sensorM, SensorManager.SENSOR_DELAY_NORMAL);
        }
		
	}
	
	private String getGrivityMsg() {
		if (gravityEvent != null && magEvent != null) {
			
			StringBuffer buf = new StringBuffer();
			buf.append("grivity:").append(valueOf(gravityEvent.values)).append("\n");
			buf.append("geoMagnetic:").append(valueOf(magEvent.values)).append("\n");
			
			float[] rMatrix = new float[9];
			float[] iMatrix = new float[9];
			
			boolean s = SensorManager.getRotationMatrix(rMatrix, iMatrix, gravityEvent.values, magEvent.values);
			if (s) {
				float[] rRotation = new float[3];
//				float[] iRotation = new float[3];
				double[] degreeMatrix = new double[3];
				for (int i=0; i<3; i++) {
					degreeMatrix[i] = Math.asin(rMatrix[i]);
				}
				SensorManager.getOrientation(rMatrix, rRotation);
				buf.append("rotation:").append(valueOf(rMatrix)).append("\n");
				buf.append("degree:").append(valueOf(degreeMatrix)).append("\n");
				
//				SensorManager.getOrientation(iMatrix, iRotation);
			}
			
			return buf.toString();
		}
		else
		{
			return "blank";
		}
	}
    
	public void onSensorChanged(SensorEvent sensorEvent) {
		if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
			
			Orien newOrien = detector.updateSensorEvent(sensorEvent);
			if (newOrien != null && currentOrien != newOrien) {
				currentOrien = newOrien;
				handler.sendEmptyMessage(MSG_UPDATE_ORIENTATION);
				
				if (gravityEvent != null && magEvent != null) {
					Message msg = handler.obtainMessage(MSG_UPDATE_GRIVITY, getGrivityMsg());
					handler.sendMessage(msg);
				}
			}
				
		}
		else
		{
			switch (sensorEvent.sensor.getType()) {
			case Sensor.TYPE_ACCELEROMETER:
				gravityEvent =sensorEvent;
				Log.w("ACCELEROMETER", valueOf(gravityEvent.values));
				break;
			case Sensor.TYPE_MAGNETIC_FIELD:
				magEvent = sensorEvent;
				break;
			}
		}
		
	}
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onClick(View v) {
		if (gravityEvent != null && magEvent != null) {
			
//			float[] rMatrix = new float[9];
//			float[] iMatrix = new float[9];
//			
//			boolean s = SensorManager.getRotationMatrix(rMatrix, iMatrix, gravityEvent.values, magEvent.values);
//			if (s) {
//				float[] rRotation = new float[3];
//				float[] iRotation = new float[3];
//				double[] degreeMatrix = new double[3];
//				for (int i=0; i<3; i++) {
//					degreeMatrix[i] = Math.asin(rMatrix[i]);
//				}
//				SensorManager.getOrientation(rMatrix, rRotation);
//				SensorManager.getOrientation(iMatrix, iRotation);
//				Log.w(TAG, "succed get rotation matrix data : " + valueOf(rMatrix) + "---" + valueOf(iMatrix));
//				Log.w(TAG, "the rotation data are " + valueOf(rRotation) + "++++++" + valueOf(iRotation));
//				Log.e("SPE","rotation " + degreeMatrix[0] + "?" + degreeMatrix[1] + "?" + degreeMatrix[2]);
//			}
//			else
//			{
//				Log.w(TAG, "fail to get rotation Matrix");
//			}
//			SensorManager.getOrientation(, values)
			Message msg = handler.obtainMessage(MSG_UPDATE_GRIVITY, getGrivityMsg());
			handler.sendMessage(msg);
		}
	}
    
	private String valueOf(float[] values) {
		StringBuffer buf = new StringBuffer();
		buf.append("[");
		for (float f : values) {
			buf.append(Math.rint(f)).append(",");
			
		}
		buf.append("]");
		
		return buf.toString();
	}
	
	private String valueOf(double[] values) {
		StringBuffer buf = new StringBuffer();
		buf.append("[");
		for (double f : values) {
			buf.append(Math.rint(f)).append(",");
		}
		buf.append("]");
		
		return buf.toString();
	}
    
}

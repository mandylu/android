package com.example.camerademo.util;

import android.hardware.SensorEvent;

public abstract class RotationDetector {

	public static enum Orien {
    	DEFAULT("DEFAULT",0),
    	TOP_UP("TOP_UP", 90),
    	RIGHT_UP("RIGHT_UP", 0),
    	BOTTOM_UP("BOTTOM_UP", 270),
    	LEFT_UP("LEFT_UP", 180);
    	public String des = "";
    	public int orientationDegree;
    	private Orien(String des, int degree) {
    		this.des = des;
    		this.orientationDegree = degree;
    	}
    }
	
	public abstract Orien updateSensorEvent(SensorEvent sensorEvent);
	
}

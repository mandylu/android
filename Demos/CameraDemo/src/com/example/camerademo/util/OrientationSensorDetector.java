package com.example.camerademo.util;

import android.hardware.SensorEvent;
import android.util.Log;

public class OrientationSensorDetector extends RotationDetector {

	@Override
	public Orien updateSensorEvent(SensorEvent sensorEvent) {
		float pitch = sensorEvent.values[1];
		float roll = sensorEvent.values[2];
		
		return updateOrien(pitch, roll);
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

}

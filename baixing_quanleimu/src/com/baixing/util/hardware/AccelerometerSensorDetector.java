package com.baixing.util.hardware;

import com.baixing.util.hardware.RotationDetector.Orien;

import android.hardware.SensorEvent;

public class AccelerometerSensorDetector extends RotationDetector {

	@Override
	public Orien updateSensorEvent(SensorEvent sensorEvent) {
		final float x = sensorEvent.values[0];
		final float y = sensorEvent.values[1];
		final float z = sensorEvent.values[2];
		
		if (Math.abs(x) > Math.abs(y)) {
			return x > 0 ? Orien.RIGHT_UP : Orien.LEFT_UP;
		} else {
			return y > 0 ? Orien.TOP_UP : Orien.BOTTOM_UP;
		}
		
//		return Orien.DEFAULT;
	}

}

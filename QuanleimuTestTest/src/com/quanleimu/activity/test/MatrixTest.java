package com.quanleimu.activity.test;

import android.graphics.Matrix;
import android.test.AndroidTestCase;

public class MatrixTest extends AndroidTestCase {

	public void testMatrix() {
		Matrix m1 = new Matrix();
		m1.setRotate(90);
		
		float[] values = new float[9];
		m1.getValues(values);
		
		System.out.println(values); //[0.0, -1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0]
		
		Matrix mirror = new Matrix();
		mirror.setValues(new float[] {-1, 0, 0, 0, 1, 0, 0, 0, 1});
		mirror.postConcat(m1);
		mirror.getValues(values);
		System.out.println(values);//[-0.0, -1.0, 0.0, -1.0, 0.0, 0.0, 0.0, 0.0, 1.0]
		
	}
	
}

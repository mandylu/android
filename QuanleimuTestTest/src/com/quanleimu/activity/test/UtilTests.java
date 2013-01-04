package com.quanleimu.activity.test;

import com.baixing.util.TextUtil;
import com.baixing.util.Util;

import android.test.AndroidTestCase;

public class UtilTests extends AndroidTestCase{

	public void testMd5Hash() {
		String toBeTest = "ABOASIDOAS:LDASL:DL:ASKD:LKALS:DKLASDJHASFJASHJDH";
		String result = "dd8fe64fd19d9a0ae183ffb365c4beb6";
		
		assertEquals(result, TextUtil.getMD5(toBeTest));
//		assertEquals(result, Util.MD5(toBeTest));
		
//		int c = 1000;
//		long start = System.currentTimeMillis();
//		while (c > 0) {
//			c--;
//			TextUtil.getMD5(toBeTest);
//		}
//		final long d1 = System.currentTimeMillis() - start;
//		
//		start = System.currentTimeMillis();
//		c = 1000;
//		while (c > 0) {
//			c--;
//			Util.MD5(toBeTest);
//		}
//		final long d2 = System.currentTimeMillis() - start;
//		
//		assertTrue(d2 > d1);
//		System.out.println(d2 + "   " + d1);
	}
	
}

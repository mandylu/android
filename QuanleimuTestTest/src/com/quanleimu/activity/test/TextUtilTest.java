package com.quanleimu.activity.test;

import android.test.AndroidTestCase;

import com.baixing.util.TextUtil;

public class TextUtilTest extends AndroidTestCase {

	public void testCheckNumSequence()
	{
		String target = "13512135857";
		assertTrue(target + "shoule be phone num", TextUtil.isNumberSequence(target));
		
		target = "13512135857  ";
		assertTrue(target + "shoule be phone num", TextUtil.isNumberSequence(target));
		
		target = "   13512135857  ";
		assertTrue(target + "shoule be phone num", TextUtil.isNumberSequence(target));

		target = "13512135857.0";
		assertFalse(target + "shoule not be phone num", TextUtil.isNumberSequence(target));
		
		target = "135121.35857";
		assertFalse(target + "shoule not be phone num", TextUtil.isNumberSequence(target));
		
		target = "13a512135857";
		assertFalse(target + "shoule not be phone num", TextUtil.isNumberSequence(target));
		
		target = "";
		assertFalse(target + "shoule not be phone num", TextUtil.isNumberSequence(target));
		
		target = "   ";
		assertFalse(target + "shoule not be phone num", TextUtil.isNumberSequence(target));
	}
}

package com.quanleimu.activity.test;

import com.quanleimu.util.AdViewStats;
import com.quanleimu.util.BXStats;

import android.test.AndroidTestCase;

public class BXStatusTest extends AndroidTestCase {

	public void setUp()
	{
		try {
			super.setUp();
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void testDescription()
	{
		BXStats s = new BXStats("a", 10);
		assertEquals("10", s.description());
	}
	
	public void testCount()
	{
		BXStats s = new BXStats("a", 10);
		assertEquals("a", s.getTypeName());
		assertEquals(10, s.getCount());
		
		s.increase(null);
		assertEquals(11, s.getCount());
	}
	
	public void testAdViewStat()
	{
		AdViewStats s = new AdViewStats("b", 10);
		assertEquals("b", s.getTypeName());
		assertEquals("", s.description());
		
		s.increase("1");
		assertEquals("1,", s.description());
		s.increase("4");
		assertEquals("1,4,", s.description());
		
	}
	
	
}

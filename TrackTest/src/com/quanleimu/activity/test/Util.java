package com.quanleimu.activity.test;

class Util{
	public static void Sleep(int millSec){
		try {
			Thread.sleep(millSec);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
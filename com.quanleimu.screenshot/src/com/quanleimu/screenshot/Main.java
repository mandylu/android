package com.quanleimu.screenshot;

public class Main {
	public static void main( String[] args )
    {
		System.out.println ("Hello World!");
		AndroidScreen screen = new AndroidScreen();
		try {
			screen.test();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
    }
}

package com.baixing.activity.test;

import java.util.Random;

public class RandomHan {
	private Random ran = new Random();
    private final static int delta = 0x9fa5 - 0x4e00 + 1;
     
    public char getRandomHan() {
        return (char)(0x4e00 + ran.nextInt(delta)); 
    }
    
    public String getRandomString(int len) {
    	String value = "";
    	for(int l = 0; l < len; l++) {
			value += this.getRandomHan();
		}
    	return value;
    }
}

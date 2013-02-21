package com.baixing.network.test.func;

public class ResultHolder<T> {
	enum STATE {
		UNKNOWN, SUCCED, FAIL
	}
	
	public STATE state = STATE.UNKNOWN;
	public T result;
}

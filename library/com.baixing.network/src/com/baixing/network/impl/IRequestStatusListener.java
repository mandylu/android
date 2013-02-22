package com.baixing.network.impl;

/**
 * 
 * @author liuchong
 *
 * @date 2013-2-6
 */
public interface IRequestStatusListener {
    /**
     * Notify network will begin to connect.
     * 
     */
    public void onConnectionStart();

    /**
     * notify receiving response data..
     * 
     * @param cursor how much bytes had been received.
     * @param total total bytes to receive.
     */
    public void onReceiveData(long cursor, long total);

    /**
     * Notify start to process data.
     * 
     */
    public void onProcessingData();

    /**
     * Notify request is canceled.
     * 
     */
    public void onCancel();

    /**
     * Notify finishing request and return with data.
     * 
     * @param response response data.
     */
    public void onRequestDone(Object response);
}

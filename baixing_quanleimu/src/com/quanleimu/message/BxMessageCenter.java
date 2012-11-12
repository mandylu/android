package com.quanleimu.message;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;


/**
 *@author liuchong
 *
 */
public class BxMessageCenter
{
    public static interface IBxNotification
    {
        public String getName();
        public Object getObejct();
        public Map getAdditionalInfo();
    }
    
    private static BxMessageCenter defaultCenter;
    private Map<String, Observable> observMapper;
    
    private BxMessageCenter()
    {
        observMapper = new Hashtable<String, Observable>();
    }
    
    public static BxMessageCenter defaultMessageCenter()
    {
        if (defaultCenter == null)
        {
            defaultCenter = new BxMessageCenter();
        }
        
        return defaultCenter;
    }
    
    public void registerObserver(Observer observer, String notificationName)
    {
        Observable observ = observMapper.get(notificationName);
        if (observ == null)
        {
            observ = new InnerObservable();
            observMapper.put(notificationName, observ);
        }
        
        observ.addObserver(observer);
    }
    
    public void removeObserver(Observer observer)
    {
        Iterator<Observable> list = observMapper.values().iterator();
        while (list.hasNext())
        {
            list.next().deleteObserver(observer);
        }
    }
    
    public void removeObserver(Observer observer, String notificationName)
    {
        Observable observ = observMapper.get(notificationName);
        if (observ != null)
        {
            observ.deleteObserver(observer);
        }
    }
    
    public void postNotification(String name, Object obj)
    {
        postNotification(name, obj, null);
    }
    
    public void postNotification(String name, Object obj, Map additionalInfo)
    {
    	postNotification(new DefaultNotification(name, obj, additionalInfo));
    }
    
    public void postNotification(IBxNotification notification)
    {
    	if (notification == null)
    	{
    		return;
    	}
    	
    	Observable observ = observMapper.get(notification.getName());
        if (observ != null)
        {
            observ.notifyObservers(notification);
        }
    }
    
    private class InnerObservable extends Observable
    {

        @Override
        public void notifyObservers()
        {
            setChanged();
            super.notifyObservers();
            clearChanged();
        }

        @Override
        public void notifyObservers(Object arg)
        {
            setChanged();
            super.notifyObservers(arg);
            clearChanged();
        }
        
    }
    
    
    private class DefaultNotification implements IBxNotification
    {
        private String notificationName;
        private Object obj;
        private Map additionalInfo; 
        
        public DefaultNotification(String name, Object obj, Map additonal)
        {
            this.notificationName = name;
            this.obj = obj;
            this.additionalInfo = additonal;
        }
        
        public String getName()
        {
            return notificationName;
        }

        @Override
        public Object getObejct()
        {
            return obj;
        }

        @Override
        public Map getAdditionalInfo()
        {
            return additionalInfo;
        }
        
    }
    
}

package com.quanleimu.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.quanleimu.view.BaseView;

import android.content.Context;
import android.os.Bundle;
public class ViewStack {	
	public class StackItem extends Object{
		public BaseView m_view = null;
		public Bundle m_bundle = null;
		public String m_baseViewVClassName = null;
		
		public StackItem(BaseView view){
			m_view = view;
			m_baseViewVClassName = view.getClass().getName();
		}
		
		public BaseView getBaseView(Context context){
			if(null == m_view && null != m_bundle){
				try {
					Class viewClass = Class.forName(m_baseViewVClassName);
					Class[] paramsClass = new Class[]{Context.class, Bundle.class};
					Constructor<BaseView> viewContructor = viewClass.getConstructor(paramsClass);
					Object[] paramsObject = new Object[]{context, m_bundle};
					m_view = viewContructor.newInstance(paramsObject);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}catch( NoSuchMethodException e){
					
				}catch(InstantiationException e){
					
				}catch(IllegalAccessException e){
					
				}catch(InvocationTargetException e){
					
				}				
			}
			
			if(null != m_view)
				m_view.onResume();
			
			return m_view;
		}
		
		public void releaseBaseView(){
			if(null != m_view){
				if(null == m_bundle){
					m_bundle = m_view.extracBundle();
				}
				
				m_view = null;
			}
		}
	};
	
	
	public List<StackItem> m_viewList = new ArrayList<StackItem>();
	public Context m_context = null;
	
	public ViewStack(Context context){
		m_context = context;
	}
	
	public BaseView peer(){
		if(m_viewList.size() > 0){
			StackItem item = m_viewList.get(m_viewList.size() - 1);
			if(null != item){
				return item.getBaseView(m_context);
			}
		}
		
		return null;		
	}
	
	public String peerClassName(){
		if(m_viewList.size() > 0){
			StackItem item = m_viewList.get(m_viewList.size() - 1);
			if(null != item){
				return item.m_baseViewVClassName;
			}
		}
		
		return null;		
	}	
	
	public BaseView pop(){
		if(m_viewList.size() > 0){
			StackItem item = m_viewList.remove(m_viewList.size() - 1);
			if(null != item){
				return item.getBaseView(m_context);
			}
		}
		
		return null;
	}
	
	public void push(BaseView view){
		m_viewList.add(new StackItem(view));
	}
	
	public void clear(){
		m_viewList.clear();
	}
	

	public void recycle(){
		for(StackItem item : m_viewList){
			item.releaseBaseView();
		}
	}
	
	public int size(){
		return m_viewList.size();
	}
}

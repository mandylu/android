package com.quanleimu.widget;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;

public class TextEditInputConnected extends EditText{

	public interface OnActionListener{
		public void onActionFired();
	}
	
	protected OnActionListener actionListener = null;
	
	public TextEditInputConnected(Context context, AttributeSet attrs) {
		super(context, attrs);	
	}
	
	public class SearchInputConnector extends BaseInputConnection{
		
		public SearchInputConnector(View targetView, boolean fullEditor) {			 
			super(targetView, fullEditor);
			}
		
		@Override
		public boolean performEditorAction(int editorAction){
			if(/*1 == editorAction && */actionListener != null){
				actionListener.onActionFired();
			}
			
			return super.performEditorAction(editorAction);
		}
	}
	
	@Override
	public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
	 
		return new SearchInputConnector(this, false);	 
	}	
	
	@Override
	public boolean onCheckIsTextEditor(){
		return true;
	}
	
	public void setOnActionListener(OnActionListener listener){
		actionListener = listener;
	}

}
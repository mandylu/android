package com.quanleimu.broadcast.push;

import org.json.JSONObject;

import android.content.Context;

/**
 * 
 * @author liuchong
 *
 */
public class PushDispatcher {
	private Context context;
	private PushHandler[] handlers;// = new ArrayList<PushHandler>();
	public PushDispatcher(Context context)
	{
		this.context = context;
		handlers = new PushHandler[] {
				new ChatMessageHandler(context),
				new BXInfoHandler(context)
		};
	}
	
	public void dispatch(String msgJson)
	{

			
		try
		{
			JSONObject jsonObj = new JSONObject(msgJson);
			if (jsonObj == null || !jsonObj.has("type"))
			{
				return;
			}
			
			final String type = jsonObj.getString("type");
			for (PushHandler h : handlers)
			{
				if (h.acceptMessage(type))
				{
					try {
						h.processMessage(msgJson);
					}
					catch(Throwable t)
					{
						//Ignor, handler do not affect each other.
					}
				}
			}
		}
		catch(Throwable t)
		{
			
		}
	}
	
}

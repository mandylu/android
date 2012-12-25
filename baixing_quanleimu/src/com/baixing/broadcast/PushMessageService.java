//liuchong@baixing.com
package com.baixing.broadcast;


import java.lang.ref.WeakReference;
import java.util.Observable;
import java.util.Observer;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import com.baixing.broadcast.push.PushDispatcher;
import com.baixing.data.GlobalDataManager;
import com.baixing.entity.UserBean;
import com.baixing.message.BxMessageCenter;
import com.baixing.message.IBxNotificationNames;
import com.baixing.message.BxMessageCenter.IBxNotification;
import com.baixing.util.Communication;
import com.baixing.util.ParameterHolder;
import com.baixing.util.TraceUtil;
import com.baixing.util.Util;
import android.util.Log;

/**
 * 
 * @author liuchong
 *
 */
public class PushMessageService extends Service implements Observer
{
	public static final String SERVICE_THREAD_NAME = "quanleimu.app.push.service";
	
    // The following actions are documented and registered in our manifest
    public final static String ACTION_CONNECT = "com.quanleimu.action.CONNECT";
    public final static String ACTION_DISCONNECT = "com.quanleimu.action.DISCONNECT";

    public final static String ACTION_BROADCAST_STATUS = "com.quanleimu.action.BROADCAST_STATUS";
    public final static String ACTION_NETWORK_CHANGED = "com.quanleimu.action.NETWORK_CHANGED";

    // A list of intent actions that the XmppManager broadcasts.
    public static final String ACTION_XMPP_MESSAGE_RECEIVED = "com.quanleimu.action.XMPP.MESSAGE_RECEIVED";
    public static final String ACTION_XMPP_CONNECTION_CHANGED = "com.quanleimu.action.XMPP.CONNECTION_CHANGED";
    
    
    
	public static final String TAG = "PushMessageService";
	
    private long mHandlerThreadId;
	private static XMPPManager sXmppMgr;
    private static BroadcastReceiver sXmppConChangedReceiver;
    private static volatile Looper sServiceLooper;
    public static boolean IsRunning = false;
    private static volatile ServiceHandler sServiceHandler;
//    private static ChatMessageManager chatManager;
    private PushDispatcher pushHandler;
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            onHandleIntent((Intent) msg.obj, msg.arg1);
        }
    }
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	public void onCreate() 
	{
		TraceUtil.trace(TAG, "service create start");
		super.onCreate();
		
		BxMessageCenter.defaultMessageCenter().registerObserver(this, IBxNotificationNames.NOTIFICATION_USER_CREATE);
		BxMessageCenter.defaultMessageCenter().registerObserver(this, IBxNotificationNames.NOTIFICATION_LOGIN);
		BxMessageCenter.defaultMessageCenter().registerObserver(this, IBxNotificationNames.NOTIFICATION_LOGOUT);
		
		if(GlobalDataManager.context == null){
			GlobalDataManager.context = new WeakReference<Context>(this);
		}
		HandlerThread thread = new HandlerThread(SERVICE_THREAD_NAME);
		thread.start();
        sServiceLooper = thread.getLooper();
		sServiceHandler = new ServiceHandler(sServiceLooper);
        mHandlerThreadId = thread.getId();
//        chatManager = new ChatMessageManager(this);
        pushHandler = new PushDispatcher(this);
        
        IsRunning = true;
        TraceUtil.trace(TAG, "service create end");
	}
	
    public void onDestroy() {
    	TraceUtil.trace(TAG, "destory the service--begin");
    	
    	BxMessageCenter.defaultMessageCenter().removeObserver(this);//.removeObserver(this, IBxNotificationNames.NOTIFICATION_USER_CREATE);
    	
        IsRunning = false;
        
//        // If the _xmppManager is non-null, then our service was "started" (as
//        // opposed to simply "created" - so tell the user it has stopped.
        if (sXmppMgr != null) {
            // do some cleanup
            unregisterReceiver(sXmppConChangedReceiver);
            sXmppConChangedReceiver = null;
            TraceUtil.trace(TAG, "disconnect onDestory()");
            sXmppMgr.xmppRequestStateChange(XMPPManager.DISCONNECTED);
            sXmppMgr = null;
        }
        
        pushHandler = null;
        
        sServiceLooper.quit();
        
        super.onDestroy();
        TraceUtil.trace(TAG, "destory the service--end");
    }
    
    public int onStartCommand(Intent intent, int flags, int startId) {
    	TraceUtil.trace(TAG, "service on start command. intent is " + intent);
        this.startForeground(0, null);
    	
        if (intent == null) {
            // The application has been killed by Android and
            // we try to restart the connection
            // this null intent behavior is only for SDK < 9
        	TraceUtil.trace(TAG, "onStartCommand() call PushMessageService.ACTION_CONNECT ");
        	startService(new Intent(PushMessageService.ACTION_CONNECT));
//            if (Build.VERSION.SDK_INT < 9) {
//            } else {
////                Log.w("onStartCommand() null intent with Gingerbread or higher");
//            }
            
            registeDevice(null, null);
            return START_STICKY;
        }
//        Log.i("onStartCommand(): Intent " + intent.getAction());
        // A special case for the 'broadcast status' intent - we avoid setting
        // up the _xmppMgr etc
        if (intent.getAction().equals(ACTION_BROADCAST_STATUS)) {
            // A request to broadcast our current status even if _xmpp is null.
            int state = getConnectionStatus();
//            XMPPManager.broadcastStatus(this, state, state);
            // A real action request
        } else {
            // redirect the intent to the service handler thread
            sendToServiceHandler(startId, intent);
        }
        
        if (intent.getBooleanExtra("updateToken", false))
        {
        	registeDevice(null, null); //
        }
        TraceUtil.trace(TAG, "onStartCommand finish.");
        return START_STICKY;
    }
    
    public int getConnectionStatus() {
        return sXmppMgr == null ? XMPPManager.DISCONNECTED : sXmppMgr.getConnectionStatus();
    }
	
    protected static Looper getServiceLooper() {
        return sServiceLooper;
    }
    
    public static boolean sendToServiceHandler(int i, Intent intent) {
        if (sServiceHandler != null) {
            Message msg = sServiceHandler.obtainMessage();
            msg.arg1 = i;
            msg.obj = intent;
            sServiceHandler.sendMessage(msg);
            return true;
        } else {
        	TraceUtil.trace(TAG, "sendToServiceHandler() handler is not created yet.");
            return false;
        }
    }

    public static boolean sendToServiceHandler(Intent intent) {
        return sendToServiceHandler(0, intent);
    }
    
    private void setupXmppManagerAndCommands() {
        sXmppConChangedReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                intent.setClass(PushMessageService.this, PushMessageService.class);
                onConnectionStatusChanged(intent.getIntExtra("old_state", 0), intent.getIntExtra("new_state", 0));
                startService(intent);
            }
        };
        IntentFilter intentFilter = new IntentFilter(ACTION_XMPP_CONNECTION_CHANGED);
        registerReceiver(sXmppConChangedReceiver, intentFilter);

        sXmppMgr = XMPPManager.getInstance(this);
    }
    
    private void onConnectionStatusChanged(int oldStatus, int status) {
    }
    
    protected void onHandleIntent(final Intent intent, int id) {
        // ensure XMPP manager is setup (but not yet connected)
        if (sXmppMgr == null)
            setupXmppManagerAndCommands();

        // Set Disconnected state by force to manage pending tasks
        // This is not actively used any more
        if (intent.getBooleanExtra("force", false) && intent.getBooleanExtra("disconnect", false)) {
            // request to disconnect.
        	TraceUtil.trace(TAG, "onHandleIntent() force to disconnect");
            sXmppMgr.xmppRequestStateChange(XMPPManager.DISCONNECTED);
        }

        if (Thread.currentThread().getId() != mHandlerThreadId) {
        	TraceUtil.trace(TAG, "onHandleIntent illegal state on handle server intent.");
            throw new IllegalThreadStateException();
        }
        // We need to handle XMPP state changes which happened "externally" -
        // eg, due to a connection error, or running out of retries, or a retry
        // handler actually succeeding etc.
        int initialState = getConnectionStatus();
        updateListenersToCurrentState(initialState);

        String action = intent.getAction();

        if (action.equals(ACTION_CONNECT)) {
            if (intent.getBooleanExtra("disconnect", false)) {
                // Request to disconnect. We will stop the service if
                // we are in "DISCONNECTED" state at the end of the method
            	TraceUtil.trace(TAG, "onHandleIntent disconnect when do connect action");
                sXmppMgr.xmppRequestStateChange(XMPPManager.DISCONNECTED);
            } else {
                // A simple 'connect' request.
                sXmppMgr.xmppRequestStateChange(XMPPManager.CONNECTED);
            }
        } else if (action.equals(ACTION_DISCONNECT)) {
        	TraceUtil.trace(TAG, "onHandleIntent disconnect when do disconnect action");
            sXmppMgr.xmppRequestStateChange(XMPPManager.DISCONNECTED);
        } 
        else if (action.equals(ACTION_XMPP_MESSAGE_RECEIVED)) {
        	String msg = intent.getStringExtra("message");
        	pushHandler.dispatch(msg);
        }  else if (action.equals(ACTION_NETWORK_CHANGED)) {
            boolean available = intent.getBooleanExtra("available", true);
            boolean failover = intent.getBooleanExtra("failover", false);
            // We are in a waiting state and have a network - try to connect.
            if (available && (initialState == XMPPManager.WAITING_TO_CONNECT || initialState == XMPPManager.WAITING_FOR_NETWORK)) {
                sXmppMgr.xmppRequestStateChange(XMPPManager.CONNECTED);
            } else if (!available && !failover && initialState == XMPPManager.CONNECTED) {
                // We are connected but the network has gone down - disconnect
                // and go into WAITING state so we auto-connect when we get a future
                // notification that a network is available.
                sXmppMgr.xmppRequestStateChange(XMPPManager.WAITING_FOR_NETWORK);
            }
        } else if (!action.equals(ACTION_XMPP_CONNECTION_CHANGED)) {
        }

        // stop the service if we are disconnected (but stopping the service
        // doesn't mean the process is terminated - onStart can still happen.)
        if (getConnectionStatus() == XMPPManager.DISCONNECTED) {
        	TraceUtil.trace(TAG, "onHandleIntent will stop sercice ??????");
            if (stopSelfResult(id)) {
            } else {
            }
        }
    }
    
    private int updateListenersToCurrentState(int currentState) {
        return currentState;
    }
	
    
    private void registeDevice(BroadcastReceiver receiver, UserBean userBean)
    {
		RegisterCommandListener cmdListener = new RegisterCommandListener();
		ParameterHolder parameters = new ParameterHolder();
		String userId = userBean == null ? GlobalDataManager.getInstance().getAccountManager().getMyId(this) : userBean.getId();
		if (userId != null) {
			parameters.addParameter("userId", userId);
		}
		
		if (receiver != null)
		{
			this.unregisterReceiver(receiver);
		}
    	
		Communication.executeAsyncGetTask("tokenupdate", parameters, cmdListener);
		
    }
    
    class RegisterCommandListener implements Communication.CommandListener
    {
    	
		public void onServerResponse(String serverMessage) {
			Log.d(TAG, "updatetoken succed " + serverMessage);
		}

		@Override
		public void onException(Exception ex) {
			final BroadcastReceiver receiver = new BroadcastReceiver() {
				public void onReceive(Context arg0, Intent arg1) {
					registeDevice(this, null);
				}
			};
			PushMessageService.this.registerReceiver(receiver, new IntentFilter(CommonIntentAction.ACTION_BROADCAST_XMPP_CONNECTED));
		}
    	
    }
	@Override
	public void update(Observable observable, Object data) {
		if (data instanceof IBxNotification)
		{
			IBxNotification note = (IBxNotification) data;
			if (IBxNotificationNames.NOTIFICATION_USER_CREATE.equals(note.getName())
					|| IBxNotificationNames.NOTIFICATION_LOGIN.equals(note.getName())
					|| IBxNotificationNames.NOTIFICATION_LOGOUT.equals(note.getName())) {
				registeDevice(null, (UserBean) note.getObject());
			}
		}
	}
    
}


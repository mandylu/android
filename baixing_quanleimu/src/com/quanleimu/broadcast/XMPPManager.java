package com.quanleimu.broadcast;

import java.util.Iterator;
import java.util.Random;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.ConfigureProviderManager;
import org.jivesoftware.smackx.OfflineMessageManager;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.util.ViewUtil;

public class XMPPManager {
	
	public static final String TAG = "XmppManager";
	
    public static final int DISCON_TIMEOUT = 1000 * 10; // 10s
    
    public static boolean DEBUG_MODE = false;
    

	public static final int DISCONNECTED = 1;
    // A "transient" state - will only be CONNECTING *during* a call to start()
    public static final int CONNECTING = 2;
    public static final int CONNECTED = 3;
    // A "transient" state - will only be DISCONNECTING *during* a call to stop()
    public static final int DISCONNECTING = 4;
    // This state means we are waiting for a retry attempt etc.
    // mostly because a connection went down
    public static final int WAITING_TO_CONNECT = 5;
    // We are waiting for a valid data connection
    public static final int WAITING_FOR_NETWORK = 6;
    
	
	public static final String SERVER = "message.baixing.com";//192.168.4.145//"192.168.8.56";
	private static XMPPManager manager;
	
	private XMPPConnection mConnection;
	private Context context;
	
    // Indicates the current state of the service (disconnected/connecting/connected)
    private int mStatus = DISCONNECTED;
    
    private Handler mReconnectHandler;
    private int mCurrentRetryCount = 0;
    private PkgListener mPacketListener;
    private ConnectionListener mConnectionListener = null;
    
    private Runnable mReconnectRunnable = new Runnable() {
        public void run() {
            final Intent i = new Intent(PushMessageService.ACTION_CONNECT);
            if (context != null)
            {
            	context.startService(i);
            }
        }
    };
	
	
	private XMPPManager(Context context)
	{
		this.context = context;
		
		SmackAndroid.init(context);
        
        mReconnectHandler= new Handler(PushMessageService.getServiceLooper());
	}
	
	public static final XMPPManager getInstance(Context context)
	{
		if (manager == null)
		{
			manager = new XMPPManager(context);
		}
		
		return manager;
	}
	
    /** 
     * This method *requests* a state change - what state things actually
     * wind up in is impossible to know (eg, a request to connect may wind up
     * with a state of CONNECTED, DISCONNECTED or WAITING_TO_CONNECT...
     */
    protected void xmppRequestStateChange(int newState) {
        int currentState = getConnectionStatus();
//        Log.i("xmppRequestStateChange " + statusAsString(currentState) + " => " + statusAsString(newState));
        switch (newState) {
        case XMPPManager.CONNECTED:
            if (!isXmppConnected()) {
                cleanupConnection();
                start(XMPPManager.CONNECTED);
            }
            break;
        case XMPPManager.DISCONNECTED:
            stop();
            break;
        case XMPPManager.WAITING_TO_CONNECT:
            cleanupConnection();
            start(XMPPManager.WAITING_TO_CONNECT);
            break;
        case XMPPManager.WAITING_FOR_NETWORK:
            cleanupConnection();
            start(XMPPManager.WAITING_FOR_NETWORK);
            break;
        default:
        	Log.d(TAG, "xmppRequestStateChange() invalid state to switch to: " + statusAsString(newState));
        }
    }
    
    private void start(int initialState) {
        switch (initialState) {
            case CONNECTED:
                initConnection();
                break;
            case WAITING_TO_CONNECT:
            case WAITING_FOR_NETWORK:
                updateStatus(initialState);
                break;
            default:
                throw new IllegalStateException("xmppMgr start() Invalid State: " + initialState);
        }
    }
    
    /**
     * calls cleanupConnection and 
     * sets _status to DISCONNECTED
     */
    private void stop() {
        updateStatus(DISCONNECTING);
        cleanupConnection();
        updateStatus(DISCONNECTED);
        mConnection = null;
    }
    
	private void maybeStartReconnect() {
		int timeout;
		updateStatus(WAITING_TO_CONNECT);
		cleanupConnection();
		mCurrentRetryCount += 1;
		if (mCurrentRetryCount < 20) {
			// a simple linear-backoff strategy.
			timeout = 5000 * mCurrentRetryCount;
		} else {
			// every 5 min
			timeout = 1000 * 60 * 5;
		}
		mReconnectHandler.postDelayed(mReconnectRunnable, timeout);
	}
	
    private void cleanupConnection() {
        mReconnectHandler.removeCallbacks(mReconnectRunnable);

        if (mConnection != null) {
            if (mPacketListener != null) {
                mConnection.removePacketListener(mPacketListener);
            }
            if (mConnectionListener != null) {
                mConnection.removeConnectionListener(mConnectionListener);
            }
//            if (mPresencePacketListener != null) {
//                mConnection.removePacketListener(mPresencePacketListener);
//            }
            
            if (mConnection.isConnected()) {
                // Try to disconnect
                Thread t = new Thread(new Runnable() {
                    public void run() {
                        try {
                            mConnection.disconnect();
                        } catch (Exception e) {}
                    }
                }, "xmpp-disconnector");
                // we don't want this thread to hold up process shutdown so mark as daemon.
                t.setDaemon(true);
                t.start();

                try {
                    t.join(DISCON_TIMEOUT);
                } catch (InterruptedException e) {
                    mConnection = null;
                }
            }
        }
        mPacketListener = null; 
        mConnectionListener = null;
//        mPresencePacketListener = null;
    }
    
    /**
     * Initializes the XMPP connection
     * 
     * 1. Creates a new XMPPConnection object if necessary
     * 2. Connects the XMPPConnection
     * 3. Authenticates the user with the server
     * 
     * Calls maybeStartReconnect() if something went wrong
     * 
     */
    private void initConnection() {
        XMPPConnection connection;

        // assert we are only ever called from one thread 
        assert (!Thread.currentThread().getName().equals(PushMessageService.SERVICE_THREAD_NAME));
//        
        // everything is ready for a connection attempt
        updateStatus(CONNECTING);

        // create a new connection if the connection is obsolete or if the
        // old connection is still active
        if (mConnection == null) {
            
            try {
                connection = createNewConnection();
            } catch (Exception e) {
                // connection failure
                Log.e(TAG, "Exception creating new XMPP Connection");
                maybeStartReconnect();
                return;
            }
//            SettingsManager.connectionSettingsObsolete = false;
            if (!connectAndAuth(connection)) {
                // connection failure
                return;
            }                  
//            sNewConnectionCount++;
        } else {
            // reuse the old connection settings
            connection = mConnection;
            // we reuse the xmpp connection so only connect() is needed
            if (!connectAndAuth(connection)) {
                // connection failure
                return;
            }
//            sReusedConnectionCount++;
        }
        // this code is only executed if we have an connection established
        onConnectionEstablished(connection);
    }
    
    
    private void onConnectionEstablished(XMPPConnection connection) {
        mConnection = connection;               
        mConnectionListener = new ConnectionListener() {
            @Override
            public void connectionClosed() {
                xmppRequestStateChange(getConnectionStatus());
            }

            @Override
            public void connectionClosedOnError(Exception e) {
                maybeStartReconnect();
            }

            @Override
            public void reconnectingIn(int arg0) {
                throw new IllegalStateException("Reconnection Manager is running");
            }

            @Override
            public void reconnectionFailed(Exception arg0) {
                throw new IllegalStateException("Reconnection Manager is running");
            }

            @Override
            public void reconnectionSuccessful() {
                throw new IllegalStateException("Reconnection Manager is running");
            }
        };
        mConnection.addConnectionListener(mConnectionListener);            

        try {
//            informListeners(mConnection);
        	handleOfflineMessages(mConnection, context);
            mPacketListener = new PkgListener();
            mConnection.addPacketListener(mPacketListener, mPacketListener);

        } catch (Exception e) {
            maybeStartReconnect();
            return;
        }

        mCurrentRetryCount = 0;
        updateStatus(CONNECTED);
    }
    
    /**
     * Tries to fully establish the given XMPPConnection
     * Calls maybeStartReconnect() or stop() in an error case
     * 
     * @param connection
     * @return true if we are connected and authenticated, false otherwise
     */
    private boolean connectAndAuth(XMPPConnection connection) {
        try {
            connection.connect();
        } catch (Exception e) {
            e.printStackTrace();
            maybeStartReconnect();
            mConnection = null;
            return false;
        }          
        
        // if we reuse the connection and the auth was done with the connect()
        if (connection.isAuthenticated()) {
        	Log.w(TAG, "in connect status");
            return true;
        }
        
        try {
			final String userId = QuanleimuApplication.getDeviceUdid(context);
        	checkAndRegister(connection, userId);
        	
            Random generator = new Random();
            int resource_int = generator.nextInt();
            connection.login(userId, "123456", 
                             "Smack_" + Integer.toString(resource_int));
            Log.d(TAG, "login succed");
        } catch (Exception e) {
        	Log.e(TAG, "authentication failed.");
            cleanupConnection();
            return false;
        }
        return true;
    }   
    
    
    
    /**
     * Parses the current preferences and returns an new unconnected
     * XMPPConnection 
     * @return
     * @throws XMPPException 
     */
    private static XMPPConnection createNewConnection(/*SettingsManager settings*/) throws XMPPException {
    	ConfigureProviderManager.configureProviderManager();
    	ConnectionConfiguration conf = new ConnectionConfiguration(SERVER, 5222);
    	XMPPConnection.DEBUG_ENABLED = true;
    	conf.setServiceName("localhost");
    	conf.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
    	conf.setSASLAuthenticationEnabled(false);
        
        // disable the built-in ReconnectionManager
        // since we handle this
        conf.setReconnectionAllowed(false);
//        conf.setSendPresence(false);
        
        return new XMPPConnection(conf);
    }
    
	public boolean checkAndRegister(XMPPConnection currentConn, String userId){
	    if (!currentConn.isAuthenticated()){
	        AccountManager acManager = currentConn.getAccountManager();
	        try {
	            acManager.createAccount(userId, "123456");
	        } catch (XMPPException e) {
	            Log.e(TAG, "create account failed." + e.getLocalizedMessage());
	            return false;
	        }
	        return true;
	    }else{
	        return true;
	    }
	}

    /** returns the current connection state */
    public int getConnectionStatus() {
        return mStatus;
    }
    
    public boolean isConnected() {
        return isXmppConnected() && mStatus == CONNECTED;
    }
    
    /**
     * updates the connection status
     * and calls broadCastStatus()
     * 
     * @param status
     */
    private void updateStatus(int status) {
        if (status != mStatus) {
            // ensure _status is set before broadcast, just in-case
            // a receiver happens to wind up querying the state on
            // delivery.
            int old = mStatus;
            mStatus = status;  
            Log.d(TAG, "broadcasting state transition from " + statusAsString(old) + " to " + statusAsString(status) + " via Intent " + PushMessageService.ACTION_XMPP_CONNECTION_CHANGED);
//            broadcastStatus(context, old, status);
            if (DEBUG_MODE)
            {
            	ViewUtil.putOrUpdateNotification(context, NotificationIds.NOTIFICATION_XMPP_CONNECTION_STATUS, "XMPPStatus", statusAsString(status) + ":" +  QuanleimuApplication.udid, null, true);
            }
        }
    }
    
    public boolean isXmppConnected() {
        return mConnection != null && mConnection.isConnected();
    }

	class PkgListener implements PacketListener, PacketFilter
	{
		
		@Override
		public void processPacket(Packet msg) 
		{
			if (msg instanceof Message)
			{
				Message message = (Message) msg;
				final Intent i = new Intent(PushMessageService.ACTION_XMPP_MESSAGE_RECEIVED, null, context, PushMessageService.class);
		        i.putExtra("message", filterXml(message.getBody()));
		        i.putExtra("from", message.getFrom());
		        PushMessageService.sendToServiceHandler(i);
			}
			
		}

		@Override
		public boolean accept(Packet arg0) {
			return true;
		}
		
	}
	
	   public static String statusAsString(int state) {
	        String res = "??";
	        switch(state) {
	        case DISCONNECTED:
	            res = "Disconnected";
	            break;
	        case CONNECTING:
	            res = "Connecting";
	            break;
	        case CONNECTED:
	            res = "Connected";
	            break;
	        case DISCONNECTING:
	            res = "Disconnecting";
	            break;
	        case WAITING_TO_CONNECT:
	            res = "Waiting to connect";
	            break;
	        case WAITING_FOR_NETWORK:
	            res = "Waiting for network";
	            break;
	        }
	        return res;                        
	    }

	    public static void handleOfflineMessages(XMPPConnection connection, Context ctx) throws XMPPException {
	        OfflineMessageManager offlineMessageManager = new OfflineMessageManager(connection);

	        if (!offlineMessageManager.supportsFlexibleRetrieval())
	            return;

	        Iterator<Message> i = offlineMessageManager.getMessages();
	        while (i.hasNext()) {
	            Message msg = i.next();
	            String fullJid = msg.getFrom();
	            String messageBody = msg.getBody();
	            if (messageBody != null) {
	                final Intent intent = new Intent(PushMessageService.ACTION_XMPP_MESSAGE_RECEIVED, null, ctx, PushMessageService.class);
	                intent.putExtra("message", filterXml(messageBody));
	                intent.putExtra("from", fullJid);
	                PushMessageService.sendToServiceHandler(intent);
	            }
	        }
	        offlineMessageManager.deleteMessages();
	    }
	
	    static String filterXml(String message)
	    {
	    	String[] from = new String[] {"&amp;", "&lt;", "&gt;", "&quot;", "&apos;"};
	    	String[] to	= new String[] {  "&", 		"<", 	">",	"\"",	  "'"};
	    	String result = message;
	    	for (int i=0; i<from.length; i++)
	    	{
	    		result = result.replaceAll(from[i], to[i]);
	    	}
	    	
	    	return result;
	    }
}

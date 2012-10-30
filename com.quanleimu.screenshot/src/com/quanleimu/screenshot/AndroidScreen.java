package com.quanleimu.screenshot;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import java.util.Date;
import java.text.SimpleDateFormat; 

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.RawImage;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AndroidScreen {
    private final Logger logger = LoggerFactory.getLogger(AndroDemon.class);
    public static final int CONNECTING_PAUSE = 200;
	private String sdkPath;
    private AndroidDebugBridge bridge;
    private IDevice device;
    private final Map<String, IDevice> devices = new HashMap<String, IDevice>();

    public void test() throws Exception {
    	System.out.println("xxx");
    	initBridge();
    	sleep(5 * 1000);
    	while (true) {
            
            IDevice[] devices = bridge.getDevices();
            for (IDevice d : devices) {
            	if (d != null) {
            		setDevice(d);
                	BXOutputReceiver rev = new BXOutputReceiver();
                	d.executeShellCommand("ls /mnt/sdcard/Athrun/", rev);
                	//logger.debug(d.toString() + "recieved:" + rev.revString);
                	//String errLockFile = d.toString() + "_err.lock";
                	String errLockFile = "bxtestcase_err.lock";
                	if (rev.revString.indexOf(errLockFile) > 0) {
                		Image img = fetchScreen();
                		if (img != null) {
                			saveImage(img);
                			logger.debug("image saved");
                		}
                		d.executeShellCommand("rm /mnt/sdcard/Athrun/" + errLockFile, rev);
                		sleep(1 * 1000);
                		Date nowTime=new Date();
                		SimpleDateFormat time=new SimpleDateFormat("yyyyMMdd");
                    	BXOutputReceiver log = new BXOutputReceiver();
                    	log.logFile = "logcat_test_" + d.toString() + "_" + time.format(nowTime) + ".log";
                		d.executeShellCommand("logcat -d", log);
                		sleep(1 * 1000);
                		d.executeShellCommand("logcat -c", rev);
                	}
                } else {
                    sleep(CONNECTING_PAUSE);
                }
            }
            //break;
        }
    }
    
	private void initBridge() {
		sdkPath = "/Users/wyhw/android-sdk-macosx";
		
		if (!AndroidSdkHelper.validatePath(sdkPath)) {
            logger.error("Android SDK is not properly configured.");
            return;
        }
		System.out.println("initBridge");
        AndroidDebugBridge.init(false);
        System.out.println("create bridge");
        logger.trace("create bridge");
        String adbPath = sdkPath + File.separator + "platform-tools" + File.separator + "adb";
        bridge = AndroidDebugBridge.createBridge(adbPath, true);
        logger.trace("bridge is created");

        AndroidDebugBridge.addDeviceChangeListener(new AndroidDebugBridge.IDeviceChangeListener() {

            @Override
            public void deviceConnected(IDevice device) {
                logger.info("deviceConnected: {}", device + "." + device.toString());
                addDevice(device);
                devices.put(device.toString(), device);
            }

            @Override
            public void deviceDisconnected(IDevice device) {
                logger.trace("deviceDisconnected: {}", device);
                removeDeviceByName(device.toString());
                //removeDevice(device);
            }

            @Override
            public void deviceChanged(IDevice device, int changeMask) {
                logger.trace("deviceChanged: {} - {}", device, changeMask);
            }

        });
    }
	
	private Image fetchScreen() {
        final IDevice d = device;
        Image image = null;
        if (d != null) {
            try {
                RawImage screenshot = device.getScreenshot();

                if (screenshot != null) {
                    image = renderImage(screenshot);
                }

            } catch (Exception ex) {
                logger.error("", ex);
            } 
        }
        return image;
    }
	
	private Image renderImage(RawImage screenshot) {
        BufferedImage image = new BufferedImage(screenshot.width, screenshot.height, BufferedImage.TYPE_INT_RGB);

        int index = 0;
        int indexInc = screenshot.bpp >> 3;

        int value;

        for (int y = 0; y < screenshot.height; y++) {
            for (int x = 0; x < screenshot.width; x++, index += indexInc) {
                value = screenshot.getARGB(index);
                image.setRGB(x, y, value);
            }
        }

        return image;
    }
	
	void saveImage(Image img) {
		Date nowTime=new Date();
		SimpleDateFormat time=new SimpleDateFormat("yyyyMMddHHmmss"); 
		String filePath = "logs/" + time.format(nowTime) + "_" + device.toString() + String.valueOf((int)(Math.random() * 10000)) + ".png";
        File target = new java.io.File(filePath);
        logger.debug("img path:" + target.getAbsolutePath());
        try {
            ImageIO.write((RenderedImage) img, "PNG", target);
            logger.info("status.saved", target.getName());
        } catch (IOException ex) {
            logger.error("Cannot save image to file.", ex);
            logger.info("error.save.image", target.getPath());
        }
    }
	
	private void addDevice(IDevice device) {
        
        if (this.device == null) {
           setDevice(device);
        }
    }

    private void removeDevice(IDevice device) {
        if (this.device.equals(device)) {
            IDevice[] devices = bridge.getDevices();
            if (devices.length == 0) {
                setDevice(null);
            } else {
                setDevice(devices[0]);
            }
        }
    }

    private void setDevice(IDevice device) {
        this.device = device;
    }
    
    public void connectTo(String str) {
        if (StringUtils.isBlank(str)) {
            return ;
        }
        IDevice[] devices = bridge.getDevices();
        for (IDevice d : devices) {
            if (str.equals(d.toString())) {
                setDevice(d);
            }
        }
    }
    
    void removeDeviceByName(final String deviceStr) {
    	if (devices.containsKey(deviceStr)) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        IDevice d = devices.get(deviceStr);
                        devices.remove(deviceStr);
                        removeDevice(d);
                    }
                });
            } catch (Exception ignore) {
            }
        }
    }
    
    private void sleep(int t) {
        try {
            Thread.sleep(t);
        } catch (InterruptedException ignore) {
        }
    }
}

package com.quanleimu.screenshot;

import java.io.BufferedWriter;
import java.io.FileWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedWriter;
import java.io.FileWriter;

import com.android.ddmlib.MultiLineReceiver;

public class BXOutputReceiver extends MultiLineReceiver {
    //private final Logger logger = LoggerFactory.getLogger(BXOutputReceiver.class);
	public String logFile = "";

    public boolean isCancelled = false;
    public String revString = "";

    public BXOutputReceiver() {
        super();

        setTrimLine(false);
    }

    @Override
    public void processNewLines(String[] lines) {
    	FileWriter fw = null;
		BufferedWriter out = null;
		try {
	    	if (logFile.length() > 0) {
	        	fw = new FileWriter(logFile, true);
	    		out = new BufferedWriter(fw);
	    	}
	        if (isCancelled == false) {
	                for(String line : lines) {
	                	if (out != null) {
	                		out.write(line);
	                	} else {
	                		revString += line;
	                	}
	                	//logger.debug("recieved:" + revString);
	                }
	        }
			if (out != null) out.close();
		} catch (Exception ex) {
		}
    }

    public boolean isCancelled() {
        return isCancelled;
    }
}


package com.quanleimu.activity.test;

import java.io.BufferedReader;
import java.io.File;  
import java.io.FileInputStream;
import java.io.FileOutputStream;  
import java.io.InputStreamReader;
import java.io.PrintWriter;  
import java.io.StringWriter;  
import java.util.Calendar;  
import java.util.TimeZone;  

import android.os.Environment;
  
public class BXLog {  
    public static final int CLOSE = 10;  
    public static final int OPEN = 0;  
  
    public static final int VERBOSE = 2;  
    public static final int DEBUG = 3;  
    public static final int INFO = 4;  
    public static final int WARN = 5;  
    public static final int ERROR = 6;  
    public static final int ASSERT = 7;  
    public static final int SPECIAL = 100;  
    public static final int CRASH = 1000;  
  
    public static int logfile_switch = CLOSE;  
    public static int logcat_switch = CLOSE;  
  
    private static String log_path = null;  // 日志文件路径   
    private static String log_default_tag = "Baixing";  
  
    public static boolean isShowV() {  
        return logfile_switch <= VERBOSE || logcat_switch <= VERBOSE;  
    }  
  
    public static boolean isShowD() {  
        return logfile_switch <= DEBUG || logcat_switch <= DEBUG;  
    }  
  
    public static boolean isShowI() {  
        return logfile_switch <= INFO || logcat_switch <= INFO;  
    }  
  
    public static boolean isShowW() {  
        return logfile_switch <= WARN || logcat_switch <= WARN;  
    }  
  
    public static boolean isShowE() {  
        return logfile_switch <= ERROR || logcat_switch <= ERROR;  
    }  
  
    public static int v(String msg) {  
        return v(log_default_tag, msg);  
    }  
    public static int v(String tag, String msg) {  
        return println(VERBOSE, tag, msg);  
    }  
    public static int v(String tag, byte[] buffer) {  
        return printByteToFile(VERBOSE, tag, buffer, 0, buffer.length);  
    }  
    public static int v(String tag, byte[] buffer, int offset, int count) {  
        return printByteToFile(VERBOSE, tag, buffer, offset, count);  
    }  
    public static int v(String tag, String msg, Throwable tr) {  
        return println(VERBOSE, tag, msg + '\n' + getStackTraceString(tr));  
    }  
      
    public static int d(String msg) {  
        return d(log_default_tag, msg);  
    }  
    public static int d(String tag, String msg) {  
        return println(DEBUG, tag, msg);  
    }  
    public static int d(String tag, byte[] buffer) {  
        return printByteToFile(DEBUG, tag, buffer, 0, buffer.length);  
    }  
    public static int d(String tag, byte[] buffer, int offset, int count) {  
        return printByteToFile(DEBUG, tag, buffer, offset, count);  
    }  
    public static int d(String tag, String msg, Throwable tr) {  
        return println(DEBUG, tag, msg + '\n' + getStackTraceString(tr));  
    }  
      
    public static int i(String msg) {  
        return i(log_default_tag, msg);  
    }  
    public static int i(String tag, String msg) {  
        return println(INFO, tag, msg);  
    }  
    public static int i(String tag, byte[] buffer) {  
        return printByteToFile(INFO, tag, buffer, 0, buffer.length);  
    }  
    public static int i(String tag, byte[] buffer, int offset, int count) {  
        return printByteToFile(INFO, tag, buffer, offset, count);  
    }  
    public static int i(String tag, String msg, Throwable tr) {  
        return println(INFO, tag, msg + '\n' + getStackTraceString(tr));  
    }  
      
    public static int w(String msg) {  
        return w(log_default_tag, msg);  
    }  
    public static int w(String tag, String msg) {  
        return println(WARN, tag, msg);  
    }  
    public static int w(String tag, byte[] buffer) {  
        return printByteToFile(WARN, tag, buffer, 0, buffer.length);  
    }  
    public static int w(String tag, byte[] buffer, int offset, int count) {  
        return printByteToFile(WARN, tag, buffer, offset, count);  
    }  
    public static int w(String tag, String msg, Throwable tr) {  
        return println(WARN, tag, msg + '\n' + getStackTraceString(tr));  
    }  
    public static int w(String tag, Throwable tr) {  
        return println(WARN, tag, getStackTraceString(tr));  
    }  
      
    public static int e(String msg) {  
        return e(log_default_tag, msg);  
    }  
    public static int e(String tag, String msg) {  
        return println(ERROR, tag, msg);  
    }  
    public static int e(String tag, byte[] buffer) {  
        return printByteToFile(ERROR, tag, buffer, 0, buffer.length);  
    }  
    public static int e(String tag, byte[] buffer, int offset, int count) {  
        return printByteToFile(ERROR, tag, buffer, offset, count);  
    }  
    public static int e(String tag, String msg, Throwable tr) {  
        int r = println(ERROR, tag, msg + '\n' + getStackTraceString(tr));  
        return r;  
    }
    
    public static int x(String msg) {
    	return x(msg, true);
    }
    public static int x(String msg, boolean append) {
    	int ret = 0;  
        String logpath = getLogPath();   
        if (!checkLogPath(logpath)) {  
            return ret;
        }
        if (msg == null) {  
            msg = "[null]";  
        }
        msg += "\n";
        byte[] buffer = msg.getBytes(); 
        String fileName = String.format("%s/baixing.trc", logpath);  
  
        FileOutputStream fo = null;  
        try {  
            File file = new File(fileName);
            fo = new FileOutputStream(file, append); 
            ret = buffer.length;
            fo.write(buffer, 0, ret); 
        } catch (Throwable tr) {  
            ret = 0;  
        } finally {  
            if (fo != null) {  
                try {  
                    fo.close();  
                } catch (Throwable tr) {  
                }  
            }  
        }  
        return ret;
    }
    public static String xr() {
    	String logpath = getLogPath();   
    	if (!checkLogPath(logpath)) {  
    		return "";
    	}
    	String fileName = String.format("%s/baixing.trc", logpath);
    	String ret = "";
    	FileInputStream fi = null;
    	InputStreamReader isr = null;
    	BufferedReader br = null;
    	File file = null;
        String linex = "";
    	try {  
            file = new File(fileName);
            if (!file.exists()) return "";
            fi = new FileInputStream(fileName);
            isr = new InputStreamReader(fi, "UTF-8");
            br = new BufferedReader(isr);
            while (true) {
            	String line = br.readLine();
            	if (line == null) break;
            	linex += line + "\n";
            }
        } catch (Throwable tr) {  
        } finally {
        	if (br != null) {
        		try {  
        			br.close(); 
                } catch (Throwable tr) {  
                } 
        	}
            if (isr != null) try {  
            	isr.close();
            } catch (Throwable tr) {  
            } 
            if (fi != null) {  
                try {  
                    fi.close();  
                } catch (Throwable tr) {  
                }  
            }
        }
    	String[] lines = linex.split("\n");
    	int l = lines.length;
    	int maxLine = 1000;
    	for(int i = (l < maxLine) ? 0 : l - maxLine; i < l; i++) {
    		ret += lines[i] + "\n";
    		
    	}
    	x(ret, false);
    	return ret;
    }
    public static int crash(String tag, Throwable tr){  
        if(tr == null) return -1;  
        return println(CRASH, tag, tr.getMessage() + '\n' + getStackTraceString(tr));  
    }  
    public static int crash(String tag, String msg){  
        return println(CRASH, tag, msg);  
    }  
    public static String getLogPath() {
    	if (log_path == null) {
    		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
    			log_path = Environment.getExternalStorageDirectory() + "/Athrun/";
    		} else log_path = "/sdcard/";
    	}
        return log_path;  
    }
  
    private static boolean checkLogPath(String newPath) {  
        if (newPath == null) {  
            return false;  
        }  
        boolean ret = false;  
        try {  
            File logPath = new File(newPath);  
            if (!logPath.exists()) {  
                logPath.mkdirs();  
            }  
            if (logPath.isDirectory()) {  
                ret = true;  
            } else {  
                ret = false;  
            }  
        } catch (Throwable e) {  
            ret = false;  
        }  
        return ret;  
    }  
  
    public static boolean setLogPath(String newPath) {  
        boolean ret = checkLogPath(newPath);  
        if (ret) {  
            log_path = newPath;  
        }  
        return ret;  
    }  
  
    public static String getStackTraceString(Throwable tr) {  
        if (tr == null) {  
            return "";  
        }  
        StringWriter sw = new StringWriter();  
        PrintWriter pw = new PrintWriter(sw);  
        tr.printStackTrace(pw);  
        return sw.toString();  
    }  
  
    private static int printToFile(int priority, String tag, String type,  
            byte[] buffer, int offset, int count) {  
        int ret = 0;  
        String logpath = getLogPath();  
        if(priority == CRASH){  
            type = "crash";  
        }  
        if (priority < logfile_switch || !checkLogPath(logpath)) {  
            if(priority == CRASH){  
                //logpath = "/sdcard/";  
                type = "crash";  
                if(!checkLogPath(logpath)){  
                    return ret;  
                }  
            }else{  
                return ret;  
            }  
        }  
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+08:00"));  
        int year = cal.get(Calendar.YEAR);  
        int month = cal.get(Calendar.MONTH) + 1;  
        int day = cal.get(Calendar.DAY_OF_MONTH);  
        int hour = cal.get(Calendar.HOUR_OF_DAY);  
        int minute = cal.get(Calendar.MINUTE);  
        int second = cal.get(Calendar.SECOND);  
        int millisecond = cal.get(Calendar.MILLISECOND);  
  
        String timeString = String.format("%d-%d-%d %d:%d:%d.%d", year, month,  
                day, hour, minute, second, millisecond);  
        String headString = String.format("\r\n%s\t(%d)\ttag:%s\tdata:",  
                timeString, priority, tag);  
        byte[] headBuffer = headString.getBytes();  
  
        String fileName = String.format("%s/BXLog%d%02d%02d.%s", logpath,  
                year, month, day, type);  
  
        FileOutputStream fo = null;  
        try {  
            File file = new File(fileName);  
            fo = new FileOutputStream(file, true);  
            fo.write(headBuffer);  
            fo.write(buffer, offset, count);  
            ret = headBuffer.length + count;  
        } catch (Throwable tr) {  
            ret = 0;  
        } finally {  
            if (fo != null) {  
                try {  
                    fo.close();  
                } catch (Throwable tr) {  
                }  
            }  
        }  
        return ret;  
    }  
  
    private static int printByteToFile(int priority, String tag, byte[] buffer,  
            int offset, int count) {  
        if (buffer == null || offset < 0 || buffer.length < count)  
            return 0;  
        return printToFile(priority, tag, "dat", buffer, offset, count);  
    }  
  
    public static int printMsgToFile(int priority, String tag, String msg) {  
        if (msg == null) {  
            msg = "[null]";  
        }  
        byte[] buffer = msg.getBytes();  
        return printToFile(priority, tag, "log", buffer, 0, buffer.length);  
    }  
  
    private static int println(int priority, String tag, String msg) {  
        int ret = 0;  
        if (priority >= logcat_switch) {  
            //ret += Log.println(priority, tag, msg);   //android需取消注释  
        }  
        if (priority >= logfile_switch) {  
            ret += printMsgToFile(priority, tag, msg);  
        }  
        return ret;  
    }  
} 

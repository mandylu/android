package com.quanleimu.util;

import java.lang.Thread.UncaughtExceptionHandler;  
import java.util.Properties;  
  
import android.content.Context;  
import android.os.Looper;  
import android.widget.Toast;  
  
/** 
25. *  
26. *  
27. * UncaughtExceptionHandler���߳�δ�����쳣����������������δ�����쳣�ġ�  
28. *                           �����������δ�����쳣Ĭ�������������ǿ�йرնԻ��� 
29. *                           ʵ�ָýӿڲ�ע��Ϊ�����е�Ĭ��δ�����쳣����  
30. *                           ����δ�����쳣����ʱ���Ϳ�����Щ�쳣������� 
31. *                           ���磺�ռ��쳣��Ϣ�����ʹ��󱨸� �ȡ� 
32. *  
33. * UncaughtException������,��������Uncaught�쳣��ʱ��,�ɸ������ӹܳ���,����¼���ʹ��󱨸�. 
34. */  
public class CrashHandler implements UncaughtExceptionHandler {  
    /** Debug Log Tag */  
    public static final String TAG = "CrashHandler";  
    /** �Ƿ�����־���, ��Debug״̬�¿���, ��Release״̬�¹ر�������������� */  
    public static final boolean DEBUG = true;  
    /** CrashHandlerʵ�� */  
    private static CrashHandler INSTANCE;  
    /** �����Context���� */  
    private Context mContext;  
    /** ϵͳĬ�ϵ�UncaughtException������ */  
    private Thread.UncaughtExceptionHandler mDefaultHandler;  
      
    /** ʹ��Properties�������豸����Ϣ�ʹ����ջ��Ϣ */  
    public Properties mDeviceCrashInfo = new Properties();  
    public static final String VERSION_NAME = "versionName";  
    public static final String VERSION_CODE = "versionCode";  
    public static final String STACK_TRACE = "STACK_TRACE";  
    /** ���󱨸��ļ�����չ�� */  
    public static final String CRASH_REPORTER_EXTENSION = ".cr";  
      
    /** ��ֻ֤��һ��CrashHandlerʵ�� */  
    private CrashHandler() {  
    }  
  
    /** ��ȡCrashHandlerʵ�� ,����ģʽ */  
    public static CrashHandler getInstance() {  
        if (INSTANCE == null)  
            INSTANCE = new CrashHandler();  
        return INSTANCE;  
    }  
      
    /** 
67.     * ��ʼ��,ע��Context����, ��ȡϵͳĬ�ϵ�UncaughtException������, ���ø�CrashHandlerΪ�����Ĭ�ϴ����� 
68.     *  
69.     * @param ctx 
70.     */  
    public void init(Context ctx) {  
        mContext = ctx;  
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();  
        Thread.setDefaultUncaughtExceptionHandler(this);  
    }  
      
    /** 
78.     * ��UncaughtException����ʱ��ת��ú��������� 
79.     */  
    @Override  
    public void uncaughtException(Thread thread, Throwable ex) {  
        if (!handleException(ex) && mDefaultHandler != null) {  
            // ����û�û�д�������ϵͳĬ�ϵ��쳣������������  
            mDefaultHandler.uncaughtException(thread, ex);  
        } else {  
            // Sleepһ���������  
            // �����߳�ֹͣһ����Ϊ����ʾToast��Ϣ���û���Ȼ��Kill����  
//            try {  
//                Thread.sleep(3000);  
//            } catch (InterruptedException e) {  
//                Log.e(TAG, "Error : ", e);  
//            }  
            android.os.Process.killProcess(android.os.Process.myPid());  
            System.exit(10);  
        }  
    }  
  
    /** 
99.     * �Զ��������,�ռ�������Ϣ ���ʹ��󱨸�Ȳ������ڴ����. �����߿��Ը���Լ���������Զ����쳣�����߼� 
100.     *  
101.     * @param ex 
102.     * @return true:������˸��쳣��Ϣ;���򷵻�false 
103.     */  
    private boolean handleException(Throwable ex) {  
        if (ex == null) {  
            return true;  
        }  
        final String msg = ex.getLocalizedMessage();  
        // ʹ��Toast����ʾ�쳣��Ϣ  
        new Thread() {  
            @Override  
            public void run() {  
                // Toast ��ʾ��Ҫ������һ���̵߳���Ϣ������  
                Looper.prepare();  
                Toast.makeText(mContext, "���������:" + msg, Toast.LENGTH_LONG).show();  
                Looper.loop();  
            }  
        }.start();  
//        // �ռ��豸��Ϣ  
//        collectCrashDeviceInfo(mContext);  
//        // ������󱨸��ļ�  
//        String crashFileName = saveCrashInfoToFile(ex);  
//        // ���ʹ��󱨸浽������  
//        sendCrashReportsToServer(mContext);  
        return true;  
    }  
  
//    /** 
//129.     * �ռ�����������豸��Ϣ 
//130.     *  
//131.     * @param ctx 
//132.     */  
//    public void collectCrashDeviceInfo(Context ctx) {  
//        try {  
//            // Class for retrieving various kinds of information related to the  
//            // application packages that are currently installed on the device.  
//            // You can find this class through getPackageManager().  
//            PackageManager pm = ctx.getPackageManager();  
//            // getPackageInfo(String packageName, int flags)  
//            // Retrieve overall information about an application package that is installed on the system.  
//            // public static final int GET_ACTIVITIES  
//            // Since: API Level 1 PackageInfo flag: return information about activities in the package in activities.  
//            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);  
//            if (pi != null) {  
//                // public String versionName The version name of this package,  
//                // as specified by the <manifest> tag's versionName attribute.  
//                mDeviceCrashInfo.put(VERSION_NAME, pi.versionName == null ? "not set" : pi.versionName);  
//                // public int versionCode The version number of this package,   
//                // as specified by the <manifest> tag's versionCode attribute.  
//                mDeviceCrashInfo.put(VERSION_CODE, pi.versionCode);  
//            }  
//        } catch (NameNotFoundException e) {  
//            Log.e(TAG, "Error while collect package info", e);  
//        }  
//        // ʹ�÷������ռ��豸��Ϣ.��Build���а�����豸��Ϣ,  
//        // ����: ϵͳ�汾��,�豸����� �Ȱ�����Գ����������Ϣ  
//        // ���� Field �����һ�����飬��Щ����ӳ�� Class �������ʾ�����ӿ��������������ֶ�  
//        Field[] fields = Build.class.getDeclaredFields();  
//        for (Field field : fields) {  
//            try {  
//                // setAccessible(boolean flag)  
//                // ���˶���� accessible ��־����Ϊָʾ�Ĳ���ֵ��  
//                // ͨ������Accessible����Ϊtrue,���ܶ�˽�б������з��ʣ���Ȼ��õ�һ��IllegalAccessException���쳣  
//                field.setAccessible(true);  
//                mDeviceCrashInfo.put(field.getName(), field.get(null));  
//                if (DEBUG) {  
//                   Log.d(TAG, field.getName() + " : " + field.get(null));  
//                }  
//            } catch (Exception e) {  
//                Log.e(TAG, "Error while collect crash info", e);  
//            }  
//        }  
//    }  
      
//    /** 
//176.     * ���������Ϣ���ļ��� 
//177.     *  
//178.     * @param ex 
//179.     * @return 
//180.     */  
//    private String saveCrashInfoToFile(Throwable ex) {  
//        Writer info = new StringWriter();  
//        PrintWriter printWriter = new PrintWriter(info);  
//        // printStackTrace(PrintWriter s)  
//        // ���� throwable ����׷�������ָ���� PrintWriter  
//        ex.printStackTrace(printWriter);  
//187.  
//188.        // getCause() ���ش� throwable �� cause����� cause �����ڻ�δ֪���򷵻� null��  
//189.        Throwable cause = ex.getCause();  
//190.        while (cause != null) {  
//191.            cause.printStackTrace(printWriter);  
//192.            cause = cause.getCause();  
//193.        }  
//194.  
//195.        // toString() ���ַ����ʽ���ظû�����ĵ�ǰֵ��  
//196.        String result = info.toString();  
//197.        printWriter.close();  
//198.        mDeviceCrashInfo.put(STACK_TRACE, result);  
//199.  
//200.        try {  
//201.            long timestamp = System.currentTimeMillis();  
//202.            String fileName = "crash-" + timestamp + CRASH_REPORTER_EXTENSION;  
//203.            // �����ļ�  
//204.            FileOutputStream trace = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);  
//205.            mDeviceCrashInfo.store(trace, "");  
//206.            trace.flush();  
//207.            trace.close();  
//208.            return fileName;  
//209.        } catch (Exception e) {  
//210.            Log.e(TAG, "an error occured while writing report file...", e);  
//211.        }  
//212.        return null;  
//213.    }  
      
//    /** 
//216.     * �Ѵ��󱨸淢�͸������,���²���ĺ���ǰû���͵�. 
//217.     *  
//218.     * @param ctx 
//219.     */  
//220.    private void sendCrashReportsToServer(Context ctx) {  
//221.        String[] crFiles = getCrashReportFiles(ctx);  
//222.        if (crFiles != null && crFiles.length > 0) {  
//223.            TreeSet<String> sortedFiles = new TreeSet<String>();  
//224.            sortedFiles.addAll(Arrays.asList(crFiles));  
//225.  
//226.            for (String fileName : sortedFiles) {  
//227.                File cr = new File(ctx.getFilesDir(), fileName);  
//228.                postReport(cr);  
//229.                cr.delete();// ɾ���ѷ��͵ı���  
//230.            }  
//231.        }  
//232.    }  
//233.  
//234.    /** 
//235.     * ��ȡ���󱨸��ļ��� 
//236.     *  
//237.     * @param ctx 
//238.     * @return 
//239.     */  
//240.    private String[] getCrashReportFiles(Context ctx) {  
//241.        File filesDir = ctx.getFilesDir();  
//242.        // ʵ��FilenameFilter�ӿڵ���ʵ������ڹ������ļ���  
//243.        FilenameFilter filter = new FilenameFilter() {  
//244.            // accept(File dir, String name)  
//245.            // ����ָ���ļ��Ƿ�Ӧ�ð���ĳһ�ļ��б��С�  
//246.            public boolean accept(File dir, String name) {  
//247.                return name.endsWith(CRASH_REPORTER_EXTENSION);  
//248.            }  
//249.        };  
//250.        // list(FilenameFilter filter)  
//251.        // ����һ���ַ����飬��Щ�ַ�ָ���˳���·�����ʾ��Ŀ¼������ָ�����������ļ���Ŀ¼  
//252.        return filesDir.list(filter);  
//253.    }  
//254.  
//255.    private void postReport(File file) {  
//256.        // TODO ʹ��HTTP Post ���ʹ��󱨸浽������  
//257.        // ���ﲻ������,�����߿��Ը��OPhoneSDN�ϵ������������  
//258.        // �̳����ύ���󱨸�  
//259.    }  
//260.  
//261.    /** 
//262.     * �ڳ�������ʱ��, ���Ե��øú�����������ǰû�з��͵ı��� 
//263.     */  
//264.    public void sendPreviousReportsToServer() {  
//265.        sendCrashReportsToServer(mContext);  
//266.    }  
}  

package com.quanleimu.screenshot;

import java.awt.Image;
import java.util.List;
import java.util.prefs.Preferences;

import org.apache.commons.lang.StringUtils;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.TaskEvent;
import org.jdesktop.application.TaskListener.Adapter;

public class ShotRunner extends Application implements PreferencesNames{
	private final Preferences p = Preferences.userNodeForPackage(ShotRunner.class);
	private final AndroDemon demon = new AndroDemon(this);
	
	// State
    private ImageEx lastImage;
    @Override
    protected void startup() {
        getContext().getResourceManager().setApplicationBundleNames(null);

    }

    @Override
    protected void initialize(String[] args) {
        super.initialize(args);
    }

    @Override
    protected void ready() {
        String sdkPath = p.get(PREF_ANDROID_SDK_PATH, null);
        if (!AndroidSdkHelper.validatePath(sdkPath)) {
            do {
                sdkPath = "";//todo
                if (StringUtils.isBlank(sdkPath)) exit();
                if (!AndroidSdkHelper.validatePath(sdkPath))
                    showErrorMessage("error.sdk");
            } while (!AndroidSdkHelper.validatePath(sdkPath));
            p.put(PREF_ANDROID_SDK_PATH, sdkPath);
        }
        getContext().getTaskService().execute(demon);
        
        demon.addTaskListener(new Adapter<Void, ImageEx>() {
            @Override
            public void process(TaskEvent<List<ImageEx>> event) {
                List<ImageEx> value = event.getValue();

                if (!value.isEmpty()) {
                    final ImageEx img = value.get(0);

                    lastImage = img;
                } else {
                    //showImage(generateDummyImage());
                }
            }

        });
    }

    @Override
    protected void shutdown() {
        super.shutdown();

    }


    void showErrorMessage(String messageKey, Object... args) {
        ResourceMap resourceMap = getContext().getResourceMap();
        String message = resourceMap.getString(messageKey, args);
        String errorTitle = resourceMap.getString("error.title");
    }

    void showMessage(String messageKey, Object... args) {
        ResourceMap resourceMap = getContext().getResourceMap();
        String message = resourceMap.getString(messageKey, args);
        String errorTitle = resourceMap.getString("info.title");

    }
}

package com.example.camerademo;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Build.VERSION;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

/** A basic Camera preview class */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;

    public CameraPreview(Context context) {
        super(context);
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
    
    public void setCamera(Camera camera) {
        mCamera = camera;
        if (mCamera != null) {
            this.forceLayout();
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d("CAMPRE", "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
          // preview surface does not exist
          return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
          // ignore: tried to stop a non-existent preview
        }
        
        // set preview size and make any resize, rotate or
        // reformatting changes here
        
        final boolean isNewDevice = VERSION.SDK_INT > 8;
        // start preview with new settings
        try {
        	mCamera.setDisplayOrientation(90); //FIXME: this is business related.
//        	adjustOrientation();
            mCamera.setPreviewDisplay(mHolder);
            adjustSize(isNewDevice? h : w, isNewDevice ? w : h);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d("CAMPRE", "Error starting camera preview: " + e.getMessage());
        }
    }
    
    private void adjustSize(int w, int h) {
    	Parameters params = mCamera.getParameters();
    	List<Size> sL = params.getSupportedPreviewSizes();
    	Size op = getOptimalPreviewSize(sL, w, h);
    	params.setPreviewSize(op.width, op.height);
    	mCamera.setParameters(params);
    }
    
    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
    
    private void adjustOrientation() {
    	Display disp = ((WindowManager)this.getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    	switch (disp.getOrientation()) {
    	case Surface.ROTATION_0:
    		mCamera.setDisplayOrientation(90);
    		break;
    	case Surface.ROTATION_270:
    		mCamera.setDisplayOrientation(180);
    		break;
    	case Surface.ROTATION_90:
    	case Surface.ROTATION_180:
    		break;
    	}
    }
}

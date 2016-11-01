package com.bees4honey.vinscanner;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.lang.ref.WeakReference;
import java.util.List;


/**
 * A Fragment which controls device camera and scanner native library. Fragment captures
 * camera output and pass it to native library to detect VIN codes.
 * ScannerFragment may both work in portrait and landscape orientation.
 *
 * To get returned code from ScannerFragment you need to implement {@link ScannerListener} interface
 * and pass implementation to fragment via {@link #setListener(ScannerListener)} method.
 * If {@link ScannerListener} is not set then fragment does nothing with the scanned code.
 *
 * Fragment scans both for 1D and 2D vin codes. For 1D vin codes two separate modes are supported:
 * vertical scanning and horizontal scanning. By default horizontal mode is used. Two switch between modes
 * use {@link #setScanVertically(boolean)} method.
 *
 */
@SuppressWarnings("deprecation")
public class ScannerFragment extends Fragment {
    private static final int DECODE = 2131099648;
    private static final long DECODE_DELAY_MSECS = 100;

    private static final int AUTOFOCUS_TIMEOUT = 1500;

    private static final String TAG = ScannerFragment.class.getCanonicalName();

    private SurfaceView surfaceView;
    private FrameLayout surfaceContainer;
    private Camera camera;
    private int camOrientation;
    private SurfaceCamCallback callback;
    private Handler handler;
    private OrientationEventListener orientationListener = null;

    private boolean scanVertically;
    private boolean flashOn;    //shows if camera flash is on or off

    private boolean isPreviewing;

    private final Runnable autoFocusRunnable;
    private long lastAutofocusTime;

    private ScannerListener listener;

    /**
     * Set fragment's listener.
     * @param listener an implementation of {@link ScannerListener} interface.
     */
    public void setListener(ScannerListener listener) {
        this.listener = listener;
    }

    /**
     * Get ScannerFragment listener.
     * @return returns current ScannerFragment. If listener was never set for fragment then null is returned.
     */
    public ScannerListener getListener() {
        return listener;
    }

    public ScannerFragment() {
        super();

        scanVertically = false;
        flashOn = false;
        handler = new ScannerHandler(new WeakReference<ScannerFragment>(this));
        camOrientation = 0;

        lastAutofocusTime = java.lang.System.currentTimeMillis();

        /*
         * Setting camera focus mode to Camera.Parameters.FOCUS_MODE_AUTO may actually be not enough for fast and reliable scanning
         * If device camera would be forced to autofocus frequently enough it may provide way better results on many devices.
         */
        autoFocusRunnable = new Runnable() {
            @Override
            public void run() {

                long currentTime = java.lang.System.currentTimeMillis();

                handler.postDelayed(autoFocusRunnable, AUTOFOCUS_TIMEOUT / 2);

                if (camera == null || !isPreviewing ||
                        (currentTime - lastAutofocusTime < AUTOFOCUS_TIMEOUT)) {
                    return;
                }

                lastAutofocusTime = currentTime;

                camera.autoFocus(new Camera.AutoFocusCallback() {

                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if (!success) {
                            Log.d(TAG, "onAutoFocus failed");
                        }
                    }
                });
            }
        };
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String packageName = inflater.getContext().getApplicationInfo().packageName;
        Resources r = inflater.getContext().getApplicationContext().getResources();

        int resourceId = r.getIdentifier("scanner_fragment", "layout", packageName);
        View v = inflater.inflate(resourceId, container, false);
        surfaceView = new SurfaceView(inflater.getContext());

        resourceId = r.getIdentifier("surface_container", "id", packageName);
        surfaceContainer = (FrameLayout) v.findViewById(resourceId);

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        surfaceView = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (surfaceView != null) {
            this.camera = getCameraInstance();
            if (camera != null) {
                callback = new SurfaceCamCallback(camera);
                surfaceView.getHolder().addCallback(callback);
                surfaceContainer.addView(surfaceView,
                        new ViewGroup.LayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT)));
            }
        }

        if ((getActivity() != null) && (orientationListener == null)) {
            orientationListener = new OrientationEventListener(getActivity()) {
                private int orientationPrev = 0;

                @Override
                public void onOrientationChanged(int orientation) {
                    if (orientationPrev != orientation) {
                        orientationPrev = orientation;
                        setCameraDisplayOrientation();
                    }
                }
            };
            orientationListener.enable();
        }

        handler.post(autoFocusRunnable);
    }

    private void setCameraDisplayOrientation() {
        if (camera == null) {
            return;
        }
        Display display = ((WindowManager) getActivity().getSystemService(Activity.WINDOW_SERVICE)).getDefaultDisplay();
        switch (display.getRotation()) {
            case Surface.ROTATION_0:
                camOrientation = 90;
                break;
            case Surface.ROTATION_90:
                camOrientation = 0;
                break;
            case Surface.ROTATION_180:
                camOrientation = 270;
                break;
            case Surface.ROTATION_270:
                camOrientation = 180;
                break;
        }
        camera.setDisplayOrientation(camOrientation);
    }

    private Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            Log.e(TAG, "Error opening camera: " + e.getMessage());
        }
        return c; // returns null if camera is unavailable
    }


    @Override
    public void onPause() {
        super.onPause();

        handler.removeCallbacks(autoFocusRunnable);

        if (orientationListener != null) {
            orientationListener.disable();
            orientationListener = null;
        }
        if (surfaceView != null) {
            surfaceView.getHolder().removeCallback(callback);
            callback = null;
        }
        if (camera != null) {
            camera.setPreviewCallback(null);
            surfaceContainer.removeView(surfaceView);
        }
        releaseCamera();
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    /**
     * Check if ScannerFragment scans vertically for 1D codes.
     * @return return true if 1D scanner is set for vertical scanning and false otherwise. Default value is false
     */
    public boolean isScanVertically() {
        return scanVertically;
    }

    /**
     * Set 1D scanner mode
     * @param scanVertically true to start scanning process vertically and false otherwise.
     */
    public void setScanVertically(boolean scanVertically) {
        this.scanVertically = scanVertically;
    }

    /**
     * Check if camera flash is on
     * @return true if flash is working and false otherwise
     */
    public boolean isFlashOn() {
        return flashOn;
    }

    /**
     * Turn camera flash on. Turning flash on may fail for several reasons. For example device may not have flash
     * @return method returns true if flash was successfully turned on and returns false otherwise.
     */
    public boolean flashTurnOn() {
        if (camera == null) {
            return false;
        }
        Camera.Parameters p = camera.getParameters();
        List<String> modes = p.getSupportedFlashModes();
        if (modes == null || !modes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
            return false;
        }

        p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        camera.setParameters(p);
        flashOn = true;

        return true;
    }

    /**
     * Turn camera flash off. Turning flash off may fail for several reasons. For example device may not have flash
     * @return method returns true if flash was successfully turned off and returns false otherwise.
     */
    public boolean flashTurnOff() {
        if (camera == null) {
            return false;
        }
        Camera.Parameters p = camera.getParameters();
        List<String> modes = p.getSupportedFlashModes();
        if (modes == null || !modes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
            return false;
        }

        p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        camera.setParameters(p);
        flashOn = false;

        return true;
    }

    private static class ScannerHandler extends Handler {
        WeakReference<ScannerFragment> fragment;
        B4HScanner scanner = new B4HScanner();

        ScannerHandler(WeakReference<ScannerFragment> fragment) {
            super();
            this.fragment = fragment;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DECODE:
                    // message called when getting image from camera
                    ImageBuffer buffer = (ImageBuffer) msg.obj;
                    ScannerFragment f = fragment.get();
                    if (f != null) {
                        Context context = f.getActivity();
                        ScannerListener listener = f.getListener();
                        if (context != null && listener != null) {
                            if ((f.isScanVertically() && buffer.orientation == ImageBuffer.ORIENTATION_LANDSCAPE) ||
                                    (!f.isScanVertically() && buffer.orientation == ImageBuffer.ORIENTATION_PORTRAIT)) {
                                int w = buffer.height;
                                int h = buffer.width;
                                Log.d(TAG, "Will rotate camera image");
                                byte[] data = rotateCameraImage(buffer.data, buffer.width, buffer.height);
                                buffer = new ImageBuffer(data, w, h, ImageBuffer.ORIENTATION_UNKNOWN);
                            }

                            Log.d(TAG, "Buffer size: " + buffer.data.length +
                                    ", buffer width: " + buffer.width + ", buffer height: " + buffer.height);
                            String code = scanner.parse(buffer.data, buffer.width, buffer.height, context);
                            if (code != null) {
                                listener.scannedCode(code);
                            }
                        }
                    }
                    break;
                default:
                    //do nothing
                    break;
            }
        }

        private byte[] rotateCameraImage(byte[] data, int width, int height) {
            byte[] rotatedData = new byte[data.length];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++)
                    rotatedData[x * height + height - y - 1] = data[x + y * width];
            }
            return rotatedData;
        }
    }

    private class SurfaceCamCallback implements SurfaceHolder.Callback, Camera.PreviewCallback {
        private Camera camera;
        Camera.Size previewSize;

        SurfaceCamCallback(Camera camera) {
            this.camera = camera;
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            //do nothing
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (camera != null && holder != null) {
                camera.setPreviewCallback(null);
                camera.stopPreview();    // stop camera preview
                try {
                    // restart camera preview
                    configureCamera(width, height);
                    setCameraDisplayOrientation();
                    camera.setPreviewDisplay(holder);
                    camera.setPreviewCallback(this);
                    camera.startPreview();
                    isPreviewing = true;

                } catch (Exception e) {
                    Log.e(TAG, "Exception raised configuring camera: " + e.getMessage());
                }
            }
        }

        private void configureCamera(int width, int height) {
            if (camera == null) {
                return;
            }

            Camera.Parameters cameraParams = camera.getParameters();
            cameraParams.set("orientation", "portrait");
            List<Camera.Size> sizes = cameraParams.getSupportedPreviewSizes();
            previewSize = getOptimalPreviewSize(sizes, Math.max(width, height), Math.min(width, height));
            cameraParams.setPreviewSize(previewSize.width, previewSize.height);
            // set YUV data format.
            cameraParams.setPreviewFormat(ImageFormat.NV21);
            cameraParams.setFlashMode(flashOn ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);
            // set frequency of capture
            setAcceptableFrameRate(cameraParams);

            if (getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS)) {
                if (cameraParams.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    cameraParams.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                }
            }

            camera.setParameters(cameraParams);
        }

        private void setAcceptableFrameRate(Camera.Parameters params) {
            List<int[]> ranges = params.getSupportedPreviewFpsRange();
            int[] frameRate = {0, 0};
            for (int[] range : ranges) {
                if (range[0] > frameRate[0]) {
                    frameRate[0] = range[0];
                    frameRate[1] = range[1];
                }
            }
            params.setPreviewFpsRange(frameRate[0], frameRate[1]);
        }

        private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int targetWidth, int targetHeight) {
            final double ASPECT_TOLERANCE = 0.05;
            double targetRatio = (double) targetWidth / targetHeight;
            if (sizes == null)
                return null;

            Camera.Size optimalSize = null;
            double minDiff = Double.MAX_VALUE;

            for (Camera.Size size : sizes) {
                double ratio = (double) size.width / size.height;
                if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                    continue;
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
            if (optimalSize == null) {
                minDiff = Double.MAX_VALUE;
                for (Camera.Size size : sizes) {
                    if (Math.abs(size.height - targetHeight) < minDiff) {
                        optimalSize = size;
                        minDiff = Math.abs(size.height - targetHeight);
                    }
                }
            }

            return optimalSize;
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (camera != null) {
                camera.setPreviewCallback(null);
                camera.stopPreview();
                isPreviewing = false;
            }
        }

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (handler.hasMessages(DECODE) || (previewSize == null)) {
                return;
            }

            int bufferOrientation = ImageBuffer.calcImgOrientation(camOrientation);
            Message msg = new Message();
            msg.what = DECODE;
            msg.obj = new ImageBuffer(data, previewSize.width, previewSize.height, bufferOrientation);
            handler.sendMessageDelayed(msg, DECODE_DELAY_MSECS);
        }
    }
}
package com.bees4honey.vinscanner;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Wrapper Activity for native VIN scanner library. Activity can function in two modes: single scan and continuous scan.
 * In single mode the first scanned VIN-code is returned in intent.
 * In continuous mode the scanned code is shown to user and then scanner continues the scanning process.
 * Code to start Scanner activity in single mode:
 *
 * <pre>
 *
 * {@code
 * Intent intent = new Intent(curContext, com.bees4honey.vinscanner.Scanner.class);
 * intent.putExtra(com.bees4honey.vinscanner.ScannerActivity.SINGLE_SCAN, true);
 * startActivityForResult(intent, SCAN_CODE);
 * }
 * </pre>
 *
 * If Scanner was started in a single mode then you may use the following code to extract scanned code
 *
 * <pre>
 *
 * {@code
 * protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 *     if (requestCode == SCAN_CODE && resultCode == RESULT_OK) {
 *         String scannedVIN = data.getStringExtra(com.bees4honey.vinscanner.Scanner.SCANNED_CODE);
 *         Log.d("VinScanner", "Scanned vin: " + scannedVIN);
 *     }
 * }
 * }
 * </pre>
 */
public class ScannerActivity extends AppCompatActivity implements ScannerListener {
    /**
     * Key for boolean extra which should be passed to the activity. If true then first scanned VIN code would be passed
     * back to the calling activity with intent containing {@link #SCANNED_CODE} activity. If no value passed with SINGLE_SCAN value
     * then scanner is functioning in continuous mode.
     */
    public static final String SINGLE_SCAN = "single_scan";

    /**
     * A key for scanned code returned from activity if it was started in single scan mode.
     */
    public static final String SCANNED_CODE = "scanned_code";
    private static final String CAMERA_FRAGMENT = "camera_fragment";
    private static final int VIN_PERMISSIONS_REQUEST_CAMERA = 102;

    private ScannerFragment scanner;
    private ViewFinder finder;
    private TextView vincodeView;
    private boolean singleScan;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        singleScan = getIntent().getBooleanExtra(SINGLE_SCAN, false);
        setContentView(getResId("scanner_activity", "layout"));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            instantiateScannerFragment();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    VIN_PERMISSIONS_REQUEST_CAMERA);
        }
    }

    //Get resource id. Getting id via this tricky behaviour
    //is necessary to easily integrate this activity in a phonegap plugin
    private int getResId(String name, String type) {
        return getApplication().getResources().getIdentifier(name, type, getApplication().getPackageName());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (scanner != null) {
            scanner.setListener(this);
            finder.setContentOrientation(scanner.isScanVertically() ? ViewFinder.ContentOrientation.VERTICAL :
                    ViewFinder.ContentOrientation.HORIZONTAL);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (scanner != null) {
            scanner.setListener(null);
        }
    }

    @Override
    public void scannedCode(String code) {
        if (singleScan) {
            finishScan(code);
        }
        vincodeView.setText(code);
        vincodeView.setVisibility(View.VISIBLE);
    }

    public void finishScan(String codeResult) {
        Intent intent = getIntent();
        intent.putExtra(SCANNED_CODE, codeResult);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case VIN_PERMISSIONS_REQUEST_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    instantiateScannerFragment();
                }
                break;
        }
    }

    private void instantiateScannerFragment() {
        FragmentManager fm = getFragmentManager();
        scanner = (ScannerFragment)fm.findFragmentByTag(CAMERA_FRAGMENT);
        if (scanner == null) {
            scanner = new ScannerFragment();
            scanner.setRetainInstance(true);
            fm.beginTransaction().add(getResId("fragment_container", "id"),
                    scanner, CAMERA_FRAGMENT).commit();
        }

        finder = (ViewFinder)findViewById(getResId("view_finder", "id"));
        finder.setRunning(true);

        vincodeView = (TextView)findViewById(getResId("tv_vincode", "id"));
        vincodeView.setVisibility(View.INVISIBLE);

        findViewById(getResId("orientationButton", "id")).
                setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        boolean vertically = !scanner.isScanVertically();
                        scanner.setScanVertically(vertically);
                        finder.setContentOrientation(vertically ? ViewFinder.ContentOrientation.VERTICAL :
                                ViewFinder.ContentOrientation.HORIZONTAL);
                    }
                });

        findViewById(getResId("torchButton", "id")).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (scanner.isFlashOn()) {
                    if (scanner.flashTurnOff()) {
                        ((ImageButton) v).setImageResource(getResId("light", "drawable"));
                    }
                } else {
                    if (scanner.flashTurnOn()) {
                        ((ImageButton) v).setImageResource(getResId("light_on", "drawable"));
                    }
                }
            }
        });
    }

}
package com.bees4honey.vinscanner.plugin;

import android.app.Activity;
import android.content.Intent;
import com.bees4honey.vinscanner.ScannerActivity;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This calls out to the VinBarScanner and returns the result.
 */
public class VinBarScanner extends CordovaPlugin {
    public static final String ACTION_SCAN = "scan";
    public static final int REQUEST_CODE = 0x0ba7c0de;

    private CallbackContext cbContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.cbContext = callbackContext;

        if (action.equals(ACTION_SCAN)) {
            scan();
        } else {
            callbackContext.error("Invalid Action");
            return false;
        }
        return true;
    }


    public void scan() {
        Intent intentScan = new Intent("com.bees4honey.vinscanner.plugin.SCAN");
        intentScan.addCategory(Intent.CATEGORY_DEFAULT);
        intentScan.putExtra(ScannerActivity.SINGLE_SCAN, true);

        this.cordova.startActivityForResult((CordovaPlugin) this, intentScan, REQUEST_CODE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("VINCode", intent.getStringExtra(ScannerActivity.SCANNED_CODE));
                    obj.put("cancelled", false);
                } catch (JSONException e) {
                    //Log.d(LOG_TAG, "This should never happen");
                }
                this.cbContext.success(obj);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("VINCode", "NON RESULT");
                    obj.put("cancelled", "true");
                } catch (JSONException e) {
                    //Log.d(LOG_TAG, "This should never happen");
                }
                this.cbContext.success(obj);
            } else {
                this.cbContext.error("Invalid Activity");
            }
        }
    }
}
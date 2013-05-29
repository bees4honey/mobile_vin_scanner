package com.bees4honey.vinscanner.plugin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;

import org.apache.cordova.api.CallbackContext;
import org.apache.cordova.api.CordovaPlugin;

/**
 * This calls out to the VinBarSacanner and returns the result.
 */
public class VinBarScanner extends CordovaPlugin {

    public static final int REQUEST_CODE = 0x0ba7c0de;

    public String callback;
    private CallbackContext cbContext;
    /**
     * Executes the request and returns PluginResult.
     *
     * @param action        The action to execute.                                                                                                                      
     * @param args          JSONArray of arguments for the plugin.
     * @param callbackId    The callback id used when calling back into JavaScript.
     * @return              A PluginResult object with a status and message.
     */
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        this.cbContext = callbackContext;

        if (action.equals("scan")) {
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

        this.cordova.startActivityForResult((CordovaPlugin) this, intentScan, REQUEST_CODE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("text", intent.getStringExtra("returnedData"));
                    obj.put("cancelled", false);
                } catch(JSONException e) {
                    //Log.d(LOG_TAG, "This should never happen");
                }
                this.cbContext.success(obj);
            } if (resultCode == Activity.RESULT_CANCELED) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("text", "NON RESULT");
                    obj.put("cancelled", "true");
                } catch(JSONException e) {
                    //Log.d(LOG_TAG, "This should never happen");
                }
                this.cbContext.success(obj);
            } else {
                this.cbContext.error("Invalid Activity");
            }
        }
    }


}
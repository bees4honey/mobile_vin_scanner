package com.bees4honey.vinscanner.plugin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import org.apache.cordova.api.Plugin;
import org.apache.cordova.api.PluginResult;

/**
 * This calls out to the VinBarSacanner and returns the result.
 */
public class VinBarScanner extends Plugin {

    public static final int REQUEST_CODE = 0x0ba7c0de;

    public String callback;

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action        The action to execute.                                                                                                                      
     * @param args          JSONArray of arguments for the plugin.
     * @param callbackId    The callback id used when calling back into JavaScript.
     * @return              A PluginResult object with a status and message.
     */
    public PluginResult execute(String action, JSONArray args, String callbackId) {
        this.callback = callbackId;

        if (action.equals("scan")) {
            scan();
        } else {
            return new PluginResult(PluginResult.Status.INVALID_ACTION);
        }
        PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
        r.setKeepCallback(true);
        return r;
    }


    public void scan() {
        Intent intentScan = new Intent("com.bees4honey.vinscanner.plugin.SCAN");
        intentScan.addCategory(Intent.CATEGORY_DEFAULT);

        this.ctx.startActivityForResult((Plugin) this, intentScan, REQUEST_CODE);
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
                this.success(new PluginResult(PluginResult.Status.OK, obj), this.callback);
            } if (resultCode == Activity.RESULT_CANCELED) {
                JSONObject obj = new JSONObject();
                try {
                    obj.put("text", "NON RESULT");
                    obj.put("cancelled", "true");
                } catch(JSONException e) {
                    //Log.d(LOG_TAG, "This should never happen");
                }
                this.success(new PluginResult(PluginResult.Status.OK, obj), this.callback);
            } else {
                this.error(new PluginResult(PluginResult.Status.ERROR), this.callback);
            }
        }
    }


}
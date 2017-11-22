package com.tuimedia.cordova.keystorage;

import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * Android implementation of Key Storage Cordova Plugin
 */
public final class Keychain extends CordovaPlugin {
    private static final String TAG = Keychain.class.getSimpleName();

    private static final String GET_VALUE = "get";
    private static final String SET_VALUE = "set";
    private static final String REMOVE_VALUE = "remove";

    public Keychain() {
    }

    /**
     * Sets the context of the Command. This can then be used to do things like
     * get file paths associated with the Activity.
     *
     * @param cordova The context of the main Activity.
     * @param webView The CordovaWebView Cordova is running in.
     */

    public void initialize(final CordovaInterface cordova, final CordovaWebView webView) {
        super.initialize(cordova, webView);
        Log.v(TAG, "Init KeyChain");
    }

    /**
     *
     * @param action          The action to execute.
     * @param args            The exec() arguments.
     * @param callbackContext The callback context used when calling back into JavaScript.
     * @return true if action method found and false if not
     * @throws JSONException
     */
    public boolean execute(final String action, final JSONArray args,
                           final CallbackContext callbackContext) throws JSONException
    {
        if (GET_VALUE.equals(action)) {
            final String key = args.getString(0);
            processGet(key, callbackContext);
            return true;
        } else if (SET_VALUE.equals(action)) {
            final String key = args.getString(0);
            final String value = args.getString(1);
            processSet(key, value, callbackContext);
            return true;
        } else if (REMOVE_VALUE.equals(action)) {
            final String key = args.getString(0);
            processRemove(key, callbackContext);
            return true;
        }

        return false;
    }

    /**
     * Retrieve and decrypt a stored value
     *
     * @param key        The key used to identify the value
     * @param callback   The callback
     */
    private void processGet(final String key, final CallbackContext callback) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    final SecureStorage secureStorage = new SecureStorage(cordova);
                    callback.success(secureStorage.getValue(key));
                } catch (ReportableException e) {
                    callback.error(e.getCause().getMessage());
                }
            }
        });
    }

    /**
     * Encrypt and store a value
     *
     * @param key        The key used to identify the value
     * @param value      The value to store
     * @param callback   The callback
     */
    private void processSet(final String key, final String value, final CallbackContext callback) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    final SecureStorage secureStorage = new SecureStorage(cordova);
                    secureStorage.setValue(key, value);
                    callback.success();
                } catch (ReportableException e) {
                    callback.error(e.getCause().getMessage());
                }
            }
        });
    }

    /**
     * Remove a stored key/value pair
     *
     * @param key        The key used to identify the value
     * @param callback   The callback
     */
    private void processRemove(final String key, final CallbackContext callback) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    final SecureStorage secureStorage = new SecureStorage(cordova);
                    secureStorage.removeValue(key);
                    callback.success();
                } catch (ReportableException e) {
                    callback.error(e.getCause().getMessage());
                } catch (Exception e) {
                    callback.error(e.getMessage());
                }
            }
        });
    }
}

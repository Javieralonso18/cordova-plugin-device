/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/
       package org.apache.cordova.device;

       import java.util.TimeZone;

       import org.apache.cordova.CordovaWebView;
       import org.apache.cordova.CallbackContext;
       import org.apache.cordova.CordovaPlugin;
       import org.apache.cordova.CordovaInterface;
       import org.json.JSONArray;
       import org.json.JSONException;
       import org.json.JSONObject;

       import android.provider.Settings;
       import android.database.Cursor;
       import android.content.Context;
       import android.net.Uri;

       public class Device extends CordovaPlugin {
        public static final String TAG = "Device";

    public static String platform;                            // Device OS
    public static String uuid;// Device UUID
    public static String gsfId;

    private static final Uri sUri = Uri.parse("content://com.google.android.gsf.gservices");
    private static final String ANDROID_PLATFORM = "Android";
    private static final String AMAZON_PLATFORM = "amazon-fireos";
    private static final String AMAZON_DEVICE = "Amazon";

    /**
     * Constructor.
     */
    public Device() {
    }

    /**
     * Sets the context of the Command. This can then be used to do things like
     * get file paths associated with the Activity.
     *
     * @param cordova The context of the main Activity.
     * @param webView The CordovaWebView Cordova is running in.
     */
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Device.uuid = getUuid();
    }

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action            The action to execute.
     * @param args              JSONArry of arguments for the plugin.
     * @param callbackContext   The callback id used when calling back into JavaScript.
     * @return                  True if the action was valid, false if not.
     */
              
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if ("getDeviceInfo".equals(action)) {
            JSONObject r = new JSONObject();
            r.put("uuid", Device.uuid);
            r.put("gsfId", this.getGSFID(this.cordova.getActivity().getApplicationContext()));
            r.put("directory", this.cordova.getActivity().getFilesDir().getPath());
            r.put("version", this.getOSVersion());
            r.put("platform", this.getPlatform());
            r.put("model", this.getModel());
            r.put("manufacturer", this.getManufacturer());
            r.put("isVirtual", this.isVirtual());
            r.put("serial", this.getSerialNumber());
            callbackContext.success(r);
        }
        else {
            return false;
        }
        return true;
    }

    //--------------------------------------------------------------------------
    // LOCAL METHODS
    //--------------------------------------------------------------------------

    /**
     * Get the OS name.
     *
     * @return
     */
    public String getPlatform() {
        String platform;
        if (isAmazonDevice()) {
            platform = AMAZON_PLATFORM;
        } else {
            platform = ANDROID_PLATFORM;
        }
        return platform;
    }

    /**
     * Get the device's Universally Unique Identifier (UUID).
     *
     * @return
     */
    public String getUuid() {
        String uuid = Settings.Secure.getString(this.cordova.getActivity().getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        return uuid;
    }

    public String getModel() {
        String model = android.os.Build.MODEL;
        return model;
    }

    public String getProductName() {
        String productname = android.os.Build.PRODUCT;
        return productname;
    }

    public String getManufacturer() {
        String manufacturer = android.os.Build.MANUFACTURER;
        return manufacturer;
    }

    public String getSerialNumber() {
       String serial = "";
       if (android.os.Build.VERSION_CODES.O > getSDKVersion()){
           serial = android.os.Build.SERIAL;
       }else{
        if (android.os.Build.VERSION_CODES.Q > getSDKVersion()){
           serial = android.os.Build.getSerial();
       }
   }
   return serial;
}

    /**
     * Get the OS version.
     *
     * @return
     */
    public String getOSVersion() {
        String osversion = android.os.Build.VERSION.RELEASE;
        return osversion;
    }

    public Integer getSDKVersion() {
        Integer sdkversion = android.os.Build.VERSION.SDK_INT;
        return sdkversion;
    }

    public String getTimeZoneID() {
        TimeZone tz = TimeZone.getDefault();
        return (tz.getID());
    }

    /**
     * Function to check if the device is manufactured by Amazon
     *
     * @return
     */
    public boolean isAmazonDevice() {
        if (android.os.Build.MANUFACTURER.equals(AMAZON_DEVICE)) {
            return true;
        }
        return false;
    }

    public boolean isVirtual() {
       return (android.os.Build.BRAND.startsWith("generic") && android.os.Build.DEVICE.startsWith("generic"))
        || android.os.Build.FINGERPRINT.startsWith("generic")
        || android.os.Build.FINGERPRINT.startsWith("unknown")
        || android.os.Build.HARDWARE.contains("goldfish")
        || android.os.Build.HARDWARE.contains("ranchu")
        || android.os.Build.MODEL.contains("google_sdk")
        || android.os.Build.MODEL.contains("Emulator")
        || android.os.Build.MODEL.contains("Android SDK built for x86")
        || android.os.Build.MANUFACTURER.contains("Genymotion")
        || android.os.Build.PRODUCT.contains("sdk_google")
        || android.os.Build.PRODUCT.contains("google_sdk")
        || android.os.Build.PRODUCT.contains("sdk")
        || android.os.Build.PRODUCT.contains("sdk_x86")
        || android.os.Build.PRODUCT.contains("vbox86p")
        || android.os.Build.PRODUCT.contains("emulator")
        || android.os.Build.PRODUCT.contains("simulator");
   }

    /**
     * Get the device's Google Service Framework ID (GSFID).
     *
     * @return
     */
    public static String getGSFID(Context context) {
       try {
          Cursor query = context.getContentResolver().query(sUri, null, null, new String[] { "android_id" }, null);
          if (query == null) {
             return "";
         }
         if (!query.moveToFirst() || query.getColumnCount() < 2) {
             query.close();
             return "";
         }
         final String toHexString = Long.toHexString(Long.parseLong(query.getString(1)));
         query.close();
         return toHexString.toUpperCase().trim();
     } catch (SecurityException e) {
      e.printStackTrace();
      return null;
  } catch (Exception e2) {
      e2.printStackTrace();
      return null;
  }
}

}

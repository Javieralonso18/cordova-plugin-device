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
import java.io.*;

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
import android.os.Build;

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
  String model = Build.MODEL;
  return model;
}

public String getProductName() {
  String productname = Build.PRODUCT;
  return productname;
}

public String getManufacturer() {
  String manufacturer = Build.MANUFACTURER;
  return manufacturer;
}

public String getSerialNumber() {
  String serial = "";
  if (Build.VERSION_CODES.O > getSDKVersion()){
    serial = Build.SERIAL;
}else{
    if (Build.VERSION_CODES.Q > getSDKVersion()){
      serial = Build.getSerial();
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
  String osversion = Build.VERSION.RELEASE;
  return osversion;
}

public Integer getSDKVersion() {
  Integer sdkversion = Build.VERSION.SDK_INT;
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
  if (Build.MANUFACTURER.equals(AMAZON_DEVICE)) {
    return true;
}
return false;
}
/**
 * Detects if app is currently running on emulator, or real device.
 *
 * @param context Apprication context
 * @return true for emulator, false for real devices
 */

private static final String[] QEMU_DRIVERS = {"goldfish"};
private static final String[] GENY_FILES = {
    "/dev/socket/genyd",
    "/dev/socket/baseband_genyd"
};
private static final String[] PIPES = {
    "/dev/socket/qemud",
    "/dev/qemu_pipe"
};
private static final String[] X86_FILES = {
    "ueventd.android_x86.rc",
    "x86.prop",
    "ueventd.ttVM_x86.rc",
    "init.ttVM_x86.rc",
    "fstab.ttVM_x86",
    "fstab.vbox86",
    "init.vbox86.rc",
    "ueventd.vbox86.rc"
};
private static final String[] ANDY_FILES = {
    "fstab.andy",
    "ueventd.andy.rc"
};
private static final String[] NOX_FILES = {
    "fstab.nox",
    "init.nox.rc",
    "ueventd.nox.rc"
};

private static boolean checkBasic() {
    int rating = 0;

    if (Build.PRODUCT.equals("sdk_x86_64") ||
        Build.PRODUCT.equals("sdk_google_phone_x86") ||
        Build.PRODUCT.equals("sdk_google_phone_x86_64") ||
        Build.PRODUCT.equals("sdk_google_phone_arm64") ||
        Build.PRODUCT.equals("vbox86p")) {
        rating++;
}

if (Build.MANUFACTURER.equals("unknown")) {
    rating++;
}

if (Build.BRAND.equals("generic") ||
    Build.BRAND.equalsIgnoreCase("android") ||
    Build.BRAND.equals("generic_arm64") ||
    Build.BRAND.equals("generic_x86") ||
    Build.BRAND.equals("generic_x86_64")) {
    rating++;
}

if (Build.DEVICE.equals("generic") ||
    Build.DEVICE.equals("generic_arm64") ||
    Build.DEVICE.equals("generic_x86") ||
    Build.DEVICE.equals("generic_x86_64") ||
    Build.DEVICE.equals("vbox86p")) {
    rating++;
}

if (Build.MODEL.equals("sdk") ||
    Build.MODEL.equals("Android SDK built for arm64") ||
    Build.MODEL.equals("Android SDK built for armv7") ||
    Build.MODEL.equals("Android SDK built for x86") ||
    Build.MODEL.equals("Android SDK built for x86_64")) {
    rating++;
}

if (Build.HARDWARE.equals("ranchu")) {
    rating++;
}

if (Build.FINGERPRINT.contains("sdk_google_phone_arm64") ||
    Build.FINGERPRINT.contains("sdk_google_phone_armv7")) {
    rating++;
}

boolean result = Build.FINGERPRINT.startsWith("generic")
|| Build.MODEL.contains("google_sdk")
|| Build.MODEL.toLowerCase().contains("droid4x")
|| Build.MODEL.contains("Emulator")
|| Build.MODEL.contains("Android SDK built for x86")
|| Build.MANUFACTURER.contains("Genymotion")
|| Build.HARDWARE.equals("goldfish")
|| Build.HARDWARE.equals("vbox86")
|| Build.PRODUCT.equals("sdk")
|| Build.PRODUCT.startsWith("google_sdk")
|| Build.PRODUCT.equals("sdk_x86")
|| Build.PRODUCT.equals("vbox86p")
|| Build.BOARD.toLowerCase().contains("nox")
|| Build.BOOTLOADER.toLowerCase().contains("nox")
|| Build.HARDWARE.toLowerCase().contains("nox")
|| Build.PRODUCT.toLowerCase().contains("nox")
|| Build.SERIAL.toLowerCase().contains("nox")
|| Build.HOST.contains("Droid4x-BuildStation")
|| Build.MANUFACTURER.startsWith("iToolsAVM")
|| Build.DEVICE.startsWith("iToolsAVM")
|| Build.MODEL.startsWith("iToolsAVM")
|| Build.BRAND.startsWith("generic")
|| Build.HARDWARE.startsWith("vbox86");

if (result) return true;
result |= Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic");
if (result) return true;
result |= "google_sdk".equals(Build.PRODUCT);
if (result) return true;

return rating >= 2;
}

private static boolean checkQEmuDrivers() {
    for (File drivers_file : new File[]{new File("/proc/tty/drivers"), new File("/proc/cpuinfo")}) {
        if (drivers_file.exists() && drivers_file.canRead()) {
            byte[] data = new byte[1024];
            try {
                InputStream is = new FileInputStream(drivers_file);
                is.read(data);
                is.close();
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            String driver_data = new String(data);
            for (String known_qemu_driver : QEMU_DRIVERS) {
                if (driver_data.contains(known_qemu_driver)) {
                    return true;
                }
            }
        }
    }

    return false;
}

private static boolean checkAdvanced() {
    boolean result = checkFiles(GENY_FILES)
    || checkFiles(ANDY_FILES)
    || checkFiles(NOX_FILES)
    || checkQEmuDrivers()
    || checkFiles(PIPES)
    || (checkFiles(X86_FILES));
    return result;
}

private static boolean checkFiles(String[] targets) {
    for (String pipe : targets) {
        File qemu_file = new File(pipe);
        if (qemu_file.exists()) {
            return true;
        }
    }
    return false;
}

public boolean isVirtual() {
    if (checkBasic()) return true;
    if (checkAdvanced()) return true;

    return false;
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

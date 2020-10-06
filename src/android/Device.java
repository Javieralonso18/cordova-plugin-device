package com.alauddin.cordova.deviceinfo;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.pm.PackageManager;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.os.Build;
import android.provider.Settings;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.TimeZone;

public class DeviceInfo extends CordovaPlugin {
  CallbackContext context;
  private static final String ANDROID_PLATFORM = "Android";

  @Override
  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException
  {
    this.context = callbackContext;

    if(action.equals("getInfo"))
    {
      JSONObject phoneInfo = this.getPhoneDetails();

      callbackContext.success(phoneInfo);

      return true;
    }
    else
    {
      callbackContext.error("devideinfo." + action + " is not a supported function. Did you mean 'getInfo'?");
      return false;
    }
  }

  private JSONObject getPhoneDetails()
  {
    JSONObject info = new JSONObject();

    TelephonyManager tm = (TelephonyManager) cordova.getActivity().getSystemService(Context.TELEPHONY_SERVICE);

    try {
     info.put("imei", "");
     if (Build.VERSION_CODES.Q > getOSSdk()){
      info.put("imei", tm.getDeviceId());
    }

    info.put("msisdn", tm.getLine1Number());
    info.put("operator", tm.getNetworkOperator());
    info.put("operator_name", tm.getNetworkOperatorName());
    info.put("country_iso", tm.getNetworkCountryIso());
    info.put("roaming", tm.isNetworkRoaming());
    info.put("model", getModelName());
    info.put("manufacturer", getManufacturerName());
    info.put("product_name", getProductName());
    info.put("os_type", ANDROID_PLATFORM);
    info.put("os_version", getOSVersion());
    info.put("os_sdk", getOSSdk());
    info.put("uuid", getUuid());
    info.put("phone_type", getPhoneType(tm));
    info.put("ip", getLocalIpAddress());
    info.put("timezone", getTimeZoneID());
    info.put("connection_type", connectionType(cordova.getActivity()));
  }
  catch (JSONException e) {
    e.printStackTrace();
  }
  return info;
}

private String connectionType(Context con)
{
  boolean isCon = Connectivity.isConnected(cordova.getActivity());
  boolean isWifi = Connectivity.isConnectedWifi(cordova.getActivity());
  boolean isMobile = Connectivity.isConnectedMobile(cordova.getActivity());
  boolean isFast = Connectivity.isConnectedFast(cordova.getActivity());

  String conType = "Not Connected!";

  if(isCon)
  {
    if(isWifi)
    {
      conType = "Wifi";
    }
    else if(isMobile)
    {
      conType = "2G";

      if(isFast)
      {
        conType = "3G";
      }
    }
  }
  return conType;
}

private String getPhoneType(TelephonyManager tm)
{
  int phoneType = tm.getPhoneType();
  String type = "NONE";
  switch (phoneType)
  {
    case (TelephonyManager.PHONE_TYPE_CDMA):
    type = "CDMA";
    break;
    case (TelephonyManager.PHONE_TYPE_GSM):
    type = "GSM";
    break;
    case (TelephonyManager.PHONE_TYPE_NONE):
    type = "NONE";
    break;
  }
  return type;
}

private String getModelName() {
  String model = Build.MODEL;
  return capitalize(model);
}

private String getManufacturerName()
{
  String manufacturer = Build.MANUFACTURER;
  return capitalize(manufacturer);
}

private String getOSVersion() {
  String osversion = Build.VERSION.RELEASE;
  return osversion;
}

private Integer getOSSdk() {
  Integer ossdk = Build.VERSION.SDK_INT;
  return ossdk;
}

private String getProductName() {
  String productname = Build.PRODUCT;
  return productname;
}

public String getUuid() {
  String uuid = Settings.Secure.getString(this.cordova.getActivity().getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
  return uuid;
}

public String getTimeZoneID() {
  TimeZone tz = TimeZone.getDefault();
  return (tz.getID());
}

private String capitalize(String s) {
  if (s == null || s.length() == 0) {
    return "";
  }
  char first = s.charAt(0);
  if (Character.isUpperCase(first)) {
    return s;
  } else {
    return Character.toUpperCase(first) + s.substring(1);
  }
}

private String getLocalIpAddress(){
  try {
    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
      en.hasMoreElements();)
    {
      NetworkInterface intf = en.nextElement();
      for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
        InetAddress inetAddress = enumIpAddr.nextElement();
        if (!inetAddress.isLoopbackAddress()) {
          return inetAddress.getHostAddress().toString();
        }
      }
    }
  }
  catch (Exception ex) {
    ex.printStackTrace();
  }
  return null;
}
}


package com.reactlibrary.alipay;

import com.alipay.sdk.app.H5PayCallback;
import com.alipay.sdk.util.H5PayResultModel;
import com.alipay.sdk.app.AuthTask;
import com.alipay.sdk.app.PayTask;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class AlipayModule extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;

  public AlipayModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RCTAlipay";
  }

  @ReactMethod
  public void authWithInfo(final String infoStr, final Promise promise) {
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        AuthTask authTask = new AuthTask(getCurrentActivity());
        Map<String, String> map = authTask.authV2(infoStr, true);
        promise.resolve(getWritableMap(map));
      }
    };
    Thread thread = new Thread(runnable);
    thread.start();
  }

  @ReactMethod
  public void pay(final String orderInfo, final Promise promise) {
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        try {
          JSONObject jsonObject = new JSONObject(orderInfo);
          String sign = jsonObject.getString("sign");
          sign = URLEncoder.encode(sign, "UTF-8");
          // 完整的符合支付宝参数规范的订单信息
          final String payInfo = jsonObject.getString("order_info")
                  + "&sign=\""
                  + sign
                  + "\"&"
                  + "sign_type=\""
                  + jsonObject.getString("sign_type")
                  + "\"";
          PayTask payTask = new PayTask(getCurrentActivity());
          Map<String, String> map = payTask.payV2(payInfo, true);
          promise.resolve(getWritableMap(map));
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    };
    Thread thread = new Thread(runnable);
    thread.start();
  }

  @ReactMethod
  public void payInterceptorWithUrl(final String h5PayUrl, final Promise promise) {
    PayTask payTask = new PayTask(getCurrentActivity());
    payTask.payInterceptorWithUrl(h5PayUrl, true, new H5PayCallback() {
      @Override
      public void onPayResult(H5PayResultModel h5PayResultModel) {
        WritableMap map = Arguments.createMap();
        map.putString("resultCode", h5PayResultModel.getResultCode());
        map.putString("returnUrl", h5PayResultModel.getReturnUrl());
        promise.resolve(map);
      }
    });
  }

  @ReactMethod
  public void getVersion(Promise promise) {
    PayTask payTask = new PayTask(getCurrentActivity());
    promise.resolve(payTask.getVersion());
  }

  private WritableMap getWritableMap(Map<String, String> map) {
    WritableMap writableMap = Arguments.createMap();
    for (Map.Entry<String, String> entry : map.entrySet()) {
      writableMap.putString(entry.getKey(), entry.getValue());
    }
    return writableMap;
  }
}
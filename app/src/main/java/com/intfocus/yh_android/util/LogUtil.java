package com.intfocus.yh_android.util;

import android.util.Log;
import android.widget.Toast;

import com.intfocus.yh_android.BuildConfig;

/**
 * Created by lijunjie on 16/7/22.
 */
public class LogUtil {

  /*
   * Log.d(tag, str, limit)
   */
  public static void d(String tag, String str, int limit) {
    int maxLength = 2000;
    str = str.trim();
    Log.d(tag, str.substring(0, str.length() > maxLength ? maxLength : str.length()));
    if(str.length() > maxLength && limit < 4) {
      str = str.substring(maxLength, str.length());
      LogUtil.d(tag, str, limit);
    }
  }

  /*
   * Log.d(tag, str)
   */
  public static void d(String tag, String str) {
    if (!BuildConfig.DEBUG) {
      /*
       * 若应用不处于 DEBUG 模式，则不打印输出信息
       */
      return;
    }
     LogUtil.d(tag, str, 0);
  }
}

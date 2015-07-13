package com.spjanson.sqlistrecycler;
/**
 * Copyright 2015 Sean Janson. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.WindowManager;

class UT {  private UT() {}     //enforce singleton patern
  private static final String LOGTAG = "_X_";

  static SharedPreferences pfs;
  static Context acx;
  static ContentResolver cr;
  static String auth;
  public static void init(Context ctx) {    // initialize constant values to simplify the app code
    acx = ctx.getApplicationContext();
    pfs = PreferenceManager.getDefaultSharedPreferences(acx);
    cr = acx.getContentResolver();
    auth = acx.getResources().getString(R.string.auth_name);
  }

  // check portrait / landscape based on display ratio
  static boolean isLand() {
    Point size = new Point();
    ((WindowManager)acx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(size);
    return size.x > size.y;
  }

  static void lg(String msg) {  Log.d(LOGTAG, msg);  }  // debug log message
  static void le(Throwable ex){ Log.e(LOGTAG, Log.getStackTraceString(ex)); }  // error log message
}
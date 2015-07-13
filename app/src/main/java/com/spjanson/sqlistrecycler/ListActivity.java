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

import android.content.ContentValues;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class ListActivity extends AppCompatActivity {
  @Override
  protected void onCreate(Bundle bundle) {   super.onCreate(bundle);                                   //UT.lg("MA create");
    UT.init(this);  // application-wide initialization (context, prefs, ...)
    setContentView(R.layout.act_main);

    if (null == bundle) {
      loadDemoData();    // load SQLite table with dummy data
      getFragmentManager().beginTransaction().replace(R.id.fl_listcontnr, new ListFragment()).commit();
    }
  }

  // dummy code loads the SQLite DB table with CNT entries to play with
  private static final int CNT = 99;
  private void loadDemoData() {
    ContentValues[] cvs = new ContentValues[CNT];
    for (int i = 0; i < CNT; i++) // ------ COL_TITL ---|-- COL_DATA
      cvs[i] = DataProvider.Data.contVals(String.format("%02d", (i+1)), "DATA OF: " + (i+1));
    UT.cr.delete(DataProvider.Contract.tbl(), null, null);
    UT.cr.bulkInsert(DataProvider.Contract.tbl(), cvs);
  }

}

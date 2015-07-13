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

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DetailFragment extends Fragment {
  public static final String ITEM_ID = "item_id";
  private String mItem;

  public DetailFragment(){} //for fragmgr to instantiate the fragment (orientation changes)

  @Override
  public void onCreate(Bundle bundle) {  super.onCreate(bundle);
    Bundle args = getArguments();
    if (args.containsKey(ITEM_ID))
      mItem = args.getString(ITEM_ID);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
    View rootView = inflater.inflate(R.layout.frg_detail, container, false);
    if (mItem != null)
      ((TextView)rootView.findViewById(R.id.tv_detail)).setText(mItem);
    return rootView;
  }
}

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

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
  // select states (where is select request coming from)
  private static final int NO_CLICK = 0;
  private static final int SHORT_CLICK = 1;
  private static final int LONG_CLICK = 2;

  // fragment can have multiple active loaders
  private static final int NAMES_LOADER = 1;
  private static final String POS = "act_pos";

  // list control values
  private int mSelPos = -1;   // current position could not use cursor's position, it does not persist

  private ListActivity mAct;   // used instead of getActivity(), abused as 'alive' flag
  private RecyclerView mRecyclView;     // list view
  private ListAdapter mLstAdptr;        // loads requested list items

  public ListFragment(){}                 // for the fragmgr to instantiate the fragment (orientation changes)

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {         //UT.lg("LF create view");
    if (mAct != null) {
      View rootView = inflater.inflate(R.layout.list_view, container, false);
      mRecyclView =(RecyclerView)rootView.findViewById(R.id.rv_listvert);
      mRecyclView.setLayoutManager(new LinearLayoutManager(mAct));
      mLstAdptr = new ListAdapter();
      mRecyclView.setAdapter(mLstAdptr);

      mSelPos = bundle != null ? bundle.getInt(POS) : -1;                                     //UT.lg("   " + mSelPos);
      return rootView;
    }
    return super.onCreateView(inflater, container, bundle);  //----------------->>>
  }
  @Override
  public void onSaveInstanceState(Bundle bundle) {  super.onSaveInstanceState(bundle);            //UT.lg("LF save instance");
    bundle.putInt(POS, mSelPos);
  }
  @Override
  public void onResume() {  super.onResume();                                                //UT.lg("LF resume " + mSelPos);
    if (mSelPos < 0) {                                                                         //UT.lg("   create loader");
      getLoaderManager().restartLoader(NAMES_LOADER, null, this);
    } else {                                                                                   //UT.lg("   refresh loader");
      getLoaderManager().initLoader(NAMES_LOADER, null, this);
    }
  }
  @Override
  public void onAttach(Activity act) { super.onAttach(act);                                            //UT.lg("LF attach");
    mAct = act instanceof ListActivity ? (ListActivity)act : null;
  }
  @Override
  public void onDetach() { super.onDetach();                                                           //UT.lg("LF detach");
    mAct = null;
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle a) {                                     //UT.lg("LF create loader");
    return mAct == null ? null :
      new CursorLoader(mAct,             // !!! keep in sync with 'query' in 'on swipe'
        DataProvider.Contract.tbl(), // table name
        DataProvider.Contract.COLS,  // projection (new String[]{Table._ID, Table.COL_TITL, ...})
        null,    // selargs (new String[]{"val1", "val2"})
        null,    // selection ("Column1 =? AND Column2 =?")
        (DataProvider.Contract.Tbl.COL_TITL + " ASC").trim()    // sortorder ("title ASC")
      );
  }
  @Override
  public void onLoadFinished(Loader<Cursor>loader, Cursor cur){                                 //UT.lg("LF load finished");
    if (mLstAdptr != null) {
      mLstAdptr.mCursor = cur;
      select(mSelPos, NO_CLICK);
    }
  }
  @Override
  public void onLoaderReset(Loader<Cursor> loader) {                                              //UT.lg("LF kill loader");
    if (mLstAdptr != null)
      mLstAdptr.mCursor = null;
  }

  /**
   * select list item
   * @param selPos     position to be selected
   * @param clickType  selection mode (init / past rotation,  shortclick, longclick) - impacts behavior
   */
  private void select(int selPos, final int clickType) {                                     //UT.lg("LF select " + selPos);
    if (mAct == null || mLstAdptr.mCursor == null) return; //-------------->>>

    // trim position into cursor range (needed for prospective 'delete')
    int endPos = mLstAdptr.mCursor.getCount() - 1;
    mSelPos = selPos < 0 ? 0 : selPos > endPos ? endPos : selPos;
    if (mSelPos < 0) return; //--- empty cursor----------->>>

    mLstAdptr.mCursor.moveToPosition(mSelPos);
    String data = mLstAdptr.mCursor.getString(DataProvider.Contract.IDX_DATA);

    boolean needsHilite = true;  // no list selection/hilite needed if going to new actitvity
    if (data != null) switch (clickType) {
      case LONG_CLICK:  // starts fullscreen no matter what the orintation is
        needsHilite = false;
        mAct.startActivity(new Intent(mAct, DetailActivity.class).putExtra(DetailFragment.ITEM_ID, data));
        break;
      case SHORT_CLICK:
        if (UT.isLand())  // show detail in two-pane mode
          splitScreen(data);
        else {  // show detail in fullscreen mode
          needsHilite = false;
          mAct.startActivity(new Intent(mAct, DetailActivity.class).putExtra(DetailFragment.ITEM_ID, data));
        }
        break;
      case NO_CLICK: default:  // init, rotation, resume, ....
        if (UT.isLand())  // show detail in two-pane mode
          splitScreen(data);
    }

    if (mRecyclView != null && needsHilite) {
      // item selection/hilite, perform after view tree layout's finished
      mRecyclView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
          if (mRecyclView.getChildCount() > 0) {
            mRecyclView.getViewTreeObserver().removeOnPreDrawListener(this);   // heard you already, enough
            if (mLstAdptr != null) {
              LinearLayoutManager llMgr = (LinearLayoutManager) mRecyclView.getLayoutManager();
              if (mSelPos < llMgr.findFirstCompletelyVisibleItemPosition() ||
              mSelPos > llMgr.findLastCompletelyVisibleItemPosition() ) {
                View itmVw = llMgr.getChildAt(0);
                if (itmVw != null)
                  llMgr.scrollToPositionWithOffset(mSelPos, (llMgr.getHeight() - itmVw.getHeight())/2);
                else
                  llMgr.scrollToPosition(mSelPos);
              }
              mLstAdptr.notifyDataSetChanged();  // !!! err: Can not perform this action inside of on Load Finished
            }
            return true;
          }
          return false;
        }
      });
    }
  }

  // switch detail fragment in two-pane mode
  private void splitScreen(final String data) {
    new Thread(new Runnable() { @Override public void run() {
      Bundle args = new Bundle();
      args.putString(DetailFragment.ITEM_ID, data);
      DetailFragment fragment = new DetailFragment();
      fragment.setArguments(args);
      mAct.getFragmentManager().beginTransaction().replace(R.id.fl_detcontnr, fragment).commit();
    }}).start();
  }

  private class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHldr> {
    private Cursor mCursor;

    // construct this holder in  'on create view' !
    class ViewHldr extends RecyclerView.ViewHolder {
      final TextView titlVw;
      final LinearLayout titlLL;

      ViewHldr(View vwItem) { super(vwItem);
        titlLL = (LinearLayout)vwItem.findViewById(R.id.ll_titl);
        titlVw = (TextView)titlLL.findViewById(R.id.tv_titl);

        titlLL.setOnClickListener(new View.OnClickListener() {
          @Override public void onClick(View vw) {
            select(getAdapterPosition(), SHORT_CLICK);
          }
        });
        titlLL.setOnLongClickListener(new View.OnLongClickListener() {
          @Override public boolean onLongClick(View vw) {
            select(getAdapterPosition(), LONG_CLICK);
            return true;
          }
        });
      }
    }

    @Override
    public ViewHldr onCreateViewHolder(ViewGroup parent, int viewType) {                      //UT.lg("LA create " + mPos);
      return  !(parent instanceof RecyclerView) ? null:
      new ViewHldr(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHldr vwHldr, int pos) {                                             //UT.lg("LA bind ");
      if (mCursor.moveToPosition(pos)) {
        String titl = mCursor.getString(DataProvider.Contract.IDX_TITL);
        if (pos == mSelPos && UT.isLand())   // modify current item text
          titl += "  longtouch for fullscreen";
        vwHldr.titlVw.setText(titl);
      }
      vwHldr.titlLL.setSelected(pos == mSelPos);   // (un)hilite if (not)selected
    }

    @Override
    public int getItemCount() {
      return mCursor == null ? 0 : mCursor.getCount();
    }

  }
}


package com.spjanson.sqlistrecycler;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

// MUST BE DECLARED IN MANIFEST
public class DataProvider extends ContentProvider {
  private static final int DATENAME = 100;
  private static final int DATENAME_ID = 101;

  private static Data mDB;

  @Override
  public boolean onCreate() {
    Context ctx = getContext();
    UT.init(ctx);
    if (mDB != null)
      mDB.close();
    mDB = new Data(UT.acx, null);
    return true;
  }
  /**
   * Content Provider required CRUD method
   * CAUTION! if you use non-null projection, adjust column indices accordingly !!!
   * @param uri URI  (content://com.andyscan.andyscan/names)
   * @param proj projection (new String[] {NamesTable.COL_TITL, NamesTable.COL_RSID, ... }
   * @param sel selection ("NamesTable.COL_TITL = ?")
   * @param selArgs selection arguments (new String[] {"140131-101112"})
   * @param sort sort sort_order (NamesTable.COL_TITL  ASC)
   * @return a cursor of the result set
   */
  @Override
  public Cursor query(Uri uri, String[] proj, String sel, String[] selArgs, String sort) {
    Cursor retCursor = null;
    if (mDB != null) {
      switch (uriMatcher().match(uri)) {
        case DATENAME_ID: {
          retCursor = mDB.getReadableDatabase().query(
            Contract.Tbl.TBL_TEMP,
            proj,
            Contract.Tbl._ID + " = '" + ContentUris.parseId(uri) + "'",
            null,
            null, null, sort
          );
          break;
        }
        case DATENAME: {
          retCursor = mDB.getReadableDatabase().query(Contract.Tbl.TBL_TEMP, proj, sel, selArgs, null, null, sort);
          break;
        }
        default: throw new UnsupportedOperationException("Unknown uri: " + uri);
      }
      retCursor.setNotificationUri(UT.cr, uri);
    }
    return retCursor;
  }
  /**
   * Content Provider required CRUD method
   * @param uri URI (content://com.andyscan.andyscan/names)
   * @param vls values ("tags=water utility" "resid=w_0B1mQ" "name=140131-101112")
   * @return an URI  (content://com.andyscan.andyscan/names/1)
   */
  @Override
  public Uri insert(Uri uri, ContentValues vls) {
    Uri returnUri = null;
    if (mDB != null) {
      final SQLiteDatabase db = mDB.getWritableDatabase();
      final int match = uriMatcher().match(uri);
      switch (match) {
        case DATENAME: {
          long id = db.insert(Contract.Tbl.TBL_TEMP, null, vls);
          if (id > 0L) {
            returnUri = Contract.buildUri(id);
            UT.cr.notifyChange(uri, null);                                                               //UT.lg("insert");
          } else {
            throw new android.database.SQLException("Insert fail " + uri);
          }
          break;
        }
        default: throw new UnsupportedOperationException("Unknown uri: " + uri);
      }
    }
    return returnUri;
  }
  /**
   * Content Provider required SCRUD method
   * @param uri URI  (content://com.andyscan.andyscan/names)
   * @param sel selection  ("NamesTable.COL_TITL = ?")
   * @param selArgs selection arguments (new String[] {"140131-101112"})
   * @return number of rows  (1)
   */
  @Override
  public int delete(Uri uri, String sel, String[] selArgs) {
    int rowsDeleted = 0;
    if (mDB != null) {
      final SQLiteDatabase db = mDB.getWritableDatabase();
      final int match = uriMatcher().match(uri);
      switch (match) {
        case DATENAME:
          rowsDeleted = db.delete(Contract.Tbl.TBL_TEMP, sel, selArgs);
          if (rowsDeleted != 0) {
            UT.cr.notifyChange(uri, null);                                                               //UT.lg("delete");
          }
          break;
        default: throw new UnsupportedOperationException("Unknown uri: " + uri);
      }
    }
    return rowsDeleted;
  }
  /**
   * Content Provider required SCRUD method
   * @param uri URI (content://com.andyscan.andyscan/names)
   * @param vls values to update ("_id=2" "tags=gas bill" "name=151413-121110" "resid=g_W2aFJ")
   * @param sel selection          ("NamesTable.COL_TITL = ?")  (_id= ?)
   * @param selArgs selection arguments  (new String[]{"140131-101112"})  (new String[]{"2"})
   * @return number of rows
   */
  @Override
  public int update(Uri uri, ContentValues vls, String sel, String[] selArgs) {
    int rowsUpdated = 0;
    if (mDB != null) {
      final SQLiteDatabase db = mDB.getWritableDatabase();
      final int match = uriMatcher().match(uri);
      switch (match) {
        case DATENAME:
          rowsUpdated = db.update(Contract.Tbl.TBL_TEMP, vls, sel, selArgs);
          break;
        default: throw new UnsupportedOperationException("Unknown uri: " + uri);
      }
      if (rowsUpdated != 0) {
        UT.cr.notifyChange(uri, null);                                                                   //UT.lg("update");
      }
    }
    return rowsUpdated;
  }
  /**
   * Content Provider required method
   * @param uri URI  (content://com.andyscan.andyscan/names)
   * @return type   (vnd.android.cursor.dir/com...sunshine.app/[weather|location])
   */
  @Override
  public String getType(Uri uri) {
    final int match = uriMatcher().match(uri);
    switch (match) {
      case DATENAME:      return Contract.LIST_TYPE + UT.auth + "/" + Contract.Tbl.TBL_TEMP;
      case DATENAME_ID:   return Contract.ITEM_TYPE + UT.auth + "/" + Contract.Tbl.TBL_TEMP;
      default: throw new UnsupportedOperationException("Unknown uri: " + uri);
    }
  }

  /**
   * Content Provider OPTIONAL, but quicker
   * @param uri URI (content://com.andyscan.andyscan/names)
   * @param vlss values to update ( )
   * @return number of rows
   */
  @Override
  public int bulkInsert(Uri uri, @NonNull  ContentValues[] vlss) {
    int recCnt = 0;
    if (mDB != null) {
      final int match = uriMatcher().match(uri);
      switch (match) {
        case DATENAME: {
          final SQLiteDatabase db = mDB.getWritableDatabase();
          db.beginTransaction();
          try {
            for (ContentValues vls : vlss) {
              if (vls.getAsString(Contract.Tbl.COL_TITL) != null) {
                if (-1 != db.insert(Contract.Tbl.TBL_TEMP, null, vls)) {
                  recCnt++;
                }
              }
            }
            db.setTransactionSuccessful();
          } finally {
            db.endTransaction();
          }
          UT.cr.notifyChange(uri, null);                                                          //UT.lg("bulk " + recCnt);
          break;
        }
        default:
          recCnt = super.bulkInsert(uri, vlss);
          break;
      }
    }
    return recCnt;
  }

  private static UriMatcher uriMatcher() {
    final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
    matcher.addURI(UT.auth, Contract.Tbl.TBL_TEMP + "/#", DATENAME_ID);
    matcher.addURI(UT.auth, Contract.Tbl.TBL_TEMP, DATENAME);
    return matcher;
  }

  // DBASE
  final static class Data extends SQLiteOpenHelper {
    private static final int DB_VER = 1;

    Data(Context ctx, String name) { super(ctx, name, null, DB_VER);}

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
      sqLiteDatabase.execSQL( "CREATE TABLE " + Contract.Tbl.TBL_TEMP + " (" +
      Contract.Tbl._ID + " INTEGER PRIMARY KEY," +
      Contract.Tbl.COL_TITL + " TEXT UNIQUE NOT NULL," +
      Contract.Tbl.COL_DATA + " TEXT );"
      );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
      sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Contract.Tbl.TBL_TEMP);
      onCreate(sqLiteDatabase);
    }

    static ContentValues contVals(String name, String cont) {
      ContentValues values = new ContentValues();
      if (name != null) values.put(Contract.Tbl.COL_TITL, name);
      if (cont != null) values.put(Contract.Tbl.COL_DATA, cont);
      return values;
    }
  }

  // CONTRACT
  final static class Contract { private Contract(){}
    private static final String LIST_TYPE = "vnd.android.cursor.dir/";
    private static final String ITEM_TYPE = "vnd.android.cursor.item/";

    static final int IDX_ID = 0;
    static final int IDX_TITL = 1;
    static final int IDX_DATA = 2;
    static final String[] COLS = {
    Tbl._ID,
    Tbl.COL_TITL,
    Tbl.COL_DATA,
    };

    static final class Tbl implements BaseColumns {
      static final String TBL_TEMP = "temp";
      static final String COL_TITL = "titl";
      static final String COL_DATA = "data";

    }

    static Uri tbl() {return Uri.parse("content://" + UT.auth).buildUpon().appendPath(Tbl.TBL_TEMP).build(); }
    static Uri buildUri(long id) { return ContentUris.withAppendedId(tbl(), id); }
    static long parseUri(Uri uri) { return ContentUris.parseId(uri);  }
  }
}

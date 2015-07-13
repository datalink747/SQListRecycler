# SQListRecycler
A basic portrait / landscape RecyclerView backed by a SQLite table.

# SQListRecycler

Demo of Android RecyclerView connected to SQLite using recommended architecture. The demo also shows handling of one-pane / two-pane fragment modes throughout orientation changes. The behaviour is shown on [YouTube - https://youtu.be/lME4ZZI5pBU](https://youtu.be/lME4ZZI5pBU) and you can download, install and run the APK (needs no permissions) to test the concept before you dig into the code. 

The solution itself is trivial and broadly published. The only intelligence I added can be seen in 'ListFragment.select()' method. It handles an aspect I could not find elsewhere, that is the ability to maintain (and center) the currently selected item throughout the orientation changes.

The code is built using Android Studio 1.2.2. For dependencies, see the 'app/build.gradle' file.

Included Java modules:

##### UT
Utilities, methods/constants used throughout the application

##### ListActivity  (main - launch activity)
Main application activity.
* 1/ loads content view
* 2/ builds a dummy SQLite table to be used in the demo
* 3/ instantiates a list fragment used to display SQLite table's items

##### ListFragment
A fragment that implements LaoderManager and handles item selection, invoking detail view based on current orientation (portrait / landscape).

In PORTRAIT mode, it always creates a new detail activity and plugs detail fragment into this activity.
In LANDSCAPE mode, there are two options:
  - standard selection displays detail fragment in the right pane
  - enhanced selection (long-touch) mimics portrait mode by plugging detail fragment into a new detail activity.

An important function of this class is to maintain current list position across orientation changes. It uses a local variable (mSelPos) to save/restore the cursor position.     
  
Included is a 'ListAdapter'class, a standard RecyclerView Adapter with a ViewHolder. The list items are being build here and it invokes the LstFrag's 'select' method upon user's action (touch / long-touch).

##### DetailActivity
Activity to display the detail in full screen. Does nothing, just passes the arguments to the detail fragment that is plugged into this activity.   

##### DetailFragment
Fragment that appears either on the right side of two-pane layout (landscape), or in the 'DetailActivity' activity upon list item's selection. This is the place where all the detail presentation work is done (writing text, displaying image, ...).  

##### DataProvider
A standard SQLite content provider that feeds the cursor.  

Included are 2 classes:
  - Class 'Data' derived from SQLitOpenHeper
  - Class 'Contract' defines database schema

Good Luck


package com.common_id.campusguide;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.common_id.campusguide.Adapter.ClassroomAdapter;

import java.lang.reflect.Field;

public class UserSearchActivity extends AppCompatActivity {

    public static final String MyPreferences = "Myprefs";
    //    public static final String GeneralPref = "Generalprefs";
    public static final String mSearchList = "searchlistKey";

    private SearchView searchView;
    private ImageView closeButton;
//    private RecyclerView recyclerView;
//    private SearchAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_search);

        Toolbar mytoolbar = (Toolbar) findViewById(R.id.mCustomToolbar);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        setSupportActionBar(mytoolbar); //mempersiapkan toolbar
        getSupportActionBar().setTitle("");
        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        //Searching the close button on SearchView
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        ImageView mSearchCloseButton = null;
        try {
            Field searchField = SearchView.class.getDeclaredField("mCloseButton");
            searchField.setAccessible(true);
            mSearchCloseButton = (ImageView) searchField.get(mSearchView);
            closeButton = (ImageView) searchField.get(mSearchView);
        } catch (Exception e) {
            Log.e("Description", "Error finding close button");
        }

        if (mSearchCloseButton != null) {
//            mSearchCloseButton.setEnabled(false);
            mSearchCloseButton.setVisibility(View.GONE);
        }

        // listening to search query text change
        final ImageView finalMSearchCloseButton = mSearchCloseButton;

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean queryTextFocused) {
                if (!queryTextFocused) {
                    MenuItemCompat.collapseActionView(searchItem);
                }
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
//                filter recycler view when query submitted
//                mAdapter.getFilter().filter(query);

                finish();
                Intent intent = new Intent(UserSearchActivity.this, UserResultActivity.class);
                Bundle extras = new Bundle();
                extras.putString("search", query);
                intent.putExtras(extras);
                startActivity(intent);
                overridePendingTransition(0, 0);

                return true; //return true means you will be directed to other activity
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                MenuItemCompat.collapseActionView(searchItem);
//                mAdapter.getFilter().filter(query);
                if (finalMSearchCloseButton != null) {
//                    finalMSearchCloseButton.setEnabled(true);
                    //when the text is empty hide the close "x" button
                    finalMSearchCloseButton.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                }
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        switch (item.getItemId()) {
            case R.id.action_search:
                break;
            case android.R.id.home:
                finish(); //go back to previous activity or fragment
                overridePendingTransition(0, 0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_search).expandActionView();
        searchView.setIconified(false);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        ImageView mSearchCloseButton = null;
        try {
            Field searchField = SearchView.class.getDeclaredField("mCloseButton");
            searchField.setAccessible(true);
            mSearchCloseButton = (ImageView) searchField.get(mSearchView);
        } catch (Exception e) {
            Log.e("Description", "Error finding close button");
        }

        if (mSearchCloseButton != null) {
//            mSearchCloseButton.setEnabled(false);
            mSearchCloseButton.setVisibility(View.GONE);
        }
        return super.onPrepareOptionsMenu(menu);

    }

//    @Override
//    public void onBackPressed() {
////         close search view on back button pressed
////        if (!searchView.isIconified()) {
////            searchView.setIconified(true);
////            return;
////        }
//        View view = this.getCurrentFocus();
//        if (view != null) {
//            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
//        }
//        searchView.setVisibility(View.GONE);
//        finish();
//        overridePendingTransition(0, 0);
//    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getKeyCode() == KeyEvent.ACTION_DOWN) {
//            View view = this.getCurrentFocus();
//            if (view != null) {
//                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
//            }
            finish();
            overridePendingTransition(0, 0);
            return true;
        }
        return super.dispatchKeyEvent(event);
    }


//    @Override
//    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
//            finish();
//            return true;
//        }
//        return false;
//    }
}

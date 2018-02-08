package com.example.lixiang.musicplayer;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.polaric.colorful.CActivity;

import static com.example.lixiang.musicplayer.R.id.recent;
import static com.example.lixiang.musicplayer.R.id.settings_toolbar;

public class searchActivity extends CActivity implements SearchView.OnQueryTextListener{
    private SearchView searchView;
    private searchAdapter mSearchAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar search_toolbar = (Toolbar) findViewById(R.id.search_toolbar);
        search_toolbar.setTitleTextColor(getResources().getColor(R.color.colorCustomAccent));
        search_toolbar.setTitle("搜索");
        setSupportActionBar(search_toolbar);//设置返回键可用
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView search_recycler_view = (RecyclerView) findViewById(R.id.search_RecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        search_recycler_view.setLayoutManager(layoutManager);
        search_recycler_view.setItemAnimator(new DefaultItemAnimator());
        mSearchAdapter = new searchAdapter();
        search_recycler_view.setAdapter(mSearchAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem search = menu.findItem(R.id.search_menu);
        searchView = (SearchView) MenuItemCompat.getActionView(search);
        searchView.onActionViewExpanded();
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mSearchAdapter.getFilter().filter(newText);
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }
}

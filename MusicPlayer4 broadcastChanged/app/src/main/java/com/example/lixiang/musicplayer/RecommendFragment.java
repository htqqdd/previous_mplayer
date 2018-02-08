package com.example.lixiang.musicplayer;


import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static android.content.ContentValues.TAG;
import static com.example.lixiang.musicplayer.R.id.recent;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecommendFragment extends Fragment {

    private PermissionReceiver permissionReceiver;


    public RecommendFragment() {
        // Required empty public constructor
    }
    public View rootView;


    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(permissionReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_recommend, container, false);

        //动态注册广播
        permissionReceiver = new PermissionReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("permission_granted");
        getActivity().registerReceiver(permissionReceiver, intentFilter);

        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            new initialTask().execute();
        }else {
        }
        return rootView;




    }


    private class PermissionReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            new initialTask().execute();
        }
    }
    private void showRecentList(){
        RecyclerView recent = (RecyclerView) rootView.findViewById(R.id.recent_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recent.setLayoutManager(layoutManager);
        recent.setItemAnimator(new DefaultItemAnimator());
        RecentListAdapter adapter = new RecentListAdapter();
        recent.setAdapter(adapter);
    }
    private void showFavouriteList(){
        RecyclerView favourite = (RecyclerView) rootView.findViewById(R.id.favourite_recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(),3);
        favourite.setLayoutManager(layoutManager);
        favourite.setItemAnimator(new DefaultItemAnimator());
        FavouriteListAdapter adapter = new FavouriteListAdapter();
        favourite.setAdapter(adapter);
        //与Scrollview滑动冲突
        favourite.setNestedScrollingEnabled(false);
        favourite.setHasFixedSize(true);
//        RecyclerView favourite = (RecyclerView) rootView.findViewById(R.id.favourite_recycler_view);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
//        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
//        favourite.setLayoutManager(layoutManager);
//        favourite.setItemAnimator(new DefaultItemAnimator());
//        FavouriteListAdapter adapter = new FavouriteListAdapter();
//        favourite.setAdapter(adapter);
    }

    public static void openSAF(Activity context) {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        context.startActivityForResult(intent, 42);

    }

    private class initialTask extends AsyncTask{
        @Override
        protected Object doInBackground(Object[] objects) {
            Data.initialMusicInfo(getActivity());
            Data.initialMusicDate(getActivity());
            Data.initialMusicPlaytimes(getActivity());
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            showRecentList();
            showFavouriteList();
        }
    }
}

package com.example.lixiang.musicplayer;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import static com.example.lixiang.musicplayer.Data.playAction;

/**
 * Created by lixiang on 2017/8/3.
 */

public class FastScrollListAdapter extends RecyclerView.Adapter<FastScrollListAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter{
    private Context mContext;
    private String[] media_music_info;
    private Cursor cursor;
    private int[] _ids;
    private int[] _albumids;
    private String[] _artists;// 保存艺术家
    private String[] _titles;
    private String[] _data;// 标题临时数组
    private String[] _album;
    private int[] _duration;
    private int total;

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mContext = recyclerView.getContext();
        initialMusicInfo(recyclerView.getContext());

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(LayoutInflater.from(
                mContext).inflate(R.layout.musiclist, parent,
                false));
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final int Position = position;
        holder.song.setText(_titles[position]);
        holder.singer.setText(_artists[position]);
        //设置歌曲封面
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri uri = ContentUris.withAppendedId(sArtworkUri, _albumids[position]);
        Glide
                .with(mContext)
                .load(uri)
                .placeholder(R.drawable.default_album)
                .into(holder.cover);
        //处理整个点击事件
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //播放
                Data.setPlayMode(3);
                Data.setRecent(false);
                Data.setFavourite(false);
                Data.setPosition(position);
                Intent intent = new Intent("service_broadcast");
                intent.putExtra("ACTION", playAction);
                mContext.sendBroadcast(intent);
            }
        });
        //处理菜单点击
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //弹出菜单
                menu_util.popupMenu((Activity) mContext, view, position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView cover;
        ImageView button;
        TextView song;
        TextView singer;
        RelativeLayout relativeLayout;

        public ViewHolder(View view) {
            super(view);
            relativeLayout = (RelativeLayout) view;
            cover = (ImageView) view.findViewById(R.id.list_cover);
            button = (ImageView) view.findViewById(R.id.list_button);
            song = (TextView) view.findViewById(R.id.list_song);
            singer = (TextView) view.findViewById(R.id.list_singer);
        }
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        String s = _titles[position];
        return s.subSequence(0,1).toString();
    }

    private void initialMusicInfo(Context context) {
        //初始化音乐信息
        media_music_info = new String[]{
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.ALBUM};

        cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, media_music_info,
                null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        total = cursor.getCount();
        cursor.moveToFirst();// 将游标移动到初始位置
        _ids = new int[total];// 返回int的一个列
        _artists = new String[total];// 返回String的一个列
        _titles = new String[total];// 返回String的一个列
        _data = new String[total];
        _albumids = new int[total];
        _album = new String[total];
        _duration = new int[total];
        for (int i = 0; i < total; i++) {
            _ids[i] = cursor.getInt(3);
            _titles[i] = cursor.getString(0);
            _artists[i] = cursor.getString(2);
            _data[i] = cursor.getString(4);
            _albumids[i] = cursor.getInt(5);
            _album[i] = cursor.getString(6);
            _duration[i] = cursor.getInt(1);
            cursor.moveToNext();// 将游标移到下一行
        }
    }
}

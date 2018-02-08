package com.example.lixiang.musicplayer;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
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

public class FastScrollListAdapter extends FastScrollRecyclerView.Adapter<FastScrollListAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter{
    private Context mContext;
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        ViewHolder holder = new ViewHolder(LayoutInflater.from(
                mContext).inflate(R.layout.musiclist, parent,
                false));
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final int Position = position;
        holder.song.setText(Data.getTitle(position));
        holder.singer.setText(Data.getArtist(position));
        //设置歌曲封面
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri uri = ContentUris.withAppendedId(sArtworkUri, Data.getAlbumId(position));
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
        return Data.getCursor().getCount();
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
        String s = Data.getTitle(position);
        return s.subSequence(0,1).toString();
    }
}

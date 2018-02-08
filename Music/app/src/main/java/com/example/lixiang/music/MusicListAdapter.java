package com.example.lixiang.music;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import static com.example.lixiang.music.getCover.getArtwork;

/**
 * Created by lixiang on 2017/3/5.
 */


public class MusicListAdapter extends BaseAdapter{

    private Context myContext;
    private Cursor myCursor;
    private int pos = -1;

    public MusicListAdapter(Context context, Cursor cursor) {
        myContext = context;
        myCursor = cursor;
    }

    @Override
    public int getCount() {
        return myCursor.getCount();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View listItemView;
        ViewHolder viewHolder;

        if (convertView == null) {
            listItemView = LayoutInflater.from(myContext).inflate(R.layout.musiclist, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.music_singer_holder = (TextView) listItemView.findViewById(R.id.list_singer);
            viewHolder.music_title_holder = (TextView) listItemView.findViewById(R.id.list_song);
            viewHolder.music_cover_holder = (ImageView) listItemView.findViewById(R.id.list_cover);
            listItemView.setTag(viewHolder);
        }else{
            listItemView = convertView;
            viewHolder = (ViewHolder) listItemView.getTag();
        }
        myCursor.moveToPosition(position);

        TextView music_title = (TextView) listItemView.findViewById(R.id.list_song);
        TextView music_singer = (TextView) listItemView.findViewById(R.id.list_singer);

        //设置歌名
            viewHolder.music_title_holder.setText(myCursor.getString(0).trim());
        //设置歌手
            viewHolder.music_singer_holder.setText(myCursor.getString(2));

        //设置歌曲封面
            Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
            Uri uri = ContentUris.withAppendedId(sArtworkUri, myCursor.getInt(6));
            Glide
                    .with(myContext)
                    .load(uri)
                    .placeholder(R.drawable.isplaying)
                    .into(viewHolder.music_cover_holder);

        return listItemView;
    }
class ViewHolder{
    ImageView music_cover_holder;
    TextView music_singer_holder;
    TextView music_title_holder;
}

}
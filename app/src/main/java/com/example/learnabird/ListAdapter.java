package com.example.learnabird;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.learnabird.AsyncTasks.AsyncLoadImage;

public class ListAdapter extends ArrayAdapter {

    private String[] birdNames;
    private String[] birdPics;
    private Context context;

    public ListAdapter(Context context, String[] names, String[] pics) {
        super(context, R.layout.list_view_item);
        this.birdNames = names;
        this.birdPics = pics;
        this.context = context;
    }

    @Override
    public int getCount(){
        return birdNames.length;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = new ViewHolder();
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_view_item, parent, false);
            viewHolder.mBird = convertView.findViewById(R.id.imgView_list_item_img);
            viewHolder.mName = convertView.findViewById(R.id.txt_lst_itemName);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder)convertView.getTag();
        }
        new AsyncLoadImage(viewHolder.mBird).execute(birdPics[position]);
        viewHolder.mName.setText(birdNames[position]);
        return convertView;
    }

    static class ViewHolder{
        ImageView mBird;
        TextView mName;
    }

}

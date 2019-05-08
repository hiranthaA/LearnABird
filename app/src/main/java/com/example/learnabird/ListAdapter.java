package com.example.learnabird;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.learnabird.AsyncTasks.AsyncLoadImage;

import java.io.File;
import java.io.IOException;

public class ListAdapter extends ArrayAdapter {

    private String[] birdNames;
    private String[] birdPics;
    private String[] locations;
    private Context context;
    private String storage="/storage/emulated/0/Android/data/com.example.learnabird/files/Pictures/";
    public ListAdapter(Context context, String[] names, String[] pics, String[] locations) {
        super(context, R.layout.list_view_item);
        this.birdNames = names;
        this.birdPics = pics;
        this.locations = locations;
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
        if(locations[position].equals("api")) {
            new AsyncLoadImage(viewHolder.mBird).execute(birdPics[position]);
        }
        else{
            //load data from db
            //Bitmap bitmap = BitmapFactory.decodeFile(storage+birdPics[position]);
            //viewHolder.mBird.setImageBitmap(bitmap);
            Bitmap bitmap = resizeBitmap(storage+birdPics[position]);
            viewHolder.mBird.setImageBitmap(bitmap);
        }
        viewHolder.mName.setText(birdNames[position]);
        return convertView;
    }

    static class ViewHolder{
        ImageView mBird;
        TextView mName;
    }

    public Bitmap resizeBitmap(String path){
        int DESIREDWIDTH = 100;
        int DESIREDHEIGHT = 100;
        File file = new File(path);
        Bitmap bitmap = ScalingUtilities.decodeFile(file,DESIREDWIDTH,DESIREDHEIGHT, ScalingUtilities.ScalingLogic.FIT);
        if (!(bitmap.getWidth() <= DESIREDWIDTH && bitmap.getHeight() <= DESIREDHEIGHT)) {
            // Part 2: Scale image
            bitmap = ScalingUtilities.createScaledBitmap(bitmap, DESIREDWIDTH, DESIREDHEIGHT, ScalingUtilities.ScalingLogic.FIT);
        } else {
            //bitmap.recycle();
            bitmap = ScalingUtilities.createScaledBitmap(bitmap, DESIREDWIDTH, DESIREDHEIGHT, ScalingUtilities.ScalingLogic.FIT);
        }
        return bitmap;
    }

}

package com.example.learnabird;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ListAdapter extends ArrayAdapter {

    private String[] birdNames;
    private String[] birdPics;
    private String[] locations;
    private Context context;
    private Map<String,Bitmap> imgCacheMap = new HashMap<>();

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

        if(imgCacheMap.containsKey(birdPics[position])) {
                Bitmap bitmap = imgCacheMap.get(birdPics[position]);
                viewHolder.mBird.setImageBitmap(bitmap);
        }else{
            if (locations[position].equals("api")) {
                new AsyncLoadImage(viewHolder.mBird).execute(birdPics[position]);
            } else {
                //load data from db
                Bitmap bitmap = resizeBitmap(storage + birdPics[position]);
                imgCacheMap.put(birdPics[position],bitmap);
                viewHolder.mBird.setImageBitmap(bitmap);
            }
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
            //Scale image
            bitmap = ScalingUtilities.createScaledBitmap(bitmap, DESIREDWIDTH, DESIREDHEIGHT, ScalingUtilities.ScalingLogic.FIT);
        } else {
            //bitmap.recycle();
            bitmap = ScalingUtilities.createScaledBitmap(bitmap, DESIREDWIDTH, DESIREDHEIGHT, ScalingUtilities.ScalingLogic.FIT);
        }
        return bitmap;
    }

    public class AsyncLoadImage extends AsyncTask<String, Void, Bitmap> {

        ImageView target;
        ProgressDialog progressDialog;
        String position;

        public AsyncLoadImage(ImageView imageView){
            this.target = imageView;
            progressDialog = MainActivity.progressDialog;
        }

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Loading data...");
            progressDialog.show();
        }

        @Override
        protected Bitmap doInBackground(String... images) {

            position = images[0];
            Bitmap result = null;
            try {
                URL url = new URL(MainActivity.host +position);
                InputStream in = url.openStream();
                result = BitmapFactory.decodeStream(in);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            target.setImageBitmap(bitmap);
            imgCacheMap.put(position,bitmap);
            progressDialog.dismiss();
        }
    }
}

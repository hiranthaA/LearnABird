package com.example.learnabird;

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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class ListAdapter extends ArrayAdapter {

    private String[] birdNames;
    private String[] birdPics;
    private Context context;
    private String hostUrl;

    public ListAdapter(Context context, String[] names, String[] pics, String hostUrl) {
        super(context, R.layout.list_view_item);
        this.birdNames = names;
        this.birdPics = pics;
        this.context = context;
        this.hostUrl = hostUrl;
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
        new LoadImage(viewHolder.mBird).execute(birdPics[position]);
        viewHolder.mName.setText(birdNames[position]);
        return convertView;
    }

    static class ViewHolder{
        ImageView mBird;
        TextView mName;
    }

    public class LoadImage extends AsyncTask<String, Void, Bitmap> {

        ImageView target;

        public LoadImage(ImageView imageView){
            this.target = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... images) {

            Bitmap result = null;
            try {
                URL url = new URL(hostUrl+images[0]);
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
        }
    }

}

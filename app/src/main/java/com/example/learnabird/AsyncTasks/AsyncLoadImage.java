package com.example.learnabird.AsyncTasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.example.learnabird.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class AsyncLoadImage extends AsyncTask<String, Void, Bitmap> {

    ImageView target;

    public AsyncLoadImage(ImageView imageView){
        this.target = imageView;
    }

    @Override
    protected Bitmap doInBackground(String... images) {

        Bitmap result = null;
        try {
            URL url = new URL(MainActivity.host +images[0]);
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

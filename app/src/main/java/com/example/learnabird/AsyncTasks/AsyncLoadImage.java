package com.example.learnabird.AsyncTasks;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.learnabird.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/*
* AsyncLoadImage
* Load images from a provide url
* this is a Background task
*/
public class AsyncLoadImage extends AsyncTask<String, Void, Bitmap> {

    ImageView target;
    ProgressDialog progressDialog;

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
        progressDialog.dismiss();
    }
}

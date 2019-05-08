package com.example.learnabird;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.learnabird.AsyncTasks.AsyncLoadImage;

import java.io.IOException;

public class DetailActivity extends AppCompatActivity {

    private ImageView iv_imgPreview;
    private TextView tv_birdName;
    private TextView tv_birdDetails;
    private Button btn_playSound;
    private MediaPlayer mediaPlayer;
    private int birdId;
    private String location;
    private String pic;
    private String sound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        btn_playSound = findViewById(R.id.btn_play);
        iv_imgPreview = findViewById(R.id.imgPreview);
        tv_birdName = findViewById(R.id.txt_BirdName);
        tv_birdDetails = findViewById(R.id.txt_birdDetails);
        tv_birdDetails.setMovementMethod(new ScrollingMovementMethod());

        final Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            birdId = bundle.getInt("birdId");
            tv_birdName.setText(bundle.getString("birdName"));
            location = bundle.getString("location");
            pic = bundle.getString("birdPic");
            sound = bundle.getString("birdSound");

            tv_birdDetails.setText(bundle.getString("birdDetails"));
            getSupportActionBar().setTitle("Learn A Bird : " + bundle.getString("birdName"));
            mediaPlayer = new MediaPlayer();

            if(location.equals("api")){
                new AsyncLoadImage(iv_imgPreview).execute(bundle.getString("birdPic"));
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                try {
                    mediaPlayer.setDataSource(MainActivity.host+bundle.getString("birdSound"));
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else{
                //load images form storage
                Bitmap bitmap = BitmapFactory.decodeFile(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath()+"/"+pic);
                iv_imgPreview.setImageBitmap(bitmap);
                try {
                    mediaPlayer.setDataSource(getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsolutePath()+"/"+sound);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        btn_playSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.start();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.pause();
        mediaPlayer.release();
    }
}



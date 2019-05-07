package com.example.learnabird;

import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.learnabird.AsyncTasks.AsyncLoadImage;

public class DetailActivity extends AppCompatActivity {

    private ImageView iv_imgPreview;
    private TextView tv_birdName;
    private TextView tv_birdDetails;
    private Button btn_playSound;
    private MediaPlayer mediaPlayer;
    private int birdId;

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

            new AsyncLoadImage(iv_imgPreview).execute(bundle.getString("birdPic"));
            tv_birdDetails.setText(bundle.getString("birdDetails"));
            getSupportActionBar().setTitle("Learn A Bird : " + bundle.getString("birdName"));
            mediaPlayer = (MediaPlayer) MediaPlayer.create(this, bundle.getInt("birdSound"));
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



package com.example.learnabird;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class DetailActivity extends AppCompatActivity {

    private ImageView iv_imgPreview;
    private TextView tv_birdName;
    private TextView tv_birdDetails;
    private Button btn_playSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        btn_playSound = findViewById(R.id.btn_play);
        iv_imgPreview = findViewById(R.id.imgPreview);
        tv_birdName = findViewById(R.id.txt_BirdName);
        tv_birdDetails = findViewById(R.id.txt_birdDetails);
        tv_birdDetails.setMovementMethod(new ScrollingMovementMethod());

        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){
            tv_birdName.setText(bundle.getString("birdName"));
            iv_imgPreview.setImageResource(bundle.getInt("birdPic"));
            tv_birdDetails.setText(bundle.getString("birdDetails"));
            getSupportActionBar().setTitle("Learn A Bird : "+bundle.getString("birdName"));
        }
    }
}

package com.example.learnabird;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class DetailActivity extends AppCompatActivity {

    ImageView iv_imgPreview;
    TextView tv_birdName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        iv_imgPreview = findViewById(R.id.imgPreview);
        tv_birdName = findViewById(R.id.txt_BirdName);

        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){
            tv_birdName.setText(bundle.getString("birdName"));
            iv_imgPreview.setImageResource(bundle.getInt("birdPic"));
            getSupportActionBar().setTitle("Learn A Bird : "+bundle.getString("birdName"));
        }
    }
}

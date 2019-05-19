package com.example.learnabird;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.learnabird.AsyncTasks.AsyncLoadImage;

import java.io.File;
import java.io.IOException;

public class DetailActivity extends AppCompatActivity {

    private ImageView iv_imgPreview;
    public TextView tv_birdName;
    private TextView tv_birdDetails;
    private Button btn_playSound;
    private MediaPlayer mediaPlayer;
    private int birdId;
    private String location;
    private String pic;
    private String sound;
    private DatabaseHelper db;
    Boolean isViewChanged = false;
    private static final int EDIT_DETAILS_REQUEST_CODE=2000;
    private static final int DETAIL_ACTIVITY_REQUEST_CODE=3000;

    /*
    Construct the view on onCreate state
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        db = new DatabaseHelper(this);

        btn_playSound = findViewById(R.id.btn_play);
        iv_imgPreview = findViewById(R.id.imgPreview);
        tv_birdName = findViewById(R.id.txt_BirdName);
        tv_birdDetails = findViewById(R.id.txt_birdDetails);
        tv_birdDetails.setMovementMethod(new ScrollingMovementMethod());

        //initialize with data from the intent
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
                //load images and sound files form storage
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
    public boolean onCreateOptionsMenu(Menu menu) {
        if(location.equals("db")) {
            getMenuInflater().inflate(R.menu.menu_detail_activity, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_delete:
                AlertDialog.Builder builder = new AlertDialog.Builder(DetailActivity.this);
                builder.setTitle("")
                        .setMessage("Want to delete this content?")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                            public void onClick(DialogInterface dialog, int which) {
                                File targetPhoto = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), pic);
                                File targetSound = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), sound);
                                if(targetPhoto.delete() && targetSound.delete() && db.deleteBird(birdId)){
                                    Toast.makeText(DetailActivity.this,"Deleted successfully.",Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent();
                                    setResult(DETAIL_ACTIVITY_REQUEST_CODE,intent);
                                    if(mediaPlayer!=null & mediaPlayer.isPlaying()){
                                        mediaPlayer.stop();
                                    }
                                    finish();
                                }
                                else{
                                    Toast.makeText(DetailActivity.this,"Cannot Delete!",Toast.LENGTH_SHORT).show();
                                }
                            }
                            })
                        .setNegativeButton("Cancel",null);
                AlertDialog alert = builder.create();
                alert.show();
                return true;
            case R.id.action_edit:
                Intent intent = new Intent(DetailActivity.this,EditDetails.class);
                intent.putExtra("birdId",birdId);
                intent.putExtra("birdName",tv_birdName.getText().toString());
                intent.putExtra("birdImageName",pic);
                intent.putExtra("birdSound",sound);
                intent.putExtra("birdInfo",tv_birdDetails.getText().toString());
                intent.putExtra("from","details");
                startActivityForResult(intent, EDIT_DETAILS_REQUEST_CODE);
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed
        if(resultCode == EDIT_DETAILS_REQUEST_CODE) {
            {
                tv_birdName.setText(data.getStringExtra("updatedName"));
                tv_birdDetails.setText(data.getStringExtra("updatedInfo"));
                pic = data.getStringExtra("updatedPhoto");
                sound = data.getStringExtra("updatedSound");
                Bitmap bitmap = BitmapFactory.decodeFile(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath()+"/"+pic);
                iv_imgPreview.setImageBitmap(bitmap);
                getSupportActionBar().setTitle("Learn A Bird : " + data.getStringExtra("updatedName"));
                isViewChanged = true;
                try {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsolutePath()+"/"+sound);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(isViewChanged){
            Intent intent = new Intent();
            setResult(DETAIL_ACTIVITY_REQUEST_CODE,intent);
            finish();
        }
        else{
            finish();
        }
        if(mediaPlayer!=null & mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
    }
}



package com.example.learnabird;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

public class AddBird extends AppCompatActivity {

    private static final int PERMISSION_CODE_BROWSE = 1000;
    private static final int PERMISSION_CODE_CAMERA = 1001;
    private static final int PERMISSION_CODE_SOUND_RECODING = 1004;
    private static final int IMAGE_PICK_CODE = 1002;
    private static final int IMAGE_CAPTURE_CODE = 1003;
    private static final int AUDIO_FILE_BROWSE_CODE = 1005;

    private DatabaseHelper db;

    String currentImagePath;
    Bitmap currentImageBitmap;

    ImageButton btnCamera;
    ImageButton btnImageBrowse;
    ImageView imgPreview;
    ImageButton btnPlayStop;
    ImageButton btnRecStop;
    ImageButton btnSoundBrowse;
    Button btnAddBird;
    Uri img_uri = null;
    String recFilePath = "";
    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;
    boolean recStatus = false;
    boolean playStatus = false;
    String rec_file_name;
    TextView txtRecFileName;
    TextView txtBirdName;
    TextView txtBirdInfo;

    String image_file_name;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bird);

        db = new DatabaseHelper(this);

        btnCamera = findViewById(R.id.btn_camera);
        imgPreview = findViewById(R.id.img_bird);
        btnImageBrowse = findViewById(R.id.btn_imageBrowse);
        btnPlayStop = findViewById(R.id.btn_play);
        btnRecStop = findViewById(R.id.btn_record);
        btnSoundBrowse = findViewById(R.id.btn_soundBrowse);
        txtRecFileName = findViewById(R.id.txt_rec_file_name);
        btnAddBird = findViewById(R.id.btn_addBird);
        txtBirdName = findViewById(R.id.txt_name);
        txtBirdInfo = findViewById(R.id.txt_details);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 23){
                    if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.CAMERA)==PackageManager.PERMISSION_DENIED){
                        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA};
                        requestPermissions(permissions,PERMISSION_CODE_CAMERA);
                    }
                    else{
                        openCamera();
                    }
                }
                else{
                    openCamera();
                }
                openCamera();
            }

        });

        btnImageBrowse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 23){
                    if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED){
                        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        requestPermissions(permissions,PERMISSION_CODE_BROWSE);
                    }
                    else{
                        pickImageFromGallery();
                    }
                }
                else{
                    pickImageFromGallery();
                }
            }
        });

        btnPlayStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playStatus){
                    if(mediaPlayer != null){
                        mediaPlayer.stop();
                        mediaPlayer.release();
                    }
                    playStatus = false;
                    btnPlayStop.setImageResource(R.drawable.ic_play_white_32dp);
                    Toast.makeText(AddBird.this,"Sound preview stopped.",Toast.LENGTH_SHORT).show();
                }
                else{
                    mediaPlayer = new MediaPlayer();
                    try {
                        mediaPlayer.setDataSource(recFilePath);
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                        Toast.makeText(AddBird.this,"Playing sound preview...",Toast.LENGTH_SHORT).show();
                        playStatus = true;
                        btnPlayStop.setImageResource(R.drawable.ic_stop_white_32dp);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(AddBird.this,"No sound selected...",Toast.LENGTH_SHORT).show();
                        playStatus = false;
                    }
                }
            }
        });

        btnRecStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 23){
                    if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED ||
                            checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_DENIED){
                        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO};
                        requestPermissions(permissions,PERMISSION_CODE_SOUND_RECODING);
                    }
                    else{
                        recordSound();
                    }
                }
                else{
                    recordSound();
                }
            }
        });

        btnSoundBrowse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialFilePicker()
                        .withActivity(AddBird.this)
                        .withRequestCode(AUDIO_FILE_BROWSE_CODE)
                        .withFilter(Pattern.compile(".*\\.mp3$")) // Filtering files and directories by file name using regexp
                        //.withFilterDirectories(true) // Set directories filterable (false by default)
                        //.withHiddenFiles(true) // Show hidden files and folders
                        .start();
            }
        });

        btnAddBird.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateImage(img_uri) & validateSound(recFilePath) & validteTextView(txtBirdName) & validteTextView(txtBirdInfo)){
                    saveImageToStorage(img_uri);
                    db.addBird(txtBirdName.getText().toString(),txtBirdInfo.getText().toString(),image_file_name,rec_file_name);
                    Toast.makeText(AddBird.this,"Information saved successfully.",Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(AddBird.this,"Please fill all the fields..",Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    public boolean validteTextView(TextView txtview){
        if(txtview ==null || txtview.getText().equals("")){
            return false;
        }
        return true;
    }

    public boolean validateImage(Uri uri){
        if(uri ==null || uri.equals("")){
            return false;
        }
        return true;
    }

    public  boolean validateSound(String url){
        if(url == null || url.equals("")){
            return false;
        }
        else{
            return true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer !=null && mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void recordSound(){
        if(recStatus){
            //while recording press button
            mediaRecorder.stop();
            Toast.makeText(AddBird.this,"Recording Stopped.",Toast.LENGTH_SHORT).show();
            txtRecFileName.setText(rec_file_name);

            btnRecStop.setBackgroundTintList(this.getResources().getColorStateList(R.color.colorPrimary));
            btnPlayStop.setBackgroundTintList(this.getResources().getColorStateList(R.color.colorPrimary));
            btnPlayStop.setEnabled(true);
            btnSoundBrowse.setBackgroundTintList(this.getResources().getColorStateList(R.color.colorPrimary));
            btnSoundBrowse.setEnabled(true);
            btnImageBrowse.setBackgroundTintList(this.getResources().getColorStateList(R.color.colorPrimary));
            btnImageBrowse.setEnabled(true);
            btnCamera.setBackgroundTintList(this.getResources().getColorStateList(R.color.colorPrimary));
            btnCamera.setEnabled(true);

            recStatus = false;
        }
        else{
            //while not recording press button
            if(mediaPlayer!=null && mediaPlayer.isPlaying()){
                mediaPlayer.stop();
                mediaPlayer.release();
                playStatus = false;
                btnPlayStop.setImageResource(R.drawable.ic_play_white_32dp);
            }

            rec_file_name = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())+"_rec.mp3";
            //recFilePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+ rec_file_name;
            recFilePath = getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsolutePath()+"/"+ rec_file_name;
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
            mediaRecorder.setOutputFile(recFilePath);
            try {
                mediaRecorder.prepare();
                mediaRecorder.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(AddBird.this,"Recording...",Toast.LENGTH_SHORT).show();
            btnRecStop.setBackgroundTintList(this.getResources().getColorStateList(R.color.colorRed));
            btnPlayStop.setBackgroundTintList(this.getResources().getColorStateList(R.color.imgBackground));
            btnPlayStop.setEnabled(false);
            btnSoundBrowse.setBackgroundTintList(this.getResources().getColorStateList(R.color.imgBackground));
            btnSoundBrowse.setEnabled(false);
            btnImageBrowse.setBackgroundTintList(this.getResources().getColorStateList(R.color.imgBackground));
            btnImageBrowse.setEnabled(false);
            btnCamera.setBackgroundTintList(this.getResources().getColorStateList(R.color.imgBackground));
            btnCamera.setEnabled(false);

            recStatus = true;
        }

    }

    private void openCamera() {
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(MediaStore.Images.Media.TITLE,"New Picture");
//        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Photo taken from LearnABird");
//        img_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
//        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,img_uri);
//        startActivityForResult(cameraIntent,IMAGE_CAPTURE_CODE);
// -----------------------------------------------------------------------------------
//        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        //cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        //File picDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
//
//        String picName = "lb_"+new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())+".jpg";
//        //File imgFile = new File(picDir,picName);
//        File file = new File(this.getFilesDir(), picName);
//        img_uri = Uri.fromFile(file);
//
//        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,img_uri);
//        startActivityForResult(cameraIntent,IMAGE_CAPTURE_CODE);

        //save file using fileprovider code
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(cameraIntent.resolveActivity(getPackageManager())!=null){
            File imageFile = null;
            imageFile = getImageFile();
            if(imageFile!=null){
                img_uri = FileProvider.getUriForFile(this,"com.example.android.fileprovider",imageFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,img_uri);
                startActivityForResult(cameraIntent,IMAGE_CAPTURE_CODE);
            }
        }
    }

    public File getImageFile(){
        String picName = "lb_"+new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imgFile = null;
        try {
            imgFile = File.createTempFile(picName,".jpg",storageDir);
            currentImagePath = imgFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imgFile;
    }

    private void pickImageFromGallery() {

        File picDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String dirPath = picDir.getPath();

        Uri data = Uri.parse(dirPath);

        Intent intent = new Intent(Intent.ACTION_PICK);
        //intent.setType("image/*");
        intent.setDataAndType(data,"image/*");
        startActivityForResult(intent,IMAGE_PICK_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case PERMISSION_CODE_BROWSE : {
                if (grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    pickImageFromGallery();
                }
                else{
                    Toast.makeText(AddBird.this,"Permission denied...",Toast.LENGTH_SHORT).show();
                }
            }
            break;
            case PERMISSION_CODE_CAMERA : {
                if (grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    openCamera();
                }
                else{
                    Toast.makeText(AddBird.this,"Permission denied...",Toast.LENGTH_SHORT).show();
                }
            }
            break;
            case PERMISSION_CODE_SOUND_RECODING :{
                if (grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    recordSound();
                }
                else{
                    Toast.makeText(AddBird.this,"Permission denied...",Toast.LENGTH_SHORT).show();
                }
            }
            default:Toast.makeText(AddBird.this,"All Permissions denied...",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //image browse
        if(resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE){
            imgPreview.setImageURI(data.getData());
            img_uri = data.getData();
        }
        //camera capture
        else if(resultCode == RESULT_OK && requestCode == IMAGE_CAPTURE_CODE){
            imgPreview.setImageURI(img_uri);
            image_file_name = img_uri.getLastPathSegment();
        }
        //audio browse
        else if(resultCode == RESULT_OK && requestCode == AUDIO_FILE_BROWSE_CODE){
            recFilePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            txtRecFileName.setText(recFilePath.substring(recFilePath.lastIndexOf("/")+1));
            rec_file_name = recFilePath.substring(recFilePath.lastIndexOf("/")+1);
        }
    }

    private void saveImageToStorage(Uri uri) {
        InputStream inputStream;
        try {
            //--------------------------------------------
            inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
//            imgPreview.setImageBitmap(bitmap);
            String picName = "lb_"+new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())+".png";
            image_file_name = picName;

            //create a file to write bitmap data
            //File f = new File(this.getFilesDir(), picName);
            File f = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), picName);
            f.createNewFile();

            //Convert bitmap to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
            byte[] bitmapdata = bos.toByteArray();

            //write the bytes in file
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();

            //--------------------------------------------
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

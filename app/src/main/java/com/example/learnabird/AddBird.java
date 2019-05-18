package com.example.learnabird;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
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
    private static final int ADD_BIRD_REQUEST_CODE=6000;

    private DatabaseHelper db;
    ImageButton btnCamera;
    ImageButton btnImageBrowse;
    ImageView imgPreview;
    ImageButton btnPlayStop;
    ImageButton btnRecStop;
    ImageButton btnSoundBrowse;
    Button btnAddBird;
    Uri img_uri = null;
    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;
    boolean recStatus = false;
    boolean playStatus = false;
    TextView txtRecFileName;
    TextView txtBirdName;
    TextView txtBirdInfo;
    String currentImagePath;
    String selAudioFileType;
    String image_file_name;
    String recFilePath = "";
    String rec_file_name;
    ProgressDialog progressDialog;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bird);

        //create a new instance of the database connection
        db = new DatabaseHelper(this);

        //initialize UI elements
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

        //request for permission to access the device camera.
        //use the camera if permission is granted
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
            }

        });

        //open file browser for images
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

        //stop/play sound
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

        //record sound from mic of the device
        //ask for permission if required
        btnRecStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selAudioFileType="rec";
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

        //browse files for audio files
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

        //update the details with new data.
        btnAddBird.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Utils.validateImage(img_uri) & Utils.validateSound(recFilePath) & Utils.validteTextView(txtBirdName) & Utils.validteTextView(txtBirdInfo)){
                    new SaveData(img_uri).execute("lb_"+new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())+".png");
                }
                else{
                    Toast.makeText(AddBird.this,"Please fill all the fields..",Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    //when activity ends
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /*
    Record audio using device microphone and store in file system
     */
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

    /*
    create and return initial image file to store the image in the storage
    A new name for the file also will be generated here
     */
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

    /*
    open file browse which has a filter only for images
     */
    private void pickImageFromGallery() {

        File picDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String dirPath = picDir.getPath();

        Uri data = Uri.parse(dirPath);

        Intent intent = new Intent(Intent.ACTION_PICK);
        //intent.setType("image/*");
        intent.setDataAndType(data,"image/*");
        startActivityForResult(intent,IMAGE_PICK_CODE);
    }

    /*
    listen for permisdion results from the permission requests
     */
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

    /*
        listen for results for sent requests
    */
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
            selAudioFileType = "file";
        }
    }

    /*
    copy files from browsed location to application directory
     */
    public boolean copyFile(String from, String to) {
        try {
            File sd = Environment.getExternalStorageDirectory();
            if (sd.canWrite()) {
                //int end = from.toString().lastIndexOf("/");
                //String str1 = from.toString().substring(0, end);
                //String str2 = from.toString().substring(end+1, from.length());
                File source = new File(from);
                File destination= new File(to);
                if (source.exists()) {
                    FileChannel src = new FileInputStream(source).getChannel();
                    FileChannel dst = new FileOutputStream(destination).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /*
    Background process to save new details to the database during adding new bird
     */
    public class SaveData extends AsyncTask<String, Void, Boolean> {

        Uri uri;

        public SaveData(Uri uri) {
            this.uri = uri;
            progressDialog = new ProgressDialog(AddBird.this);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
        }

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Saving data... Please wait...");
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {

            InputStream inputStream;
            try {
                //--------------------------------------------
                inputStream = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
//            imgPreview.setImageBitmap(bitmap);
                String picName = strings[0];
                image_file_name = picName;
                //create a file to write bitmap data
                File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), picName);
                file.createNewFile();

                //Convert bitmap to byte array
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
                byte[] bitmapdata = bos.toByteArray();

                //write the bytes in file
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(bitmapdata);
                fos.flush();
                fos.close();

                //--------------------------------------------
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //copy browsed audio file to app path
            if(selAudioFileType.equals("file")){
                copyFile(recFilePath,getExternalFilesDir(Environment.DIRECTORY_MUSIC).toString()+"/"+rec_file_name);
            }
            db.addBird(txtBirdName.getText().toString(),txtBirdInfo.getText().toString(),image_file_name,rec_file_name);

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            progressDialog.dismiss();
            AlertDialog.Builder builder = new AlertDialog.Builder(AddBird.this);
            builder.setMessage("Saved successfully.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent();
                    setResult(ADD_BIRD_REQUEST_CODE,intent);
                    finish();
                }
            });
            AlertDialog alert = builder.create();
            alert.setCancelable(false);
            alert.setCanceledOnTouchOutside(false);
            alert.show();

        }
    }

}

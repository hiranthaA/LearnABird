package com.example.learnabird;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.learnabird.AsyncTasks.AsyncLoadImage;
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

public class EditDetails extends AppCompatActivity {

    private static final int PERMISSION_CODE_BROWSE = 1000;
    private static final int PERMISSION_CODE_CAMERA = 1001;
    private static final int PERMISSION_CODE_SOUND_RECODING = 1004;
    private static final int IMAGE_PICK_CODE = 1002;
    private static final int IMAGE_CAPTURE_CODE = 1003;
    private static final int AUDIO_FILE_BROWSE_CODE = 1005;
    private static final int EDIT_DETAILS_REQUEST_CODE=2000;
    private int EDIT_DETAILS_REQUEST_VIA_LISTVIEW=9000;

    private DatabaseHelper db;

    ImageButton btnEditCamera;
    ImageButton btnEditImageBrowse;
    ImageView imgEditPreview;
    ImageButton btnEditPlayStop;
    ImageButton btnEditRecStop;
    ImageButton btnEditSoundBrowse;
    Button btnUpdateBird;
    Uri img_uri = null;
    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;
    boolean recStatus = false;
    boolean playStatus = false;
    String rec_file_name;
    TextView txtEditRecFileName;
    TextView txtEditBirdName;
    EditText txtEditBirdInfo;
    String currentImagePath;
    String recFilePath = "";
    String selAudioFileType="";
    String editReqFrom;
    String image_file_name;
    int birdId;
    ProgressDialog progressDialog;

    /*
    construct the view on view create state
    */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_details);

        db = new DatabaseHelper(this);

        btnEditCamera = findViewById(R.id.btn_edit_camera);
        imgEditPreview = findViewById(R.id.img_edit_bird);
        btnEditImageBrowse = findViewById(R.id.btn_edit_imageBrowse);
        btnEditPlayStop = findViewById(R.id.btn_edit_play);
        btnEditRecStop = findViewById(R.id.btn_edit_record);
        btnEditSoundBrowse = findViewById(R.id.btn_edit_soundBrowse);
        txtEditRecFileName = findViewById(R.id.txt_edit_rec_file_name);
        btnUpdateBird = findViewById(R.id.btn_updateBird);
        txtEditBirdName = findViewById(R.id.txt_edit_name);
        txtEditBirdInfo = findViewById(R.id.txt_edit_details);

        mediaPlayer = new MediaPlayer();

        /*
        initialize with data from the intent
         */
        final Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            birdId = bundle.getInt("birdId");
            txtEditBirdName.setText(bundle.getString("birdName"));
            txtEditBirdInfo.setText(bundle.getString("birdInfo"));
            image_file_name = bundle.getString("birdImageName");
            rec_file_name = bundle.getString("birdSound");
            editReqFrom = bundle.getString("from");
            recFilePath = getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsolutePath()+"/"+rec_file_name;
            txtEditRecFileName.setText(rec_file_name);
            txtEditBirdInfo.setText(bundle.getString("birdInfo"),TextView.BufferType.EDITABLE);

            getSupportActionBar().setTitle("Learn A Bird : Edit");

            //load images form storage
            Bitmap bitmap = BitmapFactory.decodeFile(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath()+"/"+image_file_name);
            img_uri = Uri.fromFile(new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath()+"/"+image_file_name));
            imgEditPreview.setImageBitmap(bitmap);
        }

        /*
        request for permission to access the device camera.
        use the camera if permission is granted
         */
        btnEditCamera.setOnClickListener(new View.OnClickListener() {
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

        /*
        open file browser for images
         */
        btnEditImageBrowse.setOnClickListener(new View.OnClickListener() {
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

        /*
        stop/play sound
         */
        btnEditPlayStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playStatus){
                    if(mediaPlayer != null){
                        mediaPlayer.stop();
                        //mediaPlayer.release();
                    }
                    playStatus = false;
                    btnEditPlayStop.setImageResource(R.drawable.ic_play_white_32dp);
                    Toast.makeText(EditDetails.this,"Sound preview stopped.",Toast.LENGTH_SHORT).show();
                }
                else{
                    try {
                        mediaPlayer = new MediaPlayer();
                        mediaPlayer.setDataSource(recFilePath);
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                        Toast.makeText(EditDetails.this,"Playing sound preview...",Toast.LENGTH_SHORT).show();
                        playStatus = true;
                        btnEditPlayStop.setImageResource(R.drawable.ic_stop_white_32dp);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(EditDetails.this,"No sound selected...",Toast.LENGTH_SHORT).show();
                        playStatus = false;
                    }
                }
            }
        });

        /*
        record sound from mic of the device
        ask for permission if required
         */
        btnEditRecStop.setOnClickListener(new View.OnClickListener() {
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

        /*
        browse files for audio files
         */
        btnEditSoundBrowse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialFilePicker()
                        .withActivity(EditDetails.this)
                        .withRequestCode(AUDIO_FILE_BROWSE_CODE)
                        .withFilter(Pattern.compile(".*\\.mp3$")) // Filtering files and directories by file name using regexp
                        //.withFilterDirectories(true) // Set directories filterable (false by default)
                        //.withHiddenFiles(true) // Show hidden files and folders
                        .start();
            }
        });

        /*
        update the details with new data.
         */
        btnUpdateBird.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Utils.validateImage(img_uri) & Utils.validateSound(recFilePath) & Utils.validteTextView(txtEditBirdName) & Utils.validteTextView(txtEditBirdInfo)){
                    new EditDetails.SaveData(img_uri).execute("lb_"+new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())+".png");
                }
                else{
                    Toast.makeText(EditDetails.this,"Please fill all the fields..",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    /*
    when activity ends
    */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer !=null && mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
    }

    /*
    Record audio using device microphone and store in file system
    */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void recordSound(){
        if(recStatus){
            //while recording press button
            mediaRecorder.stop();
            Toast.makeText(EditDetails.this,"Recording Stopped.",Toast.LENGTH_SHORT).show();
            txtEditRecFileName.setText(rec_file_name);

            btnEditRecStop.setBackgroundTintList(this.getResources().getColorStateList(R.color.colorPrimary));
            btnEditPlayStop.setBackgroundTintList(this.getResources().getColorStateList(R.color.colorPrimary));
            btnEditPlayStop.setEnabled(true);
            btnEditSoundBrowse.setBackgroundTintList(this.getResources().getColorStateList(R.color.colorPrimary));
            btnEditSoundBrowse.setEnabled(true);
            btnEditImageBrowse.setBackgroundTintList(this.getResources().getColorStateList(R.color.colorPrimary));
            btnEditImageBrowse.setEnabled(true);
            btnEditCamera.setBackgroundTintList(this.getResources().getColorStateList(R.color.colorPrimary));
            btnEditCamera.setEnabled(true);

            recStatus = false;
        }
        else{
            //while not recording press button
            if(mediaPlayer!=null && mediaPlayer.isPlaying()){
                mediaPlayer.stop();
                playStatus = false;
                btnEditPlayStop.setImageResource(R.drawable.ic_play_white_32dp);
            }

            rec_file_name = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())+"_rec.mp3";
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
            Toast.makeText(EditDetails.this,"Recording...",Toast.LENGTH_SHORT).show();
            btnEditRecStop.setBackgroundTintList(this.getResources().getColorStateList(R.color.colorRed));
            btnEditPlayStop.setBackgroundTintList(this.getResources().getColorStateList(R.color.imgBackground));
            btnEditPlayStop.setEnabled(false);
            btnEditSoundBrowse.setBackgroundTintList(this.getResources().getColorStateList(R.color.imgBackground));
            btnEditSoundBrowse.setEnabled(false);
            btnEditImageBrowse.setBackgroundTintList(this.getResources().getColorStateList(R.color.imgBackground));
            btnEditImageBrowse.setEnabled(false);
            btnEditCamera.setBackgroundTintList(this.getResources().getColorStateList(R.color.imgBackground));
            btnEditCamera.setEnabled(false);

            recStatus = true;
        }

    }

    /*
    Open device camera app to capture photos
    */
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
                    Toast.makeText(EditDetails.this,"Permission denied...",Toast.LENGTH_SHORT).show();
                }
            }
            break;
            case PERMISSION_CODE_CAMERA : {
                if (grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    openCamera();
                }
                else{
                    Toast.makeText(EditDetails.this,"Permission denied...",Toast.LENGTH_SHORT).show();
                }
            }
            break;
            case PERMISSION_CODE_SOUND_RECODING :{
                if (grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    recordSound();
                }
                else{
                    Toast.makeText(EditDetails.this,"Permission denied...",Toast.LENGTH_SHORT).show();
                }
            }
            default:Toast.makeText(EditDetails.this,"All Permissions denied...",Toast.LENGTH_SHORT).show();
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
            imgEditPreview.setImageURI(data.getData());
            img_uri = data.getData();
        }
        //camera capture
        else if(resultCode == RESULT_OK && requestCode == IMAGE_CAPTURE_CODE){
            imgEditPreview.setImageURI(img_uri);
            image_file_name = img_uri.getLastPathSegment();
        }
        //audio browse
        else if(resultCode == RESULT_OK && requestCode == AUDIO_FILE_BROWSE_CODE){
            recFilePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            txtEditRecFileName.setText(recFilePath.substring(recFilePath.lastIndexOf("/")+1));
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
    Background process to save changes to the database during update
    */
    public class SaveData extends AsyncTask<String, Void, Boolean> {

        Uri uri;

        public SaveData(Uri uri) {
            this.uri = uri;
            progressDialog = new ProgressDialog(EditDetails.this);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
        }

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Updating data... Please wait...");
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
                if (!img_uri.getLastPathSegment().equals(image_file_name)) {
                    InputStream inputStream;
                    try {
                        //--------------------------------------------
                        inputStream = getContentResolver().openInputStream(uri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        //imgPreview.setImageBitmap(bitmap);
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
                }

                //copy browsed audio file to app path
                if (selAudioFileType.equals("file")) {
                    copyFile(recFilePath, getExternalFilesDir(Environment.DIRECTORY_MUSIC).toString() + "/" + rec_file_name);
                }

                boolean dbUpdateOK = db.updateBird(birdId, txtEditBirdName.getText().toString(), txtEditBirdInfo.getText().toString(), image_file_name, rec_file_name);
                if(dbUpdateOK){
                    return true;
                }
                else{
                    return false;
                }

        }


        @Override
        protected void onPostExecute(Boolean aBoolean) {
            progressDialog.dismiss();
            AlertDialog.Builder builder = new AlertDialog.Builder(EditDetails.this);
            builder.setMessage("Updated successfully.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    sendDataBack();
                }
            });
            AlertDialog alert = builder.create();
            alert.setCancelable(false);
            alert.setCanceledOnTouchOutside(false);
            alert.show();
        }
    }

    /*
    transfer updated data back to the Detailed view using a intent
     */
    public void sendDataBack(){
        if(editReqFrom.equals("details")){
            Intent intent=new Intent();
            intent.putExtra("updatedName",txtEditBirdName.getText().toString());
            intent.putExtra("updatedInfo",txtEditBirdInfo.getText().toString());
            intent.putExtra("updatedPhoto",image_file_name);
            intent.putExtra("updatedSound",rec_file_name);
            setResult(EDIT_DETAILS_REQUEST_CODE,intent);
        }
        else{
            setResult(EDIT_DETAILS_REQUEST_VIA_LISTVIEW);
        }
        finish();
    }

    /*
    action to do on back button press inside edit details activity
     */
    @Override
    public void onBackPressed() {
        Intent intent=new Intent();
        setResult(4000,intent);
        finish();
    }
}

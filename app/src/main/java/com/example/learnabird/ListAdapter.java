package com.example.learnabird;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.learnabird.model.Executable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ListAdapter extends ArrayAdapter {

    private String[] birdNames;
    private String[] birdPics;
    private String[] locations;
    private int[] birdIds;
    private String[] birdDetails;
    private String[] birdSounds;
    private Context context;
    public Map<Integer,Bitmap> imgCacheMap = new HashMap<>();
    DatabaseHelper db;
    MainActivity mainActivityRef;
    private int EDIT_DETAILS_REQUEST_VIA_LISTVIEW=9000;

    private String storage="/storage/emulated/0/Android/data/com.example.learnabird/files/Pictures/";
    public ListAdapter(Context context, String[] names, String[] pics, String[] locations, int[] ids, String[] details,String[] sounds,MainActivity mainActivity) {
        super(context, R.layout.list_view_item);
        this.birdNames = names;
        this.birdPics = pics;
        this.locations = locations;
        this.birdIds = ids;
        this.birdDetails=details;
        this.birdSounds=sounds;
        this.context = context;
        this.mainActivityRef = mainActivity;
        db = new DatabaseHelper(getContext());
    }

    @Override
    public int getCount(){
        return birdNames.length;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        System.out.println("ZZZ "+position+" "+birdNames[position]);
        ViewHolder viewHolder = new ViewHolder();
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_view_item, parent, false);
            viewHolder.mBird = convertView.findViewById(R.id.imgView_list_item_img);
            viewHolder.mName = convertView.findViewById(R.id.txt_lst_itemName);
            viewHolder.mDelete = convertView.findViewById(R.id.btn_main_list_delete);
            viewHolder.mEdit = convertView.findViewById(R.id.btn_main_list_edit);
            convertView.setTag(viewHolder);

            viewHolder.mDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("")
                            .setMessage("Want to delete this content?")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    File targetPhoto = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), birdPics[position]);
                                    File targetSound = new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), birdSounds[position]);
                                    if(targetPhoto.delete() && targetSound.delete() && db.deleteBird(birdIds[position])){
                                        Toast.makeText(getContext(),"Deleted successfully.",Toast.LENGTH_SHORT).show();
                                        mainActivityRef.loadData();
                                    }
                                    else{
                                        Toast.makeText(getContext(),"Cannot Delete!",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .setNegativeButton("Cancel",null);
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });

            viewHolder.mEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(),EditDetails.class);
                    intent.putExtra("birdId",birdIds[position]);
                    intent.putExtra("birdName",birdNames[position]);
                    intent.putExtra("birdImageName",birdPics[position]);
                    intent.putExtra("birdSound",birdSounds[position]);
                    intent.putExtra("birdInfo",birdDetails[position]);
                    intent.putExtra("from","list");
                    mainActivityRef.startActivityForResult(intent,EDIT_DETAILS_REQUEST_VIA_LISTVIEW);
                }
            });
        }
        else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        if(imgCacheMap.containsKey(position)) {
                Bitmap bitmap = imgCacheMap.get(position);
                viewHolder.mBird.setImageBitmap(bitmap);
        }else{
            if (locations[position].equals("api")) {
                Executable executable = new Executable(position,birdPics[position]);
                new AsyncLoadURLImage(viewHolder.mBird).execute(executable);
            } else {
                //load data from db
                Bitmap bitmap = resizeBitmap(storage + birdPics[position]);
                imgCacheMap.put(position,bitmap);
                viewHolder.mBird.setImageBitmap(bitmap);
            }
        }
        if(locations[position]=="api"){
            viewHolder.mDelete.setVisibility(View.INVISIBLE);
            viewHolder.mEdit.setVisibility(View.INVISIBLE);
        }
        else{
            viewHolder.mDelete.setVisibility(View.VISIBLE);
            viewHolder.mEdit.setVisibility(View.VISIBLE);
        }

        viewHolder.mName.setText(birdNames[position]);
        return convertView;
    }

    static class ViewHolder{
        ImageView mBird;
        TextView mName;
        ImageButton mDelete;
        ImageButton mEdit;
    }

    public Bitmap resizeBitmap(String path){
        int DESIREDWIDTH = 100;
        int DESIREDHEIGHT = 100;
        File file = new File(path);
        Bitmap bitmap = ScalingUtilities.decodeFile(file,DESIREDWIDTH,DESIREDHEIGHT, ScalingUtilities.ScalingLogic.FIT);
        if (!(bitmap.getWidth() <= DESIREDWIDTH && bitmap.getHeight() <= DESIREDHEIGHT)) {
            //Scale image
            bitmap = ScalingUtilities.createScaledBitmap(bitmap, DESIREDWIDTH, DESIREDHEIGHT, ScalingUtilities.ScalingLogic.FIT);
        } else {
            //bitmap.recycle();
            bitmap = ScalingUtilities.createScaledBitmap(bitmap, DESIREDWIDTH, DESIREDHEIGHT, ScalingUtilities.ScalingLogic.FIT);
        }
        return bitmap;
    }

    public class AsyncLoadURLImage extends AsyncTask<Executable, Void, Bitmap> {

        ImageView target;
        ProgressDialog progressDialog;
        int position;

        public AsyncLoadURLImage(ImageView imageView){
            this.target = imageView;
            progressDialog = MainActivity.progressDialog;
        }

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Loading data...");
            progressDialog.show();
        }

        @Override
        protected Bitmap doInBackground(Executable... executable) {
            Bitmap result = null;
            position = executable[0].getPos();
            try {
                URL url = new URL(MainActivity.host +executable[0].getName());
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
            imgCacheMap.put(position,bitmap);
            target.setImageBitmap(bitmap);
            progressDialog.dismiss();
        }
    }
}

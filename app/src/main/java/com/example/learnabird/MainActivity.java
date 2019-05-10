package com.example.learnabird;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ListView lstBirds;
    private FloatingActionButton fabAddBirds;
    private String[] arrBirdNames;
    private String[] arrBirdPics;
    private String[] arrBirdDetails;
    private String[] arrBirdSounds;

    private String[] arrLocation;

    private ArrayList<String> arrlstDbBirdNames;
    private ArrayList<String> arrlstDbBirdInfo;
    private ArrayList<String> arrlstDbBirdPhotos;
    private ArrayList<String> arrlstDbBirdSounds;
    private ArrayList<Integer> arrlstDbBirdIds;
    public static String host;
    private String getDataURL;
    private int[] arrBirdIds;
    private DatabaseHelper db;

    private SwipeRefreshLayout swipeRefreshLayout;
    private int refreshCount;

    public static ProgressDialog progressDialog;
    private static final int DETAIL_ACTIVITY_REQUEST_CODE=3000;
    private static final int ADD_BIRD_REQUEST_CODE=6000;

    private Map imgCache;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize elements
        lstBirds = findViewById(R.id.lstBirds);
        fabAddBirds = findViewById(R.id.fab_add_bird);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);

        db = new DatabaseHelper(this);

        host = "https://learn-a-bird-server.herokuapp.com/";
        getDataURL = host+"bird/getall";

        //load data to the app from api and db
        loadData();

        //add Icon to action bar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //getSupportActionBar().setIcon(R.drawable.ic_delete_white_24dp);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                System.out.println("Refreshed");
                loadData();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        lstBirds.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this,DetailActivity.class);
                intent.putExtra("birdName",arrBirdNames[position]);
                intent.putExtra("birdPic", arrBirdPics[position]);
                intent.putExtra("birdDetails",arrBirdDetails[position]);
                intent.putExtra("birdSound", arrBirdSounds[position]);
                intent.putExtra("birdId",arrBirdIds[position]);
                intent.putExtra("location",arrLocation[position]);
                startActivityForResult(intent,DETAIL_ACTIVITY_REQUEST_CODE);
            }
        });

        fabAddBirds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,AddBird.class);
                startActivityForResult(intent,ADD_BIRD_REQUEST_CODE);
            }
        });

    }

    public void loadDbData(){
        arrlstDbBirdNames = new ArrayList<String>();
        arrlstDbBirdInfo = new ArrayList<String>();
        arrlstDbBirdPhotos = new ArrayList<String>();
        arrlstDbBirdSounds = new ArrayList<String>();
        arrlstDbBirdIds = new ArrayList<Integer>();
        Cursor data = db.getBirdList();

        if(data!=null){
            if(data.getCount()>0){
                while (data.moveToNext()){
                    arrlstDbBirdIds.add(Integer.parseInt(data.getString(0)));
                    arrlstDbBirdNames.add(data.getString(1));
                    arrlstDbBirdInfo.add(data.getString(2));
                    arrlstDbBirdPhotos.add(data.getString(3));
                    arrlstDbBirdSounds.add(data.getString(4));
//                    android.widget.ListAdapter listAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,alarmlist);
//                    alarmListView.setAdapter(listAdapter);
                }
            }
        }
        System.out.println("data loaded");
    }

    public void loadData(){

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if( activeNetworkInfo != null && activeNetworkInfo.isConnected()){
            //Load data from database
            loadDbData();
            //Load data from api asynchronously
            new LoadApiData().execute(getDataURL);
        }
        else{
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Oops! Your connection seems off...").setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    loadData();
                }
            }).setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MainActivity.super.onBackPressed();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    public class LoadApiData extends AsyncTask<String, Void, String> {

        public LoadApiData() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
        }

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Loading data...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... urls) {

            String result ="";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data != -1){
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }

                return result;

            }
            catch (MalformedURLException e){
                Log.d("api call","api call failed");
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            Log.i("API content:",result);

            try {
                JSONArray jsonArray = new JSONArray(result);
                int apiDataSize = jsonArray.length();
                int dbDataSize = arrlstDbBirdNames.size();

                arrBirdNames = new String[jsonArray.length()+dbDataSize];
                arrBirdDetails = new String[jsonArray.length()+dbDataSize];
                arrBirdPics = new String[jsonArray.length()+dbDataSize];
                arrBirdSounds = new String[jsonArray.length()+dbDataSize];
                arrBirdIds = new int[jsonArray.length()+dbDataSize];
                arrLocation = new String[jsonArray.length()+dbDataSize];

                for(int i=0 ; i<apiDataSize; i++){
                    JSONObject jsonBird = jsonArray.getJSONObject(i);
                    arrBirdIds[i] = jsonBird.getInt("id");
                    arrBirdNames[i]= jsonBird.getString("name");
                    arrBirdDetails[i]= jsonBird.getString("info");
                    arrBirdPics[i]=jsonBird.getString("photo");
                    arrBirdSounds[i]=jsonBird.getString("sound");
                    arrLocation[i]="api";
                }

                //merge db data to api data array
                int count = apiDataSize;
                for(int i=0; i< dbDataSize; i++){
                    arrBirdIds[count] = arrlstDbBirdIds.get(i);
                    arrBirdNames[count] = arrlstDbBirdNames.get(i);
                    arrBirdDetails[count] = arrlstDbBirdInfo.get(i);
                    arrBirdPics[count] = arrlstDbBirdPhotos.get(i);
                    arrBirdSounds[count] = arrlstDbBirdSounds.get(i);
                    arrLocation[count] = "db";
                    count++;
                }

                System.out.println("merging lists ok");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            ListAdapter listAdapter = new ListAdapter(MainActivity.this,arrBirdNames, arrBirdPics, arrLocation);
            lstBirds.setAdapter(listAdapter);
            progressDialog.dismiss();

        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Are you sure you want to exit?").setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.super.onBackPressed();
            }
        }).setNegativeButton("Cancel",null);
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed
        if(resultCode == DETAIL_ACTIVITY_REQUEST_CODE || resultCode == ADD_BIRD_REQUEST_CODE) {
            loadData();
        }
    }
}

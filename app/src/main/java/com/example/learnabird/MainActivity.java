package com.example.learnabird;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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

public class MainActivity extends AppCompatActivity {

    private ListView lstBirds;
    private FloatingActionButton fabAddBirds;
    private String[] arrBirdNames;
    private String[] arrBirdPics;
    private String[] arrBirdDetails;
    private String[] arrBirdSounds;
    public static String host;
    private int[] arrBirdIds;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        host = "https://learn-a-bird-server.herokuapp.com/";
        String getDataURL = host+"bird/getall";

        //Load data from api asynchronously
        new LoadApiData().execute(getDataURL);

        //add Icon to action bar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_launcher_background);

        lstBirds = findViewById(R.id.lstBirds);
        fabAddBirds = findViewById(R.id.fab_add_bird);

        lstBirds.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this,DetailActivity.class);
                intent.putExtra("birdName",arrBirdNames[position]);
                intent.putExtra("birdPic", arrBirdPics[position]);
                intent.putExtra("birdDetails",arrBirdDetails[position]);
                intent.putExtra("birdSound", arrBirdSounds[position]);
                intent.putExtra("birdId",arrBirdIds[position]);
                startActivity(intent);
            }
        });

        fabAddBirds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,AddBird.class);
                startActivity(intent);
            }
        });

    }

    public class LoadApiData extends AsyncTask<String, Void, String> {

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
            super.onPostExecute(result);

            Log.i("API content:",result);

            try {
                JSONArray jsonArray = new JSONArray(result);
                arrBirdNames = new String[jsonArray.length()];
                arrBirdDetails = new String[jsonArray.length()];
                arrBirdPics = new String[jsonArray.length()];
                arrBirdSounds = new String[jsonArray.length()];
                arrBirdIds = new int[jsonArray.length()];
                for(int i=0 ; i<jsonArray.length(); i++){
                    JSONObject jsonBird = jsonArray.getJSONObject(i);
                    jsonBird.getInt("id");
                    arrBirdNames[i]= jsonBird.getString("name");
                    arrBirdDetails[i]= jsonBird.getString("info");
                    arrBirdPics[i]=jsonBird.getString("photo");
                    arrBirdSounds[i]=jsonBird.getString("sound");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            ListAdapter listAdapter = new ListAdapter(MainActivity.this,arrBirdNames, arrBirdPics);
            lstBirds.setAdapter(listAdapter);

        }
    }

}

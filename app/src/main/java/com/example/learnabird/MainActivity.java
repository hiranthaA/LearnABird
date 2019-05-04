package com.example.learnabird;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {

    ListView lstBirds;
    String[] arrBirdNames;
    int[] arrBirdPics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //add Icon to action bar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_launcher_background);

        lstBirds = findViewById(R.id.lstBirds);

        arrBirdNames = new String[]{"Parrot", "Crow", "Pigeon", "Eagle", "Crane"};

        arrBirdPics = new int[]{
                R.mipmap.img_parrot_round,
                R.mipmap.img_crow_round,
                R.mipmap.img_pigeon_round,
                R.mipmap.img_eagle_round,
                R.mipmap.img_crane_round,
        };
        ListAdapter listAdapter = new ListAdapter(MainActivity.this,arrBirdNames,arrBirdPics);
        lstBirds.setAdapter(listAdapter);

        lstBirds.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this,DetailActivity.class);
                intent.putExtra("birdName",arrBirdNames[position]);
                intent.putExtra("birdPic",arrBirdPics[position]);
                startActivity(intent);
            }
        });

    }
}

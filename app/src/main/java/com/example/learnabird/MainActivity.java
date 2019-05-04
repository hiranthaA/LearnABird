package com.example.learnabird;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private ListView lstBirds;
    private FloatingActionButton fabAddBirds;
    private String[] arrBirdNames;
    private int[] arrBirdPics;
    private String[] arrBirdDetails;
    private int[] arrBirdSounds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //add Icon to action bar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_launcher_background);

        lstBirds = findViewById(R.id.lstBirds);
        fabAddBirds = findViewById(R.id.fab_add_bird);

        arrBirdNames = new String[]{
                "Parrot",
                "Crow",
                "Pigeon",
                "Eagle",
                "Crane"};

        arrBirdPics = new int[]{
                R.mipmap.img_parrot,
                R.mipmap.img_crow,
                R.mipmap.img_pigeon,
                R.mipmap.img_eagle,
                R.mipmap.img_crane,
        };

        arrBirdSounds = new int[]{
                R.raw.parrot,
                R.raw.crow,
                R.raw.pigeon,
                R.raw.eagle,
                R.raw.crane,
        };

        arrBirdDetails = new String[]{
                "Parrots are members of the order Psittaciformes, which includes more than 350 bird species, including parakeets, macaws, cockatiels and cockatoos, according to the Integrated Taxonomic Information System (ITIS). Though there are many types of parrots, all parrot species have a few traits in common. For example, to be classified as a parrot, the bird must have a curved beak, and its feet must be zygodactyl, which means there are four toes on each foot with two toes that point forward and two that point backward.",
                "Crows are black birds known for their intelligence and adaptability, and for their loud, harsh \"caw.\" They also have a reputation for damaging crops; however, their impact may be less than previously thought. \n" +
                        "\n" +
                        "The genus Corvus comprises crows, ravens and rooks. These birds are all part of the Corvidae family, which includes jays, magpies and nutcrackers.",
                "Pigeon, any of several hundred species of birds constituting the family Columbidae (order Columbiformes). Smaller forms are usually called doves, larger forms pigeons. An exception is the white domestic pigeon, the symbol known as the “dove of peace.” \n"+
                        "Pigeons occur worldwide except in the coldest regions and the most remote islands. About 250 species are known; two-thirds of them occur in tropical Southeast Asia, Australia, and the islands of the western Pacific, but the family also has many members in Africa and South America and a few in temperate Eurasia and North America. All members of the family suck liquids, rather than sip and swallow as do other birds, and all pigeon parents feed their young “pigeon’s milk,” the sloughed-off lining of the crop, the production of which is stimulated by the hormone prolactin. The nestling obtains this “milk” by poking its bill down the parent’s throat.",
                "Eagle, any of many large, heavy-beaked, big-footed birds of prey belonging to the family Accipitridae (order Falconiformes). In general, an eagle is any bird of prey more powerful than a buteo. An eagle may resemble a vulture in build and flight characteristics but has a fully feathered (often crested) head and strong feet equipped with great curved talons. A further difference is in foraging habits: eagles subsist mainly on live prey. They are too ponderous for effective aerial pursuit but try to surprise and overwhelm their prey on the ground. Like owls, many decapitate their kills. Because of their strength, eagles have been a symbol of war and imperial power since Babylonian times. Their likeness is found on Greek and Roman ruins, coins, and medals.",
                "Crane, any of 15 species of tall wading birds of the family Gruidae (order Gruiformes). Superficially, cranes resemble herons but usually are larger and have a partly naked head, a heavier bill, more compact plumage, and an elevated hind toe. In flight the long neck is stretched out in front, the stiltlike legs trailing out behind. \n"+
                        "Cranes form an ancient group, the earliest fossils having been recovered from Eocene deposits in North America. Living forms are found worldwide except in South America, but populations of many are endangered by hunting and habitat destruction.\n" +
                        "\n" +
                        "These graceful terrestrial birds stalk about in marshes and on plains, eating small animals of all sorts as well as grain and grass shoots. Two olive-gray eggs spotted with brown are laid in a nest of grasses and weed stalks on drier ground in marsh or field. The same nest may be used year after year. The brownish, downy young can run about shortly after hatching. The trachea (windpipe) is simple in the chick but lengthens with age, coiling upon itself like a French horn. It lies buried in the hollow keel of the breastbone and reaches a length of 1.5 metres (5 feet) in the adult whooping crane (Grus americana)............................................................."
        };

        ListAdapter listAdapter = new ListAdapter(MainActivity.this,arrBirdNames,arrBirdPics);
        lstBirds.setAdapter(listAdapter);

        lstBirds.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this,DetailActivity.class);
                intent.putExtra("birdName",arrBirdNames[position]);
                intent.putExtra("birdPic",arrBirdPics[position]);
                intent.putExtra("birdDetails",arrBirdDetails[position]);
                intent.putExtra("birdSounds",arrBirdSounds[position]);
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
}

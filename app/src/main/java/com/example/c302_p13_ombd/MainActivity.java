package com.example.c302_p13_ombd;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ListView listView;
    private ArrayList<Movie> list;
    private MovieAdapter adapter;

    private FirebaseFirestore db;
    private CollectionReference colRef;
    private DocumentReference docRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.listViewMovies);
        list = new ArrayList<Movie>();

        //TODO: retrieve all documents from the "movies" collection in Firestore (realtime)
        //populate the movie objects into the ListView

        adapter = new MovieAdapter(MainActivity.this, R.layout.movie_row, list);
        listView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        colRef = db.collection("movies");

        colRef
            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot snapshot,
                                    @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    list.clear();
                    adapter.notifyDataSetChanged();

                    for (QueryDocumentSnapshot snapshot1 : snapshot) {
                        if (snapshot != null) {
                            Movie movie = snapshot1.toObject(Movie.class);
                            list.add(movie);
                            movie.setMovieId(snapshot1.getId());
                        }
                        adapter.notifyDataSetChanged();
                    }

                }
            });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Movie selectedContact = list.get(position);
                Intent i = new Intent(getBaseContext(), ViewMovieDetailsActivity.class);
                i.putExtra("movie_id", selectedContact.getMovieId());
                startActivity(i);

            }
        });

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.menu_add) {
            Intent intent = new Intent(getApplicationContext(), CreateMovieActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
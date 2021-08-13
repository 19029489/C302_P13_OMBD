package com.example.c302_p13_ombd;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ViewMovieDetailsActivity extends AppCompatActivity {

    private static final String TAG = "MovieDetailsActivity";

    private EditText etTitle, etRated, etReleased, etRuntime, etGenre, etActors, etPlot, etLanguage, etPoster;
    private Button btnUpdate, btnDelete;
    private String movieId;

    private FirebaseFirestore db;
    private CollectionReference colRef;
    private DocumentReference docRef;

    private String apikey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_movie_details);

        etTitle = findViewById(R.id.etTitle);
        etRated = findViewById(R.id.etRated);
        etReleased = findViewById(R.id.etReleased);
        etRuntime = findViewById(R.id.etRuntime);
        etGenre = findViewById(R.id.etGenre);
        etActors = findViewById(R.id.etActors);
        etPlot = findViewById(R.id.etPlot);
        etLanguage = findViewById(R.id.etLanguage);
        etPoster = findViewById(R.id.etPoster);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        apikey = prefs.getString("apikey", "");

        if (apikey.equalsIgnoreCase("")) {
            //redirect back to login screen
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        }

        db = FirebaseFirestore.getInstance();

        colRef = db.collection("movies");

        Intent intent = getIntent();
        movieId = intent.getStringExtra("movie_id");

	//TODO: get the movie record from Firestore based on the movieId
	// set the edit fields with the details
        docRef = colRef.document(movieId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + snapshot.getData());
                        Movie movie = snapshot.toObject(Movie.class);
                        etTitle.setText(movie.getTitle());
                        etActors.setText("" + movie.getActors());
                        etGenre.setText("" + movie.getGenre());
                        etLanguage.setText("" + movie.getLanguage());
                        etPlot.setText("" + movie.getPlot());
                        etPoster.setText("" + movie.getPoster());
                        etRated.setText("" + movie.getRating());
                        etReleased.setText("" + movie.getReleased());
                        etRuntime.setText("" + movie.getRuntime());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnUpdateOnClick(v);
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnDeleteOnClick(v);
            }
        });
    }//end onCreate

    
    private void btnUpdateOnClick(View v) {
		//TODO: create a Movie object and populate it with the values in the edit fields
		//save it into Firestore based on the movieId

        if (etTitle.getText().toString().equalsIgnoreCase("") || etRated.getText().toString().equalsIgnoreCase("") || etReleased.getText().toString().equalsIgnoreCase("") || etActors.getText().toString().equalsIgnoreCase("") || etGenre.getText().toString().equalsIgnoreCase("") || etLanguage.getText().toString().equalsIgnoreCase("") || etPlot.getText().toString().equalsIgnoreCase("") || etPoster.getText().toString().equalsIgnoreCase("") || etRuntime.getText().toString().equalsIgnoreCase("")) {
            Toast.makeText(ViewMovieDetailsActivity.this, "Please fill in all the blanks", Toast.LENGTH_SHORT).show();
        } else {
            Movie movie = new Movie(etTitle.getText().toString(), etRated.getText().toString(), etReleased.getText().toString(), etRuntime.getText().toString(), etGenre.getText().toString(), etActors.getText().toString(), etPlot.getText().toString(), etLanguage.getText().toString(), etPoster.getText().toString());
            docRef.set(movie);
        }

        Toast.makeText(getApplicationContext(), "Movie record updated successfully", Toast.LENGTH_SHORT).show();

        finish();

    }//end btnUpdateOnClick

    private void btnDeleteOnClick(View v) {
		//TODO: delete from Firestore based on the movieId

        docRef
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });

        Toast.makeText(getApplicationContext(), "Movie record deleted successfully", Toast.LENGTH_SHORT).show();

        finish();

    }//end btnDeleteOnClick

}//end class
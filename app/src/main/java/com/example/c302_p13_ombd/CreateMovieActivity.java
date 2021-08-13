package com.example.c302_p13_ombd;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

public class CreateMovieActivity extends AppCompatActivity {

    private static final String TAG = "CreateMovieActivity";

    private EditText etTitle, etRated, etReleased, etRuntime, etGenre, etActors, etPlot, etLanguage, etPoster;
    private Button btnCreate, btnSearch;
    private ImageButton btnCamera;
    private String apikey;

    private FirebaseFirestore db;
    private CollectionReference colRef;
    private DocumentReference docRef;

    private AsyncHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_movie);

        etTitle = findViewById(R.id.etTitle);
        etRated = findViewById(R.id.etRated);
        etReleased = findViewById(R.id.etReleased);
        etRuntime = findViewById(R.id.etRuntime);
        etGenre = findViewById(R.id.etGenre);
        etActors = findViewById(R.id.etActors);
        etPlot = findViewById(R.id.etPlot);
        etLanguage = findViewById(R.id.etLanguage);
        etPoster = findViewById(R.id.etPoster);
        btnCreate = findViewById(R.id.btnCreate);
        btnSearch = findViewById(R.id.btnSearch);
        btnCamera = findViewById(R.id.btnCamera);

        client = new AsyncHttpClient();

        //TODO: Retrieve the apikey from SharedPreferences
		//If apikey is empty, redirect back to LoginActivity
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        apikey = prefs.getString("apikey", "");

        if (apikey.equalsIgnoreCase("")) {
            //redirect back to login screen
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        }

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnCreateOnClick(v);
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSearchOnClick(v);
            }
        });

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnCameraOnClick(v);
            }
        });

    }//end onCreate

	//TODO: extract the fields and populate into a new instance of Movie class
	// Add the new movie into Firestore
    private void btnCreateOnClick(View v) {
		if (etTitle.getText().toString().equalsIgnoreCase("") || etRated.getText().toString().equalsIgnoreCase("") || etReleased.getText().toString().equalsIgnoreCase("") || etActors.getText().toString().equalsIgnoreCase("") || etGenre.getText().toString().equalsIgnoreCase("") || etLanguage.getText().toString().equalsIgnoreCase("") || etPlot.getText().toString().equalsIgnoreCase("") || etPoster.getText().toString().equalsIgnoreCase("") || etRuntime.getText().toString().equalsIgnoreCase("")) {
            Toast.makeText(CreateMovieActivity.this, "Please fill in all the blanks", Toast.LENGTH_SHORT).show();
        } else {
            db = FirebaseFirestore.getInstance();
            colRef = db.collection("movies");

            Movie movie = new Movie(etTitle.getText().toString(), etRated.getText().toString(), etReleased.getText().toString(), etRuntime.getText().toString(), etGenre.getText().toString(), etActors.getText().toString(), etPlot.getText().toString(), etLanguage.getText().toString(), etPoster.getText().toString());

            colRef
                    .add(movie)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                            Intent i = new Intent(CreateMovieActivity.this, MainActivity.class);
                            startActivity(i);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error adding document", e);
                        }
                    });

            finish();
        }
    }

	//TODO: Call www.omdbapi.com passing the title and apikey as parameters
	// extract from JSON response and set into the edit fields
    private void btnSearchOnClick(View v) {
        if (!(etTitle.getText().toString().equalsIgnoreCase(""))) {

            client.post("http://www.omdbapi.com/?apikey=56eb0f74&t=" + etTitle.getText().toString() , new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);

                    try {
                        Log.i("JSON Results: ", response.toString());

                        String result = response.getString("Response");

                        if (result.equalsIgnoreCase("True")) {

                            String title = response.getString("Title");
                            etTitle.setText(title);

                            String rated = response.getString("Rated");
                            etRated.setText(rated);

                            String released = response.getString("Released");
                            etReleased.setText(released);

                            String runtime = response.getString("Runtime");
                            etRuntime.setText(runtime);

                            String genre = response.getString("Genre");
                            etGenre.setText(genre);

                            String plot = response.getString("Plot");
                            etPlot.setText(plot);

                            String actors = response.getString("Actors");
                            etActors.setText(actors);

                            String language = response.getString("Language");
                            etLanguage.setText(language);

                            String poster = response.getString("Poster");
                            etPoster.setText(poster);

                        } else {
                            Toast.makeText(getApplicationContext(), "Movie not found!", Toast.LENGTH_SHORT).show();
                        }

                    }
                    catch (JSONException e){
                        e.printStackTrace();
                    }

                }//end onSuccess
            });

        } else {
            Toast.makeText(CreateMovieActivity.this, "Please enter title of movie to search", Toast.LENGTH_SHORT).show();
        }
    }


    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void btnCameraOnClick(View v) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            //TODO: feed imageBitmap into FirebaseVisionImage for text recognizing
            InputImage image = InputImage.fromBitmap(imageBitmap, 0);
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            Task<Text> result =
                recognizer.process(image)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {

                                for (Text.TextBlock block : visionText.getTextBlocks()) {
                                    String blockText = block.getText();
                                    etTitle.setText(blockText);
                                }
                            }
                        });
        }
    }
}
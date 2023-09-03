package com.example.rajdevlingo;



import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Spinner fromSpinner, toSpinner;
    private TextInputEditText editSource;
    private ImageView Mic;
    private MaterialButton translateBtn;
    private TextView translatedView;
    private Translator translator;

    String[] fromLanguages = {"From", "English", "Afrikaans", "Arabic", "Belarusian", "Bengali", "Catalan", "Czech", "Welsh", "Hindi", "Urdu"};
    String[] toLanguages = {"To", "English", "Afrikaans", "Arabic", "Belarusian", "Bengali", "Catalan", "Czech", "Welsh", "Hindi", "Urdu"};

    private static final int REQUEST_PERMISSION_CODE = 1;
    String langauageCode , fromLanguagesCode, toLanguagesCode;

    private ActivityResultLauncher<Intent> speechRecognitionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fromSpinner = findViewById(R.id.fromSpinner);
        toSpinner = findViewById(R.id.toSpinner);
        editSource = findViewById(R.id.editSource);
        Mic = findViewById(R.id.Mic);
        translateBtn = findViewById(R.id.translateBtn);
        translatedView = findViewById(R.id.translatedView);

        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fromLanguagesCode = getLanguageCode(fromLanguages[position]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

        ArrayAdapter<String> fromAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, fromLanguages);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromAdapter);

        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                toLanguagesCode = getLanguageCode(toLanguages[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter<String> toAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, toLanguages);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(toAdapter);

        // Initialize the ActivityResultLauncher for speech recognition
        speechRecognitionLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            ArrayList<String> speechResults = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                            editSource.setText(speechResults.get(0));
                        }
                    }
                });

        translateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                translatedView.setText("");
                if (editSource.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please Enter Your Text to Translate", Toast.LENGTH_SHORT).show();
                } else if (fromLanguagesCode == null) {
                    Toast.makeText(MainActivity.this, "Please Select Source language", Toast.LENGTH_SHORT).show();
                } else if (toLanguagesCode == null) {
                    Toast.makeText(MainActivity.this, "Please select the language to make translation", Toast.LENGTH_SHORT).show();
                } else {
                    translateText(fromLanguagesCode, toLanguagesCode, editSource.getText().toString());
                }
            }
        });

        Mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to convert into text");

                // Use the ActivityResultLauncher to start the activity
                speechRecognitionLauncher.launch(intent);
            }
        });
    }

    private void translateText(String fromLanguagesCode, String toLanguagesCode, String source) {
        translatedView.setText("Downloading Modal....");
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(fromLanguagesCode)
                .setTargetLanguage(toLanguagesCode)
                .build();
        translator = Translation.getClient(options);



        DownloadConditions conditions = new DownloadConditions.Builder().build();
        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        translatedView.setText("Translating...");
                        translator.translate(source).addOnSuccessListener(new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(String s) {
                               translatedView.setText(s);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this, "Fail to Translate"+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Fail to download "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public String getLanguageCode(String language) {
        String langauageCode = null;
        switch (language) {
            case "English":
                langauageCode = TranslateLanguage.ENGLISH;
                break;
            case "Afrikaans":
                langauageCode = TranslateLanguage.AFRIKAANS;
                break;
            case "Arabic":
                langauageCode = TranslateLanguage.ARABIC;
                break;
            case "Belarusian":
                langauageCode = TranslateLanguage.BELARUSIAN;
                break;
            case "Bengali":
                langauageCode = TranslateLanguage.BENGALI;
                break;
            case "Catalan":
                langauageCode = TranslateLanguage.CATALAN;
                break;
            case "Czech":
                langauageCode = TranslateLanguage.CZECH;
                break;
            case "Welsh":
                langauageCode = TranslateLanguage.WELSH;
                break;
            case "Hindi":
                langauageCode = TranslateLanguage.HINDI;
                break;
            case "Urdu":
                langauageCode = TranslateLanguage.URDU;
                break;
        }
        return langauageCode;
    }
}

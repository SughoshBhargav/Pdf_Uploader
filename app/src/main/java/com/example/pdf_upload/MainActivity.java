package com.example.pdf_upload;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    Uri pdfuri = null;
    Button upload_btn;
    EditText title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        upload_btn = findViewById(R.id.upload_btn);

        upload_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

    }

    private void openGallery() {

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        activityResultLauncher.launch(intent);

    }

    public ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult activityResult) {
                    int result = activityResult.getResultCode();
                    Intent data = activityResult.getData();

                    FirebaseStorage storage = FirebaseStorage.getInstance("gs://sahayadri-app.appspot.com/");
                    StorageReference storageRef = storage.getReference();

                    if(result == RESULT_OK){

                        ProgressBar progressBar = findViewById(R.id.progressBar);
                        progressBar.setVisibility(View.VISIBLE);

                        pdfuri = data.getData();
                        title = findViewById(R.id.title);
                        String Title = title.getText().toString();
                        final String timestamp = "" + System.currentTimeMillis();
                        final String messagePushID = Title;

                        final StorageReference filepath = storageRef.child(messagePushID + "." + "pdf");

                        Toast.makeText(MainActivity.this, filepath.getName(), Toast.LENGTH_SHORT).show();

                        filepath.putFile(pdfuri).continueWithTask(new Continuation() {
                            @Override
                            public Object then(@NonNull Task task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }
                                return filepath.getDownloadUrl();
                            }
                        }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
                            if ((task.isSuccessful())) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(MainActivity.this, "Uploaded Successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(MainActivity.this, "UploadedFailed", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                    else{
                        Toast.makeText(MainActivity.this,"FAILED",Toast.LENGTH_LONG).show();
                    }
                }
            }
    );

}
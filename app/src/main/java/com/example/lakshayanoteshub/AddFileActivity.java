package com.example.lakshayanoteshub;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.xml.transform.Result;

public class AddFileActivity extends AppCompatActivity {

    Button selectButton, submitButton;
    TextView textView;
    TextInputEditText name, subject, description;
    Uri pdfUri;
    ProgressDialog progressDialog;
    String s;

    FirebaseStorage storage;
    FirebaseDatabase database;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectPdf();
        } else {
            Toast.makeText(this, "Please Give Permission", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 69 && resultCode == RESULT_OK && data != null) {
            pdfUri = data.getData();
            s = getSize(pdfUri);
            Log.i("Size", s);
            textView.setText("File Is Selected!!");
            textView.setTextColor(getColor(R.color.colorAccent));
        } else {
            Toast.makeText(this, "Please Select A File", Toast.LENGTH_LONG).show();
        }
    }

    private String getSize(Uri pdfUri) {
        Cursor returnCursor =
                getContentResolver().query(pdfUri, null, null, null, null);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        int size = (int) returnCursor.getLong(sizeIndex);
        returnCursor.close();
        String s = String.valueOf(size);
        if (s.length() < 7) {
            return String.valueOf(Float.parseFloat(s)/1000).substring(0,3) + " KB";
        } else {
            return String.valueOf(Float.parseFloat(s)/1000000).substring(0,4) + " MB";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_file);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        windowFile f = new windowFile(getWindow(), getColor(R.color.backGround)).setStatusBar();

        selectButton = findViewById(R.id.selectFile);
        submitButton = findViewById(R.id.submit);
        textView = findViewById(R.id.textView);
        name = findViewById(R.id.name);
        subject = findViewById(R.id.subject);
        description = findViewById(R.id.description);

        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();

        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(AddFileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    selectPdf();
                } else {
                    ActivityCompat.requestPermissions(AddFileActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                }

            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pdfUri != null && name.getText() != null && subject.getText() != null && description.getText() != null) {
                    uploadFile(pdfUri);
                } else {
                    Toast.makeText(AddFileActivity.this, "Please Select File", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void selectPdf() {

        Intent selectFileIntent = new Intent();
        selectFileIntent.setType("application/pdf");
        selectFileIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(selectFileIntent, 69);
    }

    private void uploadFile(Uri pdfUri) {
        if (new Checker().isInternetAvailable()) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setTitle("Upload Progress");
            progressDialog.setMessage("Uploading...");
            progressDialog.setProgress(0);
            progressDialog.setCancelable(false);
            progressDialog.show();

            final String fileName = System.currentTimeMillis() + "";

            final StorageReference storageReference = storage.getReference();

            storageReference.child("Uploads").child(fileName).putFile(pdfUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            DatabaseReference databaseReference = database.getReference();
                            Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                            task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String Url = uri.toString();
                                    DatabaseReference databaseReference1 = database.getReference("Notes");
                                    String id = databaseReference1.push().getKey();
                                    Note note = getNote(id, Url, fileName);
                                    databaseReference1.child(id).setValue(note);
                                    Toast.makeText(AddFileActivity.this, "File Added Successfully!", Toast.LENGTH_LONG).show();
                                    finish();
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(AddFileActivity.this, "Something went Wrong!", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {

                    int currentProgress = (int) (100*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                    progressDialog.setProgress(currentProgress);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(AddFileActivity.this, "Upload Failed!", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "No Internet Available!", Toast.LENGTH_SHORT).show();
        }

    }

    public Note getNote(String id, String url, String pdfname) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy  HH:mm:ss");
        String currentDateTime = simpleDateFormat.format(new Date());

        Note newNote = new Note(id, name.getText().toString(), subject.getText().toString(), description.getText().toString(), url, pdfname, FirebaseAuth.getInstance().getCurrentUser().getEmail(), s, currentDateTime);
        return newNote;
    }
}

class Note {
    public String id;
    public String name;
    public String subjectName;
    public String description;
    public String url;
    public String pdfname;
    public String email;
    public String size;
    public String addedOn;


    public Note(String id, String name, String subjectName, String description, String url, String pdfname, String email, String size, String addedOn) {
        this.id = id;
        this.name = name;
        this.subjectName = subjectName;
        this.description = description;
        this.url = url;
        this.pdfname = pdfname;
        this.email = email;
        this.size = size;
        this.addedOn = addedOn;
    }
}

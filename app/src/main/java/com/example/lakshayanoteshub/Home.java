package com.example.lakshayanoteshub;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Home extends AppCompatActivity implements MyAdapter.OnClick {
    List<Note> collectionOfNotes = new ArrayList<>();
    Map<String, List<Note>> map;
    DatabaseReference databaseReference;
    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    LinearLayout homeScroll;
    Button delete;
    BottomSheet bottomSheet;
    BottomSheetBehavior bottomSheetBehavior;
    String selectedKey;
    int selectedPosition;
    ProgressDialog progressDialog;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    SharedPreferences sharedPreferences;
    CheckBox deleteFromDevice;
    Checker checker;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startDownload();
        } else {
            Toast.makeText(this, "Please Give Permission", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(int position, String key) {
        selectedKey = key;
        selectedPosition = position;

        if (ContextCompat.checkSelfPermission(Home.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            startDownload();

        } else {
            ActivityCompat.requestPermissions(Home.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
        }


    }

    @Override
    public void onLongClick(final int position, final String key) {
        selectedKey = key;
        selectedPosition = position;
        bottomSheet = new BottomSheet(map.get(key).get(position).name, map.get(key).get(position).subjectName, map.get(key).get(position).description, this);
        bottomSheet.show(getSupportFragmentManager(), "BottomSheetInfo");
//        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.AppBottomSheetDialogTheme);
//        bottomSheetDialog.setContentView(R.layout.bottom_sheet);
//        bottomSheetDialog.show();

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_item:
                addFile();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_file, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Notes...");
        progressDialog.setCancelable(true);
        progressDialog.show();

        drawerLayout = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.getDrawerArrowDrawable().setColor(getColor(R.color.colorPrimaryDark));
        actionBarDrawerToggle.syncState();

        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.logout_item:
                        signOut();
                        break;
                }
                return false;
            }
        });

        final TextView nameView, emailView;
        View headerView = navigationView.getHeaderView(0);
        nameView = headerView.findViewById(R.id.nameTxt);
        emailView = headerView.findViewById(R.id.emailTxt);

        homeScroll = findViewById(R.id.homeScroll);

        windowFile f = new windowFile(getWindow(), getColor(R.color.backGround)).setStatusBar();

        if (!new Checker().isInternetAvailable()) {
            SharedPreferences sharedPreferencesNotes = getApplicationContext().getSharedPreferences("notesData", MODE_PRIVATE);
            String str = sharedPreferencesNotes.getString("offlineData", "");
//            Log.i("Offline", str);
            Converter converter = new Converter(str);
            map = converter.convertToMap();

            sharedPreferences = getApplicationContext().getSharedPreferences("userDetails", MODE_PRIVATE);
            nameView.setText(sharedPreferences.getString("name", "Name"));
            emailView.setText(sharedPreferences.getString("email", "Email"));

            buildOffline();

            this.registerReceiver(this.broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));


        } else {
            DatabaseReference databaseReferenceUser = FirebaseDatabase.getInstance().getReference("User");
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            sharedPreferences = getApplicationContext().getSharedPreferences("userDetails", MODE_PRIVATE);
            final SharedPreferences.Editor editor = sharedPreferences.edit();

            databaseReferenceUser.child(firebaseUser.getEmail().replaceAll("\\.", "dot")).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    nameView.setText(dataSnapshot.getValue().toString());
                    editor.putString("name", dataSnapshot.getValue().toString());
                    editor.commit();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            emailView.setText(firebaseUser.getEmail());
            fetchProduct();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("===========================", "onResume");
        IntentFilter intentFilter = new IntentFilter("com.agile.internetdemo.MainActivity");
        Home.this.registerReceiver(broadcastReceiver, intentFilter);

        fetchProduct();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("===========================", "onPause");
        Home.this.unregisterReceiver(broadcastReceiver);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            String reason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
            boolean isFailover = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);

            NetworkInfo currentNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            NetworkInfo otherNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);

            if (currentNetworkInfo.isConnected()) {
                Log.d("===========================", "Connected");
                progressDialog.show();
                fetchProduct();
                Toast.makeText(getApplicationContext(), "Connected Online", Toast.LENGTH_LONG).show();
            } else {
                Log.d("===========================", "Not Connected");
                Toast.makeText(getApplicationContext(), "Offline",
                        Toast.LENGTH_LONG).show();
            }

        }
    };

    public void info(View view) {

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.info_layout);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView infoName, infoSubject, infoDescription, infoSize, infoAdded, location;
        infoName = dialog.findViewById(R.id.info_name);
        infoName.setText(map.get(selectedKey).get(selectedPosition).name);

        infoSubject = dialog.findViewById(R.id.info_subjectName);
        infoSubject.setText(map.get(selectedKey).get(selectedPosition).subjectName);

        infoDescription = dialog.findViewById(R.id.info_description);
        infoDescription.setText(map.get(selectedKey).get(selectedPosition).description);

        infoSize = dialog.findViewById(R.id.info_size);
        infoSize.setText(map.get(selectedKey).get(selectedPosition).size);

        infoAdded = dialog.findViewById(R.id.info_addedOn);
        infoAdded.setText(map.get(selectedKey).get(selectedPosition).addedOn);

        location = dialog.findViewById(R.id.info_location);
        String str_loc = this.getExternalFilesDir(null) + "/" + "LakshayaNotesHuB" + "/" + map.get(selectedKey).get(selectedPosition).name + map.get(selectedKey).get(selectedPosition).subjectName;
        location.setText(str_loc);

        dialog.findViewById(R.id.ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        bottomSheet.dismiss();
        dialog.show();

    }

    private void buildOffline() {


        for (String n : map.keySet()) {
            View view = LayoutInflater.from(this).inflate(R.layout.category, null);
            TextView head = view.findViewById(R.id.heading);
            head.setText(n);
            recyclerView = view.findViewById(R.id.maths_layout);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            adapter = new MyAdapter(getBaseContext(), map.get(n), n, this);
            recyclerView.setAdapter(adapter);
            homeScroll.addView(view);
        }
        progressDialog.dismiss();

    }

    public void rename(View view) {
        bottomSheet.dismiss();
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.rename_layout);
//        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        final EditText renameName, renameSubject, renameDescription;
        renameName = dialog.findViewById(R.id.rename_name);
        renameName.setText(map.get(selectedKey).get(selectedPosition).name);
        renameSubject = dialog.findViewById(R.id.rename_subject);
        renameSubject.setText(map.get(selectedKey).get(selectedPosition).subjectName);
        renameDescription = dialog.findViewById(R.id.rename_description);
        renameDescription.setText(map.get(selectedKey).get(selectedPosition).description);

        Button renameCancel = dialog.findViewById(R.id.rename_cancel);
        renameCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Button renameSubmit = dialog.findViewById(R.id.rename_submit);
        renameSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (new Checker().isInternetAvailable()) {
                    if (renameName.getText().toString().length() != 0 && renameSubject.getText().toString().length() != 0) {
                        dialog.dismiss();
                        databaseReference.child(map.get(selectedKey).get(selectedPosition).id).child("name").setValue(renameName.getText().toString());
                        databaseReference.child(map.get(selectedKey).get(selectedPosition).id).child("subjectName").setValue(renameSubject.getText().toString());
                        databaseReference.child(map.get(selectedKey).get(selectedPosition).id).child("description").setValue(renameDescription.getText().toString());
                        Toast.makeText(Home.this, "Successfully Changed", Toast.LENGTH_SHORT).show();
                    } else {
                        if (renameName.getText().toString().length() == 0)
                            Toast.makeText(Home.this, "Name is Empty!", Toast.LENGTH_SHORT).show();
                        else if (renameSubject.getText().toString().length() == 0)
                            Toast.makeText(Home.this, "Subject is Empty!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Home.this, "No Internet Available!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();


    }

    public void download(View view) {
        bottomSheet.dismiss();
        startDownload();
    }

    void startDownload() {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == 1) {
                    buildCard();
                }
                super.handleMessage(msg);
            }
        };

        new DownoadTask(Home.this, map.get(selectedKey).get(selectedPosition).url, map.get(selectedKey).get(selectedPosition).name, map.get(selectedKey).get(selectedPosition).subjectName, handler);
    }

    private void signOut() {
        if (new Checker().isInternetAvailable()) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Log Out...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            sharedPreferences = getApplicationContext().getSharedPreferences("userDetails", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("remember", false);
            editor.commit();

            FirebaseAuth.getInstance().signOut();

            progressDialog.dismiss();

            Intent intent = new Intent(Home.this, MainActivity.class);
            startActivity(intent);
            Home.this.finish();
        } else {
            Toast.makeText(this, "No Internet Available!", Toast.LENGTH_SHORT).show();
        }
    }

    public void delete(View view) {

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.delete_layout);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.findViewById(R.id.delete_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        deleteFromDevice = dialog.findViewById(R.id.deleteFromDevice);
        dialog.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (new Checker().isInternetAvailable()) {
                    if (deleteFromDevice.isChecked()) {
                        deleteFromDevice(map.get(selectedKey).get(selectedPosition).name + map.get(selectedKey).get(selectedPosition).subjectName + ".pdf");
                    }
                    deleteFileFromServer();
                } else {
                    Toast.makeText(Home.this, "No Internet Available!", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });

        bottomSheet.dismiss();
        dialog.show();


    }

    public void deleteFromDevice(String fileName) {
        File pdfFile = new File(this.getExternalFilesDir(null) + "/LakshayaNotesHuB/" + fileName);
        if (pdfFile.exists()) {
            pdfFile.delete();
        } else {
            Toast.makeText(this, "File Doesn't exist on Device!", Toast.LENGTH_SHORT).show();
        }
    }

    public void cancel(View view) {
        bottomSheet.dismiss();
    }

    public void deleteFileFromServer() {
        StorageReference reference = FirebaseStorage.getInstance().getReference().child("Uploads");
        StorageReference fileRef = reference.child(map.get(selectedKey).get(selectedPosition).pdfname);
        fileRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(Home.this, "File Deleted Successfully", Toast.LENGTH_SHORT).show();
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Notes");
                databaseReference.child(map.get(selectedKey).get(selectedPosition).id).removeValue();
                bottomSheet.dismiss();

            }
        });
    }

    public void addFile() {
        Intent addFileIntent = new Intent(this, AddFileActivity.class);
        startActivity(addFileIntent);
    }

    public void fetchProduct() {
        collectionOfNotes.clear();
        databaseReference = FirebaseDatabase.getInstance().getReference("Notes");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                collectionOfNotes.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Note note = new Note(ds.child("id").getValue(String.class), ds.child("name").getValue(String.class), ds.child("subjectName").getValue(String.class), ds.child("description").getValue(String.class), ds.child("url").getValue(String.class), ds.child("pdfname").getValue(String.class), ds.child("email").getValue(String.class), ds.child("size").getValue(String.class), ds.child("addedOn").getValue(String.class));
                    collectionOfNotes.add(note);
                }
                buildCard();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
    }

    public void buildCard() {
        homeScroll.removeAllViews();
        map = new HashMap<>();
        for (Note n : collectionOfNotes) {
            if (map.containsKey(n.subjectName.toUpperCase())) {
                map.get(n.subjectName.toUpperCase()).add(n);
            } else {
                List<Note> list = new ArrayList<>();
                list.add(n);
                map.put(n.subjectName.toUpperCase(), list);
            }
        }

        for (String n : map.keySet()) {
            View view = LayoutInflater.from(this).inflate(R.layout.category, null);
            TextView head = (TextView) view.findViewById(R.id.heading);
            head.setText(n);
            recyclerView = view.findViewById(R.id.maths_layout);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            adapter = new MyAdapter(getBaseContext(), map.get(n), n, this);
            recyclerView.setAdapter(adapter);
            homeScroll.addView(view);
        }
        progressDialog.dismiss();
        saveOffline();
    }

    private void saveOffline() {
        Converter converter = new Converter(map);
        String str = converter.convertToString();

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("notesData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("offlineData", str);
        boolean s = editor.commit();
        System.out.println(s);

    }

}

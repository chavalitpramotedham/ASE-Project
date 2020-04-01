package com.example.studyhotspot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.api.LogDescriptor;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class ActivityPageMain extends AppCompatActivity {
    ArrayList<Activity> listMSActivity = new ArrayList<Activity>();
    ArrayList<Activity> listIActivity = new ArrayList<>();
    ArrayList<Activity> listFAActivity = new ArrayList<>();

    private UserDatabaseManager userDatabaseManager = new UserDatabaseManager(this);

    String userID;
    String currentUser;
    String userEmail;

    String startDate;
    String startTime;


    private FloatingActionButton homeButton;
    private BottomAppBar bottomAppBar;
    private Button historyButton;
    private FloatingActionButton refresh;

    //RecyclerView stuffs
    private static final String TAG = "ActivityPageMain";

    //for 1st box

    private ArrayList<String> mImageUrls = new ArrayList<>();
    private ArrayList<String> fMS1 = new ArrayList<>();
    private ArrayList<String> fMS2 = new ArrayList<>();
    private ArrayList<String> mNames = new ArrayList<String>();
    private ArrayList<String> id1 = new ArrayList<>();

    //History
    private ArrayList<String> historyNames = new ArrayList<>();
    private ArrayList<String> historyCreators = new ArrayList<>();
    private ArrayList<String> historyIDs = new ArrayList<String>();

    //for 2nd box
    private ArrayList<String> mNames2 = new ArrayList<>();
    private ArrayList<String> mImageUrls2 = new ArrayList<>();
    private ArrayList<String> fI1 = new ArrayList<>();
    private ArrayList<String> mImageUrls22 = new ArrayList<>();
    private ArrayList<String> mImageUrls23 = new ArrayList<>();
    private ArrayList<String> id2 = new ArrayList<>();

    //for 3rd box
    private ArrayList<String> mNames3 = new ArrayList<>();
    private ArrayList<String> mImageUrls3 = new ArrayList<>();
    private ArrayList<String> fFA1 = new ArrayList<>();

    private String previousActivity = null;

    FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page);

        db = FirebaseFirestore.getInstance();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            previousActivity = extras.getString("prevActivity");
            currentUser = extras.getString("currentUser");
            userID = extras.getString("currentUID");
            userEmail = extras.getString("userEmail");
        }

        setUpBottomAppBar();
        setUpContent();
    }

    private void setUpContent() {
        historyButton = findViewById(R.id.history);
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHistory();
            }
        });

        refresh = findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initAllViews();
            }
        });

        initAllViews();
    }

    private void initAllViews() {
        initImageBitmaps();
        initImageBitmaps2();
        initImageBitmaps3();
    }

    private void initImageBitmaps() {
        Log.d(TAG, "initImageBitmaps: preparing bitmaps.");

        id1.clear();
        mNames.clear();
        mImageUrls.clear();
        fMS1.clear();
        fMS2.clear();

        historyNames.clear();
        historyIDs.clear();
        historyCreators.clear();

        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        dateFormatter.setTimeZone(TimeZone.getTimeZone("Asia/Singapore"));

        String startDateTimeString = startDate + " " + startTime;
        Date current = new Date();
        try {
            current = dateFormatter.parse(startDateTimeString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Timestamp currentTS = new Timestamp(current);


        db.collection("hashsessions").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    String status;
                    int i = 0;
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        HashMap<String, Boolean> doch = (HashMap<String, Boolean>) document.getData().get("participantStatus");
                        Log.d(TAG, "onComplete: creatorName is " + currentUser);
                        if (doch.containsKey(currentUser)) {
                            if (doch.get(currentUser) != null && doch.get(currentUser) == true) {

                                Timestamp actTS = new Timestamp(document.getDate("startDateTime"));
                                Timestamp endTS = new Timestamp(document.getDate("endDateTime"));
                                if (currentTS.compareTo(actTS) < 0) {
                                    status = "Upcoming";
                                    id1.add(document.getId());
                                    mNames.add(document.getString("title"));
                                    mImageUrls.add("https://upload.wikimedia.org/wikipedia/commons/2/25/Icon-round-Question_mark.jpg");
                                    fMS1.add("Status: " + status);
                                    fMS2.add("Created by: " + document.getString("creatorName"));
                                    initRecyclerView();
                                }
                                else if (currentTS.compareTo(endTS) < 0) {
                                    status = "Ongoing";
                                    id1.add(document.getId());
                                    mNames.add(document.getString("title"));
                                    mImageUrls.add("https://upload.wikimedia.org/wikipedia/commons/2/25/Icon-round-Question_mark.jpg");
                                    fMS1.add("Status: " + status);
                                    fMS2.add("Created by: " + document.getString("creatorName"));
                                    initRecyclerView();
                                }
                                else {
                                    status = "Past";
                                    historyIDs.add(document.getId());
                                    historyNames.add(document.getString("title"));
                                    historyCreators.add("Created by: " + document.getString("creatorName"));
                                }
                            }
                        }
                    }
                }
            }
        });

        //Working
        /*db.collection("sessions").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    int i = 0;
                    for (QueryDocumentSnapshot document:task.getResult()){
                        id1.add(document.getId());
                        mNames.add(document.getString("title"));
                        mImageUrls.add("https://upload.wikimedia.org/wikipedia/commons/2/25/Icon-round-Question_mark.jpg");
                        fMS1.add("Status: Testing");
                        fMS2.add("Created by: Testing");
                        Log.v(TAG, "this is adding id of document" + id1.get(i++));
                        Log.d(TAG, "onComplete: " + id1.size());
                        initRecyclerView();
                    }
                }
            }
        });*/

        //End of working


                /*for (int j = 0; j < id1.size(); j++){
                    DocumentReference docRef = db.collection("sessions").document(id1.get(j));
                    //final int finalJ = j;
                    docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            //documentName = id1.get(finalJ);
                            Log.d(TAG, "onSuccess: " + documentSnapshot.getString("title"));
                            mNames.add(documentSnapshot.getString("title"));
                            mImageUrls.add("https://upload.wikimedia.org/wikipedia/commons/2/25/Icon-round-Question_mark.jpg");
                            fMS1.add("Status: Testing");
                            fMS2.add("Created by: Testing");
                            initRecyclerView();

                        }
                    });
                }*/


        //Trying to read database into mNames
        /*DocumentReference docRef = db.collection("sessions").document("5nacJiy7Ch1sstSdZIyV");
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                mNames.add(documentSnapshot.getString("title"));
                mImageUrls.add("https://upload.wikimedia.org/wikipedia/commons/2/25/Icon-round-Question_mark.jpg");
                fMS1.add("Status: Testing");
                fMS2.add("Created by: Testing");
                initRecyclerView();
            }
        });*/


        /*docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Session s = documentSnapshot.toObject(Session.class);
                mNames.add(s.getTitle());
            }
        });*/
        //mNames.add("Testing");

    }

    private void initRecyclerView() {
        Log.d(TAG, "initRecyclerView: the last of titles are:" + mNames.get(mNames.size() - 1));
        if (mNames.size() != id1.size())
            return;
        Log.d(TAG, "1initRecyclerView: init recyclerview.");
        RecyclerView recyclerView = findViewById(R.id.recyclerview1);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(mNames, mImageUrls, fMS1, fMS2, this, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public void initImageBitmaps2() {
        Log.d(TAG, "initImageBitmaps: preparing bitmaps.");
        id2.clear();
        mNames2.clear();
        mImageUrls2.clear();
        fI1.clear();
        mImageUrls22.clear();
        mImageUrls23.clear();


        db.collection("hashsessions").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        HashMap<String, Boolean> doch = (HashMap<String, Boolean>) document.getData().get("participantStatus");
                        Log.d(TAG, "onComplete: creatorName is " + currentUser);
                        if (doch.containsKey(currentUser)) {
                            if (doch.get(currentUser) == null) {
                                id2.add(document.getId());
                                mNames2.add(document.getString("title"));
                                mImageUrls2.add("https://upload.wikimedia.org/wikipedia/commons/2/25/Icon-round-Question_mark.jpg");
                                fI1.add("Created by: " + document.getString("creatorName"));
                                mImageUrls22.add("https://png2.cleanpng.com/sh/cb26fdf957d05d2f15daec63603718fb/L0KzQYm3UsE1N5D6iZH0aYP2gLBuTfNpbZRwRd9qcnuwc73wkL1ieqUyfARuZX6whLrqi71uaaNwRadqOETkSIftUMk6bpc9RqI5OUC3SIa8UcUyQGc5S6U6MUC2SYW1kP5o/kisspng-check-mark-clip-art-green-tick-mark-5a84a86f099ff8.0090485515186433110394.png");
                                mImageUrls23.add("https://png2.cleanpng.com/sh/a003283ac6c66b520295b049d5fa5daf/L0KzQYm3VMA0N5puiZH0aYP2gLBuTfNpbZRwRd9qcnuwc7F0kQV1baMygdV4boOwg8r0gv9tNahmitDybnewRbLqU8NnbZRqSqI9YUCxQYq8UMMzQWU2TaQ7N0S4Q4O7WcI2QF91htk=/kisspng-check-mark-computer-icons-symbol-warning-5ac33fece204a0.1950329415227453249258.png");
                                initRecyclerView2();
                            }
                        }
                    }
                }
            }
        });
    }

    private void initRecyclerView2() {
        Log.d(TAG, "initRecyclerView: init recyclerview.");
        RecyclerView recyclerView2 = findViewById(R.id.recyclerview2);
        RecyclerViewAdapter2 adapter2 = new RecyclerViewAdapter2(mNames2, mImageUrls2, fI1, mImageUrls22, mImageUrls23, this, this);
        recyclerView2.setAdapter(adapter2);
        recyclerView2.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initImageBitmaps3() {
        Log.d(TAG, "initImageBitmaps: preparing bitmaps.");

        for (int i = 0; i < listFAActivity.size(); i++) {
            mImageUrls3.add("https://upload.wikimedia.org/wikipedia/commons/2/25/Icon-round-Question_mark.jpg");
            mNames3.add(listFAActivity.get(i).getname());
            fFA1.add("Created by: " + listFAActivity.get(i).getCreator());
        }
        initRecyclerView3();

    }

    private void initRecyclerView3() {
        Log.d(TAG, "initRecyclerView: init recyclerview.");
        RecyclerView recyclerView3 = findViewById(R.id.recyclerview3);
        RecyclerViewAdapter3 adapter3 = new RecyclerViewAdapter3(mNames3, mImageUrls3, fFA1, this, this);
        recyclerView3.setAdapter(adapter3);
        recyclerView3.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setUpBottomAppBar() {
        //find id
        homeButton = findViewById(R.id.homeButton);
        bottomAppBar = findViewById(R.id.bottomAppBar);

        //click event over Bottom bar menu item
        bottomAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                String title = item.getTitle().toString();
                if (title.contentEquals("Friends")) {
                    Intent intent = new Intent(ActivityPageMain.this, FindFriend.class);
                    intent.putExtra("currentUser", currentUser);
                    intent.putExtra("currentUID", userID);
                    intent.putExtra("userEmail", userEmail);
                    startActivity(intent);
                } else if (title.contentEquals("Activities")) {
                    Toast.makeText(ActivityPageMain.this, "Activity Page", Toast.LENGTH_LONG).show();
                } else if (title.contentEquals("Settings")) {
                    Intent intent = new Intent(ActivityPageMain.this, Logout.class);
                    intent.putExtra("currentUser", currentUser);
                    intent.putExtra("currentUID", userID);
                    intent.putExtra("userEmail", userEmail);
                    startActivity(intent);
                }

                return false;
            }
        });

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (previousActivity != null && previousActivity.contentEquals("HOME")) {
                    finish();
                } else {
                    Intent intent = new Intent(ActivityPageMain.this, MapsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        });
    }

    public void openHistory() {
        Intent intent = new Intent(this, History.class);
        intent.putExtra("IDs", historyIDs);
        intent.putExtra("Names", historyNames);
        intent.putExtra("Creators", historyCreators);
        startActivity(intent);
    }

    //From actvity page to activity info page, to pass in document id

    public void showOwnSessionInfo(int position) {
        Intent intent = new Intent(this, SessionDetails.class);
        intent.putExtra("docname", id1.get(position));
        startActivity(intent);
    }

    public void showInviteInfo(int position) {
        Intent intent = new Intent(this, InvitationPage.class);
        intent.putExtra("docname", id2.get(position));
        System.out.println(id2.get(position));
        startActivity(intent);
    }

    public void showFriendActivityInfo(int position) {
        Intent intent = new Intent(this, FriendsActivityPage.class);
        startActivity(intent);
    }
}


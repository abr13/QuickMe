package com.abr.quickme.classes;

import android.app.Application;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class QuickMe extends Application {


    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate() {
        super.onCreate();
        //Firebase offline cache
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        //Picasso offline cache
//        Picasso.Builder builder = new Picasso.Builder(this);
//        builder.downloader((new OkHttp3Downloader(this, Integer.MAX_VALUE)));
//        Picasso built = builder.build();
//        built.setIndicatorsEnabled(true);
//        built.setLoggingEnabled(true);
//        Picasso.setSingletonInstance(built);

        //online/lastseen


        try {
            mAuth = FirebaseAuth.getInstance();
            String user = mAuth.getCurrentUser().getUid();
            mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users")
                    .child(user);
            mUserDatabase.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP);
        } catch (Exception e) {

        }
    }
}

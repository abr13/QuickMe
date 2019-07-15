package com.abr.quickme;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mUserOnline;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewPager mViewPager = findViewById(R.id.main_tabPager);
        MainPagerAdapter mPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        TabLayout mTabLayout = findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        mAuth = FirebaseAuth.getInstance();
        try {
            mUserOnline = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        } catch (Exception e) {
            sentToStart();
        }

        Toolbar mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Quick Me");
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            sentToStart();
        } else {
            mUserOnline.child("online").setValue(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        final String currentDateTime = dateFormat.format(date);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mUserOnline.child("online").setValue(currentDateTime);
        }
    }

    private void sentToStart() {
        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.main_logout_btn) {
            //logout

            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            final String currentDateTime = dateFormat.format(date);

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                mUserOnline.child("online").setValue(currentDateTime);
            }

            mAuth.signOut();
            sentToStart();
        } else if (item.getItemId() == R.id.main_accsetting_btn) {
            //account settings
            Intent profileSettingsIntent = new Intent(MainActivity.this, ProfileSettingsActivity.class);
            startActivity(profileSettingsIntent);
        } else if (item.getItemId() == R.id.main_allusers_btn) {
            //all users
            Intent allUsersIntent = new Intent(MainActivity.this, UsersActivity.class);
            startActivity(allUsersIntent);
        } else if (item.getItemId() == R.id.main_settings_btn) {
            //Open Account/App Settings
            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingsIntent);
        }
        return true;
    }
}

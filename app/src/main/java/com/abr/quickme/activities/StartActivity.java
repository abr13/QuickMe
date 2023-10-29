package com.abr.quickme.activities;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.abr.quickme.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.OnSuccessListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class StartActivity extends AppCompatActivity {

    private static final int RC_APP_UPDATE = 347;
    MaterialButton btn_create, btn_login;
    CircleImageView logo;
    TextView tagLine;

    //in-app update
    private AppUpdateManager appUpdateManager;
    private final InstallStateUpdatedListener installStateUpdatedListener = new InstallStateUpdatedListener() {
        @Override
        public void onStateUpdate(@NonNull InstallState state) {
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                showCompletedUpdate();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        btn_create = findViewById(R.id.btn_create);
        btn_login = findViewById(R.id.btn_login);
        logo = findViewById(R.id.logo);
        tagLine = findViewById(R.id.tagLine);

        //in-app update
        appUpdateManager = AppUpdateManagerFactory.create(this);
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
            @Override
            public void onSuccess(AppUpdateInfo result) {
                if (result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                        && result.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                    try {
                        appUpdateManager.startUpdateFlowForResult(result, AppUpdateType.FLEXIBLE, StartActivity.this
                                , RC_APP_UPDATE);

                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        appUpdateManager.registerListener(installStateUpdatedListener);

        //start animation
        Animation splashAnime = AnimationUtils.loadAnimation(this, R.anim.start_transition);
        logo.startAnimation(splashAnime);
        tagLine.startAnimation(splashAnime);

        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent regIntent = new Intent(StartActivity.this, RegisterActivity.class);
                startActivity(regIntent);
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent logIntent = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(logIntent);
            }
        });
    }

    //in-app update
    private void showCompletedUpdate() {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "New app is ready!",
                Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Install", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appUpdateManager.completeUpdate();
            }
        });
        snackbar.show();
    }

    @Override
    protected void onStop() {
        //in-app update
        if (appUpdateManager != null)
            appUpdateManager.unregisterListener(installStateUpdatedListener);
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //in-app update
        if (requestCode == RC_APP_UPDATE && resultCode != RESULT_OK) {
            Toast.makeText(this, "Cancel", Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}

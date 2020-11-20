package com.thinqtv.thinqtv_android;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.thinqtv.thinqtv_android.data.UserRepository;

public class MainActivity extends AppCompatActivity {
    public static BottomNavigationView bottomNavigation;
    private GoogleSignInClient mGoogleSignInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent loadSavedUser = new Intent(this, StartupLoadingActivity.class);
        startActivity(loadSavedUser);

        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        // If a user is logged in, use their name. Otherwise, try to find a name elsewhere.
        if (!UserRepository.getInstance().isLoggedIn())
        {
            // if Google is signed in but user is not, just sign out of Google
            GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);
            if (googleSignInAccount != null)
                mGoogleSignInClient.signOut()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.i(getString(R.string.google_sign_in_tag), "Signed out of Google account");
                            }
                        });
        }
    }

    @Override
    public void onBackPressed() {
        // Disable back button for fragments INSIDE this activity
        // Not disabled between activities.
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Open the proper fragment depending on login
        if (UserRepository.getInstance().isLoggedIn()) {
            openFragment(conversation_fragment.newInstance());
            bottomNavigation.setSelectedItemId(R.id.action_conversation);
        } else {
            openFragment(welcome_fragment.newInstance());
            bottomNavigation.setSelectedItemId(R.id.action_profile);
        }
    }

    public void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = item ->
    {
        switch (item.getItemId()) {
            case R.id.action_conversation:
                openFragment(conversation_fragment.newInstance());
                return true;
            case R.id.action_inviteus:
                openFragment(inviteus_fragment.newInstance());
                return true;
            case R.id.action_aboutus:
                openFragment(aboutus_fragment.newInstance("",""));
                return true;
            case R.id.action_profile:
                if (UserRepository.getInstance().isLoggedIn())
                    openFragment(profile_fragment.newInstance(mGoogleSignInClient));
                else
                    openFragment(welcome_fragment.newInstance());
                return true;
        }
        return false;
    };
}
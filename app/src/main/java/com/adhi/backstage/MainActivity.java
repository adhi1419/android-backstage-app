package com.adhi.backstage;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.adhi.backstage.com.adhi.backstage.fragments.About;
import com.adhi.backstage.com.adhi.backstage.fragments.Events;
import com.adhi.backstage.com.adhi.backstage.fragments.HomeFragment;
import com.adhi.backstage.com.adhi.backstage.fragments.Inventory;
import com.adhi.backstage.com.adhi.backstage.fragments.Items;
import com.adhi.backstage.com.adhi.backstage.fragments.Members;
import com.adhi.backstage.com.adhi.backstage.fragments.Profile;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FirebaseAuth auth;
    private GoogleApiClient client;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private CoordinatorLayout cLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        cLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        setSupportActionBar(toolbar);
        if (getIntent().getIntExtra("fragment", R.id.home) == R.id.home) {
            try {
                FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            } catch (DatabaseException de) {
                de.printStackTrace();
            }
        }
        initNavigationDrawer();
        displayFragment(getIntent().getIntExtra("fragment", R.id.home));
        //showSnackBar();
        auth = FirebaseAuth.getInstance();
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void initNavigationDrawer() {
        final NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        final FirebaseUser fuUser = FirebaseAuth.getInstance().getCurrentUser();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                displayFragment(menuItem.getItemId());
                return true;
            }
        });

        final View header = navigationView.getHeaderView(0);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(fuUser.getEmail().split("\\@")[0]);

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                TextView tv_email = (TextView) header.findViewById(R.id.tv_email);
                tv_email.setText(dataSnapshot.child("name").getValue().toString() + "\n" + fuUser.getEmail());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDatabase.addValueEventListener(postListener);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View v) {
                super.onDrawerClosed(v);
            }

            @Override
            public void onDrawerOpened(View v) {
                super.onDrawerOpened(v);
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    private void displayFragment(int id) {
        Fragment fragment = null;
        String title = getString(R.string.app_name);
        switch (id) {
            case R.id.home:
                fragment = new HomeFragment();
                title = getString(R.string.title_home);
                break;
            case R.id.events:
                fragment = new Events();
                title = getString(R.string.title_events);
                break;
            case R.id.members:
                fragment = new Members();
                title = getString(R.string.title_members);
                break;
            case R.id.items:
                fragment = new Items();
                title = getString(R.string.title_items);
                break;
            case R.id.inventory:
                fragment = new Inventory();
                title = getString(R.string.title_inventory);
                break;
            case R.id.about:
                fragment = new About();
                title = getString(R.string.title_about);
                break;
            case R.id.profile:
                fragment = new Profile();
                title = getString(R.string.title_profile);
                break;
            case R.id.sign_out:
                auth.signOut();
                Intent intent = new Intent(MainActivity.this, LoginScreen.class);
                startActivity(intent);
                finish();
            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_body, fragment);
            fragmentTransaction.commit();

            getSupportActionBar().setTitle(title);
        }

        drawerLayout.closeDrawers();
    }

    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    public boolean isOnline() {

        Runtime runtime = Runtime.getRuntime();
        try {

            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void showSnackBar() {
        Snackbar snackbar = Snackbar.make(cLayout, "No internet connection!", Snackbar.LENGTH_LONG);
        if (!isOnline()) {
            snackbar.setAction("RETRY", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showSnackBar();
                }
            });

            snackbar.setActionTextColor(Color.RED);

            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(Color.YELLOW);
            snackbar.show();
        } else {
            snackbar.dismiss();
        }
    }
}

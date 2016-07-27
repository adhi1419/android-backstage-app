package com.adhi.backstage;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
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
import com.adhi.backstage.com.adhi.backstage.fragments.Allotments;
import com.adhi.backstage.com.adhi.backstage.fragments.HomeFragment;
import com.adhi.backstage.com.adhi.backstage.fragments.Inventory;
import com.adhi.backstage.com.adhi.backstage.fragments.Members;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FirebaseAuth auth;
    private GoogleApiClient client;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initNavigationDrawer();
        Intent i = getIntent();
        displayFragment(i.getIntExtra("callFrom", R.id.home));

        auth = FirebaseAuth.getInstance();
        final FirebaseUser fuUser = FirebaseAuth.getInstance().getCurrentUser();
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void initNavigationDrawer() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
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
            case R.id.allot:
                fragment = new Allotments();
                title = getString(R.string.title_allot);
                break;
            case R.id.members:
                fragment = new Members();
                title = getString(R.string.title_members);
                break;
            case R.id.inventory:
                fragment = new Inventory();
                title = getString(R.string.title_inventory);
                break;
            case R.id.about:
                fragment = new About();
                title = getString(R.string.title_about);
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
}

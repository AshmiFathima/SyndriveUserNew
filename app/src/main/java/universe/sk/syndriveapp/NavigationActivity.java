package universe.sk.syndriveapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;

public class NavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private ServiceHandler mServiceHandler;
//    private boolean isDriving = false;
    private static boolean isTracking;
    CircleImageView imageViewNavPic;
    TextView tvNavName, tvNavEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mServiceHandler = new ServiceHandler(this);
        isTracking = false;

        ActionBar actionBar = getSupportActionBar();
        //actionBar.setIcon(R.mipmap.icon1);
        actionBar.setTitle(" SYNDRIVE");
        actionBar.setDisplayUseLogoEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        storageReference = firebaseStorage.getReference();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        imageViewNavPic = header.findViewById(R.id.imageViewNavPic);
        tvNavName = header.findViewById(R.id.tvNavName);
        tvNavEmail = header.findViewById(R.id.tvNavEmail);

        setNavProfilePic();

        final DatabaseReference databaseReference = firebaseDatabase.getReference("users").child(firebaseAuth.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Userinfo userinfo;
                userinfo = dataSnapshot.getValue(Userinfo.class);

                tvNavName.setText(userinfo.getUsername().trim());
                tvNavEmail.setText(userinfo.getUemail().trim());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(NavigationActivity.this, databaseError.getCode(), Toast.LENGTH_SHORT).show();
            }
        });

        //default fragment
        ContactUsFragment contactUsFragment = new ContactUsFragment();
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.flMain, contactUsFragment).commit();

        navigationView.setCheckedItem(R.id.nav_contactus);
    } //end of onCreate

    private void setNavProfilePic() {
        storageReference = firebaseStorage.getReference();
        storageReference.child(firebaseAuth.getUid()).child("Images/Profile Pic").getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).fit().centerCrop().into(imageViewNavPic);
                    }
                });
    } // end of setNavProfilePic

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation, menu);
        //return true;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.toggle_driving) {
            if(isTracking) { //Tracking mode is true
                mServiceHandler.doUnbindService();
                item.setTitle(R.string.toggle_start_driving);
//                Toast.makeText(this, "Driving mode on", Toast.LENGTH_SHORT).show();
                isTracking = false; //Tracking mode is changed to false

            }
            else {
                mServiceHandler.doBindService();
                item.setTitle(R.string.toggle_stop_driving);
//                Toast.makeText(this, "Driving mode off", Toast.LENGTH_SHORT).show();
                isTracking = true;

            }
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
           startActivity(new Intent(this, EditProfileActivity.class));
        }
        else if (id == R.id.nav_contacts) {
            startActivity(new Intent(this, ContactsActivity.class));
        }
        else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }
        else if (id == R.id.nav_about) {
            startActivity(new Intent (this, AboutActivity.class));
        }
        else if (id == R.id.nav_logout) {
            firebaseAuth.signOut();
            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finish();
            startActivity(i);
        }
        else if (id == R.id.nav_contactus){
            ContactUsFragment contactUsFragment = new ContactUsFragment();
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.flMain, contactUsFragment).commit();
        }
        else if (id == R.id.nav_googleLogout){
            startActivity(new Intent(this,AccountActivity.class));
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    } //end of onNavigationItemSelected

} //end of Navigation Activity

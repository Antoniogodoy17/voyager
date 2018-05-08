package voyager.voyager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ListActivity extends AppCompatActivity {
    //UI initialization
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    View header;
    ListView listView;
    TextView drawerUsername;
    CircleImageView imgProfilePicture;
    CircleImageView drawerProfilePicture;
    ProgressDialog progressDialog;
    private ArrayList<Card> cardsList;
    private ArrayList<Activity> activities;
    //

    // Database setup
    private FirebaseDatabase database;
    private DatabaseReference listsDatabase, userRef;
    private FirebaseUser fbUser;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authListener;
    //

    // Variables Setup
    String fbUserId;
    User user;
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        // Database initialization
        database = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        fbUserId = firebaseAuth.getCurrentUser().getUid();
        userRef = database.getReference("User").child(fbUserId);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                displayProgressDialog(R.string.Please_Wait,R.string.Please_Wait);
                user = dataSnapshot.getValue(User.class);
                setupDrawerUsername();
                if(dataSnapshot.hasChild("profile_picture")){
                    setupDrawerProfilePicture(user.getProfile_picture());
                }
//                progressDialog.dismiss();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
        //

        //Header
        NavigationView navigationView = findViewById(R.id.navigationView);
        drawerLayout = findViewById(R.id.drawer);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setupDrawerContent(navigationView);
        header = navigationView.getHeaderView(0);
        drawerUsername = header.findViewById(R.id.drawerUsername);
        drawerProfilePicture = header.findViewById(R.id.drawerProfilePicture);
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent next = new Intent(getApplicationContext(),ProfileActivity.class);
                startActivity(next);
            }
        });
        //

        listView = findViewById(R.id.listView);

        cardsList = new ArrayList<Card>();

        //Implement a loop here to dinamically create Cards with the ordered Activities

//        for(Activity activity:activities){
//            System.out.println("------>" + activity.title);
//        }
        //End loop
//        cardsList.add(new Card("drawable://"+R.drawable.logo512,"Actividad 1"));
//        cardsList.add(new Card("drawable://"+R.drawable.logo512,"Actividad 2"));
//        cardsList.add(new Card("drawable://"+R.drawable.logo512,"Actividad 3"));
//        cardsList.add(new Card("drawable://"+R.drawable.logo512,"Actividad 4"));
//        cardsList.add(new Card("drawable://"+R.drawable.logo512,"Actividad 5"));

        CardListAdapter cardAdapter = new CardListAdapter(this, R.layout.card_layout, cardsList);
        listView.setAdapter(cardAdapter);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(drawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void setupDrawerContent(NavigationView navigationView){
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectDrawerMenu(item);
                return true;
            }
        });
    }
    public void displayProgressDialog(int title, int message){
        progressDialog.setTitle(title);
        progressDialog.setMessage(getApplicationContext().getString(message));
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);
    }
    public void selectDrawerMenu(MenuItem menu){
        Intent next = null;
        switch (menu.getItemId()){
            case R.id.homeMenu:
                next = new Intent(this,homeActivity.class);
                break;
            case R.id.categoriesMenu:
                next = new Intent(this,CategoriesActivity.class);
                break;
            case R.id.favoritesMenu:
                next = new Intent(this,FavoritesActivity.class);
                break;
            case R.id.listsMenu:
                next = new Intent(this,ListsActivity.class);
                break;
            case R.id.switchLocationMenu:
                next = new Intent(this,SwitchLocationActivity.class);
                break;
            case R.id.logoutMenu:
                firebaseAuth.signOut();
                next = new Intent(this,LogInActivity.class);
                break;
        }
        startActivity(next);
    }
    public void setupDrawerUsername(){
        drawerUsername.setText(user.name + " " + user.lastname);
    }
    public void setupDrawerProfilePicture(String url){
        Picasso.get().load(url).into(drawerProfilePicture);
    }
}
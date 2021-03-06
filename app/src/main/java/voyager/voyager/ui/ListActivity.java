package voyager.voyager.ui;

import android.app.Dialog;
import android.app.ProgressDialog;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import voyager.voyager.models.Activity;
import voyager.voyager.models.Card;
import voyager.voyager.adapters.CardListAdapter;
import voyager.voyager.R;
import voyager.voyager.models.User;
import voyager.voyager.ui.dialogs.DeleteItemDialog;

public class ListActivity extends AppCompatActivity implements DeleteItemDialog.NoticeDialogListener {
    //UI initialization
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    View header;
    ListView listView;
    TextView drawerUsername;
    CircleImageView drawerProfilePicture;
    ProgressDialog progressDialog;
    FloatingActionButton btnEditList;
    Dialog editListDialog;
    Button btnSaveChanges, btnCancel;
    ImageButton btnDelete;
    EditText txtListName;

    // Database Setup
    private DatabaseReference userRef, activitiesRef;
    private ValueEventListener userListener, activitiesListener;
    private FirebaseAuth firebaseAuth;
    //
    private User user;
    private String listName;
    private ArrayList<Card> cardsList;
    private ArrayList<Activity> activities;
    private ArrayList<HashMap<String,String>> favoriteList;
    private ArrayList<Activity> favoriteActivities;
    private CardListAdapter cardAdapter;
    //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null)
        {
            listName = bundle.getString("list");
        }
        //Check the list name and place that as a title
        setTitle(listName.substring(0,1).toUpperCase() + listName.substring(1));

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
        btnEditList = findViewById(R.id.btnEditList);

        if(listName.equals("favorites")){
            btnEditList.setVisibility(View.GONE);
        }
        btnEditList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog(listName);
            }
        });
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent next = new Intent(getApplicationContext(),ProfileActivity.class);
                startActivity(next);
            }
        });
        //

        listView = findViewById(R.id.listView);
        cardsList = new ArrayList<>();
        progressDialog = new ProgressDialog(this);
        activities = new ArrayList<>();
        favoriteList = new ArrayList<>();
        favoriteActivities = new ArrayList<>();

        // UI initialization
        editListDialog = new Dialog(ListActivity.this);
        editListDialog.setContentView(R.layout.edit_list_layout);
        editListDialog.setTitle(R.string.Rename_List);
        txtListName = editListDialog.findViewById(R.id.txtListName);
        btnSaveChanges = editListDialog.findViewById(R.id.btnSaveChanges);
        btnCancel = editListDialog.findViewById(R.id.btnCancel);
        btnDelete = editListDialog.findViewById(R.id.btnDelete);
        //

        // Database Initialization
        firebaseAuth = FirebaseAuth.getInstance();
        activitiesRef = FirebaseDatabase.getInstance().getReference("Activities");
        activitiesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                loadActivities(dataSnapshot);
                progressDialog.dismiss();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        };
        activitiesRef.addValueEventListener(activitiesListener);
        userRef = FirebaseDatabase.getInstance().getReference("User").child(firebaseAuth.getCurrentUser().getUid());
        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                setupDrawerUsername();
                if(dataSnapshot.hasChild("profile_picture")){
                    setupDrawerProfilePicture(user.getProfile_picture());
                }
                setFavoriteList();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        userRef.addValueEventListener(userListener);
        // End Database Initialization
    }

    private void openDialog(final String listName) {
        txtListName.setText(listName);
        final DatabaseReference listReference = userRef.child("lists").child(listName);
        btnSaveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String newListName = txtListName.getText().toString();
                if (!newListName.isEmpty() && !newListName.equals(listName) && !newListName.equals("favorites") && !newListName.matches("\\d+")){
                    final DatabaseReference newListReference = userRef.child("lists").child(newListName);
                    moveListChild(listReference, newListReference);
                    startActivity(getIntent().putExtra("list", newListName));
                    removeList(listReference);
                    editListDialog.cancel();
                } else {
                    Toast.makeText(ListActivity.this, "Pick a new name for your list", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //removeList(listReference);
                //editListDialog.cancel();
                txtListName.setText("");
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editListDialog.cancel();
            }
        });
        editListDialog.show();
    }
    private void removeList(DatabaseReference listReference){
        listReference.removeValue();
        finish();
    }
    private void moveListChild(final DatabaseReference fromPath, final DatabaseReference toPath) {
        fromPath.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                toPath.setValue(dataSnapshot.getValue(), new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                        if (firebaseError != null) {
                            System.out.println("Copy failed");
                        } else {
                            System.out.println("Success");

                        }
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onDeleteItem(String id) {
        ArrayList<HashMap<String,String>> temp = new ArrayList<>();

        for (HashMap hm:favoriteList){
           if(!hm.get("id").equals(id)){
               temp.add(hm);
           }
        }

        DatabaseReference listRef = FirebaseDatabase.getInstance().getReference("User").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("lists").child(listName);

        listRef.setValue(temp);
    }

    @Override
    protected void onDestroy() {
        userRef.removeEventListener(userListener);
        activitiesRef.removeEventListener(activitiesListener);
        super.onDestroy();
    }

    public void displayProgressDialog(int title, int message){
        progressDialog.setTitle(title);
        progressDialog.setMessage(getApplicationContext().getString(message));
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);
    }

    public void loadActivities(DataSnapshot data){
        activities.clear();
        for(DataSnapshot ds : data.getChildren()){
            activities.add(ds.getValue(Activity.class));
        }
    }
    public void displayActivities(){
        cardsList.clear();
        for(Activity activity:favoriteActivities){
            cardsList.add(new Card(activity));
        }
        cardAdapter = new CardListAdapter(this, R.layout.card_layout, cardsList);
        listView.setAdapter(cardAdapter);
        ListActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cardAdapter.notifyDataSetChanged();
            }
        });
        progressDialog.dismiss();
    }
    public void setFavoriteList() {
        displayProgressDialog(R.string.Loading_events, R.string.Please_Wait);
        favoriteActivities.clear();
        if (user.getLists() != null && user.getLists().containsKey(listName)) {
            favoriteList = user.getLists().get(listName);
            for (int i = 0; i < activities.size(); i++) {
                for (HashMap<String, String> hm : favoriteList) {
                    if (hm.get("id").equals(activities.get(i).get_id())) {
                        favoriteActivities.add(activities.get(i));
                    }
                }
            }
            displayActivities();
        }
        else {
            cardsList.clear();
            cardAdapter = new CardListAdapter(this, R.layout.card_layout, cardsList);
            listView.setAdapter(cardAdapter);
            ListActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    cardAdapter.notifyDataSetChanged();
                }
            });
            progressDialog.dismiss();
        }
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

    public void selectDrawerMenu(MenuItem menu){
        Class intentClass = null;
        switch (menu.getItemId()){
            case R.id.homeMenu:
                intentClass = HomeActivity.class;
                break;
            case R.id.categoriesMenu:
                intentClass = CategoriesActivity.class;
                break;
            case R.id.favoritesMenu:
                intentClass = ListActivity.class;
                break;
            case R.id.listsMenu:
                intentClass = ListsActivity.class;
                break;
            case R.id.switchLocationMenu:
                intentClass = SwitchLocationActivity.class;
                break;
            case R.id.nearMeMenu:
                intentClass = MapsActivity.class;
                break;
            case R.id.logoutMenu:
                intentClass = LogInActivity.class;
                firebaseAuth.signOut();
                break;
        }
        if(intentClass != this.getClass() && intentClass != null){
            Intent nextView = new Intent(this,intentClass);
            if(intentClass == ListActivity.class){
                nextView.putExtra("list","favorites");
            }
            startActivity(nextView);
            drawerLayout.closeDrawers();
        }
    }

    public void setupDrawerUsername(){
        drawerUsername.setText(user.getName() + " " + user.getLastname());
    }

    public void setupDrawerProfilePicture(String url){
        Picasso.get().load(url).into(drawerProfilePicture);
    }
}
package voyager.voyager;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.UserProfileChangeRequest;

import java.io.IOException;
import java.util.Calendar;

public class ProfileActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    View header;
    private static ProfileVM vm;
    private Profile.OnFragmentInteractionListener mListener;
    //Our code from profile goes here
    EditText txtNameProfile, txtLastNameProfile, txtEmailProfile, txtPhoneProfile, txtPasswordProfile, txtLocationProfile;
    TextView txtBirthDateProfile;
    Button btnSaveChanges, btnCancel;
    ImageButton btnProfilePic, btnEditProfile;
    Spinner sprCountryProfile, sprStateProfile, sprCityProfile;
    DatePickerDialog datePicker;
    String name, lastname, email, phone, birth_date, location, password;
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 71;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        NavigationView navigationView = findViewById(R.id.navigationView);
        drawerLayout = findViewById(R.id.drawer);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setupDrawerContent(navigationView);

        header = navigationView.getHeaderView(0);
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToProfile();
            }
        });

        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnProfilePic = findViewById(R.id.btnProfilePic);
        btnCancel = findViewById(R.id.btnCancel);
        txtNameProfile = findViewById(R.id.txtNameProfile);
        txtLastNameProfile = findViewById(R.id.txtLastNameProfile);
        txtEmailProfile = findViewById(R.id.txtEmailProfile);
        txtPhoneProfile = findViewById(R.id.txtPhoneProfile);
        txtPasswordProfile = findViewById(R.id.txtPasswordProfile);
        txtBirthDateProfile = findViewById(R.id.txtBirthDateProfile);
        txtLocationProfile = findViewById(R.id.txtLocationProfile);
        sprCountryProfile = findViewById(R.id.sprCountryProfile);
        sprStateProfile = findViewById(R.id.sprStateProfile);
        sprCityProfile = findViewById(R.id.sprCityProfile);

        fillFields();
        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editMode();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewMode();
            }
        });

        txtBirthDateProfile.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                final Calendar c = Calendar.getInstance();
                int selYear = c.get(Calendar.YEAR); // current year
                int selMonth = c.get(Calendar.MONTH); // current month
                int selDay = c.get(Calendar.DAY_OF_MONTH); // current day
                // date picker dialog
                datePicker = new DatePickerDialog(ProfileActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        txtBirthDateProfile.setText(dayOfMonth + "/"+ (monthOfYear + 1) + "/" + year);
                    }
                }, selYear, selMonth, selDay);
                c.add(Calendar.YEAR,-10);
                datePicker.getDatePicker().setMaxDate(c.getTimeInMillis());
                c.add(Calendar.YEAR, -100);
                datePicker.getDatePicker().setMinDate(c.getTimeInMillis());
                datePicker.show();
            }
        });

        btnSaveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChanges();
                Toast.makeText(ProfileActivity.this,"Changes have been made." , Toast.LENGTH_LONG).show();
                viewMode();
            }
        });
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
        Intent next = null;
//        switch (menu.getItemId()){
//            case R.id.homeMenu:
//                next = new Intent(this,homeActivity.class);
//                break;
//            case R.id.categoriesMenu:
//                next = new Intent(this,CategoriesActivity.class);
//                break;
//            case R.id.favoritesMenu:
//                next = new Intent(this,FavoritesActivity.class);
//                break;
//            case R.id.listsMenu:
//                next = new Intent(this,ListsActivity.class);
//                break;
//            case R.id.switchLocationMenu:
//                next = new Intent(this,SwitchLocationActivity.class);
//                break;
//            case R.id.logoutMenu:
//                break;
//        }
//        startActivity(next);
//        finish();
    }
    public void goToProfile(){
        Intent profile = new Intent(this,ProfileActivity.class);
        startActivity(profile);
        finish();
    }
    protected void fillFields(){
        txtNameProfile.setText(user.getName());
        txtLastNameProfile.setText(user.getLastname());
        txtEmailProfile.setText(user.getEmail());
        txtPhoneProfile.setText(user.getPhone());
//        txtPasswordProfile
        txtBirthDateProfile.setText(user.getBirth_date());

        viewMode();

        btnProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null ){
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                btnProfilePic.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    protected void saveChanges(){
        user.setName(txtNameProfile.getText().toString().trim());
        user.setLastname(txtLastNameProfile.getText().toString().trim());
        user.setEmail(txtEmailProfile.getText().toString().trim());
        user.setPhone(txtPhoneProfile.getText().toString().trim());
        //password pendiente
        user.setBirth_date(txtBirthDateProfile.getText().toString().trim());
        vm.getUsersDatabase().child(user.getId()).setValue(user);
        vm.setUser(user);

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(user.getName()+" "+user.getLastname()).build();
        vm.getFbUser().updateProfile(profileUpdates);
        updateDrawerUserName();
    }

    public void updateDrawerUserName(){
        NavigationView navigationView = findViewById(R.id.navigationView);
        View headerView = navigationView.getHeaderView(0);
        TextView username = headerView.findViewById(R.id.drawerUsername);
        username.setText(vm.getFbUser().getDisplayName());
    }

    protected void editMode(){
        txtNameProfile.setVisibility(View.VISIBLE);
        txtNameProfile.setEnabled(true);

        txtLastNameProfile.setVisibility(View.VISIBLE);
        txtLastNameProfile.setEnabled(true);

        txtEmailProfile.setVisibility(View.VISIBLE);
        txtEmailProfile.setEnabled(true);

        txtPhoneProfile.setVisibility(View.VISIBLE);
        txtPhoneProfile.setEnabled(true);

        txtPasswordProfile.setVisibility(View.VISIBLE);
        txtPasswordProfile.setEnabled(true);

        txtBirthDateProfile.setVisibility(View.VISIBLE);
        txtBirthDateProfile.setEnabled(true);

        txtLocationProfile.setVisibility(View.GONE);

        sprCountryProfile.setVisibility(View.VISIBLE);
        sprStateProfile.setVisibility(View.VISIBLE);
        sprCityProfile.setVisibility(View.VISIBLE);

        btnProfilePic.setEnabled(true);
        btnEditProfile.setVisibility(View.INVISIBLE);
        btnSaveChanges.setVisibility(View.VISIBLE);
        btnCancel.setVisibility(View.VISIBLE);
    }

    protected void viewMode(){
        if (txtNameProfile.getText().toString().isEmpty())
            txtNameProfile.setVisibility(View.GONE);
        else txtNameProfile.setEnabled(false);

        if (txtEmailProfile.getText().toString().isEmpty())
            txtEmailProfile.setVisibility(View.GONE);
        else txtEmailProfile.setEnabled(false);

        if (txtPhoneProfile.getText().toString().isEmpty())
            txtPhoneProfile.setVisibility(View.GONE);
        else txtPhoneProfile.setEnabled(false);

        if (txtPasswordProfile.getText().toString().isEmpty())
            txtPasswordProfile.setVisibility(View.GONE);
        else txtPasswordProfile.setEnabled(false);

        if (txtBirthDateProfile.getText().toString().isEmpty())
            txtBirthDateProfile.setVisibility(View.GONE);
        else txtBirthDateProfile.setEnabled(false);

        if (txtLocationProfile.getText().toString().isEmpty())
            txtLocationProfile.setVisibility(View.GONE);
        else txtLocationProfile.setEnabled(false);

        txtLastNameProfile.setVisibility(View.GONE);
        sprCountryProfile.setVisibility(View.GONE);
        sprStateProfile.setVisibility(View.GONE);
        sprCityProfile.setVisibility(View.GONE);

        btnProfilePic.setEnabled(false);
        btnEditProfile.setVisibility(View.VISIBLE);
        btnSaveChanges.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);
    }
}
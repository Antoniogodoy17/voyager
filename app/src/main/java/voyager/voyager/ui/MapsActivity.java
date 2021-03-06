package voyager.voyager.ui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;
import android.app.AlertDialog;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import voyager.voyager.R;
import voyager.voyager.models.Activity;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int REQUEST_LOCATION = 1;
    private GoogleMap mMap;
    private static final String FINE_LOCATION  = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private boolean mLocationPermissionGranted = false;
    private final static int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private DatabaseReference activityDatabase;
    private ValueEventListener activityValueListener;
    private LocationManager locationManager;
    private String lattitude,longitude;
    private ArrayList<Activity> activitiesList;
    LocationListener locationListener;
    Circle circle;
    LatLng latlng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        activitiesList = new ArrayList<>();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            getLocation();
        }

        activityDatabase = FirebaseDatabase.getInstance().getReference("Activities");
        activityValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                loadActivities(dataSnapshot);
                activitiesList.add(dataSnapshot.getValue(Activity.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double latti = location.getLatitude();
                double longi = location.getLongitude();
                latlng = new LatLng(latti, longi);
                lattitude = String.valueOf(latti);
                longitude = String.valueOf(longi);
                setUserMarker(latlng);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        activitiesList = new ArrayList<>();
        activityDatabase.addValueEventListener(activityValueListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityDatabase.removeEventListener(activityValueListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activityDatabase.removeEventListener(activityValueListener);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng coordinates = getLocation();
        setUserMarker(coordinates);
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return false;
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
//                sendActivity(marker.getTitle());
                if(!marker.getTitle().equals("You")){
                    Intent cardActivity = new Intent(MapsActivity.this,ActivityActivity.class);
                    cardActivity.putExtra("activity",sendActivity(marker.getTitle()));
                    MapsActivity.this.startActivity(cardActivity);
                }

            }
        });

    }
    private LatLng getLocation() {

        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        } else {
            Location location =    locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            Location location1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            Location location2 = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);


            if (location != null) {
                double latti = location.getLatitude();
                double longi = location.getLongitude();
                latlng = new LatLng(latti, longi);
                lattitude = String.valueOf(latti);
                longitude = String.valueOf(longi);

                return latlng;

            } else if (location1 != null) {
                double latti = location1.getLatitude();
                double longi = location1.getLongitude();
                latlng = new LatLng(latti, longi);
                lattitude = String.valueOf(latti);
                longitude = String.valueOf(longi);

                return latlng;

            } else if (location2 != null) {
                double latti = location2.getLatitude();
                double longi = location2.getLongitude();
                latlng = new LatLng(latti, longi);
                lattitude = String.valueOf(latti);
                longitude = String.valueOf(longi);
                return latlng;

            }
        }
        latlng = null;
        return latlng;
    }

    protected void buildAlertMessageNoGps() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please Turn ON your GPS Connection")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;
                }

            }
        }
    }

    public void setUserMarker(LatLng latlng){

        if(latlng == null){
            Toast.makeText(this," Unable to Trace your location",Toast.LENGTH_SHORT).show();
        }
        else{
            if(circle != null){
                circle.remove();
            }
            mMap.addMarker(new MarkerOptions().position(latlng).title("You").icon(bitmapDescriptorFromVector(this, R.drawable.ic_nearme)));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
            mMap.animateCamera( CameraUpdateFactory.zoomTo( 11.0f ) );

            circle = mMap.addCircle(new CircleOptions().center(latlng).radius(10000)
                    .strokeColor(Color.argb(0,0,0,0)).fillColor(Color.argb(80,128,203,196)));

        }

    }

    public void getActivitiesLatLon(){

        if(activitiesList.size() > 0 || activitiesList != null){
            for(int i = 0 ; i < activitiesList.size(); i++ ){
                String lat = activitiesList.get(i).getLocation().get("latitude");
                String lon = activitiesList.get(i).getLocation().get("longitude");
                float[] disResultado = new float[2];

                Location.distanceBetween( Double.parseDouble(lat),Double.parseDouble(lon),
                        circle.getCenter().latitude,
                        circle.getCenter().longitude,
                        disResultado);

                if(disResultado[0] < circle.getRadius()){
                    LatLng newMarker = new LatLng(Double.parseDouble(lat), Double.parseDouble(lon));
                    mMap.addMarker(new MarkerOptions().position(newMarker).title(activitiesList.get(i).getTitle()).snippet(activitiesList.get(i).getDate()+" "+ activitiesList.get(i).getSchedule()));
                }
            }

        }
    }

    public void loadActivities(DataSnapshot data){
        for(DataSnapshot ds : data.getChildren()){
            activitiesList.add(ds.getValue(Activity.class));
        }

        getActivitiesLatLon();
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
    private Activity sendActivity(String activityTitle){
        for(Activity a: activitiesList){
            if(a.getTitle().equals(activityTitle)){
                return a;
            }
        }
        return null;
    }
}


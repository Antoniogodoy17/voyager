package voyager.voyager.ui.dialogs;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import voyager.voyager.R;
import voyager.voyager.adapters.ReviewAdapter;
import voyager.voyager.models.Activity;
import voyager.voyager.models.Review;
import voyager.voyager.ui.ReviewsActivity;

public class WatchReviewsDialog extends DialogFragment {
    ImageButton btnCloseReviews;
    Button btnShowAllReviews;
    ListView reviewsListView;
    ArrayList<Review> reviews;
    ReviewAdapter reviewAdapter;
    Activity myActivity;
    private FirebaseDatabase database;
    private DatabaseReference activityRef;
//    private Date date;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        String id = bundle.getString("id");
        reviews = new ArrayList<Review>();
        //Snapshot fromDatabase
        database = FirebaseDatabase.getInstance();
        activityRef = database.getReference("Activities").child(id);
        activityRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("reviews")){
                    myActivity = dataSnapshot.getValue(Activity.class);
                    loadReviews();
                }else{

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.watch_reviews_layout, null);
        builder.setView(view);
        // ReviewAdapter
        reviewsListView = view.findViewById(R.id.reviewsListView);

        btnShowAllReviews = view.findViewById(R.id.btnShowAllReviews);
        btnShowAllReviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reviews = new Intent(getContext(), ReviewsActivity.class);
                reviews.putExtra("activity",myActivity);
                startActivity(reviews);
                WatchReviewsDialog.this.dismiss();
            }
        });
        btnCloseReviews = view.findViewById(R.id.btnCloseReviews);
        btnCloseReviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WatchReviewsDialog.this.dismiss();
            }
        });

        return builder.create();
    }

    protected void loadReviews(){
        int contador = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-mm-yyyy");
        for(HashMap<String,String> hm:myActivity.getReviews()){
            if(contador<3){
                try{
                    Date activityDate = sdf.parse(hm.get("date"));
                    reviews.add(new Review(hm.get("review"),Double.parseDouble(hm.get("rating")),activityDate));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }else{
                break;
            }
            contador++;
        }
        reviewAdapter = new ReviewAdapter(getContext(),R.layout.review_layout,reviews);
        reviewsListView.setAdapter(reviewAdapter);
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        super.show(manager, tag);
    }
}

package com.iff.onepairv2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private CardView foodCard, entertainmentCard, retailCard, othersCard;
    private ViewFlipper vFlipper;
    private ArrayList<String> imgArray = new ArrayList<>(); //arrayList to be used for url

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("1Pair");

        //add click listener to cards

        foodCard = (CardView) findViewById(R.id.food);
        entertainmentCard = (CardView) findViewById(R.id.entertainment);
        retailCard = (CardView) findViewById(R.id.retail);
        othersCard = (CardView) findViewById(R.id.others);
        //Include view flipper here
        vFlipper = (ViewFlipper)findViewById(R.id.v_flipper);
        //obtain the url for foodDealsPage/ RetailDealsPage/OtherDealPage/Entertaininment Deal page


        //add click listener to cards
        foodCard.setOnClickListener(this);
        entertainmentCard.setOnClickListener(this);
        retailCard.setOnClickListener(this);
        othersCard.setOnClickListener(this);

        //call this anywhere to get token id of current device
        //id can be used to send notifications to the device
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("123", "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        saveToken(token);

                        // Log and toast
                        //String msg = getString(R.string.msg_token_fmt, token);
                        Log.d("token ID", token);
                        Toast.makeText(MainActivity.this, token, Toast.LENGTH_SHORT).show();
                    }
                });
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String retailCheck = prefs.getString("retailCheck","");
        String othersCheck = prefs.getString("othersCheck","");
        String entertainmentCheck = prefs.getString("entertainmentCheck","");
        String foodCheck = prefs.getString("foodCheck","");
        String foodImgURL, entertainmentImgURL,othersImgURL,retailImgURL;
        System.out.println("This is foodCheck"+ foodCheck);
        System.out.println(foodCheck.getClass().getSimpleName());
        String check = "1";

        if(foodCheck.equals(check)){
            foodImgURL = prefs.getString("foodImgURL","");
            System.out.println("This is food imgurl" + foodImgURL);
            imgArray.add(foodImgURL);
            System.out.println("This is foodImgURL"+ foodImgURL);
            System.out.println("foodImage: "+ imgArray.get(0));
        }
        if(entertainmentCheck.equals(check)){
            entertainmentImgURL = prefs.getString("entertainmentImgURL","");
            imgArray.add(entertainmentImgURL);
            System.out.println("entertainmentImage: "+ imgArray.get(1));
        }
        if(retailCheck.equals(check)){
            retailImgURL = prefs.getString("retailImgURL","");
            imgArray.add(retailImgURL);
        }
        if(othersCheck.equals(check)){
            othersImgURL = prefs.getString("othersImgURL","");
            imgArray.add(othersImgURL);
        }
       if(!imgArray.isEmpty()){
           for(String imageUrl : imgArray){
               flipperImages(imageUrl);
           }
       }
       else{
           System.out.println("note that array is currently empty");
       }

       System.out.println("Size of arraylist" + imgArray.size());

    }

    public void flipperImages(String imageURL){
        ImageView imageView = new ImageView(this);
        Picasso.get().load(imageURL).into(imageView);

        vFlipper.addView(imageView);

        vFlipper.setFlipInterval(4000);//4sec
        vFlipper.setAutoStart(true);

        //animation
        vFlipper.setInAnimation(this, android.R.anim.slide_in_left);
        vFlipper.setOutAnimation(this, android.R.anim.slide_out_right);

    }

    private void saveToken(String token) {
        String email;
        String uid;
        if(mAuth.getCurrentUser() != null){
            email = mAuth.getCurrentUser().getEmail();
            uid = mAuth.getCurrentUser().getUid();

            final UserToken userToken = new UserToken(email, token);

            // Update django db too
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://128.199.167.80:8080/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            BackEndController backEndController = retrofit.create(BackEndController.class);
            Call<Void> call = backEndController.updateToken(uid, token);
            //System.out.print(token);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if(!response.isSuccessful()){
                        System.out.println("Oops something went wrong!");
                        return;
                    }
                    DatabaseReference dbUsers = FirebaseDatabase.getInstance().getReference("UserToken");
                    dbUsers.child(mAuth.getCurrentUser().getUid()).setValue(userToken).addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(MainActivity.this, "Token saved", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    System.out.println("Oops something went wrong!");
                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null){

            sendToStart();

        }
    }

    private void sendToStart() {
        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(item.getItemId() == R.id.main_logout_btn){
            FirebaseAuth.getInstance().signOut();
            sendToStart();
        }
        else if(item.getItemId() == R.id.main_profile_btn){
            Intent startIntent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(startIntent);
        }
        else if(item.getItemId() == R.id.main_homepage){
            Toast.makeText(MainActivity.this, "You're already in the Homepage", Toast.LENGTH_SHORT).show();
        }
        else if(item.getItemId() == R.id.main_chat){
            Intent startIntent = new Intent(MainActivity.this, MatchedPersonsActivity.class);
            startActivity(startIntent);
        }
        else if(item.getItemId() == R.id.main_all_users){
            Intent startIntent = new Intent(MainActivity.this, AllUsers.class);
            startActivity(startIntent);
        }
        //return super.onOptionsItemSelected(item);
        return false;
    }

    public void onClick(View v) {
        Intent i;

        switch (v.getId()) {
            case R.id.food:
                i = new Intent(this, FoodDealsPage.class);
                startActivity(i);
                break;
            case R.id.entertainment:
                i = new Intent(this, EntertainmentDealsPage.class);
                startActivity(i);
                break;
            case R.id.retail:
                i = new Intent(this, RetailDealsPage.class);
                startActivity(i);
                break;
            case R.id.others:
                i = new Intent(this, OthersDealsPage.class);
                startActivity(i);
                break;
        }
    }

}
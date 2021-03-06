package com.iff.onepairv2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * An activity that displays a list of food deals
 */
public class FoodDealsPage extends AppCompatActivity {
    /**
     * Layout list view that shows the deals
     */
    private ListView listView;
    /**
     * To adapt to the list view
     */
    private DealsListViewAdapter adapter;
    /**
     * An array list of deal objects
     */
    private ArrayList<Deal> dealList = new ArrayList<Deal>();
    /**
     * Tool bar at the top of the activity
     */
    private Toolbar mToolbar;
    /**
     * Firebase user authentication
     */
    private FirebaseAuth mAuth;

    /**
     * Called when activity is first launched
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deals_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mToolbar = (Toolbar) findViewById(R.id.deal_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Food");

        listView = findViewById(R.id.listView);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BackEndController.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        BackEndController backEndController = retrofit.create(BackEndController.class);
        Call<ArrayList<Deal>> call = backEndController.getFoodDeals();
        call.enqueue(new Callback<ArrayList<Deal>>() {
            @Override
            public void onResponse(Call<ArrayList<Deal>> call, Response<ArrayList<Deal>> response) {
                if(!response.isSuccessful()){
                    System.out.println("Oops something went wrong!");
                    return;
                }
                ArrayList<Deal> deals = response.body();
                for(Deal deal: deals){
                    //deal.printDeal();
                    //Model model = new Model(deal.getName(), "", deal.getImage());
                    dealList.add(deal);
                }

                if(!dealList.isEmpty()){
                    String foodImgURL = dealList.get(0).getImage();
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(FoodDealsPage.this);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("foodImgURL", foodImgURL);
                    editor.putString("foodCheck", "1");
                    editor.commit();
                }
                else{
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(FoodDealsPage.this);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("foodCheck", "0");
                    editor.commit();
                }

                adapter = new DealsListViewAdapter(getApplicationContext(), dealList);
                listView.setAdapter(adapter);


                //if array list is empty
                if(dealList.size() == 0)
                {
                    Toast toast = Toast.makeText(FoodDealsPage.this, "Sorry there are no deals currently.", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Deal>> call, Throwable t) {
                System.out.println("Oops something went wrong!");
            }
        });
    }

    /**
     * Creates a drop down menu at the top right of the tool bar
     * @param menu
     * @return boolean type
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dealspagemenu, menu);
        MenuItem myActionMenuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView)myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(TextUtils.isEmpty(s)) {
                    adapter.filter("");
                    listView.clearTextFilter();
                }
                else {
                    adapter.filter(s);
                }
                return true;
            }
        });

        return true;
    }

    /**
     * For selection of items in the tool bar menu
     * @param item Each item on the menu
     * @return boolean type
     */
    //for side bar menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(item.getItemId() == R.id.main_logout_btn){
            FirebaseAuth.getInstance().signOut();
            sendToStart();
        }
        else if(item.getItemId() == R.id.main_profile_btn){
            Intent startIntent = new Intent(FoodDealsPage.this, ProfileActivity.class);
            startActivity(startIntent);
        }
        else if(item.getItemId() == R.id.main_homepage){
            Intent startIntent = new Intent(FoodDealsPage.this, MainActivity.class);
            startActivity(startIntent);
        }
        else if(item.getItemId() == R.id.main_chat){
            Intent startIntent = new Intent(FoodDealsPage.this, MatchedPersonsActivity.class);
            startActivity(startIntent);
        }/*
        else if(item.getItemId() == R.id.main_all_users){
            Intent startIntent = new Intent(FoodDealsPage.this, AllUsers.class);
            startActivity(startIntent);
        }*/
        //return super.onOptionsItemSelected(item);
        return false;
    }

    /**
     * Sends user to start activity if user is not logged in
     */
    private void sendToStart() {
        Intent startIntent = new Intent(FoodDealsPage.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    /**
     * Called when activity is subsequently launched
     */
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null){

            sendToStart();

        }
    }
}

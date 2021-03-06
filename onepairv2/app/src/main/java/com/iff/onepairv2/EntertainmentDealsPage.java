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
 * Activity used to display entertainment deals
 * @author ifandonlyif
 */
public class EntertainmentDealsPage extends AppCompatActivity {

    /** Used to display deals in a list format*/
    private ListView listView;
    /** Adapter used to display deals */
    private DealsListViewAdapter adapter;
    /** Array list of entertainment deals */
    private ArrayList<Deal> dealList = new ArrayList<Deal>();
    /** Toolbar at the top of the activity */
    private Toolbar mToolbar;
    /**Firebase Authentication object */
    private FirebaseAuth mAuth;

    /**
     * Called when the activity is first launched
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deals_main);

        listView = findViewById(R.id.listView);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mToolbar = (Toolbar) findViewById(R.id.deal_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Entertainment");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BackEndController.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        BackEndController backEndController = retrofit.create(BackEndController.class);
        Call<ArrayList<Deal>> call = backEndController.getEntertainmentDeals();
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
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(EntertainmentDealsPage.this);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("entertainmentImgURL", foodImgURL);
                    editor.putString("entertainmentCheck", "1");
                    editor.commit();
                }
                else{
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(EntertainmentDealsPage.this);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("entertainmentCheck", "0");
                    editor.commit();
                }
                adapter = new DealsListViewAdapter(getApplicationContext(), dealList);
                listView.setAdapter(adapter);

                //if array list is empty
                if(dealList.size() == 0)
                {
                    Toast toast = Toast.makeText(EntertainmentDealsPage.this, "Sorry there are no deals currently.", Toast.LENGTH_SHORT);
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
     * Creates a drop down menu at the right side of the toolbar
     * @param menu
     * @return
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
     * For selection of items in the toolbar menu
     * @param item
     * @return boolean
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(item.getItemId() == R.id.main_logout_btn){
            FirebaseAuth.getInstance().signOut();
            sendToStart();
        }
        else if(item.getItemId() == R.id.main_profile_btn){
            Intent startIntent = new Intent(EntertainmentDealsPage.this, ProfileActivity.class);
            startActivity(startIntent);
        }
        else if(item.getItemId() == R.id.main_homepage){
            Intent startIntent = new Intent(EntertainmentDealsPage.this, MainActivity.class);
            startActivity(startIntent);
        }

        else if(item.getItemId() == R.id.main_chat){
            Intent startIntent = new Intent(EntertainmentDealsPage.this, MatchedPersonsActivity.class);
            startActivity(startIntent);
        }/*
        else if(item.getItemId() == R.id.main_all_users){
            Intent startIntent = new Intent(EntertainmentDealsPage.this, AllUsers.class);
            startActivity(startIntent);
        }*/
        //return super.onOptionsItemSelected(item);
        return false;
    }

    /**
     * If not logged in, this method will send user to the start activity
     */
    private void sendToStart() {
        Intent startIntent = new Intent(EntertainmentDealsPage.this, StartActivity.class);
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

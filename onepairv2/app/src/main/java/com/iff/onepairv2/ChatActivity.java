package com.iff.onepairv2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.admin.SystemUpdatePolicy;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Activity for users to chat with each other
 * @author ifandonlyif
 */

public class ChatActivity extends AppCompatActivity {

    /** UID of the target user */
    private String mChatUserTargetUid;
    /** Name of the targer user */
    private String mChatUserTargetName;
    /** Image url of the target user */
    private String mChatUserTargetImage;
    /** UID of the current user */

    private String mChatUserOwnUid;
    /** Name of the current user */
    private String mChatUserOwnName;
    /** Image url of the own user */
    private String mChatUserOwnImage;
    /** Toolbar at the top of the activity */
    private Toolbar mToolbar;
    /** Firebase authentication */
    private FirebaseAuth mAuth;
    /** Database reference */
    private DatabaseReference mRootRef;

    //xml elements
    /** Image view used to display target image */
    private ImageView mChatTargetImage;
    /** Editable text box used to craft a new message */
    private EditText mMsgField;
    /** Button used to send a new message */
    private ImageButton mSendBtn;
    /** Used to display and format messages */
    private RecyclerView mMessagesList;

    /** Stores all the messages objects of the user */
    private final List<Messages> MESSAGES_LIST = new ArrayList<>();
    /** Used to set the activity to a linear layout */
    private LinearLayoutManager mLinearLayout;
    /** Used to load and display messages */
    private MessageAdapter mAdapter;

    /** Used for background push notifications */
    private RequestQueue mRequestQueue;
    /** Used for background push notifications */
    private String URL = "https://fcm.googleapis.com/fcm/send";
    /** Users suscribed to the topic can receive background push notifications */
    private String topic;
    /** Database reference */
    private DatabaseReference mUserDatabase;

    /**
     * Called when the activity is first launched
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mRequestQueue = Volley.newRequestQueue(this);


        //Chat Target Details
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mChatUserTargetUid = getIntent().getStringExtra("user_id");
        mChatUserTargetName = getIntent().getStringExtra("user_name");
        mChatUserTargetImage = getIntent().getStringExtra("user_image");


        //Set Picture in Toolbar
        mChatTargetImage = findViewById(R.id.chat_pic);
        Picasso.get().load(mChatUserTargetImage).into(mChatTargetImage);

        //Set Title in Toolbar
        mToolbar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("          " + mChatUserTargetName);

        //Current User Details
        mAuth = FirebaseAuth.getInstance();
        mChatUserOwnUid = mAuth.getCurrentUser().getUid();
        mChatUserOwnName = mAuth.getCurrentUser().getDisplayName();
        System.out.println("DISPLAY NAME IS " + mChatUserOwnName);
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mChatUserOwnUid);
        mUserDatabase.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mChatUserOwnName = dataSnapshot.child("name").getValue().toString();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //xml elements
        mMsgField = (EditText) findViewById(R.id.msg_field);
        mSendBtn = (ImageButton) findViewById(R.id.chat_send_btn);
        mMessagesList = (RecyclerView) findViewById(R.id.messages_list);
        mLinearLayout = new LinearLayoutManager(this);

        mAdapter = new MessageAdapter(MESSAGES_LIST);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);

        mMessagesList.setAdapter(mAdapter);
        loadMessages();


        //Create Chat in Database
        mRootRef.child("Chat").child(mChatUserOwnUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(mChatUserTargetUid)) {

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("chat", true);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mChatUserOwnUid + "/" + mChatUserTargetUid, chatAddMap);
                    chatUserMap.put("Chat/" + mChatUserTargetUid + "/" + mChatUserOwnUid, chatAddMap);

                    mRootRef.updateChildren(chatUserMap);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Create Conversation
        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(mChatUserOwnName);
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
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.chat_menu, menu);
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

        if (item.getItemId() == R.id.unmatch) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BackEndController.URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            BackEndController backEndController = retrofit.create(BackEndController.class);
            Call<Void> call = backEndController.addBlacklist(mChatUserOwnUid, mChatUserTargetUid);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, retrofit2.Response<Void> response) {
                    if(!response.isSuccessful()){
                        System.out.println("Oops something went wrong!");
                        return;
                    }
                    mRootRef.child("Chat").child(mChatUserOwnUid).child(mChatUserTargetUid).child("chat").setValue(false);
                    mRootRef.child("Chat").child(mChatUserTargetUid).child(mChatUserOwnUid).child("chat").setValue(false);
                    Intent startIntent = new Intent(ChatActivity.this, MatchedPersonsActivity.class);
                    startIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(startIntent);
                    finish();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    System.out.println("Oops something went wrong!");
                }
            });

        } else if (item.getItemId() == R.id.close_chat) {
            mRootRef.child("Chat").child(mChatUserOwnUid).child(mChatUserTargetUid).child("chat").setValue(false);
            Intent startIntent = new Intent(ChatActivity.this, MatchedPersonsActivity.class);
            startIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(startIntent);
            finish();
        }
        //return super.onOptionsItemSelected(item);
        return false;
    }

    /**
     * Loads all the messages of the current user and the target user
     */
    private void loadMessages() {
        mRootRef.child("Messages").child(mChatUserOwnUid).child(mChatUserTargetUid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages message = dataSnapshot.getValue(Messages.class);
                MESSAGES_LIST.add(message);
                mAdapter.notifyDataSetChanged();
                mMessagesList.scrollToPosition(MESSAGES_LIST.size() - 1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * Sends messages and updates firebase database
     * @param sender
     */
    private void sendMessage(String sender) {

        String message = mMsgField.getText().toString();

        if (!TextUtils.isEmpty(message)) {

            String current_user_ref = "Messages/" + mChatUserOwnUid + "/" + mChatUserTargetUid;
            String target_user_ref = "Messages/" + mChatUserTargetUid + "/" + mChatUserOwnUid;

            DatabaseReference user_message_push = mRootRef.child("Messages").child(mChatUserOwnUid).child(mChatUserTargetUid).push();
            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mChatUserOwnUid);

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id + "/", messageMap);
            messageUserMap.put(target_user_ref + "/" + push_id + "/", messageMap);

            mMsgField.setText("");

            mRootRef.updateChildren(messageUserMap);

            sendNotification(message, sender);

        }
    }

    /**
     * Sends background notifications to target user once the message is sent to the target user
     * @param message
     * @param sender
     */
    private void sendNotification(final String message, final String sender) {
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mChatUserOwnUid);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //for sending of background notifications
                //our json object will look like this
                JSONObject mainObj = new JSONObject();
                topic = mChatUserOwnUid + mChatUserTargetUid;
                //System.out.println("NAME NAME" + mChatUserOwnName);

                //get data from database
                mChatUserOwnImage = dataSnapshot.child("image").getValue().toString();
               // System.out.println("IMAGE MY OWN " + mChatUserOwnImage);
                mChatUserOwnName = dataSnapshot.child("name").getValue().toString();

                try {
                    mainObj.put("to", "/topics/" + topic);
                    JSONObject notificationObj = new JSONObject();
                    notificationObj.put("title", "New Message from " + sender);
                    notificationObj.put("body", message);

                    JSONObject extraData = new JSONObject();

                    extraData.put("messageFrom", mChatUserOwnUid);
                    extraData.put("image", mChatUserOwnImage);
                    extraData.put("name", mChatUserOwnName);

                    mainObj.put("notification", notificationObj);
                    mainObj.put("data", extraData);

                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, mainObj, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            //codes here will run when notification is sent
                            System.out.println("MESSAGE SENT");
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //codes here will run on error
                            System.out.println("MESSAGE FAILED");
                        }
                    }
                    ) {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> header = new HashMap<>();
                            header.put("content-type", "application/json");
                            header.put("authorization", "key=AIzaSyAOPEEMta24s-K-XyunD5xpBGsNQ6FGcwc");
                            return header;
                        }
                    };
                    mRequestQueue.add(request);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}

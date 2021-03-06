package com.iff.onepairv2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


/**
 * Activity class for users to change their name
 * @author ifandonlyif
 */

public class ChangeNameActivity extends AppCompatActivity {

    /**
     * Toolbar at the top of the app
     */
    private Toolbar mToolbar;
    /**
     * New name input by the user
     */
    private TextInputLayout mNewName;
    /**
     * Button used for saving the user's new name
     */
    private Button mSavebtn;
    /**
     * Firebase database
     */
    private FirebaseAuth mAuth;
    /**
     * Database reference
     */
    private DatabaseReference mDisplayNameDatabase;
    /**
     * Firebase user object of the current user
     */
    private FirebaseUser mCurrentUser;
    /**
     * Progress dialog to show updating of name is in progress
     */
    private ProgressDialog mUpdateNameProgress;

    /**
     * Called when the activity is first launched
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_name);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mDisplayNameDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        mToolbar = (Toolbar) findViewById(R.id.change_name_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Change Display Name");

        mNewName = (TextInputLayout) findViewById(R.id.change_name_input);
        mSavebtn = (Button) findViewById(R.id.change_name_save_btn);

        //get old name
        final String oldName = getIntent().getStringExtra("OldName");
        //set field to old name
        mNewName.getEditText().setText(oldName);

        mSavebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Progress Dialog
                mUpdateNameProgress = new ProgressDialog(ChangeNameActivity.this);
                mUpdateNameProgress.setTitle("Updating Display Name");
                mUpdateNameProgress.setMessage("This may take a while");
                mUpdateNameProgress.setCanceledOnTouchOutside(false);
                mUpdateNameProgress.show();

                //update name
                String newName = mNewName.getEditText().getText().toString();
                mDisplayNameDatabase.child("name").setValue(newName).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mUpdateNameProgress.dismiss();
                            Intent done_change_name = new Intent(ChangeNameActivity.this, ProfileActivity.class);
                            startActivity(done_change_name);
                            finish();
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Error.", Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        });


    }
}

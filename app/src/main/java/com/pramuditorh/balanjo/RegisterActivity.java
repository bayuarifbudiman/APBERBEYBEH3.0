package com.pramuditorh.balanjo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private Button createButton;
    private EditText usernameInput, emailInput, passwordInput;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        createButton = (Button) findViewById(R.id.register_btn);
        usernameInput = (EditText) findViewById(R.id.register_username_editText);
        emailInput = (EditText) findViewById(R.id.register_email_editText);
        passwordInput = (EditText) findViewById(R.id.register_password_editText);
        loadingBar = new ProgressDialog(this);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateAccount();
            }
        });
    }

    private void CreateAccount(){
        String username = usernameInput.getText().toString();
        String encodedEmail = EncodeString(emailInput.getText().toString());
        String password = passwordInput.getText().toString();

        if(TextUtils.isEmpty(username)){
            Toast.makeText(this, "Please write your name...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(encodedEmail)){
            Toast.makeText(this, "Please write your email...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please write your password...", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Create Account");
            loadingBar.setMessage("Please wait, while we are checking the credentials");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            ValidateEmail(username, encodedEmail, password);
        }
    }

    private void ValidateEmail(final String username, final String encodedEmail, final String password) {
        final String decodedEmail = DecodeString(encodedEmail);
        //Database reference
        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();

        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!(dataSnapshot.child("Users").child(encodedEmail).exists())){
                    HashMap<String, Object> userDataMap = new HashMap<>();
                    userDataMap.put("username", username);
                    userDataMap.put("email", encodedEmail);
                    userDataMap.put("password", password);

                    RootRef.child("Users").child(encodedEmail).updateChildren(userDataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(RegisterActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                startActivity(intent);
                            }
                            else{
                                loadingBar.dismiss();
                                Toast.makeText(RegisterActivity.this, "Failed to create account", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else{
                    Toast.makeText(RegisterActivity.this, "Email address: " + decodedEmail + " already exist.", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                    Toast.makeText(RegisterActivity.this, "Please use another email!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //Firebase doesnt allow '.', '#', '$', '[', or ']'
    public static String EncodeString(String string) {
        return string.replace(".", ",");
    }
    public static String DecodeString(String string) {
        return string.replace(",", ".");
    }
}

package com.pramuditorh.balanjo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pramuditorh.balanjo.Model.Users;
import com.pramuditorh.balanjo.Prevalent.Prevalent;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {

    private Button registerButton, loginButton;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerButton = (Button) findViewById(R.id.register_btn);
        loginButton = (Button) findViewById(R.id.login_btn);
        loadingBar = new ProgressDialog(this);

        Paper.init(this);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        String UserEmailKey = EncodeString(Prevalent.UserEmailKey);
        String EncodedUserEmailKey = Paper.book().read(UserEmailKey);
        String UserPasswordKey = Paper.book().read(Prevalent.UserPasswordKey);

        if (EncodedUserEmailKey != "" && UserPasswordKey != ""){
            if(!TextUtils.isEmpty(UserEmailKey) && !TextUtils.isEmpty(UserPasswordKey)){
                AllowAccess(EncodedUserEmailKey, UserPasswordKey);

                loadingBar.setTitle("Already logged in");
                loadingBar.setMessage("Please wait...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();
            }
        }
    }

    private void AllowAccess(final String encodedEmail, final String password) {
        final String decodedEmail = DecodeString(encodedEmail);
        //Database reference
        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();

        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("Users").child(encodedEmail).exists()){
                    Users userData = dataSnapshot.child("Users").child(encodedEmail).getValue(Users.class);

                    if(userData.getEmail().equals(encodedEmail)){
                        if(userData.getPassword().equals(password)){
                            Toast.makeText(MainActivity.this, "Logged in success", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                            startActivity(intent);
                        }
                        else{
                            loadingBar.dismiss();
                            Toast.makeText(MainActivity.this, "Password is incorrect", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                else{
                    Toast.makeText(MainActivity.this, "Account with email: " + decodedEmail + "does not exist.", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                    //Toast.makeText(LoginActivity.this, "Please input correct email!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //Firebase doesnt allow '.', '#', '$', '[', or ']'
    private String EncodeString(String string){
        return string.replace(".", ",");
    }

    private String DecodeString(String string){
        return string.replace(",", ".");
    }
}

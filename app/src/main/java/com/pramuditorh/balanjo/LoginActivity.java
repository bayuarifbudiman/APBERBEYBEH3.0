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
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pramuditorh.balanjo.Model.Users;
import com.pramuditorh.balanjo.Prevalent.Prevalent;
import com.rey.material.app.ThemeManager;
import com.rey.material.widget.CheckBox;

import io.paperdb.Paper;

public class LoginActivity extends AppCompatActivity {

    private Button loginButton;
    private EditText emailInput, passwordInput;
    private ProgressDialog loadingBar;
    private TextView adminLink, notAdminLink;

    private String parentDbName = "Users";
    private CheckBox chkRememberMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ThemeManager.init(this, 1, 0, null);

        loginButton = (Button) findViewById(R.id.login_btn);
        emailInput = (EditText) findViewById(R.id.login_email_editText);
        passwordInput = (EditText) findViewById(R.id.login_password_editText);
        adminLink = (TextView) findViewById(R.id.admin_panel_link);
        notAdminLink = (TextView) findViewById(R.id.not_admin_panel_link);
        loadingBar = new ProgressDialog(this);

        chkRememberMe = (CheckBox) findViewById(R.id.remember_me_chkb);
        Paper.init(this);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginUser();
            }
        });

        adminLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginButton.setText("Login Admin");
                adminLink.setVisibility(View.INVISIBLE);
                notAdminLink.setVisibility(View.VISIBLE);
                parentDbName = "Admins";
            }
        });

        notAdminLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginButton.setText("Login");
                adminLink.setVisibility(View.VISIBLE);
                notAdminLink.setVisibility(View.INVISIBLE);
                parentDbName = "Users";
            }
        });
    }

    private void LoginUser() {
        String encodedEmail = EncodeString(emailInput.getText().toString());
        String password = passwordInput.getText().toString();

        if(TextUtils.isEmpty(encodedEmail)){
            Toast.makeText(this, "Please write your email...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please write your password...", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Login Account");
            loadingBar.setMessage("Please wait, while we are checking the credentials");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();
            
            AllowAccessToAccount(encodedEmail, password);
        }
    }

    private void AllowAccessToAccount(final String encodedEmail, final String password) {

        if(chkRememberMe.isChecked()){
            Paper.book().write(Prevalent.UserEmailKey, encodedEmail);
            Paper.book().write(Prevalent.UserPasswordKey, password);
        }

        final String decodedEmail = DecodeString(encodedEmail);
        //Database reference
        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();

        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(parentDbName).child(encodedEmail).exists()){
                    Users userData = dataSnapshot.child(parentDbName).child(encodedEmail).getValue(Users.class);

                    if(userData.getEmail().equals(encodedEmail)){
                        if(userData.getPassword().equals(password)){
                            if(parentDbName.equals("Admins")){
                                Toast.makeText(LoginActivity.this, "Admin - Logged in success", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                                Intent intent = new Intent(LoginActivity.this, AdminCategoryActivity.class);
                                startActivity(intent);
                            }
                            else if(parentDbName.equals("Users")){
                                Toast.makeText(LoginActivity.this, "Logged in success", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                Prevalent.currentOnlineUser = userData;
                                startActivity(intent);
                            }
                        }
                        else{
                            loadingBar.dismiss();
                            Toast.makeText(LoginActivity.this, "Password is incorrect", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                else{
                    Toast.makeText(LoginActivity.this, "Account with email: " + decodedEmail + " does not exist.", Toast.LENGTH_SHORT).show();
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

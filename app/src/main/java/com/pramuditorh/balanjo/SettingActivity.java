package com.pramuditorh.balanjo;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {

    private CircleImageView profileImageView;
    private EditText fullNameEditText, useEmailEditText, addressEditText;
    private TextView profileChangeTextBtn, closeTextBtn, saveTextButton;

    private Uri imageUri;
    private String myUrl = "";
    private StorageTask uploadTask;
    private StorageReference storageProfilePictureRef;
    private String checker = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        storageProfilePictureRef = FirebaseStorage.getInstance().getReference().child("Profile Pictures");
        profileImageView = (CircleImageView)findViewById(R.id.settings_profile_image);
        useEmailEditText = (EditText)findViewById(R.id.settings_email);
        addressEditText = (EditText)findViewById(R.id.setting_address);
        profileChangeTextBtn = (TextView)findViewById(R.id.profile_image_change_btn);
        closeTextBtn = (TextView)findViewById(R.id.close_settings_btn);
        saveTextButton = (TextView)findViewById(R.id.update_account_settings_btn);

    }
}

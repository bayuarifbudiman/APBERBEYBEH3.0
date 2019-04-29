package com.pramuditorh.balanjo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.SimpleTimeZone;

public class AdminAddNewProductActivity extends AppCompatActivity {

    private String categoryName, description, price, productName, saveCurrentDate, saveCurrentTime, productRandomKey, downloadImgUrl;
    private Button addProduct;
    private EditText inputProductName, inputProductDesc, inputProductPrice;
    private ImageView inputProductImage;
    private static final int GalleryPick = 1;
    private Uri imageUri;
    private StorageReference productImagesRef;
    private DatabaseReference ProductRef;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_new_product);

        categoryName = getIntent().getExtras().get("category").toString();
        productImagesRef = FirebaseStorage.getInstance().getReference().child("Products_Images");
        ProductRef = FirebaseDatabase.getInstance().getReference().child("Products");
        loadingBar = new ProgressDialog(this);

        addProduct = (Button) findViewById(R.id.add_new_product_button);
        inputProductName = (EditText) findViewById(R.id.product_name_editText);
        inputProductDesc = (EditText) findViewById(R.id.product_description_editText);
        inputProductPrice = (EditText) findViewById(R.id.product_price_editText);
        inputProductImage = (ImageView) findViewById(R.id.select_camera_imageView);

        inputProductImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });

        addProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateProductData();
            }
        });
    }

    private void ValidateProductData() {
        description = inputProductDesc.getText().toString();
        price = inputProductPrice.getText().toString();
        productName = inputProductName.getText().toString();

        if (imageUri == null) {
            Toast.makeText(this, "Please input the product image.", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Please input product description.", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(price)) {
            Toast.makeText(this, "Product price cannot be empty.", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(productName)) {
            Toast.makeText(this, "Product name is mandatory", Toast.LENGTH_SHORT).show();
        }
        else {
            storedProductInfo();
        }
    }

    private void storedProductInfo() {
        loadingBar.setTitle("Adding new product.");
        loadingBar.setMessage("Please wait, while we are adding the new product.");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        saveCurrentTime = currentTime.format(calendar.getTime());

        productRandomKey = saveCurrentDate + saveCurrentTime;

        final StorageReference filePath = productImagesRef.child(imageUri.getLastPathSegment() + productRandomKey + ".jpg");

        final UploadTask uploadTask = filePath.putFile(imageUri);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String message = e.toString();
                Toast.makeText(AdminAddNewProductActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(AdminAddNewProductActivity.this, "Image upload successfully.", Toast.LENGTH_SHORT).show();
                Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        downloadImgUrl = filePath.getDownloadUrl().toString();
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            downloadImgUrl = task.getResult().toString();

                            Toast.makeText(AdminAddNewProductActivity.this, "Product image saved to database.", Toast.LENGTH_SHORT).show();
                            SaveProductInfo();
                        }
                    }
                });
            }
        });

    }

    private void SaveProductInfo() {
        HashMap<String, Object> productMap = new HashMap<>();
        productMap.put("pid", productRandomKey);
        productMap.put("date", saveCurrentDate);
        productMap.put("time", saveCurrentTime);
        productMap.put("description", description);
        productMap.put("image", downloadImgUrl);
        productMap.put("category", categoryName);
        productMap.put("price", price);
        productMap.put("product_name", productName);

        ProductRef.child(productRandomKey).updateChildren(productMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(AdminAddNewProductActivity.this, AdminCategoryActivity.class);
                    startActivity(intent);

                    loadingBar.dismiss();
                    Toast.makeText(AdminAddNewProductActivity.this, "Product is added successfully.", Toast.LENGTH_SHORT).show();
                }
                else {
                    loadingBar.dismiss();
                    String message = task.getException().toString();
                    Toast.makeText(AdminAddNewProductActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GalleryPick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            inputProductImage.setImageURI(imageUri);
        }
    }
}
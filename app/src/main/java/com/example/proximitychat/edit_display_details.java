package com.example.proximitychat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mvc.imagepicker.ImagePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class edit_display_details extends AppCompatActivity {

    EditText edit_display_name;
    CircleImageView edit_display_photo;
    EditText edit_bio;
    Button saveChanges;
    Bitmap uploadPhoto;
    String uploadPhotoPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_display_details);

        edit_display_name = findViewById(R.id.edit_display_name);
        edit_display_photo = findViewById(R.id.profile_image);
        edit_bio = findViewById(R.id.edit_bio);
        saveChanges = findViewById(R.id.saveChanges);


        edit_display_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.pickImage(edit_display_details.this, "Select an image:");
            }
        });

        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    // Gets the email from the user and replaces the full stops for commas
                    // This is to circumvent firebase's naming restrictions
                    String userEmail = user.getEmail();
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference();
                    myRef.child("users").child(cleanEmail(userEmail)).child("edit_display_name").setValue(edit_display_name.getText().toString());

                    // Creates an instance of FirebaseStorage so that we can store user images
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference storageRef = storage.getReference();


                    // Creates a URI from the image's path
                    Uri file = Uri.fromFile(new File(uploadPhotoPath));
                    String filename = file.getLastPathSegment();
                    StorageReference picRef = storageRef.child(user.getUid());
                    // Uploads the file
                    UploadTask uploadTask = picRef.putFile(file);

                    // Closes the activity if the upload is successful
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            finish();
                        }
                    });

                    // Notifies the user if the upload fails
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(edit_display_details.this, "Image upload failed.", Toast.LENGTH_SHORT).show();
                        }
                    });


                } else {
                    Toast.makeText(edit_display_details.this, "Failed to retrieve user profile. Please try again later.", Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

    private Uri getImageUri(Context inContext, Bitmap myImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), myImage, "Title", null);
        return Uri.parse(path);
    }


    @Override
    // When the image selector returns an image, it is stored to a bitmap variable
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Assigns data to the uploadPhoto variable
        uploadPhoto = ImagePicker.getImageFromResult(this, requestCode, resultCode, data);
        uploadPhotoPath = ImagePicker.getImagePathFromResult(this, requestCode, resultCode, data);
        edit_display_photo.setImageBitmap(uploadPhoto);

    }


    /**
     * Returns an email address with its full stops (.) exchanged for commas (,)
     *
     * @param email the email address.
     * @return the email address with commas
     */
    private String cleanEmail(String email) {
        return email.replace(".", ",");
    }
}

package com.example.pawfect;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pawfect.model.Owner;
import com.example.pawfect.model.Pet;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.example.pawfect.util.ImageKitHelper;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddPetActivity extends AppCompatActivity {

    private TextInputEditText ownerNameEdit, ownerEmailEdit, ownerPhoneEdit;
    private TextInputEditText petNameEdit, petTypeEdit, petAgeEdit, petRaceEdit, petDescriptionEdit;
    private TextInputLayout ownerNameLayout, ownerEmailLayout, ownerPhoneLayout;
    private TextInputLayout petNameLayout, petTypeLayout, petAgeLayout, petRaceLayout;
    private MaterialButton addPictureButton, submitButton;
    private ImageView petImageView;
    private Uri selectedImageUri;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;
    private Pet existingPet;
    private String petId;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pet);

        // Check if we're in edit mode
        petId = getIntent().getStringExtra("petId");
        existingPet = (Pet) getIntent().getSerializableExtra("pet");
        
        if (petId != null && existingPet != null) {
            isEditMode = true;
            setTitle(R.string.edit_pet);
        } else {
            setTitle(R.string.add_pet);
        }

        initializeViews();
        setupClickListeners();
        
        if (isEditMode) {
            populateFields();
        }
    }

    private void initializeViews() {
        // Owner fields
        ownerNameEdit = findViewById(R.id.ownerNameEdit);
        ownerEmailEdit = findViewById(R.id.ownerEmailEdit);
        ownerPhoneEdit = findViewById(R.id.ownerPhoneEdit);
        ownerNameLayout = findViewById(R.id.ownerNameLayout);
        ownerEmailLayout = findViewById(R.id.ownerEmailLayout);
        ownerPhoneLayout = findViewById(R.id.ownerPhoneLayout);

        // Pet fields
        petNameEdit = findViewById(R.id.petNameEdit);
        petTypeEdit = findViewById(R.id.petTypeEdit);
        petAgeEdit = findViewById(R.id.petAgeEdit);
        petRaceEdit = findViewById(R.id.petRaceEdit);
        petDescriptionEdit = findViewById(R.id.petDescriptionEdit);
        petNameLayout = findViewById(R.id.petNameLayout);
        petTypeLayout = findViewById(R.id.petTypeLayout);
        petAgeLayout = findViewById(R.id.petAgeLayout);
        petRaceLayout = findViewById(R.id.petRaceLayout);

        addPictureButton = findViewById(R.id.addPictureButton);
        submitButton = findViewById(R.id.submitButton);
        petImageView = findViewById(R.id.petImageView);

        db = FirebaseFirestore.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.saving));
        progressDialog.setCancelable(false);
    }

    private void setupClickListeners() {
        addPictureButton.setOnClickListener(v -> openImagePicker());

        submitButton.setOnClickListener(v -> {
            if (validateForm()) {
                if (selectedImageUri != null) {
                    // New image selected, upload it
                    uploadImageAndSavePet();
                } else {
                    // No new image, save with existing or empty photo URLs
                    if (isEditMode) {
                        progressDialog.setMessage(getString(R.string.updating));
                    } else {
                        progressDialog.setMessage(getString(R.string.saving));
                    }
                    progressDialog.show();
                    savePet(null);
                }
            }
        });
    }

    private void openImagePicker() {
        // Use ACTION_OPEN_DOCUMENT for better permission handling on modern Android
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                // Try to grant persistent read permission (optional - will work without it too)
                try {
                    getContentResolver().takePersistableUriPermission(selectedImageUri, 
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (SecurityException e) {
                    // Permission not persistable, but that's okay - we'll use it immediately
                    Log.d("AddPetActivity", "Persistable permission not available, will use URI immediately");
                }
                petImageView.setImageURI(selectedImageUri);
                petImageView.setVisibility(View.VISIBLE);
                addPictureButton.setText(getString(R.string.change_picture));
            }
        }
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validate owner fields
        if (TextUtils.isEmpty(ownerNameEdit.getText())) {
            ownerNameLayout.setError(getString(R.string.required_field));
            isValid = false;
        } else {
            ownerNameLayout.setError(null);
        }

        String email = ownerEmailEdit.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            ownerEmailLayout.setError(getString(R.string.required_field));
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            ownerEmailLayout.setError(getString(R.string.invalid_email));
            isValid = false;
        } else {
            ownerEmailLayout.setError(null);
        }

        if (TextUtils.isEmpty(ownerPhoneEdit.getText())) {
            ownerPhoneLayout.setError(getString(R.string.required_field));
            isValid = false;
        } else {
            ownerPhoneLayout.setError(null);
        }

        // Validate pet fields
        if (TextUtils.isEmpty(petNameEdit.getText())) {
            petNameLayout.setError(getString(R.string.required_field));
            isValid = false;
        } else {
            petNameLayout.setError(null);
        }

        if (TextUtils.isEmpty(petTypeEdit.getText())) {
            petTypeLayout.setError(getString(R.string.required_field));
            isValid = false;
        } else {
            petTypeLayout.setError(null);
        }

        if (TextUtils.isEmpty(petAgeEdit.getText())) {
            petAgeLayout.setError(getString(R.string.required_field));
            isValid = false;
        } else {
            petAgeLayout.setError(null);
        }

        if (TextUtils.isEmpty(petRaceEdit.getText())) {
            petRaceLayout.setError(getString(R.string.required_field));
            isValid = false;
        } else {
            petRaceLayout.setError(null);
        }

        return isValid;
    }

    private void uploadImageAndSavePet() {
        if (selectedImageUri == null) {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();
        progressDialog.setMessage(getString(R.string.uploading));

        // Verify we can read the URI
        try {
            getContentResolver().openInputStream(selectedImageUri).close();
        } catch (Exception e) {
            progressDialog.dismiss();
            Log.e("AddPetActivity", "Cannot read image URI", e);
            Toast.makeText(this, "Cannot access image. Please try selecting again.", Toast.LENGTH_LONG).show();
            return;
        }

        // Upload to ImageKit
        ImageKitHelper.uploadImage(this, selectedImageUri, new ImageKitHelper.UploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.setMessage(getString(R.string.saving));
                        List<String> photoUrls = new ArrayList<>();
                        photoUrls.add(imageUrl);
                        savePet(photoUrls);
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        Log.e("AddPetActivity", "ImageKit upload failed: " + error);
                        Toast.makeText(AddPetActivity.this, "Upload failed: " + error, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void populateFields() {
        if (existingPet == null) return;

        // Populate owner fields
        if (existingPet.getOwner() != null) {
            ownerNameEdit.setText(existingPet.getOwner().getName());
            ownerEmailEdit.setText(existingPet.getOwner().getEmail());
            ownerPhoneEdit.setText(existingPet.getOwner().getPhone());
        }

        // Populate pet fields
        petNameEdit.setText(existingPet.getName());
        petTypeEdit.setText(existingPet.getType());
        petAgeEdit.setText(existingPet.getAge());
        petRaceEdit.setText(existingPet.getRace());
        petDescriptionEdit.setText(existingPet.getDescription());

        // Load existing image if available
        if (existingPet.getPhotoUrls() != null && !existingPet.getPhotoUrls().isEmpty()) {
            String imageUrl = existingPet.getPhotoUrls().get(0);
            if (imageUrl != null && !imageUrl.isEmpty()) {
                // Use Glide to load the image
                com.bumptech.glide.Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.circular_image)
                        .error(R.drawable.circular_image)
                        .circleCrop()
                        .into(petImageView);
                petImageView.setVisibility(View.VISIBLE);
                addPictureButton.setText(getString(R.string.change_picture));
            }
        }
    }

    private void savePet(List<String> photoUrls) {
        if (photoUrls == null) {
            // If no new image uploaded, keep existing photo URLs
            if (isEditMode && existingPet != null && existingPet.getPhotoUrls() != null) {
                photoUrls = existingPet.getPhotoUrls();
            } else {
                photoUrls = new ArrayList<>();
            }
        }

        Owner owner = new Owner(
                ownerNameEdit.getText().toString().trim(),
                ownerEmailEdit.getText().toString().trim(),
                ownerPhoneEdit.getText().toString().trim()
        );

        Pet pet = new Pet(
                petId, // Use existing ID if editing, null if creating new
                petNameEdit.getText().toString().trim(),
                petDescriptionEdit.getText().toString().trim(),
                petTypeEdit.getText().toString().trim(),
                petAgeEdit.getText().toString().trim(),
                petRaceEdit.getText().toString().trim(),
                owner,
                photoUrls
        );

        if (isEditMode && petId != null) {
            // Update existing pet
            db.collection("pets")
                    .document(petId)
                    .set(pet)
                    .addOnSuccessListener(aVoid -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, R.string.pet_updated, Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Create new pet
            db.collection("pets")
                    .add(pet)
                    .addOnSuccessListener(documentReference -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, R.string.success, Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
                    });
        }
    }
}


package com.example.pawfect;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.pawfect.model.Owner;
import com.example.pawfect.model.Pet;

public class PetDetailActivity extends AppCompatActivity {

    private ImageView petImage;
    private TextView petName;
    private TextView petDescription;
    private TextView petType;
    private TextView petAge;
    private TextView petRace;
    private TextView ownerName;
    private TextView ownerEmail;
    private TextView ownerPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_detail);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Pet Details");
        }

        initializeViews();
        loadPetData();
    }

    private void initializeViews() {
        petImage = findViewById(R.id.petImage);
        petName = findViewById(R.id.petName);
        petDescription = findViewById(R.id.petDescription);
        petType = findViewById(R.id.petType);
        petAge = findViewById(R.id.petAge);
        petRace = findViewById(R.id.petRace);
        ownerName = findViewById(R.id.ownerName);
        ownerEmail = findViewById(R.id.ownerEmail);
        ownerPhone = findViewById(R.id.ownerPhone);
    }

    private void loadPetData() {
        Pet pet = (Pet) getIntent().getSerializableExtra("pet");
        
        if (pet == null) {
            finish();
            return;
        }

        // Set pet information
        petName.setText(pet.getName() != null ? pet.getName() : "Unknown");
        petDescription.setText(pet.getDescription() != null ? pet.getDescription() : "No description available");
        petType.setText(pet.getType() != null ? pet.getType() : "Unknown");
        petAge.setText(pet.getAge() != null ? pet.getAge() : "Unknown");
        petRace.setText(pet.getRace() != null ? pet.getRace() : "Unknown");

        // Set owner information
        Owner owner = pet.getOwner();
        if (owner != null) {
            ownerName.setText(owner.getName() != null ? owner.getName() : "Unknown");
            ownerEmail.setText(owner.getEmail() != null ? owner.getEmail() : "N/A");
            ownerPhone.setText(owner.getPhone() != null ? owner.getPhone() : "N/A");

            // Make email clickable
            if (owner.getEmail() != null && !owner.getEmail().isEmpty() && !owner.getEmail().equals("N/A")) {
                ownerEmail.setOnClickListener(v -> {
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                    emailIntent.setData(Uri.parse("mailto:" + owner.getEmail()));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Inquiry about " + pet.getName());
                    if (emailIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(emailIntent);
                    }
                });
            }

            // Make phone clickable
            if (owner.getPhone() != null && !owner.getPhone().isEmpty() && !owner.getPhone().equals("N/A")) {
                ownerPhone.setOnClickListener(v -> {
                    Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
                    phoneIntent.setData(Uri.parse("tel:" + owner.getPhone()));
                    if (phoneIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(phoneIntent);
                    }
                });
            }
        } else {
            ownerName.setText("Unknown");
            ownerEmail.setText("N/A");
            ownerPhone.setText("N/A");
        }

        // Load pet image using Glide
        if (pet.getPhotoUrls() != null && !pet.getPhotoUrls().isEmpty() && !pet.getPhotoUrls().get(0).isEmpty()) {
            Glide.with(this)
                    .load(pet.getPhotoUrls().get(0))
                    .placeholder(R.drawable.circular_image)
                    .error(R.drawable.circular_image)
                    .centerCrop()
                    .into(petImage);
        } else {
            petImage.setImageResource(R.drawable.circular_image);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}



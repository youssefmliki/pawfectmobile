package com.example.pawfect;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pawfect.adapter.PetAdapter;
import com.example.pawfect.model.Pet;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class PetListActivity extends AppCompatActivity implements PetAdapter.OnPetClickListener {

    private RecyclerView recyclerView;
    private PetAdapter adapter;
    private List<Pet> allPets;
    private List<Pet> filteredPets;
    private ProgressBar progressBar;
    private TextView noPetsText;
    private ChipGroup filterChipGroup;
    private Chip allChip, dogChip, catChip;
    private FirebaseFirestore db;
    private String searchQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_list);

        searchQuery = getIntent().getStringExtra("searchQuery");

        initializeViews();
        setupRecyclerView();
        setupFilters();
        loadPets();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPets();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        noPetsText = findViewById(R.id.noPetsText);
        filterChipGroup = findViewById(R.id.filterChipGroup);
        allChip = findViewById(R.id.allChip);
        dogChip = findViewById(R.id.dogChip);
        catChip = findViewById(R.id.catChip);
        db = FirebaseFirestore.getInstance();
        allPets = new ArrayList<>();
        filteredPets = new ArrayList<>();
    }

    private void setupRecyclerView() {
        adapter = new PetAdapter(filteredPets, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupFilters() {
        allChip.setOnClickListener(v -> filterPets("all"));
        dogChip.setOnClickListener(v -> filterPets("dog"));
        catChip.setOnClickListener(v -> filterPets("cat"));

        allChip.setChecked(true);
    }

    private void loadPets() {
        progressBar.setVisibility(View.VISIBLE);
        noPetsText.setVisibility(View.GONE);

        db.collection("pets")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        allPets.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Pet pet = document.toObject(Pet.class);
                            pet.setId(document.getId());
                            allPets.add(pet);
                        }
                        filterPets("all");
                    } else {
                        Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
                        noPetsText.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void filterPets(String filter) {
        filteredPets.clear();

        for (Pet pet : allPets) {
            boolean matchesFilter = filter.equals("all") ||
                    pet.getType() != null && pet.getType().toLowerCase().equals(filter.toLowerCase());

            boolean matchesSearch = searchQuery == null || searchQuery.isEmpty() ||
                    (pet.getName() != null && pet.getName().toLowerCase().contains(searchQuery.toLowerCase())) ||
                    (pet.getDescription() != null && pet.getDescription().toLowerCase().contains(searchQuery.toLowerCase())) ||
                    (pet.getRace() != null && pet.getRace().toLowerCase().contains(searchQuery.toLowerCase()));

            if (matchesFilter && matchesSearch) {
                filteredPets.add(pet);
            }
        }

        adapter.notifyDataSetChanged();

        if (filteredPets.isEmpty()) {
            noPetsText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            noPetsText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPetClick(Pet pet) {
        // Navigate to PetDetailActivity
        Intent intent = new Intent(this, PetDetailActivity.class);
        intent.putExtra("pet", pet);
        startActivity(intent);
    }

    @Override
    public void onEditClick(Pet pet) {
        // Navigate to AddPetActivity in edit mode
        Intent intent = new Intent(this, AddPetActivity.class);
        intent.putExtra("petId", pet.getId());
        intent.putExtra("pet", pet);
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Pet pet) {
        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_pet)
                .setMessage(R.string.delete_confirmation)
                .setPositiveButton(R.string.yes, (dialog, which) -> deletePet(pet))
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void deletePet(Pet pet) {
        if (pet.getId() == null || pet.getId().isEmpty()) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        db.collection("pets")
                .document(pet.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, R.string.pet_deleted, Toast.LENGTH_SHORT).show();
                    // Reload pets to refresh the list
                    loadPets();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
                });
    }
}


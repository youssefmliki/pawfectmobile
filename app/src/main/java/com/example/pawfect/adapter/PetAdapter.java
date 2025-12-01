package com.example.pawfect.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pawfect.R;
import com.example.pawfect.model.Pet;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class PetAdapter extends RecyclerView.Adapter<PetAdapter.PetViewHolder> {

    private List<Pet> pets;
    private OnPetClickListener listener;

    public interface OnPetClickListener {
        void onPetClick(Pet pet);
        void onEditClick(Pet pet);
        void onDeleteClick(Pet pet);
    }

    public PetAdapter(List<Pet> pets, OnPetClickListener listener) {
        this.pets = pets;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pet_card, parent, false);
        return new PetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PetViewHolder holder, int position) {
        Pet pet = pets.get(position);
        holder.bind(pet);
    }

    @Override
    public int getItemCount() {
        return pets.size();
    }

    class PetViewHolder extends RecyclerView.ViewHolder {
        private ImageView petImage;
        private TextView petName;
        private TextView petDescription;
        private TextView petType;
        private TextView petAge;
        private TextView petRace;
        private TextView ownerName;
        private TextView ownerEmail;
        private TextView ownerPhone;
        private MaterialButton editButton;
        private MaterialButton deleteButton;

        public PetViewHolder(@NonNull View itemView) {
            super(itemView);
            petImage = itemView.findViewById(R.id.petImage);
            petName = itemView.findViewById(R.id.petName);
            petDescription = itemView.findViewById(R.id.petDescription);
            petType = itemView.findViewById(R.id.petType);
            petAge = itemView.findViewById(R.id.petAge);
            petRace = itemView.findViewById(R.id.petRace);
            ownerName = itemView.findViewById(R.id.ownerName);
            ownerEmail = itemView.findViewById(R.id.ownerEmail);
            ownerPhone = itemView.findViewById(R.id.ownerPhone);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPetClick(pets.get(getAdapterPosition()));
                }
            });

            editButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(pets.get(getAdapterPosition()));
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(pets.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Pet pet) {
            petName.setText(pet.getName() != null ? pet.getName() : "Unknown");
            petDescription.setText(pet.getDescription() != null ? pet.getDescription() : "No description");
            petType.setText(pet.getType() != null ? pet.getType() : "Unknown");
            petAge.setText(pet.getAge() != null ? pet.getAge() : "Unknown");
            petRace.setText(pet.getRace() != null ? pet.getRace() : "Unknown");

            if (pet.getOwner() != null) {
                ownerName.setText(pet.getOwner().getName() != null ? pet.getOwner().getName() : "Unknown");
                ownerEmail.setText(pet.getOwner().getEmail() != null ? pet.getOwner().getEmail() : "N/A");
                ownerPhone.setText(pet.getOwner().getPhone() != null ? pet.getOwner().getPhone() : "N/A");
            } else {
                ownerName.setText("Unknown");
                ownerEmail.setText("N/A");
                ownerPhone.setText("N/A");
            }

            // Load image using Glide
            if (pet.getPhotoUrls() != null && !pet.getPhotoUrls().isEmpty() && !pet.getPhotoUrls().get(0).isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(pet.getPhotoUrls().get(0))
                        .placeholder(R.drawable.circular_image)
                        .error(R.drawable.circular_image)
                        .circleCrop()
                        .into(petImage);
            } else {
                // Set default background color based on type
                petImage.setImageDrawable(null);
                if (pet.getType() != null) {
                    if (pet.getType().toLowerCase().equals("dog")) {
                        petImage.setBackgroundResource(R.drawable.circular_image);
                    } else if (pet.getType().toLowerCase().equals("cat")) {
                        petImage.setBackgroundResource(R.drawable.circular_image);
                    } else {
                        petImage.setBackgroundResource(R.drawable.circular_image);
                    }
                } else {
                    petImage.setBackgroundResource(R.drawable.circular_image);
                }
            }
        }
    }
}


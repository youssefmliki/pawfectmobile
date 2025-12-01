package com.example.pawfect.model;

import java.io.Serializable;
import java.util.List;

public class Pet implements Serializable {
    private String id;
    private String name;
    private String description;
    private String type; // dog, cat, etc.
    private String age;
    private String race; // breed
    private Owner owner;
    private List<String> photoUrls;

    public Pet() {
        // Default constructor required for Firestore
    }

    public Pet(String id, String name, String description, String type, String age, String race, Owner owner, List<String> photoUrls) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.age = age;
        this.race = race;
        this.owner = owner;
        this.photoUrls = photoUrls;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getRace() {
        return race;
    }

    public void setRace(String race) {
        this.race = race;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public List<String> getPhotoUrls() {
        return photoUrls;
    }

    public void setPhotoUrls(List<String> photoUrls) {
        this.photoUrls = photoUrls;
    }
}


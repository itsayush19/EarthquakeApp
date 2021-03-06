package com.example.earthquakelocateapp;

import com.google.gson.JsonObject;

import java.util.ArrayList;

public class Detail {
    private String type;
    private ArrayList<JsonObject> features;
    public String getType() {
        return type;
    }
    public ArrayList<JsonObject> getFeatures() {
        return features;
    }
}

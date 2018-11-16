package com.cs417.chadha.desai.patiala.FoodieWebService.POJO;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class Locality {

    private List<Restaurant> restaurants = new ArrayList<>();


    public List<Restaurant> getRestaurants() {
        return restaurants;
    }

    public void setRestaurants(List<Restaurant> restaurants) {
        this.restaurants = restaurants;
    }



}

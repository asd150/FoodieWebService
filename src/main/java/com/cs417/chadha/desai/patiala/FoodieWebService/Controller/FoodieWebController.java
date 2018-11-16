package com.cs417.chadha.desai.patiala.FoodieWebService.Controller;


import com.cs417.chadha.desai.patiala.FoodieWebService.POJO.Locality;
import com.cs417.chadha.desai.patiala.FoodieWebService.POJO.Restaurant;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestController
public class FoodieWebController {

    @RequestMapping(value = "/restaurants", method = RequestMethod.GET)
    public ResponseEntity<String> retrieveResturaunts(
            @RequestParam(value = "houseNumber", required = true) int houseNumber,
            @RequestParam(value = "street", required = true) String street,
            @RequestParam(value = "city", required = true) String city,
            @RequestParam(value = "state", required = true) String state) throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        String apiKey = "dc3be1ee5ee5eeb381c1fbb5ddefe56bfc331be";
        String fullAddress = houseNumber + "+" + street + "%2c+" + city + "+" + state;

        fullAddress = fullAddress.replaceAll("\\s+", "+");
        URI uri = URI.create("https://api.geocod.io/v1.3/geocode?q="+fullAddress+"&api_key="+apiKey);

        RestTemplate restTemplate = new RestTemplate();


        //FIRST API CALL GEOCODE
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(uri, String.class);

        if(responseEntity.getStatusCode().value() != 200){
            return new ResponseEntity<>("Error processing GeoCode API", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        double latitude = -1;
        double longitude = -1;


        try {
            JsonNode node = mapper.readTree(responseEntity.getBody());
            JsonNode results = node.get("results");
            JsonNode location = results.get(0).get("location");
            latitude = location.get("lat").asDouble();
            longitude = location.get("lng").asDouble();
        }catch (Exception ex){
            return new ResponseEntity<>(ex.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }


        URI zomtoURI = URI.create("https://developers.zomato.com/api/v2.1/geocode?lat="+latitude+"&lon="+longitude);
        HttpHeaders headers = new HttpHeaders();
        headers.add("user-key", "0da7e28bbd7acd04937b44badcc2c0a2");

        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

        ResponseEntity<String> zomatoResponse = restTemplate.exchange(zomtoURI, HttpMethod.POST, entity, String.class);

        if(zomatoResponse.getStatusCode().value() != 200){
            return new ResponseEntity<>("Error processing Zomato API", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        JsonNode nearbyRestaurants = null;

        try {
             nearbyRestaurants = mapper.readTree(zomatoResponse.getBody()).get("nearby_restaurants");
        }catch (Exception ex){
            return new ResponseEntity<>(ex.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        int i = 0;

        List<Restaurant> restaurants = new ArrayList<>();

        while(nearbyRestaurants.get(i) != null){
            String restaurantName = nearbyRestaurants.get(i).get("restaurant").get("name").asText();
            String address = nearbyRestaurants.get(i).get("restaurant").get("location").get("address").asText();
            String cuisines = nearbyRestaurants.get(i).get("restaurant").get("cuisines").asText();
            String rating = nearbyRestaurants.get(i).get("restaurant").get("user_rating").get("aggregate_rating").asText();

            Restaurant restaurant = new Restaurant();
            restaurant.setName(restaurantName);
            restaurant.setAddress(address);
            restaurant.setCuisines(cuisines);
            restaurant.setRating(rating);

            restaurants.add(restaurant);

            i++;
        }

        Locality response = new Locality();
        response.setRestaurants(restaurants);

        String responseJson =  null;

        try{
            responseJson = mapper.writeValueAsString(response);
        }catch (Exception ex){
            return new ResponseEntity<>(ex.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(responseJson, HttpStatus.OK);
    }


    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<String> handleMissingParams(MissingServletRequestParameterException ex) {
        String name = ex.getParameterName();
        return new ResponseEntity<>(ex.getMessage() + ". Please note other parameters may be missing as well.", HttpStatus.valueOf(400));
    }


}

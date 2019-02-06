package com.example.demo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.example.demo.LocationData;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@RestController
public class webhook {

	@PostMapping("/getLocationForBot")
	public String botStuff(@RequestBody String payload) {

		final double MILES = 10.00;
		int flag = 0;
		String error = "";
		String results = "";
		// Get the fulfillment request JSON from Dialogflow.
		JsonParser parser = new JsonParser();
		JsonObject rootObj = parser.parse(payload).getAsJsonObject();
		JsonObject locObj = rootObj.getAsJsonObject("queryResult");
		JsonObject params = locObj.getAsJsonObject("parameters");
		String location = params.get("any").getAsString();
		String storeLocation = params.get("store").getAsString();
		System.out.println("here" + storeLocation);
		// System.out.println(locObj);
		// System.out.println(location);
		System.out.println(locObj);
		System.out.println(location);
		String action = locObj.get("action").getAsString();

		if (action.equals("input.storeFinder")) {
			try {
				JsonObject myLocation = getUser();
				double myLat = myLocation.get("lat").getAsDouble();
				double myLng = myLocation.get("lng").getAsDouble();

				// System.out.println(myLat + myLng);

				if (!location.equals("")) {
					// put cords here CAN ONLY SEARCH UP TO 50,000 OR 31.0 MILES
					System.out.println(location);
					String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + location
							+ "&key=AIzaSyCrAI0t16uFey968ug2LKydc7NBqGOIkIQ";
					final String uri = url;
					HttpHeaders headers = new HttpHeaders();
					headers.add("User-Agent", uri);
					RestTemplate restTemplate = new RestTemplate();
					HttpEntity<String> request = new HttpEntity<String>(headers);
					ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);

					String res = response.getBody();
					System.out.println(res);

					JsonObject googleObj = parser.parse(res).getAsJsonObject();
					JsonArray resultObj = googleObj.get("results").getAsJsonArray();

					for (JsonElement x : resultObj) {

						JsonObject locationObj = x.getAsJsonObject();
						JsonObject geo = locationObj.getAsJsonObject("geometry");
						JsonObject loc = geo.getAsJsonObject("location");

						myLat = loc.get("lat").getAsDouble();
						myLng = loc.get("lng").getAsDouble();

					}

				}

				// put cords here CAN ONLY SEARCH UP TO 50,000 OR 31.0 MILES
				String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + myLat + ","
						+ myLng
						+ "&rankby=distance&type=store&keyword=" + storeLocation + "&fields=vicinity&key=AIzaSyCrAI0t16uFey968ug2LKydc7NBqGOIkIQ";

				final String uri = url;
				HttpHeaders headers = new HttpHeaders();
				headers.add("User-Agent", uri);
				RestTemplate restTemplate = new RestTemplate();
				HttpEntity<String> request = new HttpEntity<String>(headers);
				ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);

				String res = response.getBody();
				System.out.println(res);

				JsonObject googleObj = parser.parse(res).getAsJsonObject();
				JsonArray resultObj = googleObj.get("results").getAsJsonArray();

				if (resultObj.size() == 0) {
					JsonObject chatConvert = new JsonObject();
					chatConvert.addProperty("fulfillmentText", "No stores nearby");

					Gson gson = new Gson();
					String responsePayload = gson.toJson(chatConvert);

					return responsePayload;

				}

				for (JsonElement x : resultObj) {

					JsonObject locationObj = x.getAsJsonObject();
					JsonObject geo = locationObj.getAsJsonObject("geometry");
					JsonObject loc = geo.getAsJsonObject("location");
					JsonArray photosObj = locationObj.getAsJsonArray("photos");
					JsonObject opening_hours = locationObj.getAsJsonObject("opening_hours");
					Boolean open_now = opening_hours.get("open_now").getAsBoolean();
					// System.out.println(open_now);
					String photo_reference = "";
					for (JsonElement y : photosObj) {
						JsonObject photos = y.getAsJsonObject();
						photo_reference = photos.get("photo_reference").getAsString();
					}
					String address = locationObj.get("vicinity").getAsString();
					double latitude = loc.get("lat").getAsDouble();
					double longitude = loc.get("lng").getAsDouble();

					// calculate distance then get locations that are within 10 miles.
					LocationData currentPos = new LocationData("Current Position", myLat, myLng);
					LocationData toPos = new LocationData(address, latitude, longitude);

					double result = currentPos.distanceTo(toPos);

					result = round(result, 2);

					if (result <= MILES && flag <= 2) {
						// System.out.println(address + " is " + result + " miles.");
						String place = "{\"address\":" + "\"" + address + "\"" + "," + "\"distance\":" + "\"" + result
								+ "\"" + "," + "\"photo_reference\":" + "\"" + photo_reference + "\"" + ","
								+ "\"open_now\":" + "\"" + open_now + "\"" + "}\n";

						results += (place);
						// System.out.println(results);
						flag += 1;

					} else if (result > MILES && flag <= 2) {
						String place = "{\"address\":" + "\"" + address + "\"" + "," + "\"distance\":" + "\"" + result
								+ "\"" + "," + "\"photo_reference\":" + "\"" + photo_reference + "\"" + ","
								+ "\"open_now\":" + "\"" + open_now + "\"" + "}\n";
						results += (place);
						// System.out.println(results);
						flag += 1;
					}

					if (flag > 2) {
						break;
					}

				}

//			System.out.println(results.size());

				String text = results.toString().replace("[", "").replace("]", "");
				JsonObject chatConvert = new JsonObject();
				chatConvert.addProperty("fulfillmentText", text);

				Gson gson = new Gson();
				String responsePayload = gson.toJson(chatConvert);

				return responsePayload;

			} catch (Exception e) {
				error = "{\"error\":" + "\"" + e + "}\n";
			}
		}
		return error;
	}

	public static double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	// THANKS ERICK!!!!!!!!
	private JsonObject getUser() {
		final String url = "https://www.googleapis.com/geolocation/v1/geolocate?key=AIzaSyANdovJScv42vcUDrkbe51-Ka0sKiWvI2M";
		HttpHeaders headers = new HttpHeaders();
		headers.add("user-agent", url);
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<String> request = new HttpEntity<String>(headers);
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
		String api_response = response.getBody();
		JsonParser parser = new JsonParser();
		JsonObject rootObj = parser.parse(api_response).getAsJsonObject();
		JsonObject locObj = rootObj.getAsJsonObject("location");
		System.out.println(api_response);

		return locObj;

	}

}

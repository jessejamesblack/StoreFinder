package com.example.demo;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@RestController
public class webhook {
	
	// Test Get method
	@RequestMapping(method = RequestMethod.GET)
	public String index() {
		return "Hello";
	}

	// Make post request to Dialogflow
	@RequestMapping(method = RequestMethod.POST)
	public String getPwnedStatus(@RequestBody String payload) {
		// Get the fulfillment request JSON from Dialogflow
		Gson gson = new Gson();
		JsonParse jp = gson.fromJson(payload, JsonParse.class);
		
		
		// Get the response from pwned Dialogflow intent
		if(jp.queryResult.action.equals("input.pwned")) {
			// Set up the url
			String email = jp.queryResult.parameters.email;
			String url = "https://haveibeenpwned.com/api/v2/breachedaccount/".concat(email);
			
			// Setup the API call to haveibeenpwned 
			final String uri = url;
			HttpHeaders headers = new HttpHeaders();
			headers.add("User-Agent", "HaveIBeenPwndJava-v2");
			RestTemplate restTemplate = new RestTemplate();
			HttpEntity<String> request = new HttpEntity<String>(headers);
			ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);
			
			String res = response.getBody();
			
			// Return to Dialogdflow
			JsonObject chatConvert = new JsonObject();
			chatConvert.addProperty("fulfillmentText", res);
			
			String responsePayload = gson.toJson(chatConvert);
			return responsePayload;
		}
		else {
			return "Error has occured";
		}
		
		
	}
}

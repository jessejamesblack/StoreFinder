package com.example.demo;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.text.MessageFormat;
import java.util.Base64;


@RestController
public class dfWebhook {
	
	//Test Get method
	@GetMapping("/test")
	   public String index() {
		return "Hello World";
	}
	
	
	
	//Make POST request to the webhook.
	@PostMapping("/getStocksForBot")
	   public String botStuff(@RequestBody String payload) {
		

	    
		//Get the fulfillment request JSON from Dialogflow.
		Gson gson = new Gson();
		JsonParse jp = gson.fromJson(payload, JsonParse.class);
		
		
		//get the response from pizza intent
		if(jp.queryResult.action.equals("input.breach") )
		{
		    //setup the chatbot's response back to the user
		    String chat = "You don't get pizza";
		
		    //setup url
		    String string = "";
		    String email = jp.queryResult.parameters.email;
		   // String encodedString = Base64.getEncoder().encodeToString(email.getBytes());
		    String url = "https://haveibeenpwned.com/api/v2/breachedaccount/".concat(email);
		    String msg = MessageFormat.format(url, string);
		    
		    System.out.println(msg);
		    
		    //setup API call to haveibeenpwned
		    final String uri = msg;
		    HttpHeaders headers = new HttpHeaders();
		    headers.add("User-Agent", "HaveIBeenPwndJava-v2");
		    RestTemplate restTemplate = new RestTemplate();
		    HttpEntity<String> request = new HttpEntity<String>(headers);
		    ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);
		    
		    String res = response.getBody();
		    
		    //System.out.println(res);
		    
		    // Dialogflow V2 requires the fulfillment response come back as fulfillmentText.
		    JsonObject chatConvert = new JsonObject();
		    chatConvert.addProperty("fulfillmentText", res);
		    
		    String responsePayload = gson.toJson(chatConvert);
		    
		    //Return to DialogFlow
		    return responsePayload;
		    		      
		}
		else
		{
			System.out.println("Oof");
			return "oof";
		}
		

	}

}

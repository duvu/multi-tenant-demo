package com.example.multitenant1.controller;

import com.example.multitenant1.service.ExternalApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;

@RestController
@RequestMapping("/external")
public class ExternalApiController {
    private static final Logger logger = LoggerFactory.getLogger(ExternalApiController.class);
    private static final String DEFAULT_API_URL = "https://jsonplaceholder.typicode.com/posts";
    
    private final ExternalApiService externalApiService;
    
    @Autowired
    public ExternalApiController(ExternalApiService externalApiService) {
        this.externalApiService = externalApiService;
    }
    
    /**
     * Fetches data from an external API with retry logic
     * 
     * @param url Optional URL parameter to specify a different API endpoint
     * @return ResponseEntity containing the API response
     */
    @GetMapping("/data")
    public ResponseEntity<?> getExternalData(@RequestParam(required = false) String url) {
        // Use the default URL if none is provided
        String apiUrl = (url != null && !url.isEmpty()) ? url : DEFAULT_API_URL;
        
        try {
            logger.info("Received request to fetch external data from: {}", apiUrl);
            ResponseEntity<String> externalResponse = externalApiService.fetchExternalData(apiUrl);
            
            return ResponseEntity
                .status(externalResponse.getStatusCode())
                .headers(externalResponse.getHeaders())
                .body(externalResponse.getBody());
                
        } catch (RestClientException e) {
            logger.error("Failed to fetch external data: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Failed to retrieve data from external API: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error while fetching external data: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Unexpected error: " + e.getMessage());
        }
    }
}
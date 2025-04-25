package com.example.multitenant1.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Service
public class ExternalApiService {
    private static final Logger logger = LoggerFactory.getLogger(ExternalApiService.class);
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000; // 1 second delay between retries
    
    private final RestTemplate restTemplate;
    
    public ExternalApiService() {
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Fetches data from an external API with retry logic
     * 
     * @param url The URL of the external API
     * @return ResponseEntity containing the API response
     * @throws RestClientException if all retry attempts fail
     */
    public ResponseEntity<String> fetchExternalData(String url) {
        int retryCount = 0;
        RestClientException lastException = null;
        
        while (retryCount < MAX_RETRIES) {
            try {
                logger.info("Attempting to fetch data from external API: {}, attempt: {}", url, retryCount + 1);
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                logger.info("Successfully retrieved data from external API");
                return response;
            } catch (ResourceAccessException e) {
                // Handle timeout or connection errors
                lastException = e;
                logger.warn("Connection error when accessing external API: {}, attempt: {}, error: {}", 
                    url, retryCount + 1, e.getMessage());
            } catch (RestClientException e) {
                // Handle other REST client exceptions
                lastException = e;
                logger.warn("Error when accessing external API: {}, attempt: {}, error: {}", 
                    url, retryCount + 1, e.getMessage());
            }
            
            retryCount++;
            
            if (retryCount < MAX_RETRIES) {
                try {
                    logger.info("Waiting {} ms before retry attempt {}", RETRY_DELAY_MS, retryCount + 1);
                    TimeUnit.MILLISECONDS.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread interrupted during retry delay", ie);
                }
            }
        }
        
        // If we've exhausted all retries, throw the last exception
        logger.error("Failed to fetch data from external API after {} attempts", MAX_RETRIES);
        throw lastException != null ? lastException : 
            new RestClientException("Failed to fetch data from external API after " + MAX_RETRIES + " attempts");
    }
}
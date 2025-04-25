package com.example.multitenant1.controller;

import com.example.multitenant1.config.TenantContext;
import com.example.multitenant1.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUserProfile() {
        try {
            // Get the current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
            }

            User currentUser = (User) authentication.getPrincipal();
            
            // Verify that user belongs to the correct tenant
            String currentTenant = TenantContext.getCurrentTenant();
            if (currentTenant != null && !currentTenant.equals(currentUser.getTenantId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("User does not belong to the current tenant");
            }

            // Return user profile information
            Map<String, Object> userProfile = new HashMap<>();
            userProfile.put("user_id", currentUser.getId());
            userProfile.put("tenant_id", currentUser.getTenantId());
            userProfile.put("username", currentUser.getUsername());
            userProfile.put("email", currentUser.getEmail());
            userProfile.put("role", currentUser.getRole());
            
            return ResponseEntity.ok(userProfile);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving user profile: " + e.getMessage());
        }
    }
}
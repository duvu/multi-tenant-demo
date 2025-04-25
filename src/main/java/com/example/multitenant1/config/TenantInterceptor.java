package com.example.multitenant1.config;

import com.example.multitenant1.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.util.StringUtils;

@Component
public class TenantInterceptor implements HandlerInterceptor {
    
    @Value("${multitenancy.tenant-identifier-mode:header}")
    private String tenantIdentifierMode;
    
    @Value("${multitenancy.tenant-header:X-Tenant-ID}")
    private String tenantHeader;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String tenantId = null;
        
        // Extract tenant ID based on configured mode
        switch (tenantIdentifierMode.toLowerCase()) {
            case "header":
                tenantId = request.getHeader(tenantHeader);
                break;
                
            case "subdomain":
                String host = request.getServerName();
                if (host.contains(".")) {
                    tenantId = host.split("\\.")[0];
                }
                break;
                
            case "token":
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    try {
                        String jwt = authHeader.substring(7);
                        tenantId = jwtTokenProvider.extractTenantId(jwt);
                    } catch (Exception e) {
                        // If token parsing fails, log the error but continue
                        // We don't want to block requests just because token extraction failed
                        // Authentication filter will handle invalid tokens
                        System.err.println("Failed to extract tenant from token: " + e.getMessage());
                    }
                }
                break;
        }
        
        if (StringUtils.hasText(tenantId)) {
            TenantContext.setCurrentTenant(tenantId);
        }
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContext.clear();
    }
}

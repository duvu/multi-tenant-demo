package com.example.multitenant1.config;


/* TenantContext is a utility class that holds the current tenant identifier in a ThreadLocal variable.
 * This allows us to set and get the current tenant for the duration of a request.
 * It also stores the current user associated with the tenant.
 */

public class TenantContext {
    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();
    private static final ThreadLocal<Object> CURRENT_USER = new ThreadLocal<>();

    public static void setCurrentTenant(String tenant) {
        CURRENT_TENANT.set(tenant);
    }

    public static String getCurrentTenant() {
        return CURRENT_TENANT.get();
    }
    
    public static void setCurrentUser(Object user) {
        CURRENT_USER.set(user);
    }
    
    public static Object getCurrentUser() {
        return CURRENT_USER.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
        CURRENT_USER.remove();
    }
}

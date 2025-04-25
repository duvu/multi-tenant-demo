# Multi-Tenant Architecture Documentation

This document describes the multi-tenant architecture implemented in this application.

## Overview

Our application uses a **schema-per-tenant** approach for data isolation, where each tenant's data is stored in a separate schema within the same database. The application identifies tenants through multiple possible methods (configurable) and maintains tenant context throughout request processing.

## Data Isolation Strategy

### Schema-per-tenant

- Each tenant has its own database schema
- All tenants share the same database instance
- Hibernate automatically switches to the appropriate schema based on the tenant identifier
- Liquibase manages schema creation and migration for each tenant

**Advantages:**
- Good balance between isolation and resource sharing
- Lower operational costs compared to database-per-tenant
- Simpler database administration

**Implementation:**
- `SchemaMultiTenantConnectionProvider`: Provides database connections for tenant schemas
- `MultiTenantConfig`: Configures Hibernate for schema-based multi-tenancy
- Liquibase changesets: Separate changelog files for each tenant (`tenant1/tenant-changelog.xml`, `tenant2/tenant-changelog.xml`)

## Tenant Identification

The application supports three methods of tenant identification:

### 1. HTTP Header (default)
- Tenant ID is extracted from the `X-Tenant-ID` HTTP header
- Simple and flexible
- Works well with API gateways and proxies

### 2. Subdomain
- Tenant ID is extracted from the subdomain (e.g., `tenant1.example.com`)
- User-friendly and intuitive
- Suitable for SaaS applications with web interfaces

### 3. JWT Token Claims
- Tenant ID is extracted from a claim in the JWT authentication token
- Securely binds tenant information with authentication
- Good for applications with existing JWT authentication

Configuration in `application.properties`:
```properties
# Options: header, subdomain, token
multitenancy.tenant-identifier-mode=header
multitenancy.tenant-header=X-Tenant-ID
```

## Tenant Context Management

- `TenantContext`: ThreadLocal-based utility to store and retrieve tenant information
- `TenantInterceptor`: Extracts tenant ID from requests and sets it in TenantContext
- Tenant context is automatically cleared after request completion

## User-Tenant Association

- Each user belongs to exactly one tenant (as required)
- The `User` entity includes a `tenantId` field to track this association
- This association is used for business logic and authorization
- Data isolation is handled at the schema level, independent of this association

## Example Flow

1. User makes a request with tenant information (header, subdomain, or token)
2. `TenantInterceptor` extracts tenant ID and sets it in `TenantContext`
3. When a repository method is called, Hibernate:
   - Gets the current tenant ID from `CurrentTenantIdentifierResolver`
   - Uses `SchemaMultiTenantConnectionProvider` to get a connection with the correct schema
   - Executes the query in the tenant's schema
4. After request completion, tenant context is cleared

## Configuration

The multi-tenant behavior can be configured through `application.properties`:

```properties
# Multi-tenancy type
spring.jpa.properties.hibernate.multiTenancy=SCHEMA

# Tenant identification method (header, subdomain, token)
multitenancy.tenant-identifier-mode=header
multitenancy.tenant-header=X-Tenant-ID
```
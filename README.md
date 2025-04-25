# Multi-Tenant Spring Boot Application

## Setup Instructions
1. Ensure Java 17 and PostgreSQL are installed
2. Create database: `createdb demo1`
3. Create user: `createuser -P admin` (password: 1234566)
4. Grant privileges: `psql -d demo1 -c "GRANT ALL PRIVILEGES ON DATABASE demo1 TO admin;"`
5. Run application: `./mvnw spring-boot:run`

## Summary

### Completed
- Schema-per-tenant multi-tenancy architecture implementation
- Tenant identification via HTTP headers
- JWT-based authentication with tenant context
- Security implementation with role-based access
- Database schema management via Liquibase
- External API integration with retry mechanism

### Remaining
- Tests

### Design Decisions & Prioritization
- Chose schema-per-tenant approach to balance data isolation with time limitation
- Selected JWT for authentication to maintain stateless design with tenant context
- Implemented Liquibase for reliable, repeatable schema management
- Built retry logic into external API calls for improved eliability
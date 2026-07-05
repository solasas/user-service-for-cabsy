# Cabsy User Service

Handles rider/driver registration, login, and JWT-based authentication for the
Cabsy ride-booking platform. This is a standalone Spring Boot service — it has
no runtime dependency on any other Cabsy service.

## Tech stack

- Java 21, Spring Boot 4.1.0, Maven
- Spring Web, Spring Data JPA, Spring Security, Spring Validation
- PostgreSQL
- JWT via `io.jsonwebtoken` (jjwt 0.12.5)
- Lombok

## Prerequisites

- Java 21
- PostgreSQL reachable at the URL in `application.properties` (defaults to
  `localhost:5433`, db `cabsy_user`)
- A value for the `CABSY_JWT_SECRET` environment variable — the app will not
  start without it (there is no built-in default)

Generate a secret and create the database:

```bash
openssl rand -base64 32
```

```sql
CREATE DATABASE cabsy_user;
```

## Running locally

```bash
export CABSY_JWT_SECRET=<your-generated-secret>
./mvnw spring-boot:run
```

The service starts on **port 8081**.

Database connection details and the JWT expiry (`cabsy.jwt.expiration-ms`,
default 24h) are configured in `src/main/resources/application.properties`.

## API

| Method | Path                    | Auth required | Description                        |
|--------|-------------------------|----------------|------------------------------------|
| POST   | `/api/v1/auth/register` | No             | Register a rider or driver         |
| POST   | `/api/v1/auth/login`    | No             | Log in, receive a JWT              |
| GET    | `/api/v1/users/me`      | Yes (Bearer)   | Get the authenticated user profile |

All other routes require a valid `Authorization: Bearer <token>` header.
Requests without one get a `401`.

### Register a rider

```bash
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "rider1@example.com",
    "password": "password123",
    "fullName": "Rita Rider",
    "phoneNumber": "+15551234567",
    "role": "RIDER"
  }'
```

### Register a driver

`vehicleModel` and `vehiclePlateNumber` are required when `role` is `DRIVER`.

```bash
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "driver1@example.com",
    "password": "password123",
    "fullName": "Dave Driver",
    "phoneNumber": "+15557654321",
    "role": "DRIVER",
    "vehicleModel": "Toyota Camry",
    "vehiclePlateNumber": "ABC-1234"
  }'
```

### Login

```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "rider1@example.com",
    "password": "password123"
  }'
```

### Get current user profile

```bash
TOKEN="<token from register or login response>"

curl http://localhost:8081/api/v1/users/me \
  -H "Authorization: Bearer $TOKEN"
```

## Error responses

Errors return a consistent JSON body:

```json
{
  "timestamp": "2026-07-05T08:45:21.172107Z",
  "status": 400,
  "error": "Bad Request",
  "message": "vehicleModel and vehiclePlateNumber are required for drivers"
}
```

| Status | Cause                                              |
|--------|-----------------------------------------------------|
| 400    | Validation failure or invalid request (e.g. missing driver vehicle info) |
| 401    | Invalid credentials, missing/invalid/expired token |
| 409    | Email already registered                           |

## Security notes

- Passwords are hashed with BCrypt before storage; the hash is never returned
  in any API response.
- `CABSY_JWT_SECRET` must be set via environment variable — do not hardcode
  it in `application.properties` or commit it. A `.env` file is supported
  for local use and is gitignored.
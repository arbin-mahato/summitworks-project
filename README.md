# Hotel Booking System Backend

Spring Boot 3 backend for a JWT-protected hotel booking flow:

`Signup/Login -> Browse Hotels -> Inspect 7-day availability -> View room availability -> Create booking -> View user/admin booking history`

The frontend is not included. This README is the backend contract for local setup, seeded data, and the live API surface implemented in code.

## Stack

- Java 17
- Spring Boot 3.3.5
- Spring Security with JWT
- Spring Data JPA
- PostgreSQL
- Flyway
- Maven

## Modules

```text
src/main/java/com/example/Summit_Project/
  auth/
    config/
    controller/
    dto/
    entity/
    exception/
    repository/
    security/
    service/
  booking/
    config/
    controller/
    dto/
    entity/
    exception/
    repository/
    security/
    service/
  config/
src/main/resources/
  application.yml
  db/migration/
```

## Core Behavior

- Users can sign up and log in to receive a JWT.
- Hotels can be listed with a 7-day availability calendar.
- A hotel-specific calendar is available for one selected hotel.
- Room availability is computed from confirmed bookings and requested dates.
- Bookings are created only for a valid hotel-room pair.
- User booking history and admin booking reporting are available.
- Admin users can create hotels, create rooms, and delete rooms with booking-based safeguards.

## Availability Rules

Availability logic uses only the `bookings` table and confirmed bookings.

Overlap rule:

```text
existing.checkInDate < requested.checkOutDate
AND existing.checkOutDate > requested.checkInDate
```

Important:

- `room.isBooked` and `room.bookedDate` are legacy fields.
- They are still stored for compatibility, but they are not used for room or calendar availability decisions.

Daily calendar logic:

- A hotel day is evaluated as `[date, date.plusDays(1))`
- `totalRooms` = count of active rooms for the hotel
- `bookedRooms` = active rooms with `CONFIRMED` bookings overlapping that day
- `availableRooms = totalRooms - bookedRooms`
- `available = availableRooms > 0`

## Data Model

Main entities:

- `users`
  - `id`
  - `username`
  - `password_hash`
  - `role`
- `hotels`
  - `id`
  - `name`
  - `state`
  - `city`
  - `description`
  - `price_per_night`
- `rooms`
  - `id`
  - `hotel_id`
  - `room_label`
  - `room_type`
  - `description`
  - `features`
  - `state`
  - `is_booked`
  - `booked_date`
  - `price`
- `bookings`
  - `id`
  - `user_id`
  - `hotel_id`
  - `room_id`
  - `booking_date`
  - `check_in_date`
  - `check_out_date`
  - `total_price`
  - `status`

Relationships:

- `User -> Bookings` : one-to-many
- `Hotel -> Rooms` : one-to-many
- `Hotel -> Bookings` : one-to-many
- `Room -> Bookings` : one-to-many

## Security

Authentication:

- Stateless JWT authentication
- JWT is expected as:

```text
Authorization: Bearer <token>
```

CORS:

- Allowed origins:
  - configured by `APP_CORS_ALLOWED_ORIGINS`
  - defaults to `http://localhost:4200,http://127.0.0.1:4200`

## Profiles And Configuration

`application.yml` defines three profiles:

- `local`
- `neon`
- `prod`

Defaults:

- active profile defaults to `local`
- server port defaults to `8080`

Local datasource defaults:

```text
jdbc:postgresql://localhost:5432/summit_project
username: arbin
password: arbin
```

JWT properties are configured through:

- `JWT_SECRET`
- `JWT_EXPIRATION_MINUTES`
- `JWT_ISSUER`
- `APP_CORS_ALLOWED_ORIGINS`

## Database Migrations

Flyway migrations:

- `V1__create_users_table.sql`
- `V2__create_booking_tables.sql`
- `V3__create_rooms_and_refactor_booking_constraints.sql`
- `V4__expand_hotels_rooms_and_bookings_for_booking_history.sql`
- `V5__finalize_booking_schema_for_room_based_bookings.sql`

Flyway Maven plugin is configured, so these commands are available:

```bash
./mvnw flyway:repair
./mvnw flyway:migrate
```

## Seed Data

Users are seeded idempotently on startup only in the `local` profile:

- `admin / Admin@123` with role `ADMIN`
- `user / User@123` with role `USER`

Hotels are seeded idempotently on startup only in the `local` profile:

- `Grand Summit Hotel` in `California / San Diego`
- `Ocean Crest Resort` in `Florida / Miami`
- `Maple Leaf Suites` in `New York / Albany`

Rooms are seeded idempotently under those hotels only in the `local` profile:

- `GS-101`, `GS-102`, `GS-201`
- `OC-301`, `OC-302`
- `ML-401`, `ML-402`

## Local Setup

1. Start PostgreSQL and create the `summit_project` database.
2. Export the required environment variables in your shell or load them through your IDE/run configuration. `.env.example` is a local template only.
3. Run:

```bash
./mvnw spring-boot:run
```

4. Run tests:

```bash
./mvnw test
```

## API Reference

Base URL:

```text
http://localhost:8080
```

### Access Matrix

- Public:
  - `POST /api/v1/auth/signup`
  - `POST /api/v1/auth/login`
- `USER` or `ADMIN`:
  - `GET /api/v1/hotels`
  - `GET /api/v1/hotels/{hotelId}/calendar`
  - `GET /api/v1/hotels/{hotelId}/rooms`
  - `GET /api/v1/rooms/{roomId}`
  - `GET /api/v1/users/me/bookings`
- `USER` only:
  - `POST /api/v1/bookings`
- `ADMIN` only:
  - `PATCH /api/v1/admin/users/{username}/role`
  - `GET /api/v1/admin/bookings`
  - `POST /api/v1/admin/hotels`
  - `POST /api/v1/admin/hotels/{hotelId}/rooms`
  - `DELETE /api/v1/admin/rooms/{roomId}`

## Auth APIs

### `POST /api/v1/auth/signup`

Creates a new user with role `USER`.

Request:

```json
{
  "username": "arbin",
  "password": "abc123"
}
```

Validation:

- `username` required, 3 to 80 characters
- `password` required, 6 to 100 characters
- password must contain at least one letter and one number

Response: `201 Created`

```json
{
  "token": "jwt-token",
  "tokenType": "Bearer",
  "expiresAt": "2030-01-01T00:00:00Z",
  "role": "USER"
}
```

### `POST /api/v1/auth/login`

Request:

```json
{
  "username": "admin",
  "password": "Admin@123"
}
```

Response: `200 OK`

```json
{
  "token": "jwt-token",
  "tokenType": "Bearer",
  "expiresAt": "2030-01-01T00:00:00Z",
  "role": "ADMIN"
}
```

### `PATCH /api/v1/admin/users/{username}/role`

Bootstrap/admin role assignment.

Request:

```json
{
  "role": "ADMIN"
}
```

Response: `200 OK`

```json
{
  "username": "arbin",
  "role": "ADMIN",
  "message": "Role updated successfully. The user must log in again to receive a token with the new role."
}
```

## Hotel APIs

### `GET /api/v1/hotels`

Returns filtered hotels plus a calendar window.

Query params:

- `state` optional
- `city` optional
- `startDate` optional, format `YYYY-MM-DD`
- `days` optional, default `7`, min `1`, max `7`

Example:

```text
GET /api/v1/hotels?state=California&city=San%20Diego&startDate=2026-05-10&days=7
```

Response:

```json
[
  {
    "hotelId": 1,
    "hotelName": "Grand Summit Hotel",
    "state": "California",
    "city": "San Diego",
    "description": "Luxury business hotel near the harbor.",
    "startingPrice": 149.99,
    "totalRooms": 3,
    "availableRooms": 2,
    "calendar": [
      {
        "date": "2026-05-10",
        "available": true,
        "availableRooms": 2
      }
    ]
  }
]
```

Important:

- `calendar` is the per-day availability window requested by `startDate` and `days`
- `availableRooms` at hotel level is currently the maximum daily available room count across the returned calendar window

### `GET /api/v1/hotels/{hotelId}/calendar`

Returns the calendar for a single hotel.

Query params:

- `startDate` optional, format `YYYY-MM-DD`
- `days` optional, default `7`, min `1`, max `7`

Example:

```text
GET /api/v1/hotels/1/calendar?startDate=2026-05-10&days=7
```

Response:

```json
{
  "hotelId": 1,
  "hotelName": "Grand Summit Hotel",
  "totalRooms": 3,
  "calendar": [
    {
      "date": "2026-05-10",
      "available": true,
      "availableRooms": 2
    }
  ]
}
```

## Room APIs

### `GET /api/v1/hotels/{hotelId}/rooms`

Returns all active rooms for the hotel and computes `available` for the requested date range.

Required query params:

- `checkInDate`
- `checkOutDate`

Example:

```text
GET /api/v1/hotels/1/rooms?checkInDate=2026-05-10&checkOutDate=2026-05-12
```

Response:

```json
[
  {
    "roomId": 1,
    "roomLabel": "GS-101",
    "roomType": "Deluxe King",
    "price": 159.99,
    "features": ["WiFi", "Smart TV", "Mini Bar"],
    "available": true
  },
  {
    "roomId": 2,
    "roomLabel": "GS-102",
    "roomType": "Twin Deluxe",
    "price": 149.99,
    "features": ["WiFi", "Work Desk", "Coffee Maker"],
    "available": false
  }
]
```

Important:

- send dates as query parameters, not as a JSON body
- `checkOutDate` must be after `checkInDate`

### `GET /api/v1/rooms/{roomId}`

Returns one room and computes `available` for the requested date range.

Required query params:

- `checkInDate`
- `checkOutDate`

Example:

```text
GET /api/v1/rooms/1?checkInDate=2026-05-10&checkOutDate=2026-05-12
```

Response:

```json
{
  "roomId": 1,
  "hotelId": 1,
  "hotelName": "Grand Summit Hotel",
  "roomLabel": "GS-101",
  "roomType": "Deluxe King",
  "description": "King room with city view.",
  "features": ["WiFi", "Smart TV", "Mini Bar"],
  "price": 159.99,
  "available": true
}
```

## Booking APIs

### `POST /api/v1/bookings`

Creates a booking using the existing booking flow.

Request:

```json
{
  "hotelId": 1,
  "roomId": 2,
  "checkInDate": "2026-05-10",
  "checkOutDate": "2026-05-12"
}
```

Validation:

- `hotelId` required
- `roomId` required
- `checkInDate` required and must be today or in the future
- `checkOutDate` required and must be today or in the future
- `checkOutDate` must be after `checkInDate`
- selected room must belong to selected hotel
- overlapping confirmed bookings are rejected

Response:

```json
{
  "bookingId": 1,
  "message": "Your booking for the Hotel Grand Summit Hotel is successful",
  "hotelName": "Grand Summit Hotel",
  "roomLabel": "GS-102",
  "checkInDate": "2026-05-10",
  "checkOutDate": "2026-05-12",
  "totalPrice": 299.98,
  "status": "CONFIRMED",
  "bookingDate": "2026-05-04T15:20:24.768762+05:30"
}
```

### `GET /api/v1/users/me/bookings`

Returns booking history for the authenticated user.

Response:

```json
[
  {
    "bookingId": 1,
    "hotelName": "Grand Summit Hotel",
    "city": "San Diego",
    "state": "California",
    "roomLabel": "GS-102",
    "roomType": "Twin Deluxe",
    "bookingDate": "2026-05-04T15:20:24.768762+05:30",
    "checkInDate": "2026-05-10",
    "checkOutDate": "2026-05-12",
    "totalPrice": 299.98,
    "status": "CONFIRMED"
  }
]
```

### `GET /api/v1/admin/bookings`

Returns all bookings for admin reporting.

Response:

```json
[
  {
    "bookingId": 1,
    "username": "user",
    "hotelName": "Grand Summit Hotel",
    "city": "San Diego",
    "state": "California",
    "roomLabel": "GS-102",
    "roomType": "Twin Deluxe",
    "checkInDate": "2026-05-10",
    "checkOutDate": "2026-05-12",
    "totalPrice": 299.98,
    "status": "CONFIRMED",
    "bookingDate": "2026-05-04T15:20:24.768762+05:30"
  }
]
```

## Admin Inventory APIs

### `POST /api/v1/admin/hotels`

Creates a hotel. Requires `ADMIN`.

Request:

```json
{
  "name": "Skyline Retreat",
  "state": "Texas",
  "city": "Austin",
  "description": "Downtown stay with rooftop lounge.",
  "pricePerNight": 189.99
}
```

Validation:

- `name` required, max 120 characters
- `state` required, max 30 characters
- `city` required, max 80 characters
- `description` max 255 characters
- `pricePerNight` required and must be greater than `0`
- hotel name must be unique, checked case-insensitively

Response: `201 Created`

```json
{
  "hotelId": 4,
  "name": "Skyline Retreat",
  "state": "Texas",
  "city": "Austin",
  "description": "Downtown stay with rooftop lounge.",
  "pricePerNight": 189.99,
  "message": "Hotel created successfully"
}
```

### `POST /api/v1/admin/hotels/{hotelId}/rooms`

Creates a room under an existing hotel. Requires `ADMIN`.

Request:

```json
{
  "roomLabel": "SR-501",
  "roomType": "Premium Suite",
  "description": "Corner suite with skyline view.",
  "features": "WiFi,Balcony,Mini Bar",
  "state": "ACTIVE",
  "price": 259.99
}
```

Behavior:

- hotel must exist
- `booked` is initialized to `false`
- `bookedDate` is initialized to `null`
- those legacy fields are not used for availability logic

Response: `201 Created`

```json
{
  "roomId": 8,
  "hotelId": 4,
  "hotelName": "Skyline Retreat",
  "roomLabel": "SR-501",
  "roomType": "Premium Suite",
  "description": "Corner suite with skyline view.",
  "features": ["WiFi", "Balcony", "Mini Bar"],
  "state": "ACTIVE",
  "booked": false,
  "bookedDate": null,
  "price": 259.99,
  "message": "Room created successfully"
}
```

### `DELETE /api/v1/admin/rooms/{roomId}`

Deletes a room. Requires `ADMIN`.

Validation:

- room must exist
- deletion is blocked if the room has any `CONFIRMED` booking with `checkOutDate >= today`
- validation uses the `bookings` table and `BookingStatus.CONFIRMED`
- `room.booked` and `room.bookedDate` are not used

Additional behavior:

- if a room has historical booking rows, the database foreign key can still block hard deletion
- this is returned as a clean admin error instead of a raw SQL exception

Response:

```json
{
  "message": "Room deleted successfully"
}
```

## Error Response Shape

Auth and booking modules both use this envelope shape:

```json
{
  "timestamp": "2026-05-04T10:08:08.214530Z",
  "status": 400,
  "error": "Validation failed",
  "details": [
    "Missing required parameter: checkInDate"
  ]
}
```

Typical auth errors:

- `401 Authentication failed`
- `409 Registration failed`
- `404 User not found`

Typical booking/admin errors:

- `400 Booking failed`
- `400 Admin operation failed`
- `403 Access denied`

## Current Test Coverage

Current tests cover:

- auth signup/login service behavior
- booking creation and overlap rejection
- room availability logic
- hotel calendar logic
- single-hotel calendar logic
- admin hotel creation, room creation, and room deletion validation paths

Run:

```bash
./mvnw test
```

## Practical Notes

- If you changed an old Flyway migration locally and startup fails with checksum mismatch, use `./mvnw flyway:repair`.
- If you call room availability APIs from Postman, put dates in query params, not the request body.
- If you want a clean non-user dataset for retesting, truncate `bookings`, `hotel_availability`, `rooms`, and `hotels`, then reseed hotels and rooms.

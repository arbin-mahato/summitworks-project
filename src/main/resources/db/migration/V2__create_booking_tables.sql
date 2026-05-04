CREATE TABLE IF NOT EXISTS hotels (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    price_per_night NUMERIC(10,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS hotel_availability (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL REFERENCES hotels(id),
    available_date DATE NOT NULL,
    available BOOLEAN NOT NULL,
    CONSTRAINT uq_hotel_date UNIQUE (hotel_id, available_date)
);

CREATE TABLE IF NOT EXISTS bookings (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    room_id BIGINT,
    hotel_name VARCHAR(120) NOT NULL,
    room_label VARCHAR(40) DEFAULT 'UNKNOWN' NOT NULL,
    reserved_date DATE NOT NULL,
    reserved_for_user VARCHAR(80) NOT NULL,
    amount_paid NUMERIC(10,2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_booking_hotel_date UNIQUE (hotel_id, reserved_date)
);

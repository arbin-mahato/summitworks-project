ALTER TABLE hotels
    ADD COLUMN IF NOT EXISTS state VARCHAR(30) NOT NULL DEFAULT 'ACTIVE';

CREATE TABLE IF NOT EXISTS rooms (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL REFERENCES hotels(id),
    room_label VARCHAR(40) NOT NULL,
    state VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    is_booked BOOLEAN NOT NULL DEFAULT FALSE,
    booked_date DATE,
    price NUMERIC(10,2) NOT NULL
);

ALTER TABLE bookings
    ADD COLUMN IF NOT EXISTS room_id BIGINT;

ALTER TABLE bookings
    ADD COLUMN IF NOT EXISTS room_label VARCHAR(40) NOT NULL DEFAULT 'UNKNOWN';

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_name = 'bookings'
          AND constraint_name = 'uq_booking_hotel_date'
    ) THEN
        ALTER TABLE bookings DROP CONSTRAINT uq_booking_hotel_date;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_name = 'bookings'
          AND constraint_name = 'uq_booking_room_date'
    ) THEN
        ALTER TABLE bookings
            ADD CONSTRAINT uq_booking_room_date UNIQUE (room_id, reserved_date);
    END IF;
END $$;

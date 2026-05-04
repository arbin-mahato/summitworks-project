ALTER TABLE hotels
    ADD COLUMN IF NOT EXISTS city VARCHAR(80) NOT NULL DEFAULT 'Unknown';

ALTER TABLE hotels
    ADD COLUMN IF NOT EXISTS description VARCHAR(255);

ALTER TABLE rooms
    ADD COLUMN IF NOT EXISTS room_type VARCHAR(50) NOT NULL DEFAULT 'Standard';

ALTER TABLE rooms
    ADD COLUMN IF NOT EXISTS description VARCHAR(500);

ALTER TABLE rooms
    ADD COLUMN IF NOT EXISTS features VARCHAR(500);

ALTER TABLE bookings
    ADD COLUMN IF NOT EXISTS user_id BIGINT;

ALTER TABLE bookings
    ADD COLUMN IF NOT EXISTS booking_date TIMESTAMP WITH TIME ZONE;

ALTER TABLE bookings
    ADD COLUMN IF NOT EXISTS check_in_date DATE;

ALTER TABLE bookings
    ADD COLUMN IF NOT EXISTS check_out_date DATE;

ALTER TABLE bookings
    ADD COLUMN IF NOT EXISTS total_price NUMERIC(10,2);

ALTER TABLE bookings
    ADD COLUMN IF NOT EXISTS status VARCHAR(20);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_name = 'bookings'
          AND constraint_name = 'uq_booking_room_date'
    ) THEN
        ALTER TABLE bookings DROP CONSTRAINT uq_booking_room_date;
    END IF;
END $$;

UPDATE bookings
SET booking_date = COALESCE(booking_date, created_at),
    check_in_date = COALESCE(check_in_date, reserved_date),
    check_out_date = COALESCE(check_out_date, reserved_date + INTERVAL '1 day'),
    total_price = COALESCE(total_price, amount_paid),
    status = COALESCE(status, 'CONFIRMED')
WHERE booking_date IS NULL
   OR check_in_date IS NULL
   OR check_out_date IS NULL
   OR total_price IS NULL
   OR status IS NULL;

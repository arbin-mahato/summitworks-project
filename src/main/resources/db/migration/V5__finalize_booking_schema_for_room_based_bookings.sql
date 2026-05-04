UPDATE bookings b
SET hotel_id = COALESCE(b.hotel_id, r.hotel_id)
FROM rooms r
WHERE b.room_id = r.id
  AND b.hotel_id IS NULL;

UPDATE bookings b
SET room_id = r.id
FROM rooms r
WHERE b.room_id IS NULL
  AND b.hotel_id = r.hotel_id
  AND lower(b.room_label) = lower(r.room_label);

UPDATE bookings
SET booking_date = COALESCE(booking_date, created_at),
    check_in_date = COALESCE(check_in_date, reserved_date),
    check_out_date = COALESCE(check_out_date, reserved_date + INTERVAL '1 day'),
    total_price = COALESCE(total_price, amount_paid),
    status = COALESCE(status, 'CONFIRMED');

ALTER TABLE bookings
    ALTER COLUMN hotel_id SET NOT NULL;

ALTER TABLE bookings
    ALTER COLUMN room_id SET NOT NULL;

ALTER TABLE bookings
    ALTER COLUMN booking_date SET NOT NULL;

ALTER TABLE bookings
    ALTER COLUMN check_in_date SET NOT NULL;

ALTER TABLE bookings
    ALTER COLUMN check_out_date SET NOT NULL;

ALTER TABLE bookings
    ALTER COLUMN total_price SET NOT NULL;

ALTER TABLE bookings
    ALTER COLUMN status SET NOT NULL;

ALTER TABLE bookings
    DROP COLUMN IF EXISTS hotel_name,
    DROP COLUMN IF EXISTS reserved_date,
    DROP COLUMN IF EXISTS reserved_for_user,
    DROP COLUMN IF EXISTS amount_paid,
    DROP COLUMN IF EXISTS created_at,
    DROP COLUMN IF EXISTS room_label;

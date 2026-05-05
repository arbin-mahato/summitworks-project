ALTER TABLE users
    ADD COLUMN IF NOT EXISTS full_name VARCHAR(80);

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS email VARCHAR(120);

UPDATE users
SET full_name = COALESCE(full_name, username)
WHERE full_name IS NULL;

UPDATE users
SET email = CASE
    WHEN username LIKE '%@%' THEN LOWER(username)
    WHEN username = 'admin' THEN 'admin@example.com'
    WHEN username = 'user' THEN 'user@example.com'
    ELSE LOWER(REPLACE(username, ' ', '.')) || '@example.com'
END
WHERE email IS NULL;

ALTER TABLE users
    ALTER COLUMN full_name SET NOT NULL;

ALTER TABLE users
    ALTER COLUMN email SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_users_email ON users (email);

-- Drop the old CHECK constraint on users table (if it exists)
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;

-- Add new CHECK constraint with MODERATOR role included
ALTER TABLE users
    ADD CONSTRAINT users_role_check
    CHECK (role IN ('EMPLOYER', 'MODERATOR', 'ADMIN'));

COMMENT ON CONSTRAINT users_role_check ON users IS 'Validates user role values';

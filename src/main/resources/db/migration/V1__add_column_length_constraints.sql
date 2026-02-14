-- V1__add_column_length_constraints.sql
-- Migration to add length constraints to existing columns

-- Users table
ALTER TABLE users
    ALTER COLUMN email TYPE VARCHAR(255),
    ALTER COLUMN password_hash TYPE VARCHAR(255),
    ALTER COLUMN company_name TYPE VARCHAR(255),
    ALTER COLUMN role TYPE VARCHAR(50);

-- Tariffs table
ALTER TABLE tariffs
    ALTER COLUMN name TYPE VARCHAR(100);

-- Skills table
ALTER TABLE skills
    ALTER COLUMN name TYPE VARCHAR(100);

-- Vacancies table
ALTER TABLE vacancies
    ALTER COLUMN title TYPE VARCHAR(255),
    ALTER COLUMN city TYPE VARCHAR(100),
    ALTER COLUMN address TYPE VARCHAR(500),
    ALTER COLUMN experience_level TYPE VARCHAR(50),
    ALTER COLUMN employment_type TYPE VARCHAR(50),
    ALTER COLUMN work_format TYPE VARCHAR(50),
    ALTER COLUMN employment_format TYPE VARCHAR(50),
    ALTER COLUMN work_schedule TYPE VARCHAR(50),
    ALTER COLUMN status TYPE VARCHAR(50);

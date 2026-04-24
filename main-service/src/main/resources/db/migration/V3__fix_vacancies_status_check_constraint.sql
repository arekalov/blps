-- Drop the old CHECK constraint on vacancies table (if it exists)
ALTER TABLE vacancies DROP CONSTRAINT IF EXISTS vacancies_status_check;

-- Add new CHECK constraint with all statuses including PENDING_MODERATION and REJECTED
ALTER TABLE vacancies
    ADD CONSTRAINT vacancies_status_check
    CHECK (status IN ('DRAFT', 'PENDING_MODERATION', 'REJECTED', 'PUBLISHED', 'ARCHIVED', 'CLOSED'));

COMMENT ON CONSTRAINT vacancies_status_check ON vacancies IS 'Validates vacancy status values including moderation statuses';

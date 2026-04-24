-- Промежуточный статус перед очередью модерации (лаба №3)
ALTER TABLE vacancies DROP CONSTRAINT IF EXISTS vacancies_status_check;

ALTER TABLE vacancies
    ADD CONSTRAINT vacancies_status_check
    CHECK (status IN (
        'DRAFT',
        'SUBMISSION_PENDING',
        'PENDING_MODERATION',
        'REJECTED',
        'PUBLISHED',
        'ARCHIVED',
        'CLOSED'
    ));

COMMENT ON CONSTRAINT vacancies_status_check ON vacancies IS 'Includes SUBMISSION_PENDING for async handoff to moderation';

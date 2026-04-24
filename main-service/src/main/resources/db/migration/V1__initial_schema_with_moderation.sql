-- Add MODERATOR role to user_role enum
ALTER TYPE user_role RENAME TO user_role_old;

CREATE TYPE user_role AS ENUM (
    'EMPLOYER',
    'MODERATOR',
    'ADMIN'
);

ALTER TABLE users
    ALTER COLUMN role TYPE user_role USING role::text::user_role;

DROP TYPE user_role_old;

CREATE INDEX idx_users_role ON users(role);

-- Update VacancyStatus enum to include new statuses
ALTER TYPE vacancy_status RENAME TO vacancy_status_old;

CREATE TYPE vacancy_status AS ENUM (
    'DRAFT',
    'PENDING_MODERATION',
    'REJECTED',
    'PUBLISHED',
    'ARCHIVED',
    'CLOSED'
);

ALTER TABLE vacancies
    ALTER COLUMN status TYPE vacancy_status USING status::text::vacancy_status;

DROP TYPE vacancy_status_old;

-- Add moderation fields to vacancies table
ALTER TABLE vacancies
    ADD COLUMN moderator_id UUID,
    ADD COLUMN moderated_at TIMESTAMP,
    ADD COLUMN rejection_reason TEXT;

ALTER TABLE vacancies
    ADD CONSTRAINT fk_vacancies_moderator
        FOREIGN KEY (moderator_id) REFERENCES users(id);

CREATE INDEX idx_vacancies_status ON vacancies(status);
CREATE INDEX idx_vacancies_moderator_id ON vacancies(moderator_id);

-- Create tariff_usage_history table
CREATE TABLE tariff_usage_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vacancy_id UUID NOT NULL,
    tariff_id UUID NOT NULL,
    employer_id UUID NOT NULL,
    moderator_id UUID NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    duration_days INTEGER NOT NULL,
    published_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_tariff_history_vacancy FOREIGN KEY (vacancy_id) REFERENCES vacancies(id) ON DELETE CASCADE,
    CONSTRAINT fk_tariff_history_tariff FOREIGN KEY (tariff_id) REFERENCES tariffs(id),
    CONSTRAINT fk_tariff_history_employer FOREIGN KEY (employer_id) REFERENCES users(id),
    CONSTRAINT fk_tariff_history_moderator FOREIGN KEY (moderator_id) REFERENCES users(id)
);

CREATE INDEX idx_tariff_history_tariff_id ON tariff_usage_history(tariff_id);
CREATE INDEX idx_tariff_history_employer_id ON tariff_usage_history(employer_id);
CREATE INDEX idx_tariff_history_moderator_id ON tariff_usage_history(moderator_id);
CREATE INDEX idx_tariff_history_published_at ON tariff_usage_history(published_at DESC);

COMMENT ON TABLE tariff_usage_history IS 'History of tariff usage for published vacancies';

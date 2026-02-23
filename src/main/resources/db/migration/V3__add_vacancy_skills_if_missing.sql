-- V3__add_vacancy_skills_if_missing.sql
-- Create vacancy_skills if it was missing (e.g. DB created before V0 included it, or table was dropped)

CREATE TABLE IF NOT EXISTS vacancy_skills (
    vacancy_id UUID NOT NULL REFERENCES vacancies(id) ON DELETE CASCADE,
    skill_id UUID NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
    PRIMARY KEY (vacancy_id, skill_id)
);

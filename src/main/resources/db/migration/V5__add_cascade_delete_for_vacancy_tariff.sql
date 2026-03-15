-- V5: Add ON DELETE CASCADE to vacancies.tariff_id foreign key
-- This ensures that when a tariff is deleted, all associated vacancies are also deleted

DO $$
DECLARE
    constraint_name_var VARCHAR;
BEGIN
    -- Get the exact constraint name if it exists
    SELECT constraint_name INTO constraint_name_var
    FROM information_schema.table_constraints
    WHERE constraint_name LIKE '%tariff%'
      AND table_name = 'vacancies'
      AND constraint_type = 'FOREIGN KEY'
    LIMIT 1;

    -- Drop the old constraint if it exists
    IF constraint_name_var IS NOT NULL THEN
        EXECUTE format('ALTER TABLE vacancies DROP CONSTRAINT IF EXISTS %I', constraint_name_var);
        RAISE NOTICE 'Dropped old foreign key constraint: %', constraint_name_var;
    END IF;

    -- Add the new constraint with ON DELETE CASCADE
    ALTER TABLE vacancies
        DROP CONSTRAINT IF EXISTS fk_vacancies_tariff;
    
    ALTER TABLE vacancies
        ADD CONSTRAINT fk_vacancies_tariff
            FOREIGN KEY (tariff_id)
            REFERENCES tariffs(id)
            ON DELETE CASCADE;

    RAISE NOTICE 'Added foreign key constraint with ON DELETE CASCADE for vacancies.tariff_id';
END $$;

ALTER TABLE client
ADD COLUMN full_name VARCHAR(255);
--;;
UPDATE client
SET full_name = 'Existing User'
WHERE full_name IS NULL;
--;;
ALTER TABLE client
ALTER COLUMN full_name SET NOT NULL;

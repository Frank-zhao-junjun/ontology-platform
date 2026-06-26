-- V19: Add interface_names column to object_type
-- interface_names stored as JSON text (array of interface name strings)

ALTER TABLE object_type
  ADD COLUMN IF NOT EXISTS interface_names TEXT DEFAULT '[]';

-- Update existing rows that have null
UPDATE object_type SET interface_names = '[]' WHERE interface_names IS NULL;

-- Convert role column from smallint to varchar to support EnumType.STRING
ALTER TABLE user_profiles ALTER COLUMN role TYPE VARCHAR(20);

-- Supprimer l'ancienne contrainte d'unicité sur l'email
ALTER TABLE user_profiles DROP CONSTRAINT IF EXISTS uk_email;

-- Ajouter une contrainte composite sur email + role
ALTER TABLE user_profiles ADD CONSTRAINT uk_email_role UNIQUE (email, role);

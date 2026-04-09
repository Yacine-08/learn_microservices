-- Supprimer l'ancienne contrainte d'unicité sur l'email
ALTER TABLE auth_users DROP CONSTRAINT IF EXISTS uk6jqfsuvys3lan090p4mk16a5t;

-- Ajouter une contrainte composite sur email + role
ALTER TABLE auth_users ADD CONSTRAINT uk_email_role UNIQUE (email, role);

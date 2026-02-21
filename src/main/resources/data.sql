-- Seed data for development
-- Spring Boot runs this file after Hibernate creates/updates the schema.
-- All inserts use ON CONFLICT to be safely re-runnable.

-- Password for all seed users: 123456
-- BCrypt hash of "123456":

-- Users
INSERT INTO users (id, full_name, email, password_hash, role, english_level_type, english_level_value, email_verified, no_show_count, created_at)
VALUES
  ('a0000000-0000-0000-0000-000000000001', 'Super Admin',            'superadmin@local.dev',  '$2b$12$U9iZU6lYflR0K2ZgCFcGRuChsClor9P6p5BCBdd8oeX9BSSOniovC', 'SUPER_ADMIN', 'CEFR',      'C2',  true, 0, now()),
  ('a0000000-0000-0000-0000-000000000002', 'Admin User',             'admin@local.dev',       '$2b$12$U9iZU6lYflR0K2ZgCFcGRuChsClor9P6p5BCBdd8oeX9BSSOniovC', 'ADMIN',       'IELTS',     '7.0', true, 0, now()),
  ('a0000000-0000-0000-0000-000000000003', 'Mod User',               'moderator@local.dev',   '$2b$12$U9iZU6lYflR0K2ZgCFcGRuChsClor9P6p5BCBdd8oeX9BSSOniovC', 'MODERATOR',   'TOEFL_IBT', '90',  true, 0, now()),
  ('a0000000-0000-0000-0000-000000000004', 'Alice Participant',      'alice@local.dev',       '$2b$12$U9iZU6lYflR0K2ZgCFcGRuChsClor9P6p5BCBdd8oeX9BSSOniovC', 'PARTICIPANT', 'CEFR',      'B1',  true, 0, now()),
  ('a0000000-0000-0000-0000-000000000005', 'Bob Participant',        'bob@local.dev',         '$2b$12$U9iZU6lYflR0K2ZgCFcGRuChsClor9P6p5BCBdd8oeX9BSSOniovC', 'PARTICIPANT', 'IELTS',     '5.5', true, 0, now()),
  ('a0000000-0000-0000-0000-000000000006', 'Charlie Participant',    'charlie@local.dev',     '$2b$12$U9iZU6lYflR0K2ZgCFcGRuChsClor9P6p5BCBdd8oeX9BSSOniovC', 'PARTICIPANT', 'DUOLINGO',  '120', true, 0, now()),
  ('a0000000-0000-0000-0000-000000000007', 'Diana Participant',      'diana@local.dev',       '$2b$12$U9iZU6lYflR0K2ZgCFcGRuChsClor9P6p5BCBdd8oeX9BSSOniovC', 'PARTICIPANT', 'CEFR',      'B2',  true, 0, now()),
  ('a0000000-0000-0000-0000-000000000008', 'Eve Participant',        'eve@local.dev',         '$2b$12$U9iZU6lYflR0K2ZgCFcGRuChsClor9P6p5BCBdd8oeX9BSSOniovC', 'PARTICIPANT', 'TOEFL_ITP', '500', true, 0, now()),
  ('a0000000-0000-0000-0000-000000000009', 'Mod User Two',           'moderator2@local.dev',  '$2b$12$U9iZU6lYflR0K2ZgCFcGRuChsClor9P6p5BCBdd8oeX9BSSOniovC', 'MODERATOR',   'TOEFL_IBT', '90',  true, 0, now()),
  ('a0000000-0000-0000-0000-000000000010', 'Alice Participant Two',  'alice2@local.dev',      '$2b$12$U9iZU6lYflR0K2ZgCFcGRuChsClor9P6p5BCBdd8oeX9BSSOniovC', 'PARTICIPANT', 'CEFR',      'B1',  true, 0, now()),
  ('a0000000-0000-0000-0000-000000000011', 'Bob Participant Two',    'bob2@local.dev',        '$2b$12$U9iZU6lYflR0K2ZgCFcGRuChsClor9P6p5BCBdd8oeX9BSSOniovC', 'PARTICIPANT', 'IELTS',     '5.5', true, 0, now()),
  ('a0000000-0000-0000-0000-000000000012', 'Charlie Participant Two','charlie2@local.dev',    '$2b$12$U9iZU6lYflR0K2ZgCFcGRuChsClor9P6p5BCBdd8oeX9BSSOniovC', 'PARTICIPANT', 'DUOLINGO',  '120', true, 0, now()),
  ('a0000000-0000-0000-0000-000000000013', 'Diana Participant Two',  'diana2@local.dev',      '$2b$12$U9iZU6lYflR0K2ZgCFcGRuChsClor9P6p5BCBdd8oeX9BSSOniovC', 'PARTICIPANT', 'CEFR',      'B2',  true, 0, now()),
  ('a0000000-0000-0000-0000-000000000014', 'Eve Participant Two',    'eve2@local.dev',        '$2b$12$U9iZU6lYflR0K2ZgCFcGRuChsClor9P6p5BCBdd8oeX9BSSOniovC', 'PARTICIPANT', 'TOEFL_ITP', '500', true, 0, now())
ON CONFLICT (email) DO NOTHING;

-- Session (created by Admin User)
INSERT INTO sessions (id, title, description, start_date_time, duration_minutes, max_participants, status, created_by_id, attendance_code, created_at)
VALUES
  ('b0000000-0000-0000-0000-000000000001',
   'Weekly English Chat #1',
   'Practice your English in a friendly, relaxed environment. This week we talk about daily routines!',
   now() + interval '7 days', 60, 20, 'OPEN',
   'a0000000-0000-0000-0000-000000000002',
   lpad(floor(random() * 100)::text, 2, '0'),
   now())
ON CONFLICT (id) DO NOTHING;

-- App config
INSERT INTO app_config (config_key, config_value)
VALUES
  ('max_no_shows', '3'),
  ('blacklist_duration_days', '30'),
  ('unregister_cutoff_hours', '24')
ON CONFLICT (config_key) DO NOTHING;

-- Insert admin user (password: admin123)
INSERT INTO users (id, username, password, email, first_name, last_name, role, enabled)
VALUES (
    '11111111-1111-1111-1111-111111111111',
    'admin',
    '$2a$10$XptfskLsT1l/bRTLRiiCgegjHjOagcakvuGLMleDZ..yiFKLJqEfy', -- bcrypt hash for 'admin123'
    'admin@krainet.test',
    'System',
    'Administrator',
    'ADMIN',
    true
)
ON CONFLICT (username) DO NOTHING;

-- Insert test user (password: user123)
INSERT INTO users (id, username, password, email, first_name, last_name, role, enabled)
VALUES (
    '22222222-2222-2222-2222-222222222222',
    'user',
    '$2a$10$IqTJTjn39IU5.7sSCDQxzu3xug6z/LPU6IF0azE/8CkHCwYEnwBX.', -- bcrypt hash for 'user123'
    'user@krainet.test',
    'Test',
    'User',
    'USER',
    true
)
ON CONFLICT (username) DO NOTHING;

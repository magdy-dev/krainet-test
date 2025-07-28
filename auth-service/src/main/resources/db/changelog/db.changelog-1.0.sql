-- liquibase formatted sql

-- changeset system:1
do $$
begin
    if not exists (select 1 from pg_tables where tablename = 'users') then
        create table users (
            id uuid not null,
            username varchar(50) not null,
            email varchar(255) not null,
            password varchar(255) not null,
            first_name varchar(50),
            last_name varchar(50),
            role varchar(20) not null,
            enabled boolean not null default true,
            account_non_expired boolean not null default true,
            account_non_locked boolean not null default true,
            credentials_non_expired boolean not null default true,
            created_at timestamp not null default current_timestamp,
            updated_at timestamp not null default current_timestamp,
            constraint pk_users primary key (id),
            constraint uk_users_username unique (username),
            constraint uk_users_email unique (email)
        );
    end if;
end $$;

-- changeset system:2
do $$
begin
    if not exists (select 1 from pg_indexes where indexname = 'idx_users_username') then
        create index idx_users_username on users(username);
    end if;
    
    if not exists (select 1 from pg_indexes where indexname = 'idx_users_email') then
        create index idx_users_email on users(email);
    end if;
end $$;

-- changeset system:3
-- Add test data
do $$
declare
    admin_id uuid := '11111111-1111-1111-1111-111111111111';
    user_id uuid := '22222222-2222-2222-2222-222222222222';
    hashed_pw text := '$2a$10$xLFtBIXGt8v/VH3H3/5SHu/GMo5/gkQpx3mtdkIemGjMlfEbXgq1O'; -- 'admin123'
begin
    -- Only insert if users don't exist
    if not exists (select 1 from users where id = admin_id) then
        insert into users (id, username, email, password, first_name, last_name, role, enabled, 
                         account_non_expired, account_non_locked, credentials_non_expired)
        values (admin_id, 'admin', 'admin@example.com', hashed_pw, 'Admin', 'User', 'ADMIN', true, true, true, true);
    end if;
    
    if not exists (select 1 from users where id = user_id) then
        insert into users (id, username, email, password, first_name, last_name, role, enabled, 
                         account_non_expired, account_non_locked, credentials_non_expired)
        values (user_id, 'testuser', 'user@example.com', hashed_pw, 'Test', 'User', 'USER', true, true, true, true);
    end if;
end $$;

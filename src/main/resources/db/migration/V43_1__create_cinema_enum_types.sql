do
$$
    begin
        if not exists(select from pg_type where typname = 'user_role') then
            create type user_role as enum ('CLIENT', 'EMPLOYEE', 'MANAGER');
        end if;
    end
$$;

do
$$
    begin
        if not exists(select from pg_type where typname = 'genre') then
            create type genre as enum ('THRILLER', 'ROMANCE', 'COMEDY', 'DRAMA', 'ACTION', 'SCI_FI', 'FANTASY', 'ANIMATION');
        end if;
    end
$$;

do
$$
    begin
        if not exists(select from pg_type where typname = 'reservation_status') then
            create type reservation_status as enum ('PENDING', 'SUCCESS', 'CANCELED');
        end if;
    end
$$;

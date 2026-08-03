create table if not exists "user"
(
    id         uuid primary key default gen_random_uuid(),
    first_name varchar(255),
    last_name  varchar(255),
    birthdate  date,
    email      varchar(255) not null unique,
    password   text,
    phone      varchar(255),
    role       user_role    not null default 'CLIENT'
);

create table if not exists movie
(
    id          uuid primary key default gen_random_uuid(),
    title       varchar(255) not null,
    description text,
    duration    numeric(21)
);

create table if not exists movie_genres
(
    movie_id uuid not null references movie (id),
    genre    genre not null,
    primary key (movie_id, genre)
);

create table if not exists room
(
    id       uuid primary key default gen_random_uuid(),
    number   varchar(255),
    capacity int not null
);

create table if not exists seat
(
    id      uuid primary key default gen_random_uuid(),
    number  varchar(255),
    room_id uuid references room (id)
);

create table if not exists projection
(
    id         uuid primary key default gen_random_uuid(),
    movie_id   uuid references movie (id),
    room_id    uuid references room (id),
    datetime   timestamptz,
    seat_price numeric(10, 2)
);

create table if not exists reservation
(
    id            uuid primary key default gen_random_uuid(),
    created_at    timestamptz,
    status        reservation_status not null default 'PENDING',
    client_id     uuid references "user" (id),
    projection_id uuid references projection (id)
);

create table if not exists reservation_seat
(
    reservation_id uuid not null references reservation (id),
    seat_id        uuid not null references seat (id),
    primary key (reservation_id, seat_id)
);

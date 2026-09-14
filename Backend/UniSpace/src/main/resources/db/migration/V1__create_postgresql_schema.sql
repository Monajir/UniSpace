CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    program VARCHAR(255),
    semester INTEGER,
    section INTEGER
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(255) NOT NULL,
    CONSTRAINT user_roles_unique UNIQUE (user_id, role),
    CONSTRAINT user_roles_valid CHECK (role IN ('STUDENT', 'CR', 'FACULTY', 'ADMIN'))
);

CREATE TABLE profiles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    program VARCHAR(255),
    semester VARCHAR(255),
    role VARCHAR(255) NOT NULL,
    CONSTRAINT profiles_role_valid CHECK (role IN ('STUDENT', 'CR', 'FACULTY', 'ADMIN'))
);

CREATE TABLE classrooms (
    id UUID PRIMARY KEY,
    room_number VARCHAR(255) NOT NULL UNIQUE,
    building VARCHAR(255) NOT NULL,
    capacity INTEGER NOT NULL CHECK (capacity > 0),
    is_available BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE classroom_equipment (
    classroom_id UUID NOT NULL REFERENCES classrooms(id) ON DELETE CASCADE,
    equipment VARCHAR(255)
);

CREATE TABLE routines (
    id UUID PRIMARY KEY,
    classroom_id UUID NOT NULL REFERENCES classrooms(id) ON DELETE CASCADE,
    faculty_name VARCHAR(255) NOT NULL,
    course_code VARCHAR(255) NOT NULL,
    day_name VARCHAR(255) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    program VARCHAR(255),
    semester INTEGER,
    section INTEGER,
    CONSTRAINT routines_time_valid CHECK (end_time > start_time)
);

CREATE INDEX idx_routines_room_day ON routines(classroom_id, day_name);

CREATE TABLE bookings (
    id UUID PRIMARY KEY,
    classroom_id UUID NOT NULL REFERENCES classrooms(id) ON DELETE RESTRICT,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    faculty_email VARCHAR(255) NOT NULL,
    faculty_name VARCHAR(255) NOT NULL,
    reason VARCHAR(1000) NOT NULL,
    course_code VARCHAR(255),
    day_name VARCHAR(255) NOT NULL,
    booking_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status VARCHAR(255) NOT NULL,
    created_at DATE NOT NULL,
    approved_at DATE,
    CONSTRAINT bookings_status_valid CHECK (status IN ('PENDING', 'BOOKED', 'REJECTED')),
    CONSTRAINT bookings_time_valid CHECK (end_time > start_time),
    CONSTRAINT bookings_duration_valid CHECK (end_time - start_time <= INTERVAL '75 minutes')
);

CREATE INDEX idx_bookings_room_date_status ON bookings(classroom_id, booking_date, status);

ALTER TABLE bookings ADD CONSTRAINT bookings_no_approved_overlap
    EXCLUDE USING gist (
        classroom_id WITH =,
        tsrange(booking_date + start_time, booking_date + end_time, '[)') WITH &&
    ) WHERE (status = 'BOOKED');

CREATE TABLE pending_roles (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(255) NOT NULL,
    CONSTRAINT pending_roles_role_valid CHECK (role IN ('STUDENT', 'CR'))
);

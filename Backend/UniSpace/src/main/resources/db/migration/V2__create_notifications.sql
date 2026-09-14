CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    booking_id UUID REFERENCES bookings(id) ON DELETE SET NULL,
    type VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT notifications_type_valid CHECK (
        type IN ('BOOKING_REQUESTED', 'BOOKING_ASSIGNED', 'BOOKING_APPROVED', 'BOOKING_REJECTED')
    ),
    CONSTRAINT notifications_recipient_booking_type_unique UNIQUE (user_id, booking_id, type)
);

CREATE INDEX idx_notifications_user_created ON notifications(user_id, created_at DESC);

-- Preserve useful history when upgrading an existing database.
INSERT INTO notifications (id, user_id, booking_id, type, title, message, is_read, created_at)
SELECT
    gen_random_uuid(),
    b.user_id,
    b.id,
    'BOOKING_REQUESTED',
    'Booking request submitted',
    CONCAT(
        COALESCE(NULLIF(b.course_code, ''), 'Your classroom request'),
        ' for Room ', c.room_number, ' on ', b.booking_date,
        ' from ', TO_CHAR(b.start_time, 'HH24:MI'), ' to ', TO_CHAR(b.end_time, 'HH24:MI'),
        ' is awaiting review.'
    ),
    FALSE,
    b.created_at::timestamp AT TIME ZONE 'UTC'
FROM bookings b
JOIN classrooms c ON c.id = b.classroom_id
ON CONFLICT (user_id, booking_id, type) DO NOTHING;

INSERT INTO notifications (id, user_id, booking_id, type, title, message, is_read, created_at)
SELECT
    gen_random_uuid(),
    faculty.id,
    b.id,
    'BOOKING_ASSIGNED',
    'New booking request',
    CONCAT(
        COALESCE(NULLIF(b.course_code, ''), 'A classroom request'),
        ' for Room ', c.room_number, ' on ', b.booking_date,
        ' from ', TO_CHAR(b.start_time, 'HH24:MI'), ' to ', TO_CHAR(b.end_time, 'HH24:MI'),
        ' needs your review.'
    ),
    FALSE,
    b.created_at::timestamp AT TIME ZONE 'UTC'
FROM bookings b
JOIN classrooms c ON c.id = b.classroom_id
JOIN users faculty ON LOWER(faculty.email) = LOWER(b.faculty_email)
JOIN user_roles faculty_role ON faculty_role.user_id = faculty.id AND faculty_role.role = 'FACULTY'
ON CONFLICT (user_id, booking_id, type) DO NOTHING;

INSERT INTO notifications (id, user_id, booking_id, type, title, message, is_read, created_at)
SELECT
    gen_random_uuid(),
    b.user_id,
    b.id,
    CASE WHEN b.status = 'BOOKED' THEN 'BOOKING_APPROVED' ELSE 'BOOKING_REJECTED' END,
    CASE WHEN b.status = 'BOOKED' THEN 'Booking approved' ELSE 'Booking rejected' END,
    CONCAT(
        COALESCE(NULLIF(b.course_code, ''), 'Your classroom request'),
        ' for Room ', c.room_number, ' on ', b.booking_date,
        ' from ', TO_CHAR(b.start_time, 'HH24:MI'), ' to ', TO_CHAR(b.end_time, 'HH24:MI'),
        CASE WHEN b.status = 'BOOKED' THEN ' was approved.' ELSE ' was rejected.' END
    ),
    FALSE,
    COALESCE(b.approved_at, b.created_at)::timestamp AT TIME ZONE 'UTC'
FROM bookings b
JOIN classrooms c ON c.id = b.classroom_id
WHERE b.status IN ('BOOKED', 'REJECTED')
ON CONFLICT (user_id, booking_id, type) DO NOTHING;

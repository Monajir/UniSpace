# Presentation demo data

The `demo` Spring profile fills PostgreSQL with a connected, restart-safe dataset. Docker Compose enables this profile by default. The initializer adds missing demo records but never deletes or resets user-created data.

## Demo accounts

| Persona | Email | Password | Connected data and suggested use |
|---|---|---|---|
| Administrator | `admin@iut-dhaka.edu` | `admin12345` | Manage classrooms and routines, review two pending bookings, and review a CR role request |
| Student | `student@iut-dhaka.edu` | `student12345` | View the CSE semester 5, section 1 routine and classroom schedules |
| Class representative | `cr@iut-dhaka.edu` | `cr123456` | View the CSE routine, five connected bookings, and persistent submission/decision notifications with read state |
| Faculty | `faculty@iut-dhaka.edu` | `faculty12345` | Review the assigned pending seminar request and demonstrate personal assignment notifications, decision history, and live calendar updates |
| CR candidate | `candidate@iut-dhaka.edu` | `candidate12345` | View the EEE semester 3, section 2 routine; a pending CR request for this account is visible to the administrator |

If the administrator email or password is overridden in `.env`, use those configured values instead.

## Connected dataset

On a new database, the demo profile creates:

- 5 users and their 5 matching profiles.
- 5 classrooms across academic, laboratory, library, and administrative buildings, with capacities and equipment.
- 11 routine entries: 8 for CSE semester 5 section 1 and 3 for EEE semester 3 section 2.
- 5 bookings owned by the class representative: 2 approved, 2 pending, and 1 rejected.
- 1 pending CR role request owned by the EEE student.

Every routine points to a seeded classroom. Every booking points to both a seeded classroom and the class representative. Booking dates are generated for the next teaching week, so the schedule remains useful whenever the stack is started.

## Suggested presentation flow

1. Log in as the student and show that the routine is selected from the account's program, semester, and section.
2. Open a classroom schedule to show routine entries and approved bookings attached to that room.
3. Log in as the class representative and show pending, approved, and rejected booking history.
4. Log in as the administrator, approve or reject a pending booking, and show the changed state from the class representative account.
5. Review the candidate's pending CR role request and demonstrate role approval.
6. Show classroom equipment and capacity, then add or edit a classroom or routine to demonstrate administration.

## Restart behavior

The seed process uses stable account emails and natural keys for classrooms, routines, bookings, and requests. Restarting the backend does not duplicate the current presentation dataset. If the database volume is removed, the complete dataset is recreated on the next `docker compose up`.

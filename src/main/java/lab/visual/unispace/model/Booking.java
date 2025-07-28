package lab.visual.unispace.model;


public class Booking {
    private Classroom classroom;
    private String user;
    private String date;
    private String timeSlot; // e.g., "8:00-9:15"

    public Booking(Classroom classroom, String user, String date, String timeSlot) {
        this.classroom = classroom;
        this.user = user;
        this.date = date;
        this.timeSlot = timeSlot;
    }

    // getters and setters...
}

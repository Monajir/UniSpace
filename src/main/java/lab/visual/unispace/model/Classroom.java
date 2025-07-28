package lab.visual.unispace.model;

public class Classroom {
    private String building;
    private String room;
    private String name;

    public Classroom(String name, String building, String room) {
        this.name = name;
        this.building = building;
        this.room = room;
    }

    public String getBuilding() { return building; }
    public String getRoom() { return room; }
    public String getName() { return name; }

    @Override
    public String toString() {
        return name + " (" + building + "-" + room + ")";
    }
}

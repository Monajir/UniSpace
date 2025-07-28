package lab.visual.unispace.utils;

import lab.visual.unispace.model.*;
import java.util.*;

public class DataStore {
    private static List<Classroom> classrooms = new ArrayList<>();
    private static List<User> users = new ArrayList<>();

    static {
        classrooms.add(new Classroom("Physics Lab", "A", "101"));
        classrooms.add(new Classroom("Chem Lab", "A", "102"));
        classrooms.add(new Classroom("Comp Sci", "B", "201"));
        users.add(new User("admin", "admin"));
    }

    public static List<Classroom> getClassrooms() { return classrooms; }
    public static List<User> getUsers() { return users; }

    public static User authenticate(String username, String password) {
        return users.stream()
                .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
                .findFirst().orElse(null);
    }

    public static boolean addUser(String username, String password) {
        if (users.stream().anyMatch(u -> u.getUsername().equals(username))) return false;
        users.add(new User(username, password));
        return true;
    }
}

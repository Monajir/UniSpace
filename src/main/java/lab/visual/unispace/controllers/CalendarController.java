package lab.visual.unispace.controllers;

import javafx.scene.Parent;
import javafx.scene.Scene;
import lab.visual.unispace.model.Classroom;

import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.Random;

public class CalendarController {
    @FXML private Label classInfoLabel;
    @FXML private GridPane calendarGrid;

    private static final String[] TIMES = {
            "8:00 AM", "9:15 AM", "10:30 AM", "11:45 AM", "1:00 PM", "2:15 PM", "3:30 PM", "4:45 PM"
    };
    private static final String[] DAYS = {
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"
    };

    private Random random = new Random();

    public void setClassroom(Classroom classroom) {
        classInfoLabel.setText(classroom.getName() + " (" + classroom.getBuilding() + "-" + classroom.getRoom() + ")");
        populateGrid();
    }

    private void populateGrid() {
        calendarGrid.getChildren().clear();

        // Empty top-left cell
        Label emptyCell = new Label("");
        calendarGrid.add(emptyCell, 0, 0);

        // Time headers
        for (int x = 0; x < TIMES.length; x++) {
            Label timeHeader = new Label(TIMES[x]);
            timeHeader.getStyleClass().add("calendar-header");
            timeHeader.setPrefSize(120, 50);
            calendarGrid.add(timeHeader, x + 1, 0);
        }

        // Day headers and slots
        for (int y = 0; y < DAYS.length; y++) {
            Label dayHeader = new Label(DAYS[y]);
            dayHeader.getStyleClass().add("calendar-header");
            dayHeader.setPrefSize(100, 60);
            calendarGrid.add(dayHeader, 0, y + 1);

            for (int x = 0; x < TIMES.length; x++) {
                VBox slot = createTimeSlot();
                calendarGrid.add(slot, x + 1, y + 1);
            }
        }
    }

    private VBox createTimeSlot() {
        VBox slot = new VBox(4);
        slot.getStyleClass().add("calendar-cell");
        slot.setPrefSize(120, 60);

        // Randomly assign status for demo
        String[] statuses = {"Available", "Booked", "Pending"};
        String[] statusClasses = {"status-available", "status-booked", "status-pending"};

        int statusIndex = random.nextInt(statuses.length);
        String status = statuses[statusIndex];
        String statusClass = statusClasses[statusIndex];

        Label statusLabel = new Label(status);
        statusLabel.getStyleClass().add(statusClass);

        if (!status.equals("Available")) {
            Label bookingInfo = new Label("CSE 101");
            bookingInfo.getStyleClass().add("body-text");
            slot.getChildren().addAll(statusLabel, bookingInfo);
        } else {
            slot.getChildren().add(statusLabel);
        }

        return slot;
    }

    @FXML
    public void onBack() throws Exception {
        Stage stage = (Stage) classInfoLabel.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/lab/visual/unispace/home.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/lab/visual/unispace/style.css").toExternalForm());

        // Prev stage X, Y
        double x = stage.getX();
        double y = stage.getY();

        double width = stage.getWidth();
        double height = stage.getHeight();

        stage.setScene(scene);

        // Restore position
        stage.setX(x);
        stage.setY(y);
        stage.setWidth(width);
        stage.setHeight(height);

    }
}

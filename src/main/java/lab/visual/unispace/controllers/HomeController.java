package lab.visual.unispace.controllers;

import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lab.visual.unispace.model.Classroom;
import lab.visual.unispace.utils.DataStore;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.stream.Collectors;


public class HomeController implements Initializable {
    @FXML private ListView<Classroom> classroomList;
    @FXML private TextField searchField;
    @FXML private Label roomCountLabel;

    private ObservableList<Classroom> classrooms;

    @Override
    public void initialize(java.net.URL location, java.util.ResourceBundle resources) {
        classrooms = FXCollections.observableArrayList(DataStore.getClassrooms());
        classroomList.setItems(classrooms);
        updateRoomCount();

        // Custom cell factory for modern cards
        classroomList.setCellFactory(listView -> new ListCell<Classroom>() {
            @Override
            protected void updateItem(Classroom classroom, boolean empty) {
                super.updateItem(classroom, empty);
                if (empty || classroom == null) {
                    setGraphic(null);
                } else {
                    VBox card = new VBox(8);

                    Label nameLabel = new Label(classroom.getName());
                    nameLabel.getStyleClass().add("subtitle");

                    Label locationLabel = new Label("Building " + classroom.getBuilding() + " • Room " + classroom.getRoom());
                    locationLabel.getStyleClass().add("body-text");

                    HBox statusBox = new HBox(8);
                    Label statusLabel = new Label("Available");
                    statusLabel.getStyleClass().add("status-available");
                    statusBox.getChildren().add(statusLabel);

                    card.getChildren().addAll(nameLabel, locationLabel, statusBox);

                    setGraphic(card);
                }
            }
        });

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            ObservableList<Classroom> filtered = FXCollections.observableArrayList(
                    classrooms.stream().filter(c ->
                                    c.getBuilding().toLowerCase().contains(newValue.toLowerCase()) ||
                                            c.getRoom().toLowerCase().contains(newValue.toLowerCase()) ||
                                            c.getName().toLowerCase().contains(newValue.toLowerCase()))
                            .collect(Collectors.toList())
            );
            classroomList.setItems(filtered);
            updateRoomCount(filtered.size());
        });

        classroomList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && classroomList.getSelectionModel().getSelectedItem() != null) {
                openCalendar(classroomList.getSelectionModel().getSelectedItem());
            }
        });
    }

    private void updateRoomCount() {
        updateRoomCount(classrooms.size());
    }

    private void updateRoomCount(int count) {
        roomCountLabel.setText(count + " room" + (count != 1 ? "s" : "") + " found");
    }

    @FXML
    public void onLoginButton() throws IOException {
        Stage stage = (Stage) searchField.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/lab/visual/unispace/login.fxml"));
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

    @FXML
    private void openCalendar(Classroom classroom) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lab/visual/unispace/calendar.fxml"));
            Parent root = loader.load();
            CalendarController controller = loader.getController();
            controller.setClassroom(classroom);

            Stage stage = (Stage) searchField.getScene().getWindow();
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


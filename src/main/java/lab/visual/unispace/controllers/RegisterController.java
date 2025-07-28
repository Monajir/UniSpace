package lab.visual.unispace.controllers;

import lab.visual.unispace.utils.DataStore;
import javafx.fxml.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;

    @FXML
    public void onRegister() {
        String u = usernameField.getText();
        String p1 = passwordField.getText();
        String p2 = confirmPasswordField.getText();

        if (u.isEmpty() || p1.isEmpty() || p2.isEmpty()) {
            errorLabel.setText("All fields required.");
            return;
        }
        if (!p1.equals(p2)) {
            errorLabel.setText("Passwords do not match.");
            return;
        }
        if (DataStore.addUser(u, p1)) {
            goToLogin();
        } else {
            errorLabel.setText("Username already taken.");
        }
    }

    @FXML
    public void onBack() throws Exception {
        goToLogin();
    }

    @FXML
    private void goToLogin() {
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/lab/visual/unispace/login.fxml"));
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

    @FXML
    private void goToHome() {
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/lab/visual/unispace/home.fxml"));
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

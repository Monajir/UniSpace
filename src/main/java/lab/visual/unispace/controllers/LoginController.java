package lab.visual.unispace.controllers;

import lab.visual.unispace.utils.DataStore;
import javafx.fxml.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    public void onLogin() {
        String username = usernameField.getText();
        String pw = passwordField.getText();

        if (DataStore.authenticate(username, pw) != null) {
            goToHome();
        } else {
            errorLabel.setText("Invalid credentials!");
        }
    }

    @FXML
    public void onRegister() throws Exception {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("/lab/visual/unispace/register.fxml"));
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

package controllers;

import utils.AlertUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController extends BaseController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            AlertUtils.showError("Username dan password harus diisi!");
            return;
        }

        if (authService.login(username, password)) {
            String fxmlPath = authService.isAdmin() ? "/views/admin_dashboard.fxml" : "/views/user_dashboard.fxml";
            loadScene(fxmlPath, event);
        } else {
            AlertUtils.showError("Username atau password salah!");
        }
    }

    @FXML
    private void handleRegisterLink(ActionEvent event) {
        loadScene("/views/register.fxml", event);
    }
}
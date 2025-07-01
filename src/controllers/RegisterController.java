package controllers;

import utils.AlertUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController extends BaseController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;

    @FXML
    private void handleRegister(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            AlertUtils.showError("Semua field harus diisi!");
            return;
        }

        if (authService.register(username, password, email, phone, false)) {
            AlertUtils.showInfo("Registrasi berhasil! Silakan login.");
            handleLoginLink(event);
        } else {
            AlertUtils.showError("Registrasi gagal. Username atau email mungkin sudah digunakan.");
        }
    }

    @FXML
    private void handleLoginLink(ActionEvent event) {
        loadScene("/views/login.fxml", event);
    }
}
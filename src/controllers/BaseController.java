package controllers;

import services.AuthService;
import services.MessageService;
import services.ReportService;
import utils.AlertUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public abstract class BaseController {

    protected final AuthService authService = AuthService.getInstance();
    protected final ReportService reportService = new ReportService();
    protected final MessageService messageService = new MessageService();

    protected Stage getCurrentStage(Node node) {
        return (Stage) node.getScene().getWindow();
    }

    protected void loadScene(String fxmlPath, Node anyNode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = getCurrentStage(anyNode);
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Gagal memuat halaman: " + fxmlPath);
        }
    }

    @Deprecated
    protected void loadScene(String fxmlPath, ActionEvent event) {
        Object source = event.getSource();
        if (source instanceof Node) {
            loadScene(fxmlPath, (Node) source);
        } else {
            System.err.println("Cannot change scene from a non-Node event source. Pass a Node manually.");
            AlertUtils.showError("Internal error: Cannot determine current window.");
        }
    }
}
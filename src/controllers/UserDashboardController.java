package controllers;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import models.Message;
import models.Report;
import models.User;
import utils.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class UserDashboardController extends BaseController implements ReportItemController.ReportItemActionCallback {
    @FXML
    private ListView<Report> reportsListView;
    @FXML
    private WebView mapWebView;

    private static final DateTimeFormatter POPUP_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    @FXML
    public void initialize() {
        loadUserReports();
        initializeMap();
    }

    private void loadUserReports() {
        User currentUser = authService.getCurrentUser();
        if (currentUser != null) {
            List<Report> reports = reportService.getUserReports(currentUser.getId());
            ObservableList<Report> observableList = FXCollections.observableArrayList(reports);
            reportsListView.setItems(observableList);
            reportsListView.setCellFactory(ReportListCell.forListView(this));
        }
    }

    private void initializeMap() {
        WebEngine webEngine = mapWebView.getEngine();
        webEngine.load(getClass().getResource("/maps/map.html").toExternalForm());

        // Wait for the map to fully load before trying to add markers
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                // Once the map is ready, add a marker for each report
                populateMapWithMarkers();
            }
        });
    }

    private void populateMapWithMarkers() {
        WebEngine webEngine = mapWebView.getEngine();

        // Clear existing markers first to prevent duplicates on refresh
        webEngine.executeScript("if(window.mapFunctions) { window.mapFunctions.clearMarkers(); }");

        for (Report report : reportsListView.getItems()) {
            if (report.getLatitude() != null && report.getLongitude() != null) {
                String location = escapeJavaScriptString(report.getFloodLocation());
                String status = escapeJavaScriptString(report.getStatus());
                String date = escapeJavaScriptString(report.getCreatedAt().format(POPUP_DATE_FORMATTER));

                String popupContent = String.format("<strong>%s</strong><br>Status: %s<br>Tanggal: %s", location, status, date);

                String script = String.format("if(window.mapFunctions) { window.mapFunctions.addMarker(%f, %f, '%s'); }",
                        report.getLatitude(), report.getLongitude(), popupContent);

                webEngine.executeScript(script);
            }
        }
    }

    private String escapeJavaScriptString(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"").replace("\n", "<br>").replace("\r", "");
    }

    private void refreshUI() {
        loadUserReports();
        populateMapWithMarkers();
    }

    @FXML
    private void handleCreateReport(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/create_report.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Buat Laporan Banjir Baru");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            refreshUI(); // Refresh both list and map
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Gagal memuat form laporan");
        }
    }

    @Override
    public void onEditReport(Report report) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/edit_report.fxml"));
            Parent root = loader.load();
            EditReportController editController = loader.getController();
            editController.setReport(report);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Edit Laporan Banjir");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            refreshUI(); // Refresh both list and map
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Gagal memuat form edit laporan");
        }
    }

    @Override
    public void onDeleteReport(Report report) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Konfirmasi Hapus");
        confirmAlert.setHeaderText("Hapus Laporan Banjir");
        confirmAlert.setContentText("Apakah Anda yakin ingin menghapus laporan ini?\n\nLokasi: " + report.getFloodLocation());

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (reportService.deleteReport(report.getId())) {
                AlertUtils.showInfo("Laporan berhasil dihapus");
                refreshUI(); // Refresh both list and map
            } else {
                AlertUtils.showError("Gagal menghapus laporan");
            }
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Konfirmasi Logout");
        confirmAlert.setHeaderText("Keluar dari Aplikasi");
        confirmAlert.setContentText("Apakah Anda yakin ingin keluar?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            authService.logout();

            loadScene("/views/login.fxml", reportsListView);
        }
    }
    @Override
    public void onViewMessages(Report report) {
        if (report == null) return;

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Riwayat Pesan");
        dialog.setHeaderText("Pesan untuk Laporan: " + report.getFloodLocation());

        VBox vbox = new VBox();
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(20, 20, 10, 20));

        TextArea messageHistory = new TextArea();
        messageHistory.setEditable(false);
        messageHistory.setPrefRowCount(10);
        messageHistory.setPrefWidth(350);

        List<Message> messages = messageService.getMessagesByReportId(report.getId());
        if (messages.isEmpty()) {
            messageHistory.setText("Belum ada pesan dari admin.");
        } else {
            StringBuilder historyText = new StringBuilder();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
            for (Message msg : messages) {
                // Tampilkan pesan dari admin saja
                if (msg.isAdminMessage()) {
                    historyText.append(msg.getMessage()).append("\n\n");
                }
            }
            messageHistory.setText(historyText.length() > 0 ? historyText.toString() : "Belum ada pesan dari admin.");
        }

        vbox.getChildren().add(messageHistory);
        dialog.getDialogPane().setContent(vbox);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.showAndWait();
    }
}
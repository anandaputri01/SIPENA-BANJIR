package controllers;

import models.Report;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class ReportItemController {
    @FXML
    private VBox view;
    @FXML
    private ImageView photoView;
    @FXML
    private Label locationLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private Label reporterLabel;
    @FXML
    private Label dateLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button editStatusButton;
    @FXML
    private Button deleteButtonAdmin;
    @FXML
    private Button viewDetailButton;
    @FXML
    private Button viewMessagesButton;

    private Report currentReport;
    private ReportItemActionCallback actionCallback;
    private AdminReportActionCallback adminActionCallback;
    private boolean isUserMode = true;

    // Interface tidak perlu diubah
    public interface ReportItemActionCallback {
        void onEditReport(Report report);
        void onDeleteReport(Report report);
        void onViewMessages(Report report);
    }

    public interface AdminReportActionCallback {
        void onEditStatus(Report report);
        void onViewDetail(Report report);
        void onDeleteReport(Report report);
    }

    public ReportItemController() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/report_item.fxml"));
        loader.setController(this);
        try {
            view = loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load report item FXML", e);
        }
    }

    // setReport() dan metode lain tidak perlu diubah
    public void setReport(Report report) {
        this.currentReport = report;
        if (report.getPhotoPath() != null && !report.getPhotoPath().isEmpty()) {
            try {
                File imageFile = new File(report.getPhotoPath());
                if (imageFile.exists()) {
                    photoView.setImage(new Image(imageFile.toURI().toString()));
                } else {
                    setDefaultImage();
                }
            } catch (Exception e) {
                System.err.println("Error loading image from: " + report.getPhotoPath());
                setDefaultImage();
            }
        } else {
            setDefaultImage();
        }
        locationLabel.setText(report.getFloodLocation());
        descriptionLabel.setText(report.getDescription());
        reporterLabel.setText("Dilaporkan oleh: " + report.getReporterName());
        if (report.getCreatedAt() != null) {
            dateLabel.setText(report.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")));
        } else {
            dateLabel.setText("Tanggal tidak tersedia");
        }
        setStatusLabel(report.getStatus());
        updateButtonVisibility();
    }

    // ... setActionCallback, setAdminActionCallback, setUserMode tidak berubah ...
    public void setActionCallback(ReportItemActionCallback callback) {
        this.actionCallback = callback;
    }
    public void setAdminActionCallback(AdminReportActionCallback callback) {
        this.adminActionCallback = callback;
    }
    public void setUserMode(boolean isUserMode) {
        this.isUserMode = isUserMode;
        updateButtonVisibility();
    }

    /**
     * PERBAIKAN UTAMA 1: Memperbaiki logika untuk menampilkan/menyembunyikan tombol.
     */
    private void updateButtonVisibility() {
        if (currentReport == null) return;

        if (isUserMode) {
            boolean canModify = "PENDING".equalsIgnoreCase(currentReport.getStatus());
            editButton.setVisible(canModify);
            editButton.setManaged(canModify);
            deleteButton.setVisible(canModify);
            deleteButton.setManaged(canModify);
            viewMessagesButton.setVisible(true);
            viewMessagesButton.setManaged(true);
            viewDetailButton.setVisible(false);
            viewDetailButton.setManaged(false);
            editStatusButton.setVisible(false);
            editStatusButton.setManaged(false);
            deleteButtonAdmin.setVisible(false);
            deleteButtonAdmin.setManaged(false);
        } else {

            editButton.setVisible(false);
            editButton.setManaged(false);
            deleteButton.setVisible(false);
            deleteButton.setManaged(false);
            viewMessagesButton.setVisible(false);
            viewMessagesButton.setManaged(false);

            viewDetailButton.setVisible(true);
            viewDetailButton.setManaged(true);

            editStatusButton.setVisible(true);
            editStatusButton.setManaged(true);
            deleteButtonAdmin.setVisible(true);
            deleteButtonAdmin.setManaged(true);
        }
    }

    @FXML
    private void handleEditReport() {
        if (actionCallback != null && currentReport != null) {
            actionCallback.onEditReport(currentReport);
        }
    }

    // Metode ini HANYA untuk tombol hapus user
    @FXML
    private void handleDeleteReport() {
        if (actionCallback != null && currentReport != null) {
            actionCallback.onDeleteReport(currentReport);
        }
    }

    // Metode ini HANYA untuk tombol edit status admin
    @FXML
    private void handleEditStatus() {
        if (adminActionCallback != null && currentReport != null) {
            adminActionCallback.onEditStatus(currentReport);
        }
    }

    /**
     * PERBAIKAN UTAMA 2: Metode baru khusus untuk tombol hapus admin.
     * Metode ini terhubung ke onAction="#handleAdminDeleteReport" di FXML.
     */
    @FXML
    private void handleAdminDeleteReport() {
        if (adminActionCallback != null && currentReport != null) {
            // Memanggil callback yang benar (milik admin)
            adminActionCallback.onDeleteReport(currentReport);
        }
    }
    @FXML
    private void handleViewDetail() {
        if (adminActionCallback != null && currentReport != null) {
            adminActionCallback.onViewDetail(currentReport);
        }
    }
    @FXML
    private void handleViewMessages() {
        if (actionCallback != null && currentReport != null) {
            actionCallback.onViewMessages(currentReport);
        }
    }

    // ... setDefaultImage dan setStatusLabel tidak berubah ...
    private void setDefaultImage() {
        try {
            var defaultImageStream = getClass().getResourceAsStream("/images/no-image.png");
            if (defaultImageStream != null) {
                photoView.setImage(new Image(defaultImageStream));
            } else {
                photoView.setVisible(false);
                photoView.setManaged(false);
            }
        } catch (Exception e) {
            photoView.setVisible(false);
            photoView.setManaged(false);
        }
    }
    private void setStatusLabel(String status) {
        statusLabel.getStyleClass().removeIf(styleClass ->
                styleClass.startsWith("report-status-"));
        String statusText = "Status: ";
        String statusClass = "report-status-";
        if (status != null) {
            switch (status.toUpperCase()) {
                case "PENDING":
                    statusText += "Menunggu";
                    statusClass += "pending";
                    break;
                case "REVIEWED":
                    statusText += "Sedang Ditinjau";
                    statusClass += "reviewed";
                    break;
                case "RESOLVED":
                    statusText += "Selesai";
                    statusClass += "resolved";
                    break;
                default:
                    statusText += status;
                    statusClass += "unknown";
                    break;
            }
        } else {
            statusText += "Tidak diketahui";
            statusClass += "unknown";
        }
        statusLabel.setText(statusText);
        statusLabel.getStyleClass().add(statusClass);
    }
    public VBox getView() {
        return view;
    }
}
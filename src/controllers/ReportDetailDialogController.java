package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import models.Report;
import java.io.File;
import java.time.format.DateTimeFormatter;

public class ReportDetailDialogController extends BaseMapController {

    @FXML private Label locationLabel;
    @FXML private Label reportIdLabel;
    @FXML private Label statusLabel;
    @FXML private Label dateLabel;
    @FXML private Label reporterNameLabel;
    @FXML private Label reporterPhoneLabel;
    @FXML private Label coordinatesLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label adminNotesLabel;
    @FXML private ImageView photoView;

    private Report currentReport;

    @FXML
    public void initialize() {
        // Panggil inisialisasi peta dari BaseMapController
        super.initializeMap();
    }

    public void setReport(Report report) {
        this.currentReport = report;
        displayReportDetails();
    }

    private void displayReportDetails() {
        if (currentReport == null) return;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

        locationLabel.setText(currentReport.getFloodLocation());
        reportIdLabel.setText(String.valueOf(currentReport.getId()));
        statusLabel.setText(currentReport.getStatus());
        dateLabel.setText(currentReport.getCreatedAt().format(formatter));
        reporterNameLabel.setText(currentReport.getReporterName());
        reporterPhoneLabel.setText(currentReport.getReporterPhone());
        coordinatesLabel.setText(String.format("%.6f, %.6f", currentReport.getLatitude(), currentReport.getLongitude()));
        descriptionLabel.setText(currentReport.getDescription());
        adminNotesLabel.setText(currentReport.getAdminNotes() != null && !currentReport.getAdminNotes().isEmpty() ? currentReport.getAdminNotes() : "Tidak ada catatan.");

        // Tampilkan foto
        if (currentReport.getPhotoPath() != null && !currentReport.getPhotoPath().isEmpty()) {
            File imageFile = new File(currentReport.getPhotoPath());
            if (imageFile.exists()) {
                photoView.setImage(new Image(imageFile.toURI().toString()));
            }
        }
    }

    // Metode ini akan dipanggil setelah peta selesai dimuat
    @Override
    protected void onMapReady() {
        if (webEngine != null && currentReport != null) {
            System.out.println("Map is ready. Centering on report location.");
            // Beri sedikit jeda agar JavaScript siap
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(500));
            pause.setOnFinished(event -> {
                String script = String.format(
                        "if(window.mapFunctions) { window.mapFunctions.clearMarkers(); window.mapFunctions.addMarker(%f, %f, '%s'); window.mapFunctions.setView(%f, %f, 16); }",
                        currentReport.getLatitude(),
                        currentReport.getLongitude(),
                        "Lokasi Laporan",
                        currentReport.getLatitude(),
                        currentReport.getLongitude()
                );
                webEngine.executeScript(script);
            });
            pause.play();
        }
    }
}
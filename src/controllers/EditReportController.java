package controllers;

import models.Report;
import utils.AlertUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class EditReportController extends BaseMapController {

    @FXML private TextField reporterNameField;
    @FXML private TextField reporterPhoneField;
    @FXML private TextField floodLocationField;
    @FXML private TextArea descriptionArea;
    @FXML private ImageView photoPreview;
    @FXML private Button selectPhotoButton;

    private Report currentReport;
    private String selectedPhotoPath;
    private boolean initialLocationSet = false;

    @FXML
    public void initialize() {
        System.out.println("Initializing EditReportController...");
        photoPreview.setPreserveRatio(true);
        photoPreview.setFitWidth(200);
        photoPreview.setFitHeight(150);

        // This single call replaces all the map setup code.
        super.initializeMap();
    }

    @Override
    protected void onMapReady() {
        // If the report data was set before the map finished loading, set the location now.
        if (currentReport != null && !initialLocationSet) {
            setInitialMapLocation();
        }
    }

    /**
     * Public method to pass the report data from the dashboard to this controller.
     * @param report The Report object to be edited.
     */
    public void setReport(Report report) {
        this.currentReport = report;
        this.selectedLat = report.getLatitude();
        this.selectedLng = report.getLongitude();
        this.selectedPhotoPath = report.getPhotoPath();

        populateFields();

        // If the map is already ready when this method is called, set the location.
        if (bridgeEstablished && !initialLocationSet) {
            Platform.runLater(this::setInitialMapLocation);
        }
    }

    /**
     * Executes a JavaScript command to center the map and place a marker
     * at the report's existing coordinates.
     */
    private void setInitialMapLocation() {
        if (!bridgeEstablished || currentReport == null || initialLocationSet) {
            return;
        }

        try {
            double lat = currentReport.getLatitude();
            double lng = currentReport.getLongitude();
            System.out.printf("Setting initial map location: Lat=%.6f, Lng=%.6f%n", lat, lng);

            // Command to set map view and add a marker via JavaScript functions in map.html
            String script = String.format(
                    "if (window.mapFunctions) { " +
                            "  window.mapFunctions.setView(%f, %f, 15); " +
                            "  window.mapFunctions.addMarker(%f, %f, 'Current Report Location'); " +
                            "}",
                    lat, lng, lat, lng
            );
            webEngine.executeScript(script);

            initialLocationSet = true;

        } catch (Exception e) {
            System.err.println("Error setting initial map location: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Fills the UI fields with data from the current report.
     */
    private void populateFields() {
        if (currentReport != null) {
            reporterNameField.setText(currentReport.getReporterName());
            reporterPhoneField.setText(currentReport.getReporterPhone());
            floodLocationField.setText(currentReport.getFloodLocation());
            descriptionArea.setText(currentReport.getDescription());

            // Set coordinates field (inherited from BaseMapController)
            String coordText = String.format("%.6f, %.6f", currentReport.getLatitude(), currentReport.getLongitude());
            coordinatesField.setText(coordText);

            // Display current photo if available
            if (selectedPhotoPath != null && !selectedPhotoPath.isEmpty()) {
                loadPhotoPreview(selectedPhotoPath);
            }
        }
    }

    /**
     * Handles the action of selecting a new photo for the report.
     */
    @FXML
    private void handleSelectPhoto(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih Foto Banjir");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        // Use the helper method from BaseController to get the stage
        Stage stage = getCurrentStage(selectPhotoButton);
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                Path uploadsDir = Paths.get("uploads");
                if (!Files.exists(uploadsDir)) {
                    Files.createDirectories(uploadsDir);
                }

                String fileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                Path targetPath = uploadsDir.resolve(fileName);
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                selectedPhotoPath = targetPath.toString();
                loadPhotoPreview(selectedPhotoPath);

            } catch (IOException e) {
                e.printStackTrace();
                AlertUtils.showError("Gagal mengunggah foto: " + e.getMessage());
            }
        }
    }

    /**
     * Loads an image from the given path into the photo preview ImageView.
     */
    private void loadPhotoPreview(String photoPath) {
        try {
            File imageFile = new File(photoPath);
            if (imageFile.exists()) {
                photoPreview.setImage(new Image(imageFile.toURI().toString()));
            } else {
                System.err.println("Photo file not found at: " + photoPath);
            }
        } catch (Exception e) {
            System.err.println("Error loading photo preview: " + e.getMessage());
        }
    }

    /**
     * Handles the save button action. Validates input, updates the report object,
     * and calls the service to persist changes.
     */
    @FXML
    private void handleSave(ActionEvent event) {
        if (!validateFields()) {
            return;
        }

        try {
            // Update the report object with values from the form
            currentReport.setReporterName(reporterNameField.getText().trim());
            currentReport.setReporterPhone(reporterPhoneField.getText().trim());
            currentReport.setFloodLocation(floodLocationField.getText().trim());
            currentReport.setDescription(descriptionArea.getText().trim());
            currentReport.setPhotoPath(selectedPhotoPath);

            // The latitude and longitude are updated automatically by the JavaScript bridge
            // and are stored in the inherited 'selectedLat' and 'selectedLng' fields.
            if (selectedLat != null && selectedLng != null) {
                currentReport.setLatitude(selectedLat);
                currentReport.setLongitude(selectedLng);
            } else {
                AlertUtils.showError("Koordinat tidak valid. Silakan pilih lagi di peta.");
                return;
            }

            // Use the inherited reportService to update the report in the database
            if (reportService.updateReport(currentReport)) {
                AlertUtils.showInfo("Laporan berhasil diperbarui");
                closeWindow();
            } else {
                AlertUtils.showError("Gagal memperbarui laporan. (Laporan mungkin tidak lagi berstatus 'Pending')");
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Terjadi kesalahan saat menyimpan: " + e.getMessage());
        }
    }

    /**
     * Handles the cancel button action, closing the window.
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        closeWindow();
    }

    /**
     * Validates that all required fields are filled.
     * @return true if all fields are valid, false otherwise.
     */
    private boolean validateFields() {
        if (reporterNameField.getText().trim().isEmpty() ||
                reporterPhoneField.getText().trim().isEmpty() ||
                floodLocationField.getText().trim().isEmpty() ||
                coordinatesField.getText().trim().isEmpty() ||
                descriptionArea.getText().trim().isEmpty()) {
            AlertUtils.showError("Semua kolom harus diisi.");
            return false;
        }
        return true;
    }

    /**
     * Closes the current window.
     */
    private void closeWindow() {
        Stage stage = getCurrentStage(reporterNameField);
        stage.close();
    }
}
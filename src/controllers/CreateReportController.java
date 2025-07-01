package controllers;

import models.Report;
import models.User;
import utils.AlertUtils;
import utils.FileUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import java.io.File;

public class CreateReportController extends BaseMapController {
    @FXML private TextField floodLocationField;
    @FXML private TextArea descriptionField;
    @FXML private ImageView photoPreview;

    private String photoPath;

    @FXML
    public void initialize() {
        System.out.println("Initializing CreateReportController...");
        super.initializeMap();

        // Initialize coordinates field (inherited from BaseMapController)
        if (coordinatesField != null) {
            coordinatesField.setEditable(false);
            coordinatesField.setText("Klik pada peta untuk memilih lokasi...");
        }
    }

    @Override
    protected void onMapReady() {
        System.out.println("CreateReportController: Map is ready");
        if (bridgeEstablished) {
            syncLocationFromJavaScript();
        }
    }

    private void syncLocationFromJavaScript() {
        if (!bridgeEstablished) {
            System.out.println("Bridge not established, cannot sync location");
            return;
        }

        try {
            // Force location update from JavaScript
            Object result = webEngine.executeScript(
                    "(function() {" +
                            "  console.log('Java requesting location sync...');" +
                            "  if (window.mapFunctions && window.mapFunctions.forceLocationUpdate) {" +
                            "    console.log('Calling forceLocationUpdate...');" +
                            "    return window.mapFunctions.forceLocationUpdate();" +
                            "  } else if (window.mapFunctions && window.mapFunctions.sendLocationToJava) {" +
                            "    console.log('Calling sendLocationToJava...');" +
                            "    return window.mapFunctions.sendLocationToJava();" +
                            "  } else {" +
                            "    console.log('mapFunctions not available yet');" +
                            "    return false;" +
                            "  }" +
                            "})()"
            );
            System.out.println("Sync result: " + result);
        } catch (Exception e) {
            System.err.println("Error syncing location from JavaScript: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddPhoto(ActionEvent event) {
        Stage stage = getCurrentStage(photoPreview);
        String path = FileUtils.chooseImageFile(stage);
        if (path != null) {
            photoPath = path;
            File imageFile = new File(path);
            if (imageFile.exists()) {
                photoPreview.setImage(new Image(imageFile.toURI().toString()));
            }
        }
    }

    @FXML
    private void handleSubmit(ActionEvent event) {
        // Validate flood location
        String floodLocation = floodLocationField.getText();
        if (floodLocation == null || floodLocation.trim().isEmpty()) {
            AlertUtils.showError("Silakan masukkan nama lokasi banjir!");
            return;
        }

        // Validate coordinates
        if (selectedLat == null || selectedLng == null) {
            AlertUtils.showError("Silakan pilih lokasi pada peta terlebih dahulu!");
            return;
        }

        // Validate description
        String description = descriptionField.getText();
        if (description == null || description.trim().isEmpty()) {
            AlertUtils.showError("Silakan berikan deskripsi kondisi banjir!");
            return;
        }

        // Check user authentication
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            AlertUtils.showError("User tidak login!");
            return;
        }

        // Create and submit report
        Report report = new Report(
                0,
                currentUser.getId(),
                currentUser.getUsername(),
                currentUser.getPhone(),
                floodLocation.trim(),
                selectedLat,
                selectedLng,
                description,
                photoPath,
                "PENDING",
                null, null, null
        );

        if (reportService.createReport(report)) {
            AlertUtils.showInfo("Laporan berhasil dikirim!");
            closeWindow();
        } else {
            AlertUtils.showError("Gagal mengirim laporan");
        }
    }

    private void closeWindow() {
        Stage stage = getCurrentStage(floodLocationField);
        if (stage != null) {
            stage.close();
        }
    }

    // Add method to manually refresh location if needed
    @FXML
    private void handleRefreshLocation(ActionEvent event) {
        if (bridgeEstablished) {
            syncLocationFromJavaScript();
        } else {
            AlertUtils.showWarning("Peta belum siap. Silakan tunggu sebentar dan coba lagi.");
        }
    }

    // Debug method to check current state
    @FXML
    private void handleDebugLocation(ActionEvent event) {
        System.out.println("=== DEBUG LOCATION ===");
        System.out.println("Bridge established: " + bridgeEstablished);
        System.out.println("Selected coordinates: " + selectedLat + ", " + selectedLng);
        System.out.println("Coordinates field text: " + (coordinatesField != null ? coordinatesField.getText() : "null"));

        if (bridgeEstablished) {
            try {
                // Get location from JavaScript
                Object jsLocation = webEngine.executeScript(
                        "window.mapFunctions ? window.mapFunctions.getSelectedLocation() : null"
                );
                System.out.println("JavaScript location object: " + jsLocation);

                // Try to get specific values
                if (jsLocation != null) {
                    Object lat = webEngine.executeScript(
                            "window.mapFunctions.getSelectedLocation() ? window.mapFunctions.getSelectedLocation().lat : null"
                    );
                    Object lng = webEngine.executeScript(
                            "window.mapFunctions.getSelectedLocation() ? window.mapFunctions.getSelectedLocation().lng : null"
                    );
                    System.out.println("JavaScript lat: " + lat + ", lng: " + lng);

                    // If we have coordinates in JS but not in Java, try to sync
                    if (lat != null && lng != null && (selectedLat == null || selectedLng == null)) {
                        System.out.println("Found coordinates in JS, forcing sync...");
                        try {
                            double latValue = Double.parseDouble(lat.toString());
                            double lngValue = Double.parseDouble(lng.toString());

                            // Manually update Java-side coordinates
                            selectedLat = latValue;
                            selectedLng = lngValue;

                            Platform.runLater(() -> {
                                if (coordinatesField != null) {
                                    String coordsText = String.format("%.6f, %.6f", latValue, lngValue);
                                    coordinatesField.setText(coordsText);
                                }
                            });

                            System.out.println("Manually synced coordinates: " + latValue + ", " + lngValue);
                        } catch (NumberFormatException e) {
                            System.err.println("Error parsing coordinates: " + e.getMessage());
                        }
                    }
                }

                // Test bridge functionality
                Object bridgeTest = webEngine.executeScript(
                        "window.testBridge ? window.testBridge() : 'testBridge not available'"
                );
                System.out.println("Bridge test result: " + bridgeTest);

            } catch (Exception e) {
                System.err.println("Error in debug: " + e.getMessage());
            }
        }

        AlertUtils.showInfo("Debug selesai. Periksa console untuk detail.");
    }
}
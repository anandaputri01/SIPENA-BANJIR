package controllers;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import utils.AlertUtils;
import java.net.URL;


public abstract class BaseMapController extends BaseController {

    @FXML
    protected WebView mapWebView;
    @FXML
    protected TextField coordinatesField;

    protected WebEngine webEngine;
    protected Double selectedLat;
    protected Double selectedLng;
    protected boolean bridgeEstablished = false;

    protected void initializeMap() {
        webEngine = mapWebView.getEngine();

        webEngine.setOnAlert(event -> System.out.println("JavaScript Alert: " + event.getData()));
        webEngine.setOnError(event -> System.err.println("JavaScript Error: " + event.getMessage()));

        try {
            URL mapUrl = getClass().getResource("/maps/map.html");
            if (mapUrl == null) {
                System.err.println("ERROR: map.html not found in /maps/ directory");
                AlertUtils.showError("Map file not found.");
                return;
            }
            webEngine.load(mapUrl.toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Failed to load map: " + e.getMessage());
        }

        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                Platform.runLater(this::setupJavaScriptBridge);
                onMapReady();
            } else if (newValue == Worker.State.FAILED) {
                System.err.println("WebEngine failed to load");
                Throwable ex = webEngine.getLoadWorker().getException();
                if (ex != null) ex.printStackTrace();
            }
        });
    }

    private void setupJavaScriptBridge() {
        System.out.println("Setting up JavaScript bridge...");
        try {
            JSObject window = (JSObject) webEngine.executeScript("window");
            window.setMember("javaConnector", new JavaConnector());
            bridgeEstablished = true;
            System.out.println("JavaScript bridge established successfully.");
            webEngine.executeScript(
                    "if(window.mapFunctions && window.mapFunctions.checkBridge) { window.mapFunctions.checkBridge(); }"
            );
        } catch (Exception e) {
            System.err.println("Error setting up JavaScript bridge: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected void onMapReady() {}

    public class JavaConnector {
        public void setSelectedLocation(double lat, double lng) {
            System.out.println("=== LOCATION RECEIVED FROM JAVASCRIPT ===");
            System.out.println("Latitude: " + lat + ", Longitude: " + lng);
            selectedLat = lat;
            selectedLng = lng;

            Platform.runLater(() -> {
                try {
                    String locationText = String.format("%.6f, %.6f", lat, lng);
                    coordinatesField.setText(locationText);
                } catch (Exception e) {
                    System.err.println("Error updating UI coordinates field: " + e.getMessage());
                }
            });
        }

        public void logMessage(String message) {
            System.out.println("[JS->Java] " + message);
        }

        public void bridgeReady() {
            System.out.println("Bridge ready confirmation received from JavaScript");
        }
    }
}
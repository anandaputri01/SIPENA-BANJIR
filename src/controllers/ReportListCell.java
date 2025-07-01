package controllers;

import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import models.Report;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class ReportListCell extends ListCell<Report> {

    private ReportItemController.ReportItemActionCallback userActionCallback;
    private ReportItemController.AdminReportActionCallback adminActionCallback;

    public ReportListCell() {
        super();
    }

    public ReportListCell(ReportItemController.ReportItemActionCallback actionCallback) {
        this.userActionCallback = actionCallback;
    }

    public ReportListCell(ReportItemController.AdminReportActionCallback adminActionCallback) {
        this.adminActionCallback = adminActionCallback;
    }

    @Override
    protected void updateItem(Report report, boolean empty) {
        super.updateItem(report, empty);

        if (empty || report == null) {
            setText(null);
            setGraphic(null);
        } else {
            try {
                ReportItemController controller = new ReportItemController();
                controller.setReport(report);

                // Set the appropriate callback
                if (userActionCallback != null) {
                    controller.setActionCallback(userActionCallback);
                    controller.setUserMode(true);
                } else if (adminActionCallback != null) {
                    controller.setAdminActionCallback(adminActionCallback);
                    controller.setUserMode(false);
                }

                setGraphic(controller.getView());

                // Clear any text since we're using a graphic
                setText(null);

            } catch (Exception e) {
                System.err.println("Error creating report item: " + e.getMessage());
                e.printStackTrace();

                // Fallback to simple text display
                setText(report.getFloodLocation() + " - " + report.getDescription());
                setGraphic(null);
            }
        }
    }

    public void showOnMap(WebView mapWebView) {
        Report report = getItem();
        if (report != null && report.getLatitude() != null && report.getLongitude() != null) {
            try {
                WebEngine webEngine = mapWebView.getEngine();

                // Escape single quotes in the location text to prevent JavaScript errors
                String escapedLocation = report.getFloodLocation().replace("'", "\\'");

                String script = String.format(
                        "if (window.mapFunctions && window.mapFunctions.addMarker) {" +
                                "  window.mapFunctions.addMarker(%f, %f, '%s');" +
                                "} else if (typeof addMarker === 'function') {" +
                                "  addMarker(%f, %f, '%s');" +
                                "} else {" +
                                "  console.log('Map function not available');" +
                                "}",
                        report.getLatitude(), report.getLongitude(), escapedLocation,
                        report.getLatitude(), report.getLongitude(), escapedLocation
                );

                webEngine.executeScript(script);

                // Also center the map on this location
                String centerScript = String.format(
                        "if (window.mapFunctions && window.mapFunctions.setView) {" +
                                "  window.mapFunctions.setView(%f, %f, 15);" +
                                "}",
                        report.getLatitude(), report.getLongitude()
                );

                webEngine.executeScript(centerScript);

                System.out.println("Showing report on map: " + report.getFloodLocation() +
                        " at " + report.getLatitude() + ", " + report.getLongitude());

            } catch (Exception e) {
                System.err.println("Error showing report on map: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Cannot show on map: report or coordinates are null");
        }
    }

    public static Callback<ListView<Report>, ListCell<Report>> forListView() {
        return param -> new ReportListCell();
    }

    public static Callback<ListView<Report>, ListCell<Report>> forListView(ReportItemController.ReportItemActionCallback actionCallback) {
        return param -> new ReportListCell(actionCallback);
    }

    public static Callback<ListView<Report>, ListCell<Report>> forAdminListView(ReportItemController.AdminReportActionCallback adminActionCallback) {
        return param -> new ReportListCell(adminActionCallback);
    }
}
package repositories;

import config.DatabaseConfig;
import models.Report;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportRepository {
    public boolean create(Report report) {
        String query = "INSERT INTO reports (user_id, reporter_name, reporter_phone, flood_location, " +
                "latitude, longitude, description, photo_path, status, admin_notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, report.getUserId());
            stmt.setString(2, report.getReporterName());
            stmt.setString(3, report.getReporterPhone());
            stmt.setString(4, report.getFloodLocation());
            stmt.setDouble(5, report.getLatitude());
            stmt.setDouble(6, report.getLongitude());
            stmt.setString(7, report.getDescription());
            stmt.setString(8, report.getPhotoPath());
            stmt.setString(9, report.getStatus());
            stmt.setString(10, report.getAdminNotes());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    report.setId(rs.getInt(1));
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Report report) {
        String query = "UPDATE reports SET reporter_name = ?, reporter_phone = ?, flood_location = ?, " +
                "latitude = ?, longitude = ?, description = ?, photo_path = ?, updated_at = CURRENT_TIMESTAMP " +
                "WHERE id = ? AND status = 'PENDING'";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, report.getReporterName());
            stmt.setString(2, report.getReporterPhone());
            stmt.setString(3, report.getFloodLocation());
            stmt.setDouble(4, report.getLatitude());
            stmt.setDouble(5, report.getLongitude());
            stmt.setString(6, report.getDescription());
            stmt.setString(7, report.getPhotoPath());
            stmt.setInt(8, report.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Report> findAll() {
        List<Report> reports = new ArrayList<>();
        String query = "SELECT * FROM reports ORDER BY created_at DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                reports.add(new Report(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("reporter_name"),
                        rs.getString("reporter_phone"),
                        rs.getString("flood_location"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getString("description"),
                        rs.getString("photo_path"),
                        rs.getString("status"),
                        rs.getString("admin_notes"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reports;
    }

    public List<Report> findByUserId(int userId) {
        List<Report> reports = new ArrayList<>();
        String query = "SELECT * FROM reports WHERE user_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                reports.add(new Report(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("reporter_name"),
                        rs.getString("reporter_phone"),
                        rs.getString("flood_location"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getString("description"),
                        rs.getString("photo_path"),
                        rs.getString("status"),
                        rs.getString("admin_notes"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reports;
    }

    public boolean updateStatus(int reportId, String status, String adminNotes) {
        String query = "UPDATE reports SET status = ?, admin_notes = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, status);
            stmt.setString(2, adminNotes);
            stmt.setInt(3, reportId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int reportId) {
        String deleteMessagesQuery = "DELETE FROM messages WHERE report_id = ?";
        String deleteReportQuery = "DELETE FROM reports WHERE id = ?";
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement deleteMessagesStmt = conn.prepareStatement(deleteMessagesQuery)) {
                deleteMessagesStmt.setInt(1, reportId);
                deleteMessagesStmt.executeUpdate();
            }

            try (PreparedStatement deleteReportStmt = conn.prepareStatement(deleteReportQuery)) {
                deleteReportStmt.setInt(1, reportId);
                int affectedRows = deleteReportStmt.executeUpdate();
                conn.commit();
                return affectedRows > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    System.err.println("Transaksi dibatalkan (rollback).");
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
package repositories;

import config.DatabaseConfig;
import models.Message;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageRepository {
    public boolean create(Message message) {
        String query = "INSERT INTO messages (report_id, sender_id, message, is_admin_message) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, message.getReportId());
            stmt.setInt(2, message.getSenderId());
            stmt.setString(3, message.getMessage());
            stmt.setBoolean(4, message.isAdminMessage());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    message.setId(rs.getInt(1));
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Message> findByReportId(int reportId) {
        List<Message> messages = new ArrayList<>();
        String query = "SELECT * FROM messages WHERE report_id = ? ORDER BY created_at ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, reportId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                messages.add(new Message(
                        rs.getInt("id"),
                        rs.getInt("report_id"),
                        rs.getInt("sender_id"),
                        rs.getString("message"),
                        rs.getBoolean("is_admin_message"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }
}
package com.example.projektzielonifx.newproject;
import com.example.projektzielonifx.models.Milestone;
import com.example.projektzielonifx.database.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MilestoneService {

    public List<Milestone> list() {
        List<Milestone> milestones = new ArrayList<>();
        String sql = "SELECT id, project_id, name, description, deadline FROM Milestones ORDER BY deadline";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // Use the constructor that includes projectId
                Milestone milestone = new Milestone(rs.getInt("id"), rs.getInt("project_id"), rs.getString("name"));
                milestone.setDescription(rs.getString("description"));
                Date deadline = rs.getDate("deadline");
                if (deadline != null) {
                    milestone.setDeadline(deadline.toLocalDate());
                }
                milestones.add(milestone);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return milestones;
    }

    public int add(Milestone milestone, int projectId) {
        String sql = "INSERT INTO Milestones (project_id, name, description, deadline, progress) VALUES (?, ?, ?, ?, 0)";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, projectId);
            ps.setString(2, milestone.getName());
            ps.setString(3, milestone.getDescription());
            ps.setDate(4, milestone.getDeadline() == null ? null : Date.valueOf(milestone.getDeadline()));

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean save(Milestone milestone, int projectId) {
        String sql = "UPDATE Milestones SET project_id = ?, name = ?, description = ?, deadline = ? WHERE id = ?";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, projectId);
            ps.setString(2, milestone.getName());
            ps.setString(3, milestone.getDescription());
            ps.setDate(4, milestone.getDeadline() == null ? null : Date.valueOf(milestone.getDeadline()));
            ps.setInt(5, milestone.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean remove(int id) {
        String sql = "DELETE FROM Milestones WHERE id = ?";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Milestone> getByProjectId(int projectId) {
        List<Milestone> milestones = new ArrayList<>();
        String sql = "SELECT id, name, description, deadline FROM Milestones WHERE project_id = ? ORDER BY deadline";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Milestone milestone = new Milestone(rs.getInt("id"), projectId, rs.getString("name"));
                    milestone.setDescription(rs.getString("description"));
                    Date deadline = rs.getDate("deadline");
                    if (deadline != null) {
                        milestone.setDeadline(deadline.toLocalDate());
                    }
                    milestones.add(milestone);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return milestones;
    }
}
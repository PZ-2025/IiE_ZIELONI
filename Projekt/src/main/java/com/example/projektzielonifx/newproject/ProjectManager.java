package com.example.projektzielonifx.newproject;
import com.example.projektzielonifx.database.DatabaseConnection;
import com.example.projektzielonifx.models.Project;
import javafx.scene.chart.PieChart;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectManager {

    /* ---------- READ ---------- */

    public static List<Project> getAll() {
        String sql = "SELECT * FROM Projects ORDER BY start_date DESC";
        List<Project> list = new ArrayList<>();

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement st = c.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {

            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Project getById(int id) {
        String sql = "SELECT * FROM Projects WHERE id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement st = c.prepareStatement(sql)) {

            st.setInt(1, id);
            ResultSet rs = st.executeQuery();
            return rs.next() ? map(rs) : null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /* ---------- CREATE ---------- */

    public int create(Project p) {
        String sql = """
                INSERT INTO Projects (manager_id,name,progress,status,start_date,end_date)
                VALUES (?,?,?,?,?,?)
                """;
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement st = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            bind(st, p);
            st.executeUpdate();
            ResultSet keys = st.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /* ---------- UPDATE ---------- */

    public boolean update(Project p) {
        String sql = """
                UPDATE Projects SET manager_id=?,name=?,progress=?,status=?,
                                     start_date=?,end_date=? WHERE id=?
                """;
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement st = c.prepareStatement(sql)) {

            bind(st, p);
            st.setInt(7, p.getId());
            return st.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /* ---------- DELETE ---------- */

    public boolean delete(int id) {
        String sql = "DELETE FROM Projects WHERE id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement st = c.prepareStatement(sql)) {

            st.setInt(1, id);
            return st.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /* ---------- helpers ---------- */

    protected static Project map(ResultSet rs) throws SQLException {
        return new Project(
                rs.getInt("id"),
                rs.getInt("manager_id"),
                rs.getString("name"),
                rs.getInt("progress"),
                rs.getString("status"),
                rs.getDate("start_date").toLocalDate(),
                rs.getDate("end_date") == null ? null :
                        rs.getDate("end_date").toLocalDate()
        );
    }

    protected void bind(PreparedStatement st, Project p) throws SQLException {
        st.setInt   (1, p.getManagerId());
        st.setString(2, p.getName());
        st.setInt   (3, p.getProgress());
        st.setString(4, p.getStatus());
        st.setDate  (5, Date.valueOf(p.getStartDate()));
        if (p.getEndDate() == null)
            st.setNull(6, Types.DATE);
        else
            st.setDate(6, Date.valueOf(p.getEndDate()));
    }
}

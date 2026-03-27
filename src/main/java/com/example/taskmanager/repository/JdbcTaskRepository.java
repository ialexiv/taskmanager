package com.example.taskmanager.repository;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JdbcTaskRepository implements TaskRepository {

    private final JdbcClient jdbcClient;

    public JdbcTaskRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public Task save(Task task) {
        if (task.getId() == null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO tasks (title, description, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
                        new String[]{"id"}
                );
                ps.setString(1, task.getTitle());
                ps.setString(2, task.getDescription());
                ps.setString(3, task.getStatus().name());
                ps.setTimestamp(4, Timestamp.valueOf(task.getCreatedAt()));
                ps.setTimestamp(5, Timestamp.valueOf(task.getUpdatedAt()));
                return ps;
            }, keyHolder);
            task.setId(keyHolder.getKey().longValue());
            return task;
        } else {
            jdbcClient.sql("UPDATE tasks SET status = ?, updated_at = ? WHERE id = ?")
                    .param(1, task.getStatus().name())
                    .param(2, task.getUpdatedAt())
                    .param(3, task.getId())
                    .update();
            return task;
        }
    }

    @Override
    public Optional<Task> findById(Long id) {
        return jdbcClient.sql("SELECT id, title, description, status, created_at, updated_at FROM tasks WHERE id = ?")
                .param(1, id)
                .query(this::mapRow)
                .optional();
    }

    @Override
    public List<Task> findAll(int offset, int limit, TaskStatus status) {
        String sql = "SELECT id, title, description, status, created_at, updated_at FROM tasks " +
                "WHERE (? IS NULL OR status = ?) " +
                "ORDER BY created_at DESC " +
                "LIMIT ? OFFSET ?";
        return jdbcClient.sql(sql)
                .param(1, status != null ? status.name() : null)
                .param(2, status != null ? status.name() : null)
                .param(3, limit)
                .param(4, offset)
                .query(this::mapRow)
                .list();
    }

    @Override
    public long count(TaskStatus status) {
        String sql = "SELECT COUNT(*) FROM tasks WHERE (? IS NULL OR status = ?)";
        return jdbcClient.sql(sql)
                .param(1, status != null ? status.name() : null)
                .param(2, status != null ? status.name() : null)
                .query(Long.class)
                .single();
    }

    @Override
    public int updateStatus(Long id, TaskStatus status) {
        return jdbcClient.sql("UPDATE tasks SET status = ?, updated_at = ? WHERE id = ?")
                .param(1, status.name())
                .param(2, LocalDateTime.now())
                .param(3, id)
                .update();
    }

    @Override
    public int deleteById(Long id) {
        return jdbcClient.sql("DELETE FROM tasks WHERE id = ?")
                .param(1, id)
                .update();
    }

    private Task mapRow(ResultSet rs, int rowNum) throws SQLException {
        Task task = new Task();
        task.setId(rs.getLong("id"));
        task.setTitle(rs.getString("title"));
        task.setDescription(rs.getString("description"));
        task.setStatus(TaskStatus.valueOf(rs.getString("status")));
        task.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        task.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return task;
    }
}
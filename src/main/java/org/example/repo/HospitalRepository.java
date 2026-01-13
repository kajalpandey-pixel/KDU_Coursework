package org.example.repo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class HospitalRepository {

    private final JdbcTemplate jdbcTemplate;

    public HospitalRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String createTenant(String name) {
        String id = UUID.randomUUID().toString();
        jdbcTemplate.update(
                "INSERT INTO tenant (id, name) VALUES (?, ?)",
                id, name
        );
        return id;
    }

    public String insertUser(String tenantId, String username, boolean loggedIn, String timezone) {
        String id = UUID.randomUUID().toString();
        jdbcTemplate.update("""
                INSERT INTO users (id, tenant_id, username, is_logged_in, timezone)
                VALUES (?, ?, ?, ?, ?)
                """, id, tenantId, username, loggedIn, timezone
        );
        return id;
    }

    public int updateUser(String tenantId, String userId, String username, String timezone, Boolean loggedIn) {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("UPDATE users SET ");

        boolean any = false;

        if (username != null) {
            sql.append("username = ?");
            params.add(username);
            any = true;
        }
        if (timezone != null) {
            if (any) sql.append(", ");
            sql.append("timezone = ?");
            params.add(timezone);
            any = true;
        }
        if (loggedIn != null) {
            if (any) sql.append(", ");
            sql.append("is_logged_in = ?");
            params.add(loggedIn);
            any = true;
        }

        if (!any) return 0;

        sql.append(", updated_at_utc = UTC_TIMESTAMP() ");
        sql.append("WHERE tenant_id = ? AND id = ?");
        params.add(tenantId);
        params.add(userId);

        return jdbcTemplate.update(sql.toString(), params.toArray());
    }

    public List<Map<String, Object>> getUsersByTenant(String tenantId) {
        return jdbcTemplate.queryForList("""
                SELECT id, tenant_id, username, is_logged_in, timezone, created_at_utc, updated_at_utc
                FROM users
                WHERE tenant_id = ?
                ORDER BY created_at_utc DESC
                """, tenantId
        );
    }

    public List<Map<String, Object>> getShiftsByTenant(String tenantId) {
        return jdbcTemplate.queryForList("""
                SELECT id, tenant_id, shift_type_id, start_date, start_time, end_date, end_time, created_at_utc, updated_at_utc
                FROM shift
                WHERE tenant_id = ?
                ORDER BY start_date DESC, start_time DESC
                """, tenantId
        );
    }

    public List<Map<String, Object>> getUsersByTenantPaged(String tenantId, int size, int offset, String sortDir) {
        String dir = "ASC".equalsIgnoreCase(sortDir) ? "ASC" : "DESC";

        String sql = """
                SELECT id, tenant_id, username, is_logged_in, timezone, created_at_utc, updated_at_utc
                FROM users
                WHERE tenant_id = ?
                ORDER BY username %s
                LIMIT ? OFFSET ?
                """.formatted(dir);

        return jdbcTemplate.queryForList(sql, tenantId, size, offset);
    }

    public String insertShiftType(String tenantId, String name, String description, boolean active) {
        String id = UUID.randomUUID().toString();
        jdbcTemplate.update("""
                INSERT INTO shift_type (id, tenant_id, name, description, is_active)
                VALUES (?, ?, ?, ?, ?)
                """, id, tenantId, name, description, active
        );
        return id;
    }

    public String insertShift(String tenantId, String shiftTypeId,
                              String startDate, String startTime,
                              String endDate, String endTime) {
        String id = UUID.randomUUID().toString();
        jdbcTemplate.update("""
                INSERT INTO shift (id, tenant_id, shift_type_id, start_date, start_time, end_date, end_time)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, id, tenantId, shiftTypeId, startDate, startTime, endDate, endTime
        );
        return id;
    }

    public String assignUserToShift(String tenantId, String shiftId, String userId) {
        String id = UUID.randomUUID().toString();
        jdbcTemplate.update("""
                INSERT INTO shift_user (id, tenant_id, shift_id, user_id)
                VALUES (?, ?, ?, ?)
                """, id, tenantId, shiftId, userId
        );
        return id;
    }
}

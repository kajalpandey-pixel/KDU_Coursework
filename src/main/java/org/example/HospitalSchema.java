package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class HospitalSchema {

    private static final String URL =
            "jdbc:mysql://localhost:3306/Hospital?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "Kajal@@2002"; // <-- put your password here

    public static void main(String[] args) {
        String[] ddl = new String[]{

                // Ensure this session uses UTC timestamps
                "SET time_zone = '+00:00'",

                // 1) Tenant (recommended for multi-branch isolation)
                """
                CREATE TABLE IF NOT EXISTS tenant (
                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                  name VARCHAR(120) NOT NULL UNIQUE
                ) ENGINE=InnoDB
                """,

                // 2) Shift_Type: (ID, Name, Description, Active Status, Tenant_ID)
                """
                CREATE TABLE IF NOT EXISTS shift_type (
                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                  tenant_id BIGINT NOT NULL,
                  name VARCHAR(60) NOT NULL,
                  description VARCHAR(255),
                  is_active BOOLEAN NOT NULL DEFAULT TRUE,

                  created_at_utc DATETIME NOT NULL DEFAULT (UTC_TIMESTAMP()),
                  updated_at_utc DATETIME NOT NULL DEFAULT (UTC_TIMESTAMP()),

                  UNIQUE KEY uq_shift_type_name_per_tenant (tenant_id, name),
                  UNIQUE KEY uq_shift_type_tenant_id_id (tenant_id, id),
                  KEY idx_shift_type_tenant (tenant_id),

                  CONSTRAINT fk_shift_type_tenant
                    FOREIGN KEY (tenant_id) REFERENCES tenant(id)
                    ON DELETE RESTRICT ON UPDATE CASCADE
                ) ENGINE=InnoDB
                """,

                // 3) Users: (ID, Username, LoggedIn Status, TimeZone, Tenant_ID)
                """
                CREATE TABLE IF NOT EXISTS users (
                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                  tenant_id BIGINT NOT NULL,
                  username VARCHAR(80) NOT NULL,
                  is_logged_in BOOLEAN NOT NULL DEFAULT FALSE,
                  timezone VARCHAR(64) NOT NULL,

                  created_at_utc DATETIME NOT NULL DEFAULT (UTC_TIMESTAMP()),
                  updated_at_utc DATETIME NOT NULL DEFAULT (UTC_TIMESTAMP()),

                  UNIQUE KEY uq_username_per_tenant (tenant_id, username),
                  UNIQUE KEY uq_users_tenant_id_id (tenant_id, id),
                  KEY idx_users_tenant (tenant_id),

                  CONSTRAINT fk_users_tenant
                    FOREIGN KEY (tenant_id) REFERENCES tenant(id)
                    ON DELETE RESTRICT ON UPDATE CASCADE
                ) ENGINE=InnoDB
                """,

                // 4) Shift: (ID, Shift_Type_ID, Date Start/End, Time Start/End, Tenant_ID)
                """
                CREATE TABLE IF NOT EXISTS shift (
                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                  tenant_id BIGINT NOT NULL,
                  shift_type_id BIGINT NOT NULL,

                  start_date DATE NOT NULL,
                  start_time TIME NOT NULL,
                  end_date DATE NOT NULL,
                  end_time TIME NOT NULL,

                  created_at_utc DATETIME NOT NULL DEFAULT (UTC_TIMESTAMP()),
                  updated_at_utc DATETIME NOT NULL DEFAULT (UTC_TIMESTAMP()),

                  UNIQUE KEY uq_shift_tenant_id_id (tenant_id, id),
                  KEY idx_shift_tenant (tenant_id),
                  KEY idx_shift_type (tenant_id, shift_type_id),

                  CONSTRAINT fk_shift_tenant
                    FOREIGN KEY (tenant_id) REFERENCES tenant(id)
                    ON DELETE RESTRICT ON UPDATE CASCADE,

                  -- IMPORTANT: tenant isolation (shift_type must be same tenant)
                  CONSTRAINT fk_shift_shift_type_tenant
                    FOREIGN KEY (tenant_id, shift_type_id) REFERENCES shift_type(tenant_id, id)
                    ON DELETE RESTRICT ON UPDATE CASCADE
                ) ENGINE=InnoDB
                """,

                // 5) Shift_User: (ID, Shift_ID, User_ID, Tenant_ID)
                """
                CREATE TABLE IF NOT EXISTS shift_user (
                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                  tenant_id BIGINT NOT NULL,
                  shift_id BIGINT NOT NULL,
                  user_id BIGINT NOT NULL,

                  created_at_utc DATETIME NOT NULL DEFAULT (UTC_TIMESTAMP()),

                  UNIQUE KEY uq_shift_user (tenant_id, shift_id, user_id),
                  KEY idx_shift_user_tenant (tenant_id),
                  KEY idx_shift_user_shift (tenant_id, shift_id),
                  KEY idx_shift_user_user (tenant_id, user_id),

                  CONSTRAINT fk_shift_user_tenant
                    FOREIGN KEY (tenant_id) REFERENCES tenant(id)
                    ON DELETE RESTRICT ON UPDATE CASCADE,

                  -- tenant isolation (shift must be same tenant)
                  CONSTRAINT fk_shift_user_shift
                    FOREIGN KEY (tenant_id, shift_id) REFERENCES shift(tenant_id, id)
                    ON DELETE RESTRICT ON UPDATE CASCADE,

                  -- tenant isolation (user must be same tenant)
                  CONSTRAINT fk_shift_user_user
                    FOREIGN KEY (tenant_id, user_id) REFERENCES users(tenant_id, id)
                    ON DELETE RESTRICT ON UPDATE CASCADE
                ) ENGINE=InnoDB
                """
        };

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            for (String sql : ddl) {
                stmt.execute(sql);
            }

            System.out.println("Schema created successfully (MySQL, UTC).");

        } catch (SQLException e) {
            System.out.println("Schema creation failed:");
            e.printStackTrace();
        }
    }
}

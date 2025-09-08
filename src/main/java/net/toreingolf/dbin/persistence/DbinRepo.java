package net.toreingolf.dbin.persistence;

import lombok.extern.slf4j.Slf4j;
import net.toreingolf.dbin.domain.AllTabColumns;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
public class DbinRepo {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    DataSourceProperties props;

    public Long getTableSize(String owner, String tableName) {
        log.info("get size for table {}.{}", owner, tableName);
        var sql = "select count(*) from " + owner + ".\"" + tableName + "\"";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    public List<String> getUsers() {
        var sql = "select username from all_users order by 1";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    public List<List<String>> getData(List<AllTabColumns> columns, String sql) {
        log.info("get data columns {} using query {}", columns, sql);
        List<List<String>> rows = new ArrayList<>();
        try {
            try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    List<String> columnValues = new ArrayList<>();
                    columns.forEach(c -> {
                        try {
                            columnValues.add(rs.getString(c.getColumnName()));
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    rows.add(columnValues);
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return rows;
    }

    private Connection getConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(props.getUrl(), props.getUsername(), props.getPassword());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return conn;
    }
}

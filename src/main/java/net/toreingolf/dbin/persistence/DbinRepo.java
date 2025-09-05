package net.toreingolf.dbin.persistence;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class DbinRepo {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public Long getTableSize(String owner, String tableName) {
        log.info("get size for table {}.{}", owner, tableName);
        var sql = "select count(*) from " + owner + ".\"" + tableName + "\"";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }
}

package net.toreingolf.dbin.persistence;

import net.toreingolf.dbin.domain.AllTabColumns;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AllTabColumnsRepo extends JpaRepository<AllTabColumns, Long> {
    List<AllTabColumns> findByOwnerAndTableName(String owner, String tableName, Sort sort);
}

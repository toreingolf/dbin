package net.toreingolf.dbin.persistence;

import net.toreingolf.dbin.domain.AllConsColumns;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AllConsColumnsRepo extends JpaRepository<AllConsColumns, Long> {
    List<AllConsColumns> findByOwnerAndConstraintName(String owner, String ConstraintName);
}

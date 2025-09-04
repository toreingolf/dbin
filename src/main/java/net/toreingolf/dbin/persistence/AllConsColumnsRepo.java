package net.toreingolf.dbin.persistence;

import net.toreingolf.dbin.domain.AllConsColumns;
import net.toreingolf.dbin.domain.AllConsColumnsId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AllConsColumnsRepo extends JpaRepository<AllConsColumns, Long> {
    List<AllConsColumns> findById(AllConsColumnsId id);
}

package net.toreingolf.dbin.persistence;

import net.toreingolf.dbin.domain.AllSource;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AllSourceRepo extends JpaRepository<AllSource, Long> {
    List<AllSource> findByOwnerAndNameAndType(String owner, String name, String type, Sort sort);
}

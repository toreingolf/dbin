package net.toreingolf.dbin.persistence;

import net.toreingolf.dbin.domain.AllColComments;
import net.toreingolf.dbin.domain.IdTableNameColumnName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AllColCommentsRepo extends JpaRepository<AllColComments, Long> {
    AllColComments findById(IdTableNameColumnName id);
}

package net.toreingolf.dbin.persistence;

import net.toreingolf.dbin.domain.AllTabComments;
import net.toreingolf.dbin.domain.IdTableName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AllTabCommentsRepo extends JpaRepository<AllTabComments, Long> {
    AllTabComments findById(IdTableName id);
}

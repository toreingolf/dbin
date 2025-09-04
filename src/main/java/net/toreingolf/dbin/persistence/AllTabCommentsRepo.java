package net.toreingolf.dbin.persistence;

import net.toreingolf.dbin.domain.AllTabComments;
import net.toreingolf.dbin.domain.AllTabCommentsId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AllTabCommentsRepo extends JpaRepository<AllTabComments, Long> {
    AllTabComments findById(AllTabCommentsId id);
}

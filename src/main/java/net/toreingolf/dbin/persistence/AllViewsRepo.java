package net.toreingolf.dbin.persistence;

import net.toreingolf.dbin.domain.AllViews;
import net.toreingolf.dbin.domain.IdViewName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AllViewsRepo extends JpaRepository<AllViews, Long> {
    AllViews findById(IdViewName id);
}

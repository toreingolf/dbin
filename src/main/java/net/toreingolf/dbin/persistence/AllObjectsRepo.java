package net.toreingolf.dbin.persistence;

import net.toreingolf.dbin.domain.AllObjects;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AllObjectsRepo extends JpaRepository<AllObjects, Long> {
    List<AllObjects> findByOwnerAndObjectType(String owner, String objectType, Sort sort);
    AllObjects findByOwnerAndObjectName(String owner, String objectName);
}

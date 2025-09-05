package net.toreingolf.dbin.persistence;

import net.toreingolf.dbin.domain.AllConstraints;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AllConstraintsRepo extends JpaRepository<AllConstraints, String> {

    List<AllConstraints> findByOwnerAndTableName(String owner, String tableName);

    AllConstraints findByOwnerAndConstraintName(String owner, String constraintName);

    AllConstraints findByOwnerAndTableNameAndConstraintType(String owner, String tableName, String constraintType);

    List<AllConstraints> findByOwnerAndTargetConstraintNameAndConstraintType(String owner, String targetConstraintName, String constraintType);
}

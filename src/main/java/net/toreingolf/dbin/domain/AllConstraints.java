package net.toreingolf.dbin.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class AllConstraints {
    @Id
    private String constraintName;
    private String owner;
    private String tableName;
    private Long originConId;
    private String constraintType;
    private String searchCondition;
    private String rOwner;
    private String rConstraintName;
}

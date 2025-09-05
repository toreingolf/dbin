package net.toreingolf.dbin.domain;

import jakarta.persistence.Column;
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
    @Column(name = "R_OWNER")
    private String targetOwner;
    @Column(name = "R_CONSTRAINT_NAME")
    private String targetConstraintName;
}

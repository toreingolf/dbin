package net.toreingolf.dbin.domain;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class IdConstraintNameColumnName {
    @Column(insertable=false, updatable=false)
    private String owner;
    @Column(insertable=false, updatable=false)
    private String constraintName;
    @Column(insertable=false, updatable=false)
    private String columnName;
}

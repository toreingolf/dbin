package net.toreingolf.dbin.domain;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class AllConsColumns {
    @EmbeddedId
    private AllConsColumnsId id;
    private String tableName;
    private String columnName;
    private long position;
}

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
public class AllTabColumns {
    @Id
    private long columnId;
    private String owner;
    private String tableName;
    private String columnName;
    private String nullable;
    private String dataType;
    private String dataLength;
    private String charLength;
    private String dataDefault;
}

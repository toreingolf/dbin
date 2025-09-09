package net.toreingolf.dbin.domain;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class IdNameTypeLine {
    @Column(insertable=false, updatable=false)
    private String owner;
    @Column(insertable=false, updatable=false)
    private String name;
    @Column(insertable=false, updatable=false)
    private String type;
    @Column(insertable=false, updatable=false)
    private Long line;
}

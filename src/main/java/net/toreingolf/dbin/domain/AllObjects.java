package net.toreingolf.dbin.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class AllObjects {
    @Id
    private Long objectId;
    private String owner;
    private String objectName;
    private String objectType;
    private Date created;
    private Date lastDdlTime;
    private String status;
}

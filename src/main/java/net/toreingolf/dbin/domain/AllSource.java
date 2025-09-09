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
public class AllSource {
    @EmbeddedId
    private IdNameTypeLine id;
    private String owner;
    private String name;
    private String type;
    private Long line;
    private String text;
}

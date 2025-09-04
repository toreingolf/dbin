package net.toreingolf.dbin.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AllConsColumnsId {
    private String owner;
    private String constraintName;
}

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
public class AllTabComments {
    @EmbeddedId
    private AllTabCommentsId id;
    private String comments;
}

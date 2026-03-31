package nsu.sd.testClasses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nsu.sd.annotations.JsonLazy;
import nsu.sd.annotations.JsonSerializable;

@JsonSerializable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NodeUser {
    private String name;

    @JsonLazy
    private NodeUser friend;

    public NodeUser(String name) {
        this.name = name;
    }
}
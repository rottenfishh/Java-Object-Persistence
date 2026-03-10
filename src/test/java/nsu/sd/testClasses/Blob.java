package nsu.sd.testClasses;

import lombok.Getter;
import lombok.Setter;
import nsu.sd.annotations.JsonElement;
import nsu.sd.annotations.JsonIgnore;
import nsu.sd.annotations.JsonSerializable;

@Getter
@Setter
@JsonSerializable
public class Blob {
    @JsonElement(name = "CoolName")
    String name;
    @JsonIgnore
    String surname;
    int age;
}

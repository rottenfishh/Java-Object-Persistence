package nsu.sd;

import nsu.sd.annotations.JsonElement;
import nsu.sd.annotations.JsonIgnore;
import nsu.sd.annotations.JsonSerializable;

@JsonSerializable
public class Blob {
    @JsonElement(name = "CoolName")
    String name;
    @JsonIgnore
    String surname;
    int age;
}

package nsu.sd.testClasses;

import lombok.Getter;
import lombok.Setter;
import nsu.sd.annotations.JsonSerializable;

@Getter
@Setter
@JsonSerializable
public class Student {
    String name;
    int age;
    Address homeAddress;
}

package nsu.sd.testClasses;

import lombok.Getter;
import lombok.Setter;
import nsu.sd.annotations.JsonElement;
import nsu.sd.annotations.JsonSerializable;

import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
@JsonSerializable
public class WrongClass {
    @JsonElement(name = "student_to_age")
    ConcurrentHashMap<Integer, Student> studentToAge;
}

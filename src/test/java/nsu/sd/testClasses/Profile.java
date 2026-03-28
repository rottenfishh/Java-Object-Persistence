package nsu.sd.testClasses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nsu.sd.annotations.JsonSerializable;

@JsonSerializable
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Profile {
    private String city;
    private Integer age;
}
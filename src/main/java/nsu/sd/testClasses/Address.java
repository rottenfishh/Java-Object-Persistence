package nsu.sd.testClasses;

import lombok.Getter;
import lombok.Setter;
import nsu.sd.annotations.JsonSerializable;

@Getter
@Setter
@JsonSerializable
public class Address {
    String city;
    String street;
    int houseNumber;
}

package nsu.sd.testClasses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nsu.sd.annotations.JsonLazy;
import nsu.sd.annotations.JsonSerializable;

@JsonSerializable
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class UserWithLazyProfile {
    private String name;

    @JsonLazy
    private Profile profile;
}
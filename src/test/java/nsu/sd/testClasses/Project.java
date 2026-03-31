package nsu.sd.testClasses;

import nsu.sd.annotations.JsonSerializable;

@JsonSerializable
public class Project {
    public Developer frontend;
    public Developer backend;

    @JsonSerializable
    public static class Developer {
        public String name;
        public Laptop laptop;
    }

    @JsonSerializable
    public static class Laptop {
        public String model;
    }
}



package nsu.sd.testClasses;

import nsu.sd.annotations.JsonSerializable;

import java.util.ArrayList;
import java.util.List;

@JsonSerializable
public class University {
    public String name;
    public List<Student> students = new ArrayList<>();

    @Override
    public String toString() {
        return "University{name='" + name + "', students_count=" + (students != null ? students.size() : 0) + "}";
    }

    @JsonSerializable
    public static class Student {
        public String name;
        public int age;
        public University university;

        @Override
        public String toString() {
            String uniName = (university != null) ? university.name : "null";
            return "Student{name='" + name + "', age=" + age + ", university=" + uniName + "}";
        }
    }

}

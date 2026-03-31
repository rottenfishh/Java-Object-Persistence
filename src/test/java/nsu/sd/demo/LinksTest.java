package nsu.sd.demo;

import nsu.sd.JsonMapper;
import nsu.sd.MetadataRegistry;
import nsu.sd.testClasses.Project;
import nsu.sd.testClasses.University;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;

public class LinksTest {
    MetadataRegistry registry;
    JsonMapper mapper;

    @BeforeEach
    void setUp() {
        registry = new MetadataRegistry();
        mapper = new JsonMapper();
    }

    @Test
    public void testCyclicLinks() throws IOException {
        University nsu = new University();
        nsu.name = "NSU";

        University.Student anton = new University.Student();
        anton.name = "Anton";
        anton.age = 20;
        anton.university = nsu;

        University.Student maria = new University.Student();
        maria.name = "Maria";
        maria.age = 21;
        maria.university = nsu;

        nsu.students.add(anton);
        nsu.students.add(maria);

        String json = mapper.toJson(nsu);

        assertTrue(json.contains("@id"));
        assertTrue(json.contains("@ref"));

        File myFile = new File("blob.json");
        mapper.toJsonFile(myFile, nsu);

        University restoredNsu = (University) mapper.fromJson(json, University.class);
        University.Student restoredAnton = restoredNsu.students.get(0);
        // Один и тот же объект в памяти
        assertSame(restoredAnton.university, restoredNsu);
    }

    @Test
    public void testSharedLinks() throws IOException {
        Project.Laptop sharedLaptop = new Project.Laptop();
        sharedLaptop.model = "MacBook Pro";

        Project.Developer front = new Project.Developer();
        front.name = "Alice";
        front.laptop = sharedLaptop;

        Project.Developer back = new Project.Developer();
        back.name = "Bob";
        back.laptop = sharedLaptop;

        Project project = new Project();
        project.frontend = front;
        project.backend = back;


        String json = mapper.toJson(project);
        File file = new File("nested.json");
        mapper.toJsonFile(file, project);
        assertTrue(json.contains("@id"));
        assertTrue(json.contains("@ref"));

        Project restoredProject = (Project) mapper.fromJson(json, Project.class);
        // Один и тот же объект в памяти
        assertSame(restoredProject.frontend.laptop, restoredProject.backend.laptop);
    }
}

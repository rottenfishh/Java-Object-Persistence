package nsu.sd.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import nsu.sd.JsonMapper;
import nsu.sd.MetadataRegistry;
import nsu.sd.testClasses.Address;
import nsu.sd.testClasses.Blob;
import nsu.sd.testClasses.Student;
import nsu.sd.testClasses.WrongClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

public class SerializationTest {
    MetadataRegistry registry;
    JsonMapper mapper;

    @BeforeEach
    void setUp() {
        registry = new MetadataRegistry();
        mapper = new JsonMapper();
    }

    @Test
    public void testSimpleObject() throws JsonProcessingException {
        Blob blob = new Blob();
        blob.setName("Blob");
        blob.setSurname("Blobov");
        blob.setAge(5);


        String json = mapper.toJson(blob);

        System.out.println(json);

        Blob restoredBlob = (Blob) mapper.fromJson(json, Blob.class);

        assertEquals(blob.getName(), restoredBlob.getName());
        assertNull(restoredBlob.getSurname());
        assertEquals(blob.getAge(), restoredBlob.getAge());
    }

    @Test
    public void testNestedObject() throws JsonProcessingException {
        Address address = new Address();
        address.setCity("Novosibirsk");
        address.setStreet("Pirogova");
        address.setHouseNumber(1);

        Student anton = new Student();
        anton.setName("Anton");
        anton.setAge(13);
        anton.setHomeAddress(address);

        String antonJson = mapper.toJson(anton);

        System.out.println(antonJson);

        Student restoredAnton = (Student) mapper.fromJson(antonJson, Student.class);

        assertEquals(anton.getName(), restoredAnton.getName());
        assertEquals(anton.getAge(), restoredAnton.getAge());
        assertEquals(anton.getHomeAddress().getCity(), restoredAnton.getHomeAddress().getCity());
    }

    @Test
    public void testWritingJsonToFile() throws IOException {
        Blob blob = new Blob();
        blob.setName("Blob");
        blob.setSurname("Blobov");
        blob.setAge(5);


        File myFile = new File("blob.json");
        mapper.toJsonFile(myFile, blob);

        Blob restoredBlob = (Blob) mapper.fromJsonFile(myFile, Blob.class);

        assertEquals(blob.getName(), restoredBlob.getName());
        assertNull(restoredBlob.getSurname());
        assertEquals(blob.getAge(), restoredBlob.getAge());
    }

    @Test
    public void testNotSupportedType(){
        WrongClass wrongClass = new WrongClass();
        wrongClass.setStudentToAge(new ConcurrentHashMap<>());

        assertThrows(JsonMappingException.class, ()-> mapper.toJson(wrongClass));
    }

    @Test
    public void testHardObject() {

    }
}

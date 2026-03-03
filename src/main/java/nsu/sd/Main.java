package nsu.sd;

import nsu.sd.metadata.ClassMetadata;
import nsu.sd.testClasses.Address;
import nsu.sd.testClasses.Blob;
import nsu.sd.testClasses.Student;

public class Main {
    public static void main(String[] args) throws Exception {
        Blob blob = new Blob();
        blob.setName("Blob");
        blob.setSurname("Blobov");
        blob.setAge(5);

        MetadataRegistry registry = new MetadataRegistry();
        ClassMetadata md = registry.getClassMetadata(blob);
        System.out.println(md.getName() + "\n");

        JsonMapper mapper = new JsonMapper();

        String json = mapper.toJson(blob);
        System.out.println(json);

        Blob restoredBlob = (Blob) mapper.fromJson(json, Blob.class);
        System.out.println("\nRestored object: ");
        System.out.println("Name: " + restoredBlob.getName());
        System.out.println("Age: " + restoredBlob.getAge());
        System.out.println("Surname: " + restoredBlob.getSurname()); // Должно быть null

        // Вложенный объект
        Address address = new Address();
        address.setCity("Novosibirsk");
        address.setStreet("Pirogova");
        address.setHouseNumber(1);

        Student anton = new Student();
        anton.setName("Anton");
        anton.setAge(13);
        anton.setHomeAddress(address);

        String antonJson = mapper.toJson(anton);
        System.out.println("\n" + antonJson);

        Student restoredAnton = (Student) mapper.fromJson(antonJson, Student.class);
        System.out.println("\nRestored student: ");
        System.out.println("Name: " + restoredAnton.getName());
        System.out.println("Age: " + restoredAnton.getAge());
        System.out.println("Address: " + restoredAnton.getHomeAddress());
    }
}
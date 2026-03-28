package nsu.sd.lazy;

import nsu.sd.testClasses.NodeUser;
import nsu.sd.testClasses.Profile;
import nsu.sd.testClasses.UserWithLazyProfile;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class LazyJsonMapperTest {

    @Test
    void shouldDeserializeLazyFieldFromJsonString() throws Exception {
        LazyJsonMapper mapper = new LazyJsonMapper();

        String json = """
                {
                  "_className": "nsu.sd.testClasses.UserWithLazyProfile",
                  "name": "Alice",
                  "profile": {
                    "_className": "nsu.sd.testClasses.Profile",
                    "city": "Moscow",
                    "age": 20
                  }
                }
                """;

        UserWithLazyProfile user = (UserWithLazyProfile) mapper.fromJson(json, UserWithLazyProfile.class);

        assertNotNull(user);
        assertEquals("Alice", user.getName());

        assertNotNull(user.getProfile());
        assertNotEquals(Profile.class, user.getProfile().getClass());
        assertTrue(Profile.class.isAssignableFrom(user.getProfile().getClass()));

        assertEquals("Moscow", user.getProfile().getCity());
        assertEquals(20, user.getProfile().getAge());
    }

    @Test
    void shouldSerializeAndDeserializeThroughMapper() throws Exception {
        LazyJsonMapper mapper = new LazyJsonMapper();

        UserWithLazyProfile original =
                new UserWithLazyProfile("Alice", new Profile("Moscow", 20));

        String json = mapper.toJson(original);
        UserWithLazyProfile restored =
                (UserWithLazyProfile) mapper.fromJson(json, UserWithLazyProfile.class);

        assertNotNull(restored);
        assertEquals("Alice", restored.getName());

        assertNotNull(restored.getProfile());
        assertTrue(Profile.class.isAssignableFrom(restored.getProfile().getClass()));

        assertEquals("Moscow", restored.getProfile().getCity());
        assertEquals(20, restored.getProfile().getAge());
    }

    @Test
    void shouldWorkWithFiles() throws Exception {
        LazyJsonMapper mapper = new LazyJsonMapper();

        UserWithLazyProfile original =
                new UserWithLazyProfile("Bob", new Profile("SPb", 25));


        File tempFile = File.createTempFile("lazy-user", ".json");
        tempFile.deleteOnExit();

        mapper.toJsonFile(tempFile, original);

        UserWithLazyProfile restored =
                (UserWithLazyProfile) mapper.fromJsonFile(tempFile, UserWithLazyProfile.class);

        assertNotNull(restored);
        assertEquals("Bob", restored.getName());

        assertNotNull(restored.getProfile());
        assertEquals("SPb", restored.getProfile().getCity());
        assertEquals(25, restored.getProfile().getAge());
    }

    @Test
    void shouldResolveRefObjects() throws Exception {
        LazyJsonMapper mapper = new LazyJsonMapper();

        String json = """
                {
                  "_className": "nsu.sd.testClasses.NodeUser",
                  "@id": 1,
                  "name": "Alice",
                  "friend": {
                    "@ref": 1
                  }
                }
                """;

        NodeUser user = (NodeUser) mapper.fromJson(json, NodeUser.class);

        assertNotNull(user);
        assertEquals("Alice", user.getName());

        assertNotNull(user.getFriend());
        assertEquals("Alice", user.getFriend().getName());
    }

    @Test
    void shouldKeepNullLazyFieldAsNull() throws Exception {
        LazyJsonMapper mapper = new LazyJsonMapper();

        String json = """
                {
                  "_className": "nsu.sd.testClasses.UserWithLazyProfile",
                  "name": "Alice",
                  "profile": null
                }
                """;

        UserWithLazyProfile user = (UserWithLazyProfile) mapper.fromJson(json, UserWithLazyProfile.class);

        assertNotNull(user);
        assertEquals("Alice", user.getName());
        assertNull(user.getProfile());
    }

    @Test
    void shouldThrowWhenClassNameMissing() {
        LazyJsonMapper mapper = new LazyJsonMapper();

        String json = """
                {
                  "name": "Alice"
                }
                """;

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> mapper.fromJson(json, UserWithLazyProfile.class));

        assertTrue(ex.getMessage().contains("_className"));
    }

    @Test
    void shouldThrowWhenRefMissing() {
        LazyJsonMapper mapper = new LazyJsonMapper();

        String json = """
                {
                  "@ref": 999
                }
                """;

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> mapper.fromJson(json, NodeUser.class));

        assertTrue(ex.getMessage().contains("not found"));
    }
}
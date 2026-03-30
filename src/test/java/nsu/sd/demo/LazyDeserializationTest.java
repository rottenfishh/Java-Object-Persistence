package nsu.sd.demo;

import nsu.sd.MetadataRegistry;
import nsu.sd.lazy.LazyJsonMapper;
import nsu.sd.serializers.LazyCustomJsonDeserializer;
import nsu.sd.testClasses.Profile;
import nsu.sd.testClasses.UserWithLazyProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LazyDeserializationTest {
    private LazyJsonMapper mapper;
    private MetadataRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new MetadataRegistry();
        mapper = new LazyJsonMapper();

        LazyCustomJsonDeserializer.RESOLVED_OBJECTS.get().clear();
    }

    @Test
    void shouldDeserializeNonLazyFieldsNormally() throws Exception {
        String json = """
                {
                  "_className": "nsu.sd.testClasses.UserWithLazyProfile",
                  "name": "Alice"
                }
                """;

        Object result = mapper.fromJson(json, UserWithLazyProfile.class);

        assertNotNull(result);
        assertInstanceOf(UserWithLazyProfile.class, result);

        UserWithLazyProfile user = (UserWithLazyProfile) result;
        assertEquals("Alice", user.getName());
        assertNull(user.getProfile());
    }

    @Test
    void shouldCreateLazyProxyForLazyField() throws Exception {
        String json = """
                {
                  "_className": "nsu.sd.testClasses.UserWithLazyProfile",
                  "name": "Alice",
                  "profile": {
                    "city": "Moscow",
                    "age": 20
                  }
                }
                """;

        Object result = mapper.fromJson(json, UserWithLazyProfile.class);

        assertNotNull(result);
        UserWithLazyProfile user = (UserWithLazyProfile) result;

        assertEquals("Alice", user.getName());
        assertNotNull(user.getProfile());

        // проверяем, что это не обычный Profile, а proxy-класс
        assertNotEquals(Profile.class, user.getProfile().getClass());
    }

    @Test
    void shouldLoadLazyFieldOnFirstGetterCall() throws Exception {
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

        Object result = mapper.fromJson(json, UserWithLazyProfile.class);

        UserWithLazyProfile user = (UserWithLazyProfile) result;

        assertNotNull(user.getProfile());

        assertEquals("Moscow", user.getProfile().getCity());
        assertEquals(20, user.getProfile().getAge());
    }

    @Test
    void shouldSkipNullLazyField() throws Exception {
        String json = """
            {
              "_className": "nsu.sd.testClasses.UserWithLazyProfile",
              "name": "Alice",
              "profile": null
            }
            """;
        Object result = mapper.fromJson(json, UserWithLazyProfile.class);

        UserWithLazyProfile user = (UserWithLazyProfile) result;

        assertEquals("Alice", user.getName());
        assertNull(user.getProfile());
    }

}
package nsu.sd.demo;

import nsu.sd.tool.JsonParserTool;
import nsu.sd.tool.filter.AND;
import nsu.sd.tool.filter.Equals;
import nsu.sd.tool.filter.Expression;
import nsu.sd.tool.filter.GreaterThan;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilterTest {
    @Test
    void FilterTestFile() throws FileNotFoundException {
        FileInputStream fis = new FileInputStream("filtertest.json");
        FileOutputStream fos = new FileOutputStream("filtered.json");

        Expression filter = new AND(new Equals("name", "Alice"), new GreaterThan("age", 20.0));
        JsonParserTool.parse(fis, fos, filter);
    }

    @Test
    void reqFieldsTest() {
        Expression filter = new AND(new Equals("name", "Alice"), new GreaterThan("age", 20.0));

        assertTrue(filter.requiredFields().contains("name"));
    }

    @Test
    void nestedTest() throws IOException {
        String json = """
                [
                  {"name":"Alice",
                   "info": {
                    "age":20,
                    "city":"Moscow"
                   }
                  }, 
                  {"name":"Bob",
                   "info": {
                    "age":15,
                    "city":"Sain P"
                   }
                  },
                  {"name":"Peter",
                   "info": {
                    "age":18,
                    "city":"Moscow"
                   }
                  }
                ]
               \s""";

        Expression filter = new AND(new Equals("city", "Moscow"), new GreaterThan("age", 17.0));

        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        InputStream in= new ByteArrayInputStream(bytes);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        JsonParserTool.parse(in, out, filter);

        String result = out.toString(StandardCharsets.UTF_8);

        System.out.println(result);

        assertTrue(result.contains("Alice"));
        assertTrue(result.contains("Peter"));
    }
}

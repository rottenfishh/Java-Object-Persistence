package nsu.sd.demo;

import nsu.sd.tool.JsonParserTool;
import nsu.sd.tool.filter.AND;
import nsu.sd.tool.filter.Equals;
import nsu.sd.tool.filter.Expression;
import nsu.sd.tool.filter.GreaterThan;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

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
}

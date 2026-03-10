package nsu.sd.tool;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import nsu.sd.tool.filter.Expression;

import java.io.InputStream;
import java.util.*;

public class JsonParserTool {

    public static List<Map<String,Object>> parse(InputStream input, Expression filter) {

        List<Map<String,Object>> result = new ArrayList<>();
        Set<String> neededFields = filter.requiredFields();

        try {

            JsonFactory factory = new JsonFactory();
            JsonParser parser = factory.createParser(input);

            Map<String,Object> currentObject = null;
            String currentField = null;

            while(parser.nextToken() != null) {

                JsonToken token = parser.currentToken();

                if(token == JsonToken.START_OBJECT) {
                    currentObject = new HashMap<>();
                }

                if(token == JsonToken.FIELD_NAME) {
                    currentField = parser.currentName();
                }

                if(token.isScalarValue()) {

                    if(currentObject != null && neededFields.contains(currentField)) {
                        currentObject.put(currentField, parser.getValueAsString());
                    }

                }

                if(token == JsonToken.END_OBJECT) {

                    JsonKeysReader reader = new JsonKeysReader(currentObject);

                    if(filter.evaluate(reader)) {
                        result.add(currentObject);
                    }

                    currentObject = null;
                }
            }

        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }
}
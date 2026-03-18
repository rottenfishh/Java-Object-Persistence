package nsu.sd.tool;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import nsu.sd.tool.filter.Expression;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

public class JsonParserTool {

    public static void parse(InputStream input, OutputStream output, Expression filter) {

        Set<String> neededFields = filter.requiredFields();

        try {
            JsonFactory factory = new JsonFactory();

            JsonParser parser = factory.createParser(input);
            JsonGenerator generator = factory.createGenerator(output);

            generator.writeStartArray();

            Map<String,Object> currentObject;
            String currentField = null;

            while(parser.nextToken() != null) {

                if(parser.currentToken() == JsonToken.START_OBJECT) {
                    currentObject = new HashMap<>();

                    TokenBuffer tokenBuffer = new TokenBuffer(parser);
                    tokenBuffer.copyCurrentStructure(parser);

                    JsonParser p = tokenBuffer.asParser();

                    while (p.nextToken() != JsonToken.END_OBJECT) {
                        JsonToken token = p.currentToken();

                        if(token == JsonToken.FIELD_NAME) {
                            currentField = p.currentName();
                        }

                        if(token.isScalarValue()) {

                            if(neededFields.contains(currentField)) {
                                Object value;

                                JsonToken valueToken = p.currentToken();

                                switch (valueToken) {
                                    case VALUE_NUMBER_INT -> value = p.getIntValue();
                                    case VALUE_NUMBER_FLOAT -> value = p.getDoubleValue();
                                    case VALUE_TRUE, VALUE_FALSE -> value = p.getBooleanValue();
                                    default -> value = p.getText();
                                }

                                currentObject.put(currentField, value);
                            }
                        }
                    }

                    if(p.currentToken() == JsonToken.END_OBJECT) {

                        JsonKeysReader reader = new JsonKeysReader(currentObject);

                        if(filter.evaluate(reader)) {
                            tokenBuffer.serialize(generator);
                        }
                    }
                }
            }
            generator.writeEndArray();
            generator.close();

        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
package nsu.sd.lazy.indexing;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import nsu.sd.metadata.IndexClassMetadata;
import nsu.sd.metadata.IndexFieldMetadata;
import nsu.sd.tool.filter.Expression;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

public class IndexBuilder {
    public static List<IndexClassMetadata> parse(InputStream input, OutputStream output , Expression filter) {
        Set<String> neededFields = filter.requiredFields();
        List<IndexClassMetadata> metadata = new ArrayList<IndexClassMetadata>();
        try {
            IndexClassMetadata indexClassMetadata = new IndexClassMetadata();
            JsonFactory factory = new JsonFactory();

            JsonParser p = factory.createParser(input);
            String currentField = null;

            while(p.nextToken() != null) {

                if(p.currentToken() == JsonToken.START_OBJECT) {
                    indexClassMetadata.setOffset(p.currentTokenLocation().getByteOffset());

                    while (p.nextToken() != JsonToken.END_OBJECT) {
                        JsonToken token = p.currentToken();
                        if(token == JsonToken.FIELD_NAME) {
                            currentField = p.currentName();
                            if (currentField.equals("_className")) {
                                indexClassMetadata.setClassName(p.nextTextValue());
                            }
                            if (currentField.equals("@id")) {
                                Integer id = Integer.parseInt(p.nextTextValue());
                                indexClassMetadata.setObjectId(id);
                            }
                        }

                        if(token.isScalarValue()) {
                            if(neededFields.contains(currentField)) {
                                JsonToken valueToken = p.currentToken();
                                IndexFieldMetadata fieldMetadata = new IndexFieldMetadata();
                                fieldMetadata.setOffset(p.currentLocation().getByteOffset());
                                fieldMetadata.setValueToken(valueToken);
                                indexClassMetadata.getFields().put(currentField, fieldMetadata);
                            }
                        }
                    }

                    if(p.currentToken() == JsonToken.END_OBJECT) {
                        System.out.println("End Object");
                        metadata.add(indexClassMetadata);
                    }
                }
            }
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        return metadata;
    }
}

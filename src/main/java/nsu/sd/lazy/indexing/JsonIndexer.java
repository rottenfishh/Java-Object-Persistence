package nsu.sd.lazy.indexing;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import nsu.sd.metadata.IndexClassMetadata;
import nsu.sd.metadata.IndexFieldMetadata;
import nsu.sd.tool.filter.Expression;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class JsonIndexer {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static List<IndexClassMetadata> parse(InputStream input, OutputStream output, Expression filter) {
        Set<String> neededFields = filter.requiredFields();
        List<IndexClassMetadata> metadata = new ArrayList<>();

        try {
            JsonFactory factory = new JsonFactory();

            try (JsonParser p = factory.createParser(input)) {
                JsonToken firstToken = p.nextToken();
                if (firstToken == null) {
                    return metadata;
                }

                if (firstToken == JsonToken.START_ARRAY) {
                    while (p.nextToken() != JsonToken.END_ARRAY) {
                        if (p.currentToken() == JsonToken.START_OBJECT) {
                            metadata.add(parseSingleObject(p, neededFields));
                        } else {
                            p.skipChildren();
                        }
                    }
                } else if (firstToken == JsonToken.START_OBJECT) {
                    metadata.add(parseSingleObject(p, neededFields));
                } else {
                    throw new RuntimeException("Expected START_ARRAY or START_OBJECT, but got: " + firstToken);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while building index", e);
        }

        return metadata;
    }

    private static IndexClassMetadata parseSingleObject(JsonParser p, Set<String> neededFields) throws Exception {
        IndexClassMetadata objectMeta = new IndexClassMetadata();
        objectMeta.setOffset(p.currentTokenLocation().getByteOffset());

        Deque<PathEntry> stack = new ArrayDeque<>();
        String pendingFieldName = null;

        while (p.nextToken() != null) {
            JsonToken token = p.currentToken();

            if (token == JsonToken.FIELD_NAME) {
                pendingFieldName = p.currentName();
                continue;
            }

            if (token == JsonToken.START_OBJECT) {
                if (pendingFieldName != null) {
                    stack.addLast(PathEntry.objectField(pendingFieldName));
                    pendingFieldName = null;
                }
                continue;
            }

            if (token == JsonToken.START_ARRAY) {
                if (pendingFieldName != null) {
                    stack.addLast(PathEntry.arrayField(pendingFieldName));
                    pendingFieldName = null;
                }
                continue;
            }

            if (token == JsonToken.END_OBJECT) {
                if (stack.isEmpty()) {
                    long start = objectMeta.getOffset();
                    long endInclusive = p.currentLocation().getByteOffset();
                    objectMeta.setLength(endInclusive - start + 1);
                    break;
                }

                PathEntry last = stack.peekLast();
                if (last.kind == PathKind.OBJECT_FIELD) {
                    stack.removeLast();
                    incrementArrayIndexIfNeeded(stack);
                }
                continue;
            }

            if (token == JsonToken.END_ARRAY) {
                if (!stack.isEmpty() && stack.peekLast().kind == PathKind.ARRAY_FIELD) {
                    stack.removeLast();
                    incrementArrayIndexIfNeeded(stack);
                }
                continue;
            }

            if (token.isScalarValue()) {
                if (pendingFieldName != null) {
                    String fullPath = buildPath(stack, pendingFieldName);

                    if ("_className".equals(fullPath)) {
                        objectMeta.setClassName(readScalarValueAsString(p));
                    } else if ("@id".equals(fullPath)) {
                        objectMeta.setObjectId(readScalarValueAsInt(p));
                    }

                    if (neededFields.contains(fullPath) || neededFields.contains(pendingFieldName)) {
                        IndexFieldMetadata fieldMeta = new IndexFieldMetadata();
                        fieldMeta.setFullPath(fullPath);
                        fieldMeta.setFieldName(pendingFieldName);
                        fieldMeta.setOffset(p.currentTokenLocation().getByteOffset());
                        fieldMeta.setValueToken(token);
                        fieldMeta.setLength(computeScalarJsonLength(p, token));

                        if (!fullPath.contains(".") && !fullPath.contains("[")) {
                            objectMeta.getFields().put(pendingFieldName, fieldMeta);
                        }
                        objectMeta.getFields().put(fullPath, fieldMeta);
                    }

                    pendingFieldName = null;
                    incrementArrayIndexIfNeeded(stack);
                } else {
                    incrementArrayIndexIfNeeded(stack);
                }
            }
        }

        return objectMeta;
    }

    private static long computeScalarJsonLength(JsonParser p, JsonToken token) throws Exception {
        return switch (token) {
            case VALUE_STRING -> OBJECT_MAPPER.writeValueAsBytes(p.getText()).length;
            case VALUE_NUMBER_INT, VALUE_NUMBER_FLOAT,
                 VALUE_TRUE, VALUE_FALSE, VALUE_NULL ->
                    p.getText().getBytes(StandardCharsets.UTF_8).length;
            default -> throw new IllegalArgumentException("Unsupported scalar token: " + token);
        };
    }

    private static String buildPath(Deque<PathEntry> stack, String leafField) {
        StringBuilder sb = new StringBuilder();

        for (PathEntry entry : stack) {
            if (entry.kind == PathKind.OBJECT_FIELD) {
                if (!sb.isEmpty()) {
                    sb.append('.');
                }
                sb.append(entry.name);
            } else if (entry.kind == PathKind.ARRAY_FIELD) {
                if (!sb.isEmpty()) {
                    sb.append('.');
                }
                sb.append(entry.name).append('[').append(entry.arrayIndex).append(']');
            }
        }

        if (leafField != null) {
            if (!sb.isEmpty()) {
                sb.append('.');
            }
            sb.append(leafField);
        }

        return sb.toString();
    }

    private static void incrementArrayIndexIfNeeded(Deque<PathEntry> stack) {
        if (!stack.isEmpty() && stack.peekLast().kind == PathKind.ARRAY_FIELD) {
            stack.peekLast().arrayIndex++;
        }
    }

    private static String readScalarValueAsString(JsonParser p) throws Exception {
        if (p.currentToken() == JsonToken.VALUE_NULL) {
            return null;
        }
        return p.getValueAsString();
    }

    private static Integer readScalarValueAsInt(JsonParser p) throws Exception {
        if (p.currentToken() == JsonToken.VALUE_NULL) {
            return null;
        }
        return p.getIntValue();
    }

    private enum PathKind {
        OBJECT_FIELD,
        ARRAY_FIELD
    }

    private static final class PathEntry {
        private final PathKind kind;
        private final String name;
        private int arrayIndex;

        private PathEntry(PathKind kind, String name, int arrayIndex) {
            this.kind = kind;
            this.name = name;
            this.arrayIndex = arrayIndex;
        }

        static PathEntry objectField(String name) {
            return new PathEntry(PathKind.OBJECT_FIELD, name, -1);
        }

        static PathEntry arrayField(String name) {
            return new PathEntry(PathKind.ARRAY_FIELD, name, 0);
        }
    }
}
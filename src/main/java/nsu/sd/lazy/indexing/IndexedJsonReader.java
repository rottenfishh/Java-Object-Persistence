package nsu.sd.lazy.indexing;

import com.fasterxml.jackson.databind.ObjectMapper;
import nsu.sd.metadata.IndexClassMetadata;
import nsu.sd.metadata.IndexFieldMetadata;

import java.io.IOException;
import java.io.RandomAccessFile;

public class IndexedJsonReader {
    private final String filePath;
    private final ObjectMapper objectMapper;

    public IndexedJsonReader(String filePath, ObjectMapper objectMapper) {
        this.filePath = filePath;
        this.objectMapper = objectMapper;
    }

    public byte[] readRawObject(IndexClassMetadata meta) {
        if (meta.getOffset() == null || meta.getLength() == null) {
            throw new IllegalArgumentException("Object offset/length is missing");
        }

        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {
            long offset = meta.getOffset();
            long length = meta.getLength();

            if (length > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Object is too large to read into byte[]");
            }

            byte[] bytes = new byte[(int) length];
            raf.seek(offset);
            raf.readFully(bytes);
            return bytes;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read object by offset", e);
        }
    }

    public Object readObject(IndexClassMetadata meta) {
        try {
            byte[] bytes = readRawObject(meta);

            if (meta.getClassName() == null) {
                return objectMapper.readTree(bytes);
            }

            Class<?> clazz = Class.forName(meta.getClassName());
            return objectMapper.readValue(bytes, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize indexed object", e);
        }
    }

    public <T> T readObject(IndexClassMetadata meta, Class<T> clazz) {
        try {
            byte[] bytes = readRawObject(meta);
            return objectMapper.readValue(bytes, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize indexed object", e);
        }
    }

    public Object readField(IndexFieldMetadata meta) {
        return readField(meta, Object.class);
    }

    public Object readField(IndexFieldMetadata meta, Class<?> targetType) {
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {
            byte[] bytes = new byte[Math.toIntExact(meta.getLength())];
            raf.seek(meta.getOffset());
            raf.readFully(bytes);

            if (targetType == null || targetType == Object.class) {
                return objectMapper.readValue(bytes, Object.class);
            }

            if (targetType.isPrimitive()) {
                targetType = wrapPrimitive(targetType);
            }

            return objectMapper.readValue(bytes, targetType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read field: " + meta.getFullPath(), e);
        }
    }

    private Class<?> wrapPrimitive(Class<?> type) {
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == double.class) return Double.class;
        if (type == float.class) return Float.class;
        if (type == boolean.class) return Boolean.class;
        if (type == byte.class) return Byte.class;
        if (type == short.class) return Short.class;
        if (type == char.class) return Character.class;
        return type;
    }
}
package nsu.sd.tool;

import nsu.sd.lazy.LazyObjectFilter;
import nsu.sd.lazy.json.LazyProxyFactory;
import nsu.sd.lazy.json.LazyState;
import nsu.sd.lazy.indexing.IndexedJsonReader;
import nsu.sd.metadata.IndexClassMetadata;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class LazyFilter {

    public static <T> void filter(
            List<IndexClassMetadata> index,
            Class<T> clazz,
            IndexedJsonReader reader,
            LazyObjectFilter<T> filter,
            OutputStream output
    ) {
        try {
            output.write('[');

            boolean first = true;

            for (IndexClassMetadata meta : index) {
                LazyState state = new LazyState(meta, clazz);
                T proxy = LazyProxyFactory.createProxy(clazz, state, reader);

                boolean accepted;
                try {
                    accepted = filter.test(proxy);
                } catch (Exception e) {
                    throw new RuntimeException("Filter evaluation failed for object at offset " + meta.getOffset(), e);
                }

                if (!accepted) {
                    continue;
                }

                byte[] raw = reader.readRawObject(meta);

                if (!first) {
                    output.write(',');
                }
                output.write(raw);
                first = false;
            }

            output.write(']');
            output.flush();
        } catch (IOException e) {
            throw new RuntimeException("Failed to write filtered JSON", e);
        }
    }

    public static <T> String filterToString(
            List<IndexClassMetadata> index,
            Class<T> clazz,
            IndexedJsonReader reader,
            LazyObjectFilter<T> filter
    ) {
        try (java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
            filter(index, clazz, reader, filter, baos);
            return baos.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
package nsu.sd.lazy;

import nsu.sd.lazy.indexing.IndexedJsonReader;
import nsu.sd.metadata.IndexClassMetadata;

import java.util.ArrayList;
import java.util.List;

public class LazyObjectCollector {

    public static <T> List<T> collect(
            List<IndexClassMetadata> index,
            Class<T> clazz,
            IndexedJsonReader reader,
            LazyObjectFilter<T> filter
    ) {
        List<T> result = new ArrayList<>();

        for (IndexClassMetadata meta : index) {
            LazyState state = new LazyState(meta, clazz);
            T proxy = LazyProxyFactory.createProxy(clazz, state, reader);

            if (filter.test(proxy)) {
                result.add(proxy);
            }
        }

        return result;
    }
}
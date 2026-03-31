package nsu.sd.lazy;

@FunctionalInterface
public interface LazyObjectFilter<T> {
    boolean test(T object);
}

package mapper;

public interface Mapper<R, V> {
    R map(V value);
}

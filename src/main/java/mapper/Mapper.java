package mapper;

public interface Mapper<V, R> {
    R map(V value) throws Exception;
}

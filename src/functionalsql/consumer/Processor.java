package functionalsql.consumer;

@FunctionalInterface
public interface Processor<T> {

    void process(T t) throws Exception;
}

package net.jimblackler.Utils;

/**
 * Defines a class that collects values of type T and submits each value to a ResultHandler<>
 * object immediately on collection.
 */
public interface Collector<T> {

    /**
     * Perform the collection operation.
     *
     * @param handler The processor object to return results to.
     * @throws CollectionAbortedException The collection operation was aborted part way through.
     */
    void collect(ResultHandler<T> handler) throws CollectionAbortedException;
}

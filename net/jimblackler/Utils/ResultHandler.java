package net.jimblackler.Utils;

/**
 * Defines objects that handle results from a Collector&lt;&gt;, with a function called immediately as
 * each value is gathered.
 */
public interface ResultHandler<T> {

    /**
     * This method is called by collectors whenever a result is collected.
     *
     * @param value The collected result
     * @exception CollectionAbortedException The client code requests that the collection is aborted
     */
    void handleResult(T value) throws CollectionAbortedException;
}

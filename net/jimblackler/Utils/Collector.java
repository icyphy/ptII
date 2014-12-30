package net.jimblackler.Utils;

import ptolemy.kernel.util.IllegalActionException;

/**
 * Defines a class that collects values of type T and submits each value to a ResultHandler&lt;&gt;
 * object immediately on collection.
 */
public interface Collector<T> {

    /**
     * Perform the collection operation.
     *
     * @param handler The processor object to return results to.
     * @exception CollectionAbortedException The collection operation was aborted part way through.
     * @exception IllegalActionException
     */
    void collect(ResultHandler<T> handler) throws CollectionAbortedException,
            IllegalActionException;
}

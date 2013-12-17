package net.jimblackler.Utils;

import ptolemy.kernel.util.IllegalActionException;

/**
 * A class to convert methods that implement the Collector<> class into a standard Iterable<>.
 */
public interface YieldAdapter<T> {

    YieldAdapterIterable<T> adapt(Collector<T> client) throws IllegalActionException;
}

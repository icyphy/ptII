package net.jimblackler.Utils;

/**
 * A class to convert methods that implement the Collector<> class into a standard Iterable<>.
 */
public interface YieldAdapter<T> {

    YieldAdapterIterable<T> adapt(Collector<T> client);
}

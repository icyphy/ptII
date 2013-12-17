package net.jimblackler.Utils;

/**
 * A special version of Iterable<> that returns YieldAdapterIterators<>.
 */
public interface YieldAdapterIterable<T> extends Iterable<T> {

    /**
     * Returns an iterator over the results.
     */
    YieldAdapterIterator<T> iterator();
    
}

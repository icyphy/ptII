package net.jimblackler.Utils;

/**
 * A special version of Iterable&lt;&gt; that returns YieldAdapterIterators&lt;&gt;.
 */
public interface YieldAdapterIterable<T> extends Iterable<T> {

    /**
     * Returns an iterator over the results.
     */
    @Override
    YieldAdapterIterator<T> iterator();

}

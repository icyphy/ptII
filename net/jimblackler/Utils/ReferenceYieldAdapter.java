package net.jimblackler.Utils;

import java.util.ArrayList;
import java.util.Iterator;

import ptolemy.kernel.util.IllegalActionException;

/**
 * This reference adapter simply invokes the Collector&lt;&gt;, first gathering the results into a list.
 * It is provided to illustrate the simplicity of the function of the adapter, and to aid debugging
 * if threading issues are suspected as the cause of problems in the calling code.
 *
 * @author Jim Blackler (jimblackler@gmail.com)
@version $Id$
@since Ptolemy II 10.0
 */
public class ReferenceYieldAdapter<T> implements YieldAdapter<T> {

    /**
     * Convert a method that implements the Collector&lt;&gt; class with a standard Iterable&lt;&gt;, by
     * collecting the results in a list, and returning an iterator to that list.
     * @exception IllegalActionException
     */
    @Override
    public YieldAdapterIterable<T> adapt(Collector<T> client)
            throws IllegalActionException {

        final ArrayList<T> results = new ArrayList<T>();

        try {
            client.collect(new ResultHandler<T>() {
                @Override
                public void handleResult(T value) {
                    results.add(value);
                }
            });
        } catch (CollectionAbortedException e) {
            // The process was aborted by calling code.
        }

        // Wrap container's iterator with yield adapter interface for compatibility
        return new YieldAdapterIterable<T>() {
            @Override
            public YieldAdapterIterator<T> iterator() {
                final Iterator<T> iterator = results.iterator();
                return new YieldAdapterIterator<T>() {
                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public T next() {
                        return iterator.next();
                    }

                    @Override
                    public void remove() {
                        iterator.remove();
                    }

                    @Override
                    public void dispose() {
                        // Does nothing in this implementation
                    }
                };
            }
        };

    }
}

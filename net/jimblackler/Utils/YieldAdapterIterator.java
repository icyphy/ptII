package net.jimblackler.Utils;

import java.util.Iterator;

import ptolemy.kernel.util.IllegalActionException;

/**
 * A version of a standard Iterator&lt;&gt; used by the yield adapter. The only addition is a dispose()
 * function to clear resources manually when required.
 */
public abstract class YieldAdapterIterator<T> implements Iterator<T> {

    /**
     * Because the Yield Adapter starts a separate thread for duration of the collection, this can
     * be left open if the calling code only reads part of the collection. If the iterator goes out
     * of scope, when it is GCed its finalize() will close the collection thread. However garbage
     * collection is sporadic and the VM will not trigger it simply because there is a lack of
     * available threads. So, if a lot of partial reads are happening, it will be wise to manually
     * close the iterator (which will clear the resources immediately).
     */
    public abstract void dispose();

    public IllegalActionException getMessageIllegalAction() {
        return messageIllegalAction;
    }

    protected IllegalActionException messageIllegalAction = null;

}

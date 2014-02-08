package net.jimblackler.Utils;

/**
 * An exception class that can be thrown by collectors or results handlers in order to abort or
 * signal abortion of the collecting process, for any reason.
 */
@SuppressWarnings("serial")
public class CollectionAbortedException extends Exception {

    public CollectionAbortedException() {
    }

    public CollectionAbortedException(String message) {
        super(message);
    }

    public CollectionAbortedException(String message, Throwable cause) {
        super(message, cause);
    }

    public CollectionAbortedException(Throwable cause) {
        super(cause);
    }
}

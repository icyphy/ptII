package ptolemy.actor.gt;

import ptolemy.kernel.util.KernelException;

public class GraphTransformationException extends KernelException {

    public GraphTransformationException() {
    }

    public GraphTransformationException(String message) {
        super(null, null, null, message);
    }

    public GraphTransformationException(String message, Throwable cause) {
        super(null, null, cause, message);
    }

    public GraphTransformationException(Throwable cause) {
        super(null, null, cause, null);
    }

    private static final long serialVersionUID = -7656582035822778342L;

}

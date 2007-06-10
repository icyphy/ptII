package ptolemy.actor.gt;

import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Nameable;

public class RuleValidationException extends KernelException {
    
    public RuleValidationException(String message) {
        super(null, null, message);
    }
    
    public static String generateMessage(Nameable object1, Nameable object2,
            Throwable cause, String detail) {
        return detail;
    }

    private static final long serialVersionUID = -5895146104515190527L;

}

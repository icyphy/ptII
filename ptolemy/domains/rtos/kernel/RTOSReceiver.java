
package ptolemy.domains.rtos.kernel;

import ptolemy.actor.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;


public class RTOSReceiver extends DEReceiver {

    public synchronized void put(Token token) {
        try {
            RTOSDirector dir = (RTOSDirector)getDirector();
            IOPort port = getContainer();
            Parameter priority = (Parameter)port.getAttribute("priority");
            if (priority == null) {
                priority = (Parameter)((NamedObj)port.getContainer()).getAttribute("priority");
            }
            double priorityValue = 10.0;
            if (priority != null) {
                priorityValue = ((DoubleToken)priority.getToken()).doubleValue();
            }
            dir._enqueueEvent(this, token, priorityValue);
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(ex.toString());
        }
    }

    public synchronized void put(Token token, double time)
            throws IllegalActionException{
       put(token);
   }
}


package ptolemy.domains.sdf.lib;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;

import java.util.Enumeration;
import collections.LinkedList;
import ptolemy.domains.sdf.kernel.*;

public class testlistener implements ExecutionListener {

    /** Called to report an execution failure
     */
    public void executionError(ExecutionEvent event) {
        Debug.println("testlistener: executionError");
        Exception e = event.getException();
        e.printStackTrace();
    }

    /** Called to report a successful pause of execution
     */
    public void executionPaused(ExecutionEvent event) {
        Debug.println("testlistener: executionPaused");
    }
    
    /** Called to report a successfull resumption of execution
     */
    public void executionResumed(ExecutionEvent event) {
        Debug.println("testlistener: executionResumed");
    }

    /** Called to report a successful start of execution
     */
    public void executionStarted(ExecutionEvent event) {
        Debug.println("testlistener: executionStarted");
    }

    /** Called to report a successful termination of execution.
     */    
    public void executionTerminated(ExecutionEvent event) {
        Debug.println("testlistener: executionTerminated");
    } 
    
    /** Called to report that the current iteration finished and 
     *  the wrapup sequence completed normally.
     */
    public void executionFinished(ExecutionEvent event) {
        Debug.println("testlistener: executionWrappedup");
    }

    /** Called to report that a toplevel iteration has begun
     */
    public void executionIterationStarted(ExecutionEvent event) {
        Debug.println("testlistener: executionIterationStarted");
    }

}

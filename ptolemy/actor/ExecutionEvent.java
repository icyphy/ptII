
package ptolemy.actor;

public class ExecutionEvent {

    public ExecutionEvent(Manager m) {
        _manager = m;
        _iteration = 0;
        _exception = null;
    }

    /** Create a new event that occurs during the specified toplevel
     *  iteration of the specified Manager's execution.
     */
     public ExecutionEvent(Manager m, int iteration) {
        _manager = m;
        _iteration = iteration;
        _exception = null;
    }

    public ExecutionEvent(Manager m, int iteration, Exception e) {
        _manager = m;
        _iteration = iteration;
        _exception = e;
    }
    
    /** Returns the Manager that generated the event
     */
    public Manager getManager() {
        return _manager;
    }

    /** Returns the number of the toplevel iteration that the event 
     *  was generated on
     */
    public int getIteration() {
        return _iteration;
    }
    
    public Exception getException() {
        return _exception;
    }

    private Manager _manager;
    private int _iteration;
    private Exception _exception;
}

package ptolemy.actor;

public interface ExecutionListener {

    /** Called to report an execution failure
     */
    public void executionError(ExecutionEvent event);

    /** Called to report a successful pause of execution
     */
    public void executionPaused(ExecutionEvent event);
    
    /** Called to report a successfull resumption of execution
     */
    public void executionResumed(ExecutionEvent event);

    /** Called to report a successful start of execution
     */
    public void executionStarted(ExecutionEvent event);

    /** Called to report a successful termination of execution.
     */    
    public void executionTerminated(ExecutionEvent event);
    
    /** Called to report that the current iteration finished and 
     *  the wrapup sequence completed normally.
     */
    public void executionWrappedup(ExecutionEvent event);

    /** Called to report that a toplevel iteration has begun
     */
    public void executionIterationStarted(ExecutionEvent event);

}

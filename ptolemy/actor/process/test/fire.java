


/**
 */
public class fire extends ProcessDirector {
    /** Psuedocode. 
     *  Includes all functionality. Does not include efficient
     *  function calls.
     */
    public void testFire1() throws IllegalActionException {
        _continueFireMethod = true;
        synchronized(this) {
            while( _continueFireMethod ) {
                _continueFireMethod = false;
                while( !_areActorsDeadlocked() && !_areActorsStopped() ) {
                    workspace.wait(this);
                }
                
                if( _areActorsDeadlocked() ) {
                    if( externalReadBlock ) {
			// Since this is an external read block
			// we might as well wait to see what
			// happens. Either the input will block
			// or the input will not block and the
			// actors will no longer be deadlocked.
                        while( _areActorsDeadlocked() && !inputBlocked() ) {
                            workspace.wait();
                        }

                        while( _areActorsDeadlocked() && inputBlocked() ) {
			    // Reaching this point means that both the
			    // actors and the input have blocked. 
			    // However, it is possible that the input
			    // could awaken resulting in an end to the
			    // external read block
                            if( execDir instanceof ProcessDirector ) {
				// Since the higher level actor is a process
				// let's register a block and wait.
                                execDir.registerBlockedBranchReceivers();
                                workspace.wait();
                            } else {
				// Since the higher level actor is not a 
				// process, let's end this iteration and
				// request another iteration. Recall that
				// calling stopInputBranchController()
				// is a blocking call that when finished
				// will guarantee that the (input) branches
				// will not restart during this iteration.
                                stopInputBranchController();

                                if( _areActorsDeadlocked() ) {
                                    _notDone = false;
                                    return;
                                } else if( !_areActorsDeadlocked() ) {
				    // It is possible that prior to
				    // stopping the branch controller
				    // a token stuck that caused the
				    // deadlocked actors to awaken.
                         	    _continueFireMethod = true;
                                }
                            }
                        }
                    } else {
			// Since this is not an external read block, 
			// we know that addition input data will do
			// no good to resolve the deadlocked actors.
			// Hmmm...FIXME
			// Attempt to resolve deadlock. If resolution
			// is not possible, then end iteration and
			// postfire() = false.
                    }
                    if( !_areActorsDeadlocked() || !inputBlocked() ) {
			// Reaching this point means that the
			// fire method should continue
                        _continueFireMethod = true;
                    }
                } else if( _areActorsStopped() ) {
                    stopInputBranchController();
                    _continueFireMethod = false;
                    _notDone = true;
                }
            }
        }
    }

    /** Psuedocode. 
     *  Assumes external read blocks. Does not include efficient
     *  function calls.
     */
    public void testFire2() throws IllegalActionException {
        _continueFireMethod = true;
        synchronized(this) {
            while( _continueFireMethod ) {
                _continueFireMethod = false;
                while( !_areActorsDeadlocked() && !_areActorsStopped() ) {
                    workspace.wait(this);
                }
                
                if( _areActorsDeadlocked() ) {
                    // This avoids a race condition.
                    if( externalReadBlock ) {
                        while( _areActorsDeadlocked() && !inputBlocked() ) {
                            workspace.wait();
                        }
                    }
                    while( _areActorsDeadlocked() && inputBlocked() ) {
                        if( execDir instanceof ProcessDirector ) {
                            execDir.registerBlockedBranchReceivers();
                            workspace.wait();
                        } else {
                            _notDone = false;
                            return;
                        }
                    }
                    if( !_areActorsDeadlocked() || !inputBlocked() ) {
                        _continueFireMethod = true;
                    }
                } else {
                    stopInputBranchController();
                    while( !_areActorsStopped() || !inputBlocked() ) {
                        workspace.wait(this);
                    }
                    _notDone = true;
                }
            }
        }
    }

    private boolean _continueFireMethod = true;
    
}

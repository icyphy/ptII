


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
                        // This avoids a race condition.
                        while( _areActorsDeadlocked() && !inputBlocked() ) {
                            workspace.wait();
                        }
                        while( _areActorsDeadlocked() && inputBlocked() ) {
                            if( execDir instanceof ProcessDirector ) {
                                execDir.registerBlockedBranchReceivers();
                                workspace.wait();
                            } else {
                                stopInputBranchController();
                                if( _areActorsDeadlocked() ) {
                                    _notDone = false;
                                    return;
                                } else if( !_areActorsDeadlocked() ) {
                         	    _continueFireMethod = true;
                                }
                                _notDone = false;
                                return;
                            }
                        }
                    } else {
                        
                    }
                    if( !_areActorsDeadlocked() || !inputBlocked() ) {
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




/** Psuedocode. 
 *  Includes all functionality. Does not include efficient
 *  function calls.
 */
public class fire extends ProcessDirector {
    /**
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

    /**
     */
    public void testFire2() throws IllegalActionException {
        _continueFireMethod = true;
        synchronized(this) {
            while( _continueFireMethod ) {
                _continueFireMethod = false;
                while( !_areActorsDeadlocked() && !_areActorsStopped() ) {
                    workspace.wait(this);
                }
            
                if( externalReadBlock ) {
                    while( _areActorsDeadlocked() && !inputBlocked() ) {
                        workspace.wait();
                    }
                }
                
                while( _areActorsDeadlocked() && inputBlocked() ) {
                    if( externalReadBlock && (execDir instanceof ProcessDirector ) {
                        execDir.registerBlockedBranchReceivers();
                        workspace.wait();
                    } else if( externalReadBlock ) {
                        _notDone = true;
                        return;
                    } else {
                        _notDone = false;
                        return;
                    }
                }
                
                if( !_areActorsDeadlocked && inputBlocked() ) {
                    _continueFireMethod = true;
                }
            }
        }
    }

    private boolean _continueFireMethod = true;
    
}

/* The director for Design Flow Management (DFM) 

 Copyright (c) 1998-1999 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.
 
                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

*/

package ptolemy.domains.dfm.kernel;

import ptolemy.domains.pn.kernel.*;
import ptolemy.domains.dfm.data.*;
import ptolemy.domains.dfm.lib.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.event.*;
import ptolemy.actor.*;
import ptolemy.actor.process.*;
import ptolemy.actor.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// DFMDirector
/** 
This is director for the DFM domain.  It is derived from PNDirector in the pn
domain.  Since DFM operates generally like timed-PN, except : <p>
* Source actor will do a fire-and-delay.  The trigger to increment the clock 
  is the if there are new tokens produced for the next iteration. 
* A flag is set in the director, if the next iteration tokens are produced.
  This flag is also set if parameter of the actor is changed.
* In <code> resolveDeakLock() </code> that flag is checked, then the clock is
  advanced/not advanced accordingly.
<p> 
@author William Wu (wbwu@eecs.berkeley.edu)
@version: $id$ 
@date: 12/3/98
*/

public class DFMDirector extends PNDirector {

    /** Constructor
     * Construct a PN director.
     */	
    public DFMDirector() {
       super();
    }

    /** Constructor.  Constructor a director with a name.  Uses 
     * PNDirector constructor.
     * @param name Name of this director.
     */	
    public DFMDirector(String name) {
       super(name);
    }

    /** Constructor.  Constructor a director with a name and given workspace.  Uses 
     * PNDirector constructor.
     * @param workspace object for synchronizing
     * @param name Name of this director.
     */	
    public DFMDirector(Workspace workspace, String name) {
       super(workspace, name);
    }
    

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public void fire()
              throws IllegalActionException {
        _waitForNextIter = false;
        super.fire();
System.out.println("finishing Director firing method");
    }
   
    public Receiver newReceiver(){
        Receiver rec = super.newReceiver();
        _allRecQueue.insertFirst(rec); 
        return rec; 
    }
  
    /** The token for next iteration is produced.  Only if the token
     * with a "New" tag no the next iteration will produce a ok signal
     * to continue the next iteration. 
     * @param tag string tag of the token for next iteration.
     */	
    public void nextIterTokenProduced(String tag) {
        if ((tag.equals("New")) || (tag.equals("Annotate"))){
            setNextIter(true);
        }
    }
    
    public void setNextIter(boolean nextIter){
        synchronized(this){
            _nextIter = nextIter;
                System.out.println("next iteration ready ");
            notifyAll();
        }
    }

    public void dfmResume(){
        _flushAllReceivers();
        _fireInitTokens();
        setNextIter(true);
    }
    
    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** Handling the responsibility of detecting and resolve DFM dead lock.
     *  A dead lock occurs in DFM when all actors are blocked on read or delay.
     *  This method is a modification on the handle deadlock method in PN director
     *  , mutation is removed, artifical deadlock is will throw an exception.
     * 
     * @see ptolemy.domains.pn.PNDirector#_handleDeadlock()
     * @return boolean for indicating the termination of the excution due to real deadlock 
     * @exception IllegalActionException throw when artifical dead lock occurs 
     */	
    protected boolean _handleDeadlock() throws IllegalActionException {
        if (_writeBlockCount==0) {
            //Check if there are any events in the future.
            //FIXME: Is this the right way?
            if (_delayBlockCount ==0) {
                System.out.println("real deadlock. Everyone would be erased");
                _notdone = false;
                return true;
            } else {
                
                //Advance time to next possible time.
                //check for if the next iteration flag is set or not.
                System.out.println("delay deadlock. check for next iteration ready");
                synchronized(this){
                    try {
                System.out.println("wait for next iteration ready");
                        if (!_nextIter) {
                              _waitForNextIter = true;  
                        }
                        while (!_nextIter){ 
                           wait();
                        }
                        _waitForNextIter = false;  
                    } catch (InterruptedException e) {}

                }

                System.out.println("done waitting ");
                setNextIter(false);

                // Continue only if the next iteration flag is set.
                // The flag is set when the token is produced for the next iteration
                // on the feedback loop, or a parameter is changed.

                synchronized(this) {
                    try {
                        _eventQueue.take();
                        _currenttime = ((Double)(_eventQueue.getPreviousKey())).doubleValue();
                        _delayUnblock();
                    } catch (IllegalAccessException e) {
                        throw new IllegalActionException(this, "Inconsistency"+
                                " in number of actors blocked on delays count"+
                                " and the entries in the CalendarQueue");
                    }
 
                    boolean sametime = true;
                    while (sametime) {
                        if (!_eventQueue.isEmpty()) {
                            try {
                                Actor actor = (Actor)_eventQueue.take();
 
                                double newtime = ((Double)(_eventQueue.getPreviousKey())).doubleValue();
                                if (newtime != _currenttime) {
                                    _eventQueue.put(new Double(newtime), actor);
                                    sametime = false;
                                } else {
                System.out.println("undelaying... ");
                                    _delayUnblock();
                                }
                            } catch (IllegalAccessException e) {
                                throw new InternalErrorException(e.toString());
                            }
                        } else {
                            sametime = false;
                        }
                    }
                    //Wake up all delayed actors
                    notifyAll();
                }
            }
        } else {
            //its an artificial deadlock;
            System.out.println("Artificial deadlock");
            // _deadlock = false;
            // find the input port with lowest capacity queue;
            // that is blocked on a write and increment its capacity;
            // since DFM all queue is a single size
    //         _incrementLowestWriteCapacityPort();
            //System.out.println("Incrementing capacity done");
        }
        return false;
        
    }

    public boolean isWaitForNextIteration(){
        return _waitForNextIter;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////


    protected boolean _nextIter;
    protected boolean _waitForNextIter = true;
    protected LinkedList _allRecQueue = new LinkedList();

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void _flushAllReceivers(){
        for (int i=0;i<_allRecQueue.size();i++){
             PNQueueReceiver rec = (PNQueueReceiver) _allRecQueue.at(i);
             rec.clear();
        }
    }

    // fires all the init token of the feedback loop
    private void _fireInitTokens(){
        for (int i=0;i<_allRecQueue.size();i++){
             PNQueueReceiver rec = (PNQueueReceiver) _allRecQueue.at(i);
             DFMActor actor = (DFMActor)(rec.getContainer().getContainer()); 
             if (actor.isFeedbackActor()){
                  DFMFeedbackActor feedback = (DFMFeedbackActor) actor;
                  feedback.sendInitToken();
             }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

}

/* Thread class for process oriented domains.

 Copyright (c) 1998 The Regents of the University of California.
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

package ptolemy.actor;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// ProcessThread
/** 
This thread is created to execute the actor iteration methods in a 
seperate thread. 
This calls the prefire(), fire() and postfire() method of the 
embedded actor.


@author Mudit Goel, Neil Smyth
@version $Id$
*/
public class ProcessThread extends PtolemyThread {

    /** Construct a thread to be used for the execution of the 
     *  iteration methods of the actor. 
     *  @param actor The actor that needs to be executed.
     *  @param director The director responsible for the execution of this 
     *  actor.
     */
    public ProcessThread(Actor actor, ProcessDirector director) {
        super();
	_actor = actor;
        _director = director;
    }

    /** Construct a thread to be used for the execution of the 
     *  iteration methods of the actor. 
     *  @param actor The actor that needs to be executed.
     *  @param director The director responsible for the execution of this 
     *  actor.
     *  @param name The name of the thread.
     */
    public ProcessThread(Actor actor, ProcessDirector director, String name) {
        super(name);
	_actor = actor;
        _director = director;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** This returns the actor being executed by this thread
     *  @return The actor being executed by this thread.
     */
    public Actor getActor() {
	return _actor;
    }
    
    /** This returns the director that started this thread.
     *  @return The director that started this thread.
     */
    public ProcessDirector getDirector() {
	return _director;
    }

    /** This initializes the actor, and iterates it through the execution 
     *  cycle till it terminates.
     */
    public void run() {
	try {
	    while (_iterate());		   
	    // System.out.println(((Entity)_actor).getName()+" finished iterations");	    
	} catch (TerminateProcessException e) {
        } catch (IllegalActionException e) {
            ((CompositeActor)_director.getContainer()).getManager().fireExecutionError(e);
        } catch (NameDuplicationException e) {
            ((CompositeActor)_director.getContainer()).getManager().fireExecutionError(e);
        } finally {
            try {
                _actor.wrapup();
            } catch (IllegalActionException e) {
                // FIXME:
            }
            _director.decreaseActiveCount();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    //container is checked for null to detect the termination of the actor
    private boolean _iterate() 
	    throws NameDuplicationException, IllegalActionException {
	if (((Entity)_actor).getContainer()!=null && _actor.prefire()) {
            _actor.fire();
     	    return _actor.postfire();
	}
	return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    private Actor _actor;
    private ProcessDirector _director;

}



















































































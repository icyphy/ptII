/* Director governs the execution of a CompositeActor

 Copyright (c) 1997 The Regents of the University of California.
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

package pt.actors;
import pt.kernel.*;
import collections.LinkedList;
import java.util.Enumeration;
//import java.util.*;


//////////////////////////////////////////////////////////////////////////
//// Director
/** 
A Director governs the execution of a CompositeActor. This should be 
associated with the top-level container of every executable application
@author Mudit Goel
@version $Id$
*/
public class Director extends NamedObj implements Executable {
    /** Constructor
     */	
    public Director(CompositeActor container, String name) {
        super(name);
        _container = container;
    }
    
    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////
    
    /** Returns the top-level container, of the executable application 
     *  that this director is responsible for.
     * @return the top-level CompositeActor
     */
    public Nameable getContainer() {
        return _container;
    }

    /** This sets a flag, indicating that on the next execution, the 
     *  static schedule schedule should be recomputed
     */
    public void invalidateSchedule() {
    }
    
    /** This does the initialization for the entire simulation. This should
     *  be called exactly once at the start of the entire execution
     */
    public void initialize() {
    }

    /** This controls the execution. 
     * @return true if the execution is complete and should be terminated
     * @exception IllegalActionException is thrown.
     * @see fire()
     */
    public boolean iterate() throws IllegalActionException {
        if (prefire()) {
            fire();
            postfire();
        } 
        return _complete;
    }
    
    /** This should invoke the fire methods of the actors according to a 
     *  schedule. This can be called more than once in the same iteration
     * @exception IllegalActionException would be required by subclasses
     */	
    public void fire() throws IllegalActionException {
    }
 
    /** This should be called after the fire methods have been called. This
     *  should invoke the postfire methods of actors according to a schedule
     */
    public void postfire() {
    }

    /** This determines if the schedule is valid. If the schedule is valid
     *  then it returns, else it calls methods for scheduling and takes care
     *  of changes due to mutation
     * @return false if application is not ready for invocation of fire()
     *  else returns true
     */
    public boolean prefire() {
        return true;
    }
    
    /** This maintains a list of all the new actors that have been created 
     *  after the last call to the iterate method, and have not begun 
     *  execution
     * @param actor is the new actor that has just been created
     */
    public void registerNewActor(Actor actor) {
        synchronized(workspace()) {
            if (_listOfNewActors == null) {
                _listOfNewActors = new LinkedList();
            }
            _listOfNewActors.insertLast(actor);
        }
    }

    /** Returns an enumeration of all the actors that have not begun execution
     * @return the enumeration of all new actors
     */
    public Enumeration getNewActors() {
        synchronized(workspace()) {
            if (_listOfNewActors == null) {
                _listOfNewActors = new LinkedList();
            }
            return _listOfNewActors.elements();    
        }
    }
    
    /** Clears all the actors from the list of new actors
     */
    public void clearNewActors() {
        _listOfNewActors = null;
    }
    
    /** This method is called to invoke an executable application. This
     *  would normally be overriden in different domains
     * @exception IllegalActionException is thrown by iterate()
     */
    public void run() throws IllegalActionException {
        initialize();
        while (!iterate());
        wrapup();
        return;
    }

    /** Indicates if the current schedule is valid or not
     * @return true indicates schedule is valid and need not be recomputed
     */
    public boolean scheduleValid() {
        return false;
    }

    /** Indicates whether the execution is complete or not
     * @return true indicates that execution is complete
     */
    public boolean getComplete() {
        return _complete;
    }

    /** This sets a flag indicating whether the iteration is complete or not
     * @param complete true indicates the end of execution
     */
    public void setComplete(boolean complete) {
        _complete = complete;
    }

    /** This invokes the corresponding methods of all the actors at the end 
     *  of simulation
     */
    public void wrapup() {
        Enumeration allactors = ((CompositeActor)getContainer()).deepGetEntities();
        while (allactors.hasMoreElements()) {
            Actor actor = (Actor)allactors.nextElement();
            actor.wrapup();
        }
    }


    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    private CompositeActor _container = null;
    private boolean _complete = true;
    private boolean _schedulevalid;
    private CompositeActor _subsystem;
    private LinkedList _listOfNewActors = null;
    
}

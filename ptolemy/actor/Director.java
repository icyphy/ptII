/* Director governs the execution of a CompositeActor.

 Copyright (c) 1997- The Regents of the University of California.
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

@author Mudit Goel, Edward A. Lee
@version $Id$
*/
public class Director extends NamedObj implements Executable {

    /** FIXME: Does this constructor belong?  Used for testing IOPort.
     */
    public Director() {
        super();
    }

    /** Constructor
     */	
    public Director(CompositeActor container, String name) {
        super(name);
        _container = container;
    }
    
    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////
    
    /** Adds a new Mutation Listener to the list of listeners to be informed
     *  about any mutation that occurs in the graph for which this director
     *  is responsible
     * @param listener is the new MutationListener
     */
    public void addMutationListener(MutationListener listener) {
        synchronized(workspace()) {
            if (_mutationListeners == null) {
                _mutationListeners = new LinkedList();
            }
            _mutationListeners.insertLast(listener);
        }
    }

    /** Clears all the actors from the list of new actors
     */
    public void clearNewActors() {
        _newactors = null;
    }
    
    /** Indicates whether the execution is complete or not
     * @return true indicates that execution is complete
     */
    public boolean complete() {
        return _complete;
    }

    /** This should invoke the fire methods of the actors according to a 
     *  schedule. This can be called more than once in the same iteration
     * @exception IllegalActionException would be required by subclasses
     */	
    public void fire() throws IllegalActionException {
    }
    
    /** Returns the top-level container, of the executable application 
     *  that this director is responsible for.
     * @return the top-level CompositeActor
     */
    public Nameable getContainer() {
        return _container;
    }

    /** Returns an enumeration of all the actors that have not begun execution
     * @return the enumeration of all new actors
     */
    public Enumeration getNewActors() {
        synchronized(workspace()) {
            if (_newactors == null) {
                _newactors = new LinkedList();
            }
            return _newactors.elements();    
        }
    }
    
    /** Test if there are new actors waiting to be scheduled.
     * @return true if there are new actors
     */
    public boolean hasNewActors() {
        synchronized(workspace()) {
            return (_newactors != null);
        }
    }
    
    /** This does the initialization for the entire simulation. This should
     *  be called exactly once at the start of the entire execution
     */
    public void initialize() {
    }

    /** This sets a flag, indicating that on the next execution, the 
     *  static schedule schedule should be recomputed
     */
    public void invalidateSchedule() {
        _schedulevalid = false;
    }
    
    /** This controls the execution. 
     * @return true if the execution is complete and should be terminated
     * @exception IllegalActionException is thrown.
     * @see #fire()
     */
    public boolean iterate() throws IllegalActionException {
        if (prefire()) {
            fire();
            postfire();
        } 
        return _complete;
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
    
    /** This does all the mutations and informs the listeners of the mutations
     */
    public final void processPendingMutations() {
        synchronized(workspace()) {
            Mutation m;
            if (!_pendingMutations.isEmpty()) {
                Enumeration mutations = _pendingMutations.elements();
                while (mutations.hasMoreElements()) {
                    m = (Mutation)mutations.nextElement();
                    // do the mutation
                    m.perform();

                    // inform all listeners
                    // FIXME the listeners should probably be attached to
                    // the graph, not the director
                    if (_mutationListeners != null) {
                        Enumeration listeners = _mutationListeners.elements();
                        while (listeners.hasMoreElements()) {
                            m.update((MutationListener)listeners.nextElement());
                        }
                    }
                }
                // Clear the mutations
                _pendingMutations = null;
            }
        }                    
    }

    /** This adds a mutation object to the director queue. These mutations
     *  are finally incorporated when the processPendingMutations() is called
     * @param mutation The new Mutation objects that contains a list of 
     *  mutations that should be executed later
     * @see #processPendingMutations()
     */
    public void queueMutation(Mutation mutation) {
        synchronized(workspace()) {
            if (_pendingMutations == null) {
                _pendingMutations = new LinkedList();
            }
            _pendingMutations.insertLast(mutation);
        }
    }

    /** This maintains a list of all the new actors that have been created 
     *  after the last call to the iterate method, and have not begun 
     *  execution
     * @param actor is the new actor that has just been created
     */
    public void registerNewActor(Actor actor) {
        synchronized(workspace()) {
            if (_newactors == null) {
                _newactors = new LinkedList();
            }
            _newactors.insertLast(actor);
        }
    }

    /** This removes the Mutation listener that does not want to be informed
     *  of any future mutations by this director. This does not do anything 
     *  if the listener was not listed with this director
     * @param listener is the MutationListener to be removed
     */
    public void removeMutationListener(MutationListener listener) {
        synchronized(workspace()) {
            _mutationListeners.removeOneOf(listener);
        }
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
        return _schedulevalid;
    }

    /** This sets a flag indicating whether the iteration is complete or not
     * @param complete true indicates the end of execution
     */
    public void setComplete(boolean complete) {
        _complete = complete;
    }

    /** Mark the current schedule to be valid.
     *  @see #invalidateSchedule()
     */
    public void validateSchedule() {
        _schedulevalid = true;
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
    private LinkedList _newactors = null;
    
    private LinkedList _pendingMutations = null;
    private LinkedList _mutationListeners = null;
}

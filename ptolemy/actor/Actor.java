/* An executable entity.
   
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
*/

package pt.actors;
import pt.kernel.*;
import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// Actor
/** 
An Actor is an executable entity. The Ports of Actors are
constrained to be IOPorts.
An actor is always contained by a CompositeActor (its container
can never be null).  In this base class, the actor does nothing
in the action methods (prefire, fire, ...).
The container argument must not be null, or a NullPointerException
will be thrown.

@author Mudit Goel, Edward A. Lee
@version $Id$
@see pt.actors.CompositeActor
@see pt.actors.IOPort
*/
public class Actor extends ComponentEntity implements Executable {

    /** Create a new actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown. 
     *  @param container The containing CompositeActor.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException Name coincides with
     *   an entity already in the container.
     */    
    public Actor(CompositeActor container, String name) 
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }
    
    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////
    
    /** Return the director responsible for the execution of this actor. 
     * @return The director that invokes this actor.
     */
    public Director getDirector() {
        return ((CompositeActor)getContainer()).getDirector();            
    }

    /** This is where the actors would normally define their actions
     * @exception IllegalActionException would be required in derived classes
     */
    public void fire() throws IllegalActionException {
    }

    /** The actors would be initialized in this method before their execution 
     *  begins
     */
    public void initialize() {
    }

    /** Return an enumeration of the input ports.
     *  This method is synchronized on the workspace.
     *  @return An enumeration of IOPort objects.
     */ 
    public Enumeration inputPorts() {
        synchronized(workspace()) {
            if(_inputPortsVersion != workspace().getVersion()) {
                // Update the cache.
                LinkedList inports = new LinkedList();
                Enumeration ports = getPorts();
                while(ports.hasMoreElements()) {
                    IOPort p = (IOPort)ports.nextElement();
                    if( p.isInput()) {
                        inports.insertLast(p);
                    }
                }
                _cachedInputPorts = inports;
                _inputPortsVersion = workspace().getVersion();
            }
            return _cachedInputPorts.elements();
        }
    }

    /** Create a new IOPort with the specified name.
     *  The container of the port is set to this actor.
     *  This method is synchronized on the workspace, and increments
     *  its version number.
     *  @param name The name of the newly created port.
     *  @return The new port.
     *  @exception IllegalActionException if the argument is null.
     *  @exception NameDuplicationException if the actor already has a port 
     *   with the specified name.
     */	
    public Port newPort(String name) 
            throws IllegalActionException, NameDuplicationException {
        synchronized(workspace()) {
            IOPort port = new IOPort(this, name);
            workspace().incrVersion();
            return port;
        }
    }

    /** get an enumeration of the output ports
     *  This method is synchronized on the workspace.
     *  @return An enumeration of IOPort objects.
     */
    public Enumeration outputPorts() {
        synchronized(workspace()) {
            if(_outputPortsVersion != workspace().getVersion()) {
                LinkedList outports = new LinkedList();
                Enumeration ports = getPorts();
                while(ports.hasMoreElements()) {
                    IOPort p = (IOPort)ports.nextElement();
                    if( p.isOutput()) { 
                        outports.insertLast(p);
                    }
                }
                _cachedOutputPorts = outports;
                _outputPortsVersion = workspace().getVersion();
            }
            return _cachedOutputPorts.elements();
        }
    }

    /** This would define the actions that an actor should do at the end of
     *  every iteration of its execution
     */
    public void postfire() {
    }

    /** This would define the actions of an actor in the beginning of every
     *  iteration of it's execution. 
     * @return true if the star is ready for firing, false otherwise.
     */
    public boolean prefire() {
        return true;
    }

    // FIXME: Override setContainer to ensure types.
    // Also, newPort... what else?
  
    /** This is called at the end of every execution of the star. This is for 
     *  cleanups and freeing resources that the actor currently has access to
     */
    public void wrapup() {
        return;
    }
        
    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // Cached lists of input and output ports.
    private transient long _inputPortsVersion = -1;
    private transient LinkedList _cachedInputPorts;
    private transient long _outputPortsVersion = -1;
    private transient LinkedList _cachedOutputPorts;
}





/* A composite executable entity in the SDF domain.

 Copyright (c) 1997-1998 The Regents of the University of California.
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

@ProposedRating Red (neuendor@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.kernel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;

import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// SDFCompositeActor
/**
A CompositeActor that can exist in an SDF-scheduled domain.   Implements
rated ports via the DataflowActor interface.

@author Stephen Neuendorffer
@version $Id$
@see ptolemy.actors.CompositeActor
@see ptolemy.actors.IOPort
*/
public class SDFCompositeActor extends CompositeActor implements DataflowActor {

    /** Construct an actor in the default workspace with an empty string
     *  The object is added to the workspace directory.
     *  as its name. Increment the version number of the workspace.
     */
    public SDFCompositeActor() {
	super();
    }

    /** Construct an actor in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the entity.
     */
    public SDFCompositeActor(Workspace workspace) {
	super(workspace);
    }

    /** Create a new actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public SDFCompositeActor(CompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If this actor is opaque, invoke the fire() method of its local
     *  director. Otherwise, throw an exception.
     *  This method is read-synchronized on the workspace, so the
     *  fire() method of the director need not be (assuming it is only
     *  called from here).
     *
     *  @exception IllegalActionException If there is no director, or if
     *   the director's fire() method throws it, or if the actor is not
     *   opaque.
     */
     public void fire() throws IllegalActionException {
        try {
            workspace().getReadAccess();
            if (!isOpaque()) {
                throw new IllegalActionException(this,
                        "Cannot fire a non-opaque actor.");
            }
            Debug.println("Running SDFcompositeActor");
            // Note that this is assured of firing the local director,
            // not the executive director, because this is opaque.
            getDirector().fire();
        } finally {
            workspace().doneReading();
        }
    }


    /** Get the number of tokens that are produced or consumed 
     *  on the designated port of this Actor.   
     *
     *  @throw IllegalActionException if port is not contained in this actor.
     *  @throw IllegalActionException if the Port is not connected on the 
     *  inside, and thus the rate cannot be determined.
     *  @return The number of tokens produced on the port.
     */
    public int getTokenProductionRate(IOPort p) throws IllegalActionException {
        
        Debug.println("getTokenProductionRate:");
        if(!p.isOutput()) throw new IllegalActionException("IOPort " +
                p.getName() + " is not an Input Port.");
        Port pp = getPort(p.getName());
        if(!p.equals(pp)) throw new IllegalActionException("IOPort " +
                p.getName() + " is not contained in Actor " + 
                getName());

        Debug.println("Finished Sanity Checks");

        SDFDirector director = (SDFDirector) getDirector();
        SDFScheduler scheduler = (SDFScheduler) director.getScheduler();
        Debug.println("Starting Sub-schedule");
        Enumeration schedule = scheduler.schedule();
        Debug.println("Finished Sub-Schedule");
        
        Enumeration ports = p.insidePorts();
        if(ports.hasMoreElements() == false) 
            throw new IllegalActionException("Port " + p.getName() +
                    " is not connected on the inside, " +
                    "leaving its rate indeterminate.");
        IOPort connectedPort = (IOPort) ports.nextElement();
        ComponentEntity connectedActor = 
            (ComponentEntity) connectedPort.getContainer();
        
        Debug.println("getting connectedrate");
        int connectedrate = ((DataflowActor) connectedActor).
            getTokenProductionRate(connectedPort);
        Debug.println((new Integer(connectedrate)).toString());

        Debug.println("getting firing");
        int firing = scheduler.getFiringCount(connectedActor);
        Debug.println((new Integer(firing)).toString());
        Debug.println("return rate of:" + (new Integer(connectedrate*firing)).intValue());

        return connectedrate * firing;
    }

        
    /** Get the number of tokens that are produced or consumed 
     *  on the designated port of this Actor.   
     *
     *  @throw IllegalActionException if port is not contained in this actor.
     *  @return The number of tokens consumed on the port.
     */
    public int getTokenConsumptionRate(IOPort p) 
            throws IllegalActionException{

        Debug.println("getTokenConsumptionRate:");
        if(!p.isInput()) throw new IllegalActionException("IOPort " +
                p.getName() + " is not an Input Port.");
        Port pp = getPort(p.getName());
        if(!p.equals(pp)) throw new IllegalActionException("IOPort " +
                p.getName() + " is not contained in Actor " + 
                getName());
       Debug.println("Finished Sanity Checks");

        SDFDirector director = (SDFDirector) getDirector();
        SDFScheduler scheduler = (SDFScheduler) director.getScheduler();
        Debug.println("Starting Sub-schedule");
        Enumeration schedule = scheduler.schedule();
        Debug.println("Finished Sub-Schedule");
        
        Enumeration ports = p.insidePorts();
        if(ports.hasMoreElements() == false) 
            throw new IllegalActionException("Port " + p.getName() +
                    " is not connected on the inside, " +
                    "leaving its rate indeterminate.");
        IOPort connectedPort = (IOPort) ports.nextElement();
        ComponentEntity connectedActor = 
            (ComponentEntity) connectedPort.getContainer();
        
        Debug.println("getting connectedrate");
        int connectedrate = ((DataflowActor) connectedActor).
            getTokenConsumptionRate(connectedPort);
        Debug.println((new Integer(connectedrate)).toString());

        Debug.println("getting firing");
        int firing = scheduler.getFiringCount(connectedActor);
        Debug.println((new Integer(firing)).toString());

        Debug.println("return rate of:" + (new Integer(connectedrate*firing)).intValue());
        return connectedrate * firing;
    }

    /** If this actor is opaque, invoke the postfire() method of its
     *  local director and transfer output data.
     *  Specifically, transfer any data from the output ports of this composite
     *  to the ports connected on the outside. The transfer is accomplished
     *  by calling the transferOuputs() method of the executive director.
     *  If there is no executive director, then no transfer occurs.
     *  This method is read-synchronized on the workspace.
     *
     *  @return True if the execution can continue into the next iteration.
     *  @exception IllegalActionException If there is no director,
     *   or if the director's postfire() method throws it, or if this
     *   actor is not opaque.
     */
    /*    public boolean postfire() throws IllegalActionException {
        try {
            workspace().getReadAccess();
            if (!isOpaque()) {
                throw new IllegalActionException(this,
                        "Cannot invoke postfire a non-opaque actor.");
            }
            // Note that this is assured of firing the local director,
            // not the executive director, because this is opaque.
            boolean oktocontinue = getDirector().postfire();
            // The composite actor is opaque.
            // Use the executive director to transfer outputs.
            Director edir = getExecutiveDirector();
            if (edir != null) {
                Enumeration ports = outputPorts();
                while(ports.hasMoreElements()) {
                    IOPort p = (IOPort)ports.nextElement();
                    edir.transferOutputs(p);
                }
            }
            return oktocontinue;
        } finally {
            workspace().doneReading();
        }
    }
*/
    /** If this actor is opaque, transfer input data and invoke the prefire()
     *  method of the local director. Specifically, transfer any data from
     *  the input ports of this composite to the ports connected on the inside.
     *  The transfer is accomplished by calling the transferInputs() method
     *  of the local director (the exact behavior of which depends on the
     *  domain).  This method returns true if the actor is
     *  ready to fire (determined by the prefire() method of the director).
     *  It is read-synchronized on the workspace.
     *
     *  @exception IllegalActionException If there is no director,
     *   or if the director's prefire() method throws it, or if this actor
     *   is not opaque.
     *  @exception NameDuplicationException If the prefire() method of the
     *   director throws it (while performing mutations, if any).
     */
    /*    public boolean prefire()
            throws IllegalActionException, NameDuplicationException {
        try {
            workspace().getReadAccess();
            if (!isOpaque()) {
                throw new IllegalActionException(this,
                        "Cannot invoke prefire a non-opaque actor.");
            }
            // Use the local director to transfer outputs.
            Enumeration ports = inputPorts();
            while(ports.hasMoreElements()) {
                IOPort p = (IOPort)ports.nextElement();
                Director direct = getDirector();
                director.transferInputs(p);
            }
            return getDirector().prefire();
        } finally {
            workspace().doneReading();
        }
    }
*/
}

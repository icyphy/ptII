/* A composite executable entity in the SDF domain.

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

@ProposedRating Red (neuendor@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.kernel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;

import java.util.*;
import collections.LinkedList;
import collections.HashedMap;

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
        _init();
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
        _init();
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
        _init();
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

   /** Get the number of tokens that are consumed
     *  on the designated port of this Actor.   If no explicit rate has been
     *  set, then attempt to calculate the rate by looking at the 
     *  contained actors.
     *
     *  @exception IllegalActionException if port is not contained 
     *  in this actor, or the port is not an Input port.
     *  @exception IllegalActionException if the Port is not connected on the
     *  inside and the rate has not been explicitly set, 
     *  thus the rate cannot be determined.
     *  @return The number of tokens consumed on the port.
     */
    public int getTokenConsumptionRate(IOPort p)
            throws IllegalActionException{

        Debug.println("getTokenConsumptionRate:" + p.toString());
        if(!p.isInput()) throw new IllegalActionException("IOPort " +
                p.getName() + " is not an Input Port.");
        Port pp = getPort(p.getName());
        if(!p.equals(pp)) throw new IllegalActionException("IOPort " +
                p.getName() + " is not contained in Actor " +
                getName());

        // If we've previously set the rate explicitly, then use that,
        // Otherwise we'll go on and try to extrapolate.
        if(_tokenconsumptionrate.includesKey(p)) {
            Integer tokens = (Integer) _tokenconsumptionrate.at(p);
            return tokens.intValue();
        }

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

    /** Get the number of tokens that are produced
     *  on the designated port of this Actor during initilization.
     *
     *  @exception IllegalActionException if port is not contained 
     *  in this actor.
     *  @return The number of tokens produced on the port, as supplied by
     *  setTokenProductionRate, or zero if setTokenProductionRate has not been
     *  called
     */
    public int getTokenInitProduction(IOPort p)
        throws IllegalActionException {

        Port pp = getPort(p.getName());
        if(!p.equals(pp)) throw new IllegalActionException("IOPort " +
                p.getName() + " is not contained in Actor " +
                getName());

        if(! _tokeninitproduction.includesKey(p)) return 0;
        Integer i = (Integer) _tokeninitproduction.at(p);

        return i.intValue();
    }

    /** Get the number of tokens that are produced
     *  on the designated port of this Actor during each firing.
     *
     *  @exception IllegalActionException if port is not contained 
     *  in this actor, or is not an output port.
     *  @exception IllegalActionException if the Port is not connected on the
     *  inside and the rate has not been explicitly set, 
     *  thus the rate cannot be determined.
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

        // If we've previously set the rate explicitly, then use that,
        // Otherwise we'll go on and try to extrapolate.
        if(_tokenproductionrate.includesKey(p)) {
            Integer tokens = (Integer) _tokenproductionrate.at(p);
            return tokens.intValue();
        }

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
        Debug.println("return rate of:" +
                (new Integer(connectedrate*firing)).intValue());

        return connectedrate * firing;
    }

    /** This method sets the value returned by getTokenConsumptionRate on the
     *  port.   If this method is not called, then getTokenConsumptionRate will
     *  attempt to extrapolate the proper values by scheduling the contained
     *  actors.   This method allows domains that in general don't support
     *  strict dataflow semantics to be encapsulated within strict dataflow
     *  domains.
     *  @param IOPort the number of tokens consumed on the port.
     *  @exception IllegalActionException if port is not contained 
     *  in this actor,
     *  or the port is not an input port.
     */
    public void setTokenConsumptionRate(IOPort p, int count)
            throws IllegalActionException {

        if(!p.isInput()) throw new IllegalActionException("IOPort " +
                p.getName() + " is not an Input Port.");
        Port pp = getPort(p.getName());
        if(!p.equals(pp)) throw new IllegalActionException("IOPort " +
                p.getName() + " is not contained in Actor " +
                getName());
        _tokenconsumptionrate.putAt(p, new Integer(count));
    }

    /** Set the number of tokens that are produced or consumed
     *  on the appropriate port of this Actor during the initialization phase.
     *  This is usually used to simulate a delay along the relation that the
     *  port is connected to, and may be necessary in order to get the SDF
     *  scheduler to create a valid schedule from certain kinds of topologies.
     *
     *  @exception IllegalActionException if port is not contained 
     *  in this actor.
     *  @exception IllegalActionException if port is not an input IOPort.
     */
    public void setTokenInitProduction(IOPort p,int r)
        throws IllegalActionException {
        if(r <= 0) throw new IllegalActionException(
                "setTokenRatePerFiring: Rate must be > 0");
        Port pp = getPort(p.getName());
        if(!p.isOutput()) throw new IllegalActionException("IOPort " +
                p.getName() + " is not an Output Port.");
        if(!p.equals(pp)) throw new IllegalActionException("IOPort " +
                p.getName() + " is not contained in Actor " +
                getName());
        _tokeninitproduction =
            (HashedMap) _tokeninitproduction.puttingAt(p,new Integer(r));
    }

    /** Set the number of tokens that are produced or consumed
     *  on the designated port of this Actor.   It may also
     *  be called in an opaque CompositeActor to place a non-dataflow domain
     *  inside of a dataflow domain.  (In this case the CompositeActor cannot
     *  determine the rate by scheduling the contained domain, and it must be
     *  explicitly declared.)
     *
     *  @exception IllegalActionException if port is not contained
     *  in this actor, or is not an output port.
     *  @return The number of tokens produced on the port.
     */
    public void setTokenProductionRate(IOPort p, int count)
            throws IllegalActionException {
        Debug.println("getTokenProductionRate:");
        if(!p.isOutput()) throw new IllegalActionException("IOPort " +
                p.getName() + " is not an Input Port.");
        Port pp = getPort(p.getName());
        if(!p.equals(pp)) throw new IllegalActionException("IOPort " +
                p.getName() + " is not contained in Actor " +
                getName());
        _tokenproductionrate.putAt(p, new Integer(count));
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private methods                      ////
    private void _init() {
        _tokenconsumptionrate = new HashedMap();
        _tokenproductionrate = new HashedMap();
        _tokeninitproduction = new HashedMap();
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ////

    private HashedMap _tokenconsumptionrate;
    private HashedMap _tokenproductionrate;
    private HashedMap _tokeninitproduction;

}

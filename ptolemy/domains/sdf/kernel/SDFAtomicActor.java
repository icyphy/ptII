/* An executable Entity in the SDF domain.

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
import collections.HashedMap;

//////////////////////////////////////////////////////////////////////////
//// SDFAtomicActor
/**
An AtomicActor is an executable entity that cannot itself contain
other actors. The container is required to be an instance of CompositeActor.
Derived classes may further constrain the container by overriding
setContainer(). The Ports of AtomicActors are constrained to be IOPorts.
Derived classes may further constrain the ports by overriding the public
method newPort() to create a port of the appropriate subclass, and the
protected method _addPort() to throw an exception if its argument is a
port that is not of the appropriate subclass. In this base class, the
actor does nothing in the action methods (prefire, fire, ...).

An SDFAtomicActor is an AtomicActor that is valid in the SDF domain.  This 
implies that is supports a static notion of the "Rate" of a port, as defined
in the DataflowActor interface. i.e. a number of tokens are created or 
destroyed on a port at during any firing, and the scheduler can ask the 
Actor what this number is.   Furthermore, for SDFActors, this number is 
constant for all firings and it is known before execution begins.  

@author Stephen Neuendorffer
@version $Id$
@see ptolemy.actors.CompositeActor
@see ptolemy.actors.IOPort
*/
public class SDFAtomicActor extends AtomicActor implements DataflowActor{

    /** Construct an actor in the default workspace with an empty string
     *  The object is added to the workspace directory.
     *  as its name. Increment the version number of the workspace.
     */
    public SDFAtomicActor() {
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
    public SDFAtomicActor(Workspace workspace) {
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
    public SDFAtomicActor(CompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the number of tokens that are produced or consumed 
     *  on the designated port of this Actor.   Return zero if
     *  setTokenConsumptionRate has not been called on this port.
     *
     *  @throw IllegalActionException if port is not contained in this actor.
     *  @return The number of tokens consumed on the port, as supplied by 
     *  setTokenConsumptionRate, or zero if setTokenConsumptionRate has 
     *  not been called.
     */
    public int getTokenConsumptionRate(IOPort p) 
        throws IllegalActionException {

        Port pp = getPort(p.getName());
        if(!p.equals(pp)) throw new IllegalActionException("IOPort " +
                p.getName() + " is not contained in Actor " + 
                getName());

        if(! _tokenconsumptionrate.includesKey(p)) return 0;

        Integer i = (Integer) _tokenconsumptionrate.at(p);

        return i.intValue();
    }

    /** Get the number of tokens that are produced or consumed 
     *  on the designated port of this Actor during each firing.   
     *  Return zero if setTokenProductionRate has not been called
     *   on this IOPort.
     *
     *  @throw IllegalActionException if port is not contained in this actor.
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

    /** Get the number of tokens that are produced or consumed 
     *  on the designated port of this Actor during each firing.   
     *  Return zero if setTokenProductionRate has not been called
     *   on this IOPort.
     *
     *  @throw IllegalActionException if port is not contained in this actor.
     *  @return The number of tokens produced on the port, as supplied by 
     *  setTokenProductionRate, or zero if setTokenProductionRate has not been
     *  called
     */
    public int getTokenProductionRate(IOPort p) 
        throws IllegalActionException {

        Port pp = getPort(p.getName());
        if(!p.equals(pp)) throw new IllegalActionException("IOPort " +
                p.getName() + " is not contained in Actor " + 
                getName());
        
        if(! _tokenproductionrate.includesKey(p)) return 0;
        Integer i = (Integer) _tokenproductionrate.at(p);

        return i.intValue();
    }


    /** Set the number of tokens that are produced or consumed 
     *  on the appropriate port of this Actor.
     *
     *  @throw IllegalActionException if port is not contained in this actor.
     *  @throw IllegalActionException if port is not an input IOPort.
     */
    public void setTokenConsumptionRate(IOPort p,int r) 
        throws IllegalActionException {

        if(r <= 0) throw new IllegalActionException("SetTokenRatePerFirint: Rate must be > 0");            
        if(!p.isInput()) throw new IllegalActionException("IOPort " +
                p.getName() + " is not an Input Port.");
         Port pp = getPort(p.getName());
        if(!p.equals(pp)) throw new IllegalActionException("IOPort " +
                p.getName() + " is not contained in Actor " + 
                getName());
        _tokenconsumptionrate = 
            (HashedMap) _tokenconsumptionrate.puttingAt(p,new Integer(r));
    }

    /** Set the number of tokens that are produced or consumed 
     *  on the appropriate port of this Actor during the initialization phase.
     *  This is usually used to simulate a delay along the relation that the
     *  port is connected to, and may be necessary in order to get the SDF
     *  scheduler to create a valid schedule from certain kinds of topologies.
     * 
     *  @throw IllegalActionException if port is not contained in this actor.
     *  @throw IllegalActionException if port is not an input IOPort.
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
     *   on the appropriate port of this Actor
     *
     *  @throw IllegalActionException if port is not contained in this actor.
     *  @throw IllegalActionException if port is not an input IOPort.
     */
    public void setTokenProductionRate(IOPort p,int r) 
        throws IllegalActionException {
        if(r <= 0) throw new IllegalActionException(
                "setTokenRatePerFiring: Rate must be > 0");
        Port pp = getPort(p.getName());
        if(!p.isOutput()) throw new IllegalActionException("IOPort " +
                p.getName() + " is not an Output Port.");
        if(!p.equals(pp)) throw new IllegalActionException("IOPort " +
                p.getName() + " is not contained in Actor " + 
                getName());
        _tokenproductionrate = 
            (HashedMap) _tokenproductionrate.puttingAt(p,new Integer(r));
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




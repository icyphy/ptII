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
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.actor.*;

import java.util.Enumeration;
import collections.HashedMap;

//////////////////////////////////////////////////////////////////////////
//// SDFAtomicActor
/**
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
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    
    /** Get the number of tokens that are consumed
     *  on the designated port of this Actor.   
     *
     *  @exception IllegalActionException if port is not contained in 
     *  this actor.
     *  @return The number of tokens consumed on the port, as supplied by
     *  setTokenConsumptionRate.
     */
    public int getTokenConsumptionRate(IOPort p)
        throws IllegalActionException {

        Port pp = getPort(p.getName());
        if(!p.equals(pp)) throw new IllegalActionException("IOPort " +
                p.getName() + " is not contained in Actor " +
                getName());
        if(!p.isInput()) throw new IllegalActionException("IOPort " +
                p.getName() + " is not an Input Port.");

        Parameter param = (Parameter)p.getAttribute("Token Consumption Rate");
        return ((IntToken)param.getToken()).intValue();

    }

    /** Get the number of tokens that are produced
     *  on the designated port of this Actor during initialization.
     *
     *  @exception IllegalActionException if port is not contained
     *  in this actor.
     *  @return The number of tokens produced on the port, as supplied by
     *  setTokenProductionRate
     */
    public int getTokenInitProduction(IOPort p)
        throws IllegalActionException {

        Port pp = getPort(p.getName());
        if(!p.equals(pp)) throw new IllegalActionException("IOPort " +
                p.getName() + " is not contained in Actor " +
                getName());

        Parameter param = (Parameter)p.getAttribute("Token Init Production");
        return ((IntToken)param.getToken()).intValue();

    }

    /** Get the number of tokens that are produced
     *  on the designated port of this Actor during each firing.
     *
     *  @exception IllegalActionException if port is not contained 
     *  in this actor.
     *  @return The number of tokens produced on the port, as supplied by
     *  setTokenProductionRate
     */
    public int getTokenProductionRate(IOPort p)
        throws IllegalActionException {

        Port pp = getPort(p.getName());
        if(!p.equals(pp)) throw new IllegalActionException("IOPort " +
                p.getName() + " is not contained in Actor " +
                getName());

        Parameter param = (Parameter)p.getAttribute("Token Production Rate");
        return ((IntToken)param.getToken()).intValue();

    }

    /** Create ports for this object.  Ports will be of type SDFIOPort, 
     *  which provides support getArray() and sendArray
     *  @exception NameDuplicationException if the SDFIOPort constructor
     *  throws it.
     */
    public Port newPort(String name) throws NameDuplicationException {
        try {
            workspace().getWriteAccess();
            IOPort port = new SDFIOPort(this, name);
            return port;
        } catch (IllegalActionException ex) {
            // This exception should not occur, so we throw a runtime
            // exception.
            throw new InternalErrorException(
                    "SDFAtomicActor.newPort: Internal error: " + 
                    ex.getMessage());
        } finally {
            workspace().doneWriting();
        }
    }
    

    /** Set the number of tokens that are consumed
     *  on the appropriate port of this Actor during each firing.
     *
     *  @exception IllegalActionException if port is not contained 
     *  in this actor, the rate is less than one, or the port is
     *  not an input port.
     */
    public void setTokenConsumptionRate(IOPort p,int r)
        throws IllegalActionException {

        if(r <= 0) throw new IllegalActionException(
                "setTokenConsumptionRate: Rate must be > 0");
        if(!p.isInput()) throw new IllegalActionException("IOPort " +
                p.getName() + " is not an Input Port.");
         Port pp = getPort(p.getName());
        if(!p.equals(pp)) throw new IllegalActionException("IOPort " +
                p.getName() + " is not contained in Actor " +
                getName());
        Parameter param = (Parameter)p.getAttribute("Token Consumption Rate");
        param.setToken(new IntToken(r));

    }

    /** Set the number of tokens that are produced
     *  on the appropriate port of this Actor during the initialization phase.
     *  This is usually used to simulate a delay along the relation that the
     *  port is connected to, and may be necessary in order to get the SDF
     *  scheduler to create a valid schedule from certain kinds of topologies.
     *
     *  @exception IllegalActionException if port is not contained 
     *  in this actor, the rate is less than one, or the port is
     *  not an output port.
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
        Parameter param = (Parameter)p.getAttribute("Token Init Production");
        param.setToken(new IntToken(r));

    }

    /** Set the number of tokens that are produced
     *   on the appropriate port of this Actor during each firing.
     *
     *  @exception IllegalActionException if port is not contained 
     *  in this actor, the rate is less than one, or the port is
     *  not an output port.
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
        Parameter param = (Parameter)p.getAttribute("Token Production Rate");
        param.setToken(new IntToken(r));
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private methods                      ////

    /** When a port is added to this object, this method creates
     *  Parameters are created for use by 
     *  the above methods, with appropriate default values.
     *  @exception NameDuplicationException If any contained method throws it.
     *  @exception IllegalActionException If any contained method throws it.
     */
    protected void _addPort(Port p)
        throws IllegalActionException, NameDuplicationException {
        super._addPort(p);
        if(p instanceof IOPort) {
            Parameter param;

            param = new Parameter(p,"Token Consumption Rate",
                    new IntToken(1));
            param = new Parameter(p,"Token Production Rate",
                    new IntToken(1));
            param = new Parameter(p,"Token Init Production",
                    new IntToken(0));
        }
    }
    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ////

}









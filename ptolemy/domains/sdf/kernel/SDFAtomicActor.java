/* An executable Entity in the SDF domain.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Yellow (johnr@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.kernel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.actor.*;
import ptolemy.actor.Mailbox;   // for javadoc

import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// SDFAtomicActor
/**
An SDFAtomicActor is an atomic actor that is valid in the SDF domain.  This
implies that it supports a static notion of the "Rate" of a port.
i.e. a number of tokens are created or
destroyed on a port during any firing.//   This information is contained in
parameters on each of the Actors ports.
<ul>
<li> TokenConsumptionRate: The number of tokens consumed on an input port
during each firing.
<li> TokenProductionRate: The number of tokens produced on an output port
during each firing.
<li> TokenInitProduction: The number of tokens produced on an output port
during initialization.
</ul>
The SDF director uses these parameters to calculate a static schedule based
on these parameters.  The static schedule allows more efficient execution,
when it can be constructed.  Creating the schedule is costly, but only
needs to be done when execution starts, or when the topology changes.

@author Stephen Neuendorffer
@version $Id$
@see ptolemy.domains.sdf.kernel.SDFDirector
@see ptolemy.actor.Mailbox
@see ptolemy.actor.TypedCompositeActor
@see ptolemy.actor.IOPort
*/
public class SDFAtomicActor extends TypedAtomicActor {

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
    public SDFAtomicActor(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** If a input port has not had its consumption rate set, then
     * it is assumed to be this.
     */
    public static final int DEFAULT_CONSUMPTION_RATE = 1;

    /** If a output port has not had its production rate set, then
     * getTokenConsumptionRate will return this
     */
    public static final int DEFAULT_PRODUCTION_RATE = 1;

    /** If a output port has not had its init production set, then
     * getTokenConsumptionRate will return this
     */
    public static final int DEFAULT_INIT_PRODUCTION = 0;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the number of tokens that are consumed
     *  on the designated port of this Actor.
     *
     *  @exception IllegalActionException if port is not contained in
     *  this actor, or the port is not an input port.
     *  @return The number of tokens consumed on the port, as specified in
     *  the TokenConsumptionRate Parameter, or DEFAULT_CONSUMPTION_RATE if the
     *  parameter does not exist.
     *  @deprecated
     */
    public int getTokenConsumptionRate(IOPort p)
            throws IllegalActionException {

        Port pp = getPort(p.getName());
        if(!p.equals(pp)) throw new IllegalActionException("IOPort " +
                p.getName() + " is not contained in Actor " +
                getName());
        if(!p.isInput()) throw new IllegalActionException("IOPort " +
                p.getName() + " is not an Input Port.");

        Parameter param = (Parameter)p.getAttribute("TokenConsumptionRate");
        if(param != null) {
            return ((IntToken)param.getToken()).intValue();
        } else {
            return DEFAULT_CONSUMPTION_RATE;
        }
    }

    /** Get the number of tokens that are produced
     *  on the designated port of this Actor during initialization.
     *
     *  @exception IllegalActionException if port is not contained
     *  in this actor, or the port is not an output port.
     *  @return The number of tokens produced on the port, as specified in
     *  the TokenInitProduction Parameter, or DEFAULT_INIT_PRODUCTION if the
     *  parameter does not exist.
     *  @deprecated
     */
    public int getTokenInitProduction(IOPort p)
            throws IllegalActionException {

        Port pp = getPort(p.getName());
        if(!p.equals(pp)) throw new IllegalActionException("IOPort " +
                p.getName() + " is not contained in Actor " +
                getName());
        if(!p.isOutput()) throw new IllegalActionException("IOPort " +
                p.getName() + " is not an Output Port.");

        Parameter param = (Parameter)p.getAttribute("TokenInitProduction");
        if(param != null) {
            return ((IntToken)param.getToken()).intValue();
        } else {
            return DEFAULT_INIT_PRODUCTION;
        }

    }

    /** Get the number of tokens that are produced
     *  on the designated port of this Actor during each firing.
     *
     *  @exception IllegalActionException If port is not contained
     *  in this actor, or the port is not an output port.
     *  @return The number of tokens produced on the port, as specified in
     *  the TokenProductionRate Parameter, or DEFAULT_PRODUCTION_RATE if the
     *  parameter does not exist.
     *  @deprecated
     */
    public int getTokenProductionRate(IOPort p)
            throws IllegalActionException {

        Port pp = getPort(p.getName());
        if(!p.equals(pp)) throw new IllegalActionException("IOPort " +
                p.getName() + " is not contained in Actor " +
                getName());
        if(!p.isOutput()) throw new IllegalActionException("IOPort " +
                p.getName() + " is not an Output Port.");

        Parameter param = (Parameter)p.getAttribute("TokenProductionRate");
        if(param != null) {
            return ((IntToken)param.getToken()).intValue();
        } else {
            return DEFAULT_PRODUCTION_RATE;
        }

    }

    /** Create ports for this object.  Ports will be of type SDFIOPort,
     *  which provides support getArray() and sendArray().
     *  @exception NameDuplicationException If the SDFIOPort constructor
     *  throws it.
     *  @return A new instance of SDFIOPort.
     */
    public Port newPort(String name) throws NameDuplicationException {
        try {
            workspace().getWriteAccess();
            SDFIOPort port = new SDFIOPort(this, name);
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
     *  @exception IllegalActionException If port is not contained
     *  in this actor, the rate is less than zero, or the port is
     *  not an input port.
     *  @deprecated
     */
    public void setTokenConsumptionRate(IOPort p, int r)
            throws IllegalActionException {

        if(r <= 0) throw new IllegalActionException(
                "Rate must be > 0");
        if(!p.isInput()) throw new IllegalActionException("IOPort " +
                p.getName() + " is not an Input Port.");
        Port pp = getPort(p.getName());
        if(!p.equals(pp)) throw new IllegalActionException("IOPort " +
                p.getName() + " is not contained in Actor " +
                getName());
        Parameter param = (Parameter)p.getAttribute("TokenConsumptionRate");
        if(param != null)
            param.setToken(new IntToken(r));
        else {
            try {
                param = new Parameter(p, "TokenConsumptionRate",
                        new IntToken(r));
            }
            catch (NameDuplicationException e) {
                // This should never happen.
                throw new InternalErrorException(e.getMessage());
            }
        }
    }

    /** Set the number of tokens that are produced
     *  on the appropriate port of this Actor during the initialization phase.
     *  This is usually used to simulate a delay along the relation that the
     *  port is connected to, and may be necessary in order to get the SDF
     *  scheduler to create a valid schedule from certain kinds of topologies.
     *
     *  @exception IllegalActionException If port is not contained
     *  in this actor, the rate is less than zero, or the port is
     *  not an output port.
     *  @deprecated
     */
    public void setTokenInitProduction(IOPort p, int r)
            throws IllegalActionException {
        if(r < 0) throw new IllegalActionException(
                "Rate must be >= 0");
        Port pp = getPort(p.getName());
        if(!p.isOutput()) throw new IllegalActionException("IOPort " +
                p.getName() + " is not an Output Port.");
        if(!p.equals(pp)) throw new IllegalActionException("IOPort " +
                p.getName() + " is not contained in Actor " +
                getName());
        Parameter param = (Parameter)p.getAttribute("TokenInitProduction");
        if(param != null)
            param.setToken(new IntToken(r));
        else {
            try {
                param = new Parameter(p, "TokenInitProduction",
                        new IntToken(r));
            }
            catch (NameDuplicationException e) {
                // This should never happen.
                throw new InternalErrorException(e.getMessage());
            }
        }
    }

    /** Set the number of tokens that are produced
     *  on the appropriate port of this Actor during each firing.
     *
     *  @exception IllegalActionException If port is not contained
     *  in this actor, the rate is less than zero, or the port is
     *  not an output port.
     *  @deprecated
     */
    public void setTokenProductionRate(IOPort p, int r)
            throws IllegalActionException {
        if(r <= 0) throw new IllegalActionException(
                "Rate must be > 0");
        Port pp = getPort(p.getName());
        if(!p.isOutput()) throw new IllegalActionException("IOPort " +
                p.getName() + " is not an Output Port.");
        if(!p.equals(pp)) throw new IllegalActionException("IOPort " +
                p.getName() + " is not contained in Actor " +
                getName());
        Parameter param = (Parameter)p.getAttribute("TokenProductionRate");
        if(param != null)
            param.setToken(new IntToken(r));
        else {
            try {
                param = new Parameter(p, "TokenProductionRate",
                        new IntToken(r));
            }
            catch (NameDuplicationException e) {
                // This should never happen.
                throw new InternalErrorException(e.getMessage());
            }
        }
    }
}
